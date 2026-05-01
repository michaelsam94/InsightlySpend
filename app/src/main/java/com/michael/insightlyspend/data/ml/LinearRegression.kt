package com.michael.insightlyspend.data.ml

import kotlin.math.max

/**
 * Ordinary least squares for y ≈ a + b x
 */
internal object LinearRegression {
    fun fit(xs: DoubleArray, ys: DoubleArray): Pair<Double, Double> {
        require(xs.size == ys.size && xs.size >= 2)
        val n = xs.size.toDouble()
        val meanX = xs.average()
        val meanY = ys.average()
        var num = 0.0
        var den = 0.0
        for (i in xs.indices) {
            val dx = xs[i] - meanX
            num += dx * (ys[i] - meanY)
            den += dx * dx
        }
        val slope = if (den == 0.0) 0.0 else num / den
        val intercept = meanY - slope * meanX
        return intercept to slope
    }

    fun predict(intercept: Double, slope: Double, x: Double): Double =
        max(0.0, intercept + slope * x)
}
