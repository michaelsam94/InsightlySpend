package com.michael.insightlyspend.data.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads [ASSET_PATH] when present. Bundle a small regression model (input: average daily burn,
 * output: predicted month-end spend) to enhance projections without cloud calls.
 *
 * Runtime JNI is provided by **LiteRT** (`com.google.ai.edge.litert:litert`), which ships
 * **16 KB page–aligned** native libraries required for Google Play (Android 15+) — unlike the older
 * `org.tensorflow:tensorflow-lite` AAR your debug APK previously pulled in.
 */
@Singleton
class TfliteForecastAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val interpreter: Interpreter? by lazy { loadInterpreter() }

    fun predictMonthEndSpend(averageDailyBurn: Double): Float? {
        val model = interpreter ?: return null
        val input = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        input.putFloat(averageDailyBurn.toFloat())
        val output = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        model.run(input, output)
        output.rewind()
        return output.float
    }

    private fun loadInterpreter(): Interpreter? =
        try {
            context.assets.openFd(ASSET_PATH).use { fd ->
                FileInputStream(fd.fileDescriptor).channel.use { channel ->
                    val mapped = channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        fd.startOffset,
                        fd.declaredLength,
                    )
                    Interpreter(mapped)
                }
            }
        } catch (_: Throwable) {
            null
        }

    companion object {
        private const val ASSET_PATH = "ml/spending_forecast.tflite"
    }
}
