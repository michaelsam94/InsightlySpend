package com.michael.insightlyspend.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.core.formatMoney
import com.michael.insightlyspend.data.local.dao.AccountDao
import com.michael.insightlyspend.data.local.dao.CategoryDao
import com.michael.insightlyspend.data.local.dao.TransactionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
) {

    suspend fun exportCsv(): Uri = withContext(Dispatchers.IO) {
        val entities = transactionDao.getRecent(5000)
        val cats = categoryDao.getAll().associateBy { it.id }
        val accs = accountDao.getAll().associateBy { it.id }
        val header = "id,amount,timestamp,category,account,income,payment,debt,note\n"
        val body = entities.joinToString("\n") { e ->
            val cat = cats[e.categoryId]?.name.orEmpty()
            val acc = accs[e.accountId]?.accountName.orEmpty()
            listOf(
                e.id,
                e.amount,
                e.timestamp,
                escapeCsv(cat),
                escapeCsv(acc),
                e.isIncome,
                e.paymentMethod,
                e.debtDirection,
                escapeCsv(e.note.orEmpty()),
            ).joinToString(",")
        }
        val file = File(context.cacheDir, "insightly_export_${System.currentTimeMillis()}.csv")
        file.writeText(header + body)
        fileProviderUri(file)
    }

    suspend fun exportPdfSummary(currencyCodeValue: String): Uri = withContext(Dispatchers.IO) {
        val locale = Locale.getDefault()
        val currencyCode = currencyCodeValue.ifBlank { "USD" }
        val monthStart = Time.startOfMonthMillis()
        val nextMonthStart = Time.startOfNextMonthMillis()
        val accounts = accountDao.getAll()
        val cats = categoryDao.getAll()
        val spendByCat = transactionDao
            .sumExpenseByCategoryBetween(monthStart, nextMonthStart)
            .associateBy { it.categoryId }
        val incomeMonth = transactionDao.sumIncomeBetween(monthStart, nextMonthStart)
        val expenseMonth = transactionDao.sumExpenseBetween(monthStart, nextMonthStart)
        val recent = transactionDao.getRecent(80)
        val catMap = cats.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }

        val dateFmt = SimpleDateFormat("yyyy-MM-dd", locale)
        val genFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", locale)
        val monthTitle = SimpleDateFormat("MMMM yyyy", locale).format(Date(monthStart))

        val document = PdfDocument()
        var pageIndex = 1
        lateinit var activePage: PdfDocument.Page
        lateinit var canvas: Canvas
        var y = 72f
        val left = 40f
        val bottomY = 800f

        val paintTitle = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val paintSection = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val paintBody = Paint().apply {
            textSize = 11f
            isAntiAlias = true
        }
        val paintSmall = Paint().apply {
            textSize = 9f
            isAntiAlias = true
        }

        fun ellipsize(s: String, max: Int = 98): String =
            if (s.length <= max) s else s.take(max - 3) + "..."

        fun startPage() {
            val info = PdfDocument.PageInfo.Builder(595, 842, pageIndex++).create()
            activePage = document.startPage(info)
            canvas = activePage.canvas
            y = 72f
        }

        fun newPageIfNeeded(lineHeight: Float) {
            if (y + lineHeight > bottomY) {
                document.finishPage(activePage)
                startPage()
            }
        }

        fun line(text: String, paint: Paint, lineHeight: Float = paint.textSize + 5f) {
            newPageIfNeeded(lineHeight)
            canvas.drawText(ellipsize(text), left, y, paint)
            y += lineHeight
        }

        fun blank(extra: Float = 10f) {
            newPageIfNeeded(extra)
            y += extra
        }

        startPage()
        line("Insightly Spend — full summary", paintTitle, 22f)
        line("Generated: ${genFmt.format(Date())}", paintBody)
        blank(14f)

        line("Accounts", paintSection)
        val totalBal = accounts.sumOf { it.currentBalance }
        line(
            "Total balance: ${formatMoney(totalBal, currencyCode, locale)}",
            paintBody,
        )
        accounts.sortedBy { it.accountName.lowercase(locale) }.forEach { a ->
            line(
                " • ${a.accountName}: ${formatMoney(a.currentBalance, currencyCode, locale)}",
                paintBody,
            )
        }
        blank(12f)

        line("This month ($monthTitle)", paintSection)
        line(
            "Income: ${formatMoney(incomeMonth, currencyCode, locale)}",
            paintBody,
        )
        line(
            "Expenses: ${formatMoney(expenseMonth, currencyCode, locale)}",
            paintBody,
        )
        line(
            "Net: ${formatMoney(incomeMonth - expenseMonth, currencyCode, locale)}",
            paintBody,
        )
        blank(12f)

        line("Categories — budget vs spending", paintSection)
        line(
            "Category | Budget | Spent | Remaining",
            paintBody,
        )
        cats.sortedBy { it.name.lowercase(locale) }.forEach { c ->
            val spent = spendByCat[c.id]?.total ?: 0.0
            val budgetStr = c.budgetLimit?.let { formatMoney(it, currencyCode, locale) } ?: "—"
            val spentStr = formatMoney(spent, currencyCode, locale)
            val remainStr = c.budgetLimit?.let { lim ->
                formatMoney(lim - spent, currencyCode, locale)
            } ?: "—"
            line(
                "${c.name} | $budgetStr | $spentStr | $remainStr",
                paintBody,
            )
        }
        blank(12f)

        line("Recent transactions (up to ${recent.size})", paintSection)
        line(
            "Date | +/- Amount | Category | Account | Note",
            paintSmall,
            12f,
        )
        recent.forEach { e ->
            val dateStr = dateFmt.format(Date(e.timestamp))
            val amtStr = if (e.isIncome) {
                "+" + formatMoney(e.amount, currencyCode, locale)
            } else {
                "-" + formatMoney(e.amount, currencyCode, locale)
            }
            val catName = catMap[e.categoryId]?.name ?: "?"
            val accName = accMap[e.accountId]?.accountName ?: "?"
            val note = e.note.orEmpty()
            line(
                "$dateStr | $amtStr | ${ellipsize(catName, 24)} | ${ellipsize(accName, 18)} | ${ellipsize(note, 30)}",
                paintSmall,
                12f,
            )
        }

        document.finishPage(activePage)
        val file = File(context.cacheDir, "insightly_summary_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        fileProviderUri(file)
    }

    private fun escapeCsv(value: String): String =
        if (value.contains(',') || value.contains('"')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    private fun fileProviderUri(file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
}
