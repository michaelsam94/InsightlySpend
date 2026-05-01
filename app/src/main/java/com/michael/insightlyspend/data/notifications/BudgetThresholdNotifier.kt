package com.michael.insightlyspend.data.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.michael.insightlyspend.R
import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.core.formatMoney
import com.michael.insightlyspend.core.resolveBudgetCategoryDisplayName
import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import com.michael.insightlyspend.domain.notifications.BudgetAlertNotifier
import com.michael.insightlyspend.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetThresholdNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferencesRepository,
    private val alerts: BudgetAlertPreferences,
    channelHolder: BudgetNotificationChannel,
) : BudgetAlertNotifier {
    init {
        channelHolder.ensureCreated()
    }

    override suspend fun notifyThresholdCrossings(rows: List<BudgetCategoryStatus>) {
        val monthKey = Time.monthKey()
        val canNotify = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        val currencyCode = prefs.currencyCode.first()
        val locList = context.resources.configuration.locales
        val locale = if (locList.size() > 0) locList[0] else Locale.getDefault()

        rows.filter { it.thresholdCrossed }.forEach { row ->
            val dedupeKey = "${monthKey}_${row.categoryId}"
            if (alerts.hasAcknowledged(dedupeKey)) return@forEach
            if (!canNotify) return@forEach

            val iconId =
                if (context.applicationInfo.icon != 0) context.applicationInfo.icon else android.R.drawable.ic_dialog_info
            val spentStr = formatMoney(row.spentThisMonth, currencyCode, locale)
            val limitStr = formatMoney(row.budgetLimit, currencyCode, locale)
            val categoryLabel = context.resolveBudgetCategoryDisplayName(row)
            val notification = NotificationCompat.Builder(context, BudgetNotificationChannel.CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(context.getString(R.string.notification_budget_title, categoryLabel))
                .setContentText(
                    context.getString(
                        R.string.notification_budget_text,
                        (row.usagePercent * 100).toInt(),
                    ),
                )
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        context.getString(
                            R.string.notification_budget_big,
                            categoryLabel,
                            spentStr,
                            limitStr,
                        ),
                    ),
                )
                .setGroup(BudgetNotificationChannel.NOTIFICATION_GROUP)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(dedupeKey.hashCode(), notification)
            alerts.acknowledge(dedupeKey)
        }
    }
}
