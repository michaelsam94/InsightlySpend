package com.michael.insightlyspend.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.michael.insightlyspend.domain.model.CategoryShare
import com.michael.insightlyspend.domain.model.DailySpendPoint

@Composable
fun SevenDayLineChart(
    points: List<DailySpendPoint>,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        if (points.isEmpty()) return@Canvas
        val maxY = (points.maxOf { it.totalExpense }.takeIf { it > 0 } ?: 1.0).toFloat().coerceAtLeast(1f)
        val stepX = size.width / (points.size.coerceAtLeast(2) - 1).coerceAtLeast(1)
        val path = Path()
        points.forEachIndexed { index, p ->
            val x = index * stepX
            val y = size.height - (p.totalExpense.toFloat() / maxY) * size.height * 0.9f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = primary, style = Stroke(width = 4f))
        points.forEachIndexed { index, p ->
            val x = index * stepX
            val y = size.height - (p.totalExpense.toFloat() / maxY) * size.height * 0.9f
            drawCircle(color = primary, radius = 6f, center = Offset(x, y))
        }
    }
}

@Composable
fun CategoryPieChart(
    slices: List<CategoryShare>,
    modifier: Modifier = Modifier,
) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF8BC34A),
        Color(0xFFFF9800),
        Color(0xFF03A9F4),
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        val total = slices.sumOf { it.amount }.takeIf { it > 0 } ?: 1.0
        var startAngle = -90f
        val radius = size.minDimension / 2f * 0.9f
        val center = Offset(size.width / 2f, size.height / 2f)
        slices.forEachIndexed { index, slice ->
            val sweep = (slice.amount / total * 360f).toFloat()
            drawArc(
                color = palette[index % palette.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
            )
            startAngle += sweep
        }
    }
}

@Composable
fun CategoryBarChart(
    slices: List<CategoryShare>,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        if (slices.isEmpty()) return@Canvas
        val max = slices.maxOf { it.amount }.takeIf { it > 0 } ?: 1.0
        val barWidth = size.width / slices.size.coerceAtLeast(1)
        slices.forEachIndexed { index, slice ->
            val h = (slice.amount / max).toFloat() * size.height * 0.85f
            drawRect(
                color = primary.copy(alpha = 0.75f),
                topLeft = Offset(index * barWidth + 8f, size.height - h),
                size = Size(barWidth - 16f, h),
            )
        }
    }
}
