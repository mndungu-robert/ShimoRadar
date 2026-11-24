package com.example.shimoradar.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

private const val TAG = "ShimoRadar"
private const val MODEL_FILE = "best.tflite"

/** One box found by the model. [rect] holds fractions (0..1) of the image width and height. */
data class DetectedBox(
    val classId: Int,
    val score: Float,
    val rect: RectF
)

/** The 9 road-damage classes the model was trained on (RDD2022). */
object ClassInfo {
    private val codes = listOf("D00", "D01", "D10", "D11", "D20", "D40", "D43", "D44", "D50")
    private val labels = listOf(
        "Longitudinal crack", "Longitudinal lateral crack", "Transverse crack",
        "Transverse lateral crack", "Alligator crack", "Pothole",
        "Crosswalk blur", "White line wear", "Other damage"
    )
    private val short = listOf(
        "Long. crack", "Lateral crack", "Transverse", "Transv. lateral",
        "Alligator", "Pothole", "Crosswalk", "Line wear", "Other"
    )

    fun label(id: Int): String = labels.getOrElse(id) { "Road damage" }
    fun shortLabel(id: Int): String = short.getOrElse(id) { "Damage" }
    fun code(id: Int): String = codes.getOrElse(id) { "" }

    /** Maps the model's classes onto the types shown in the app. */
    fun appType(id: Int): String = when (id) {
        5 -> "Pothole"
        0, 1, 2, 3, 4 -> "Crack"
        else -> "Other"
    }
}

class YoloDetector(context: Context) : DamageDetector {

    private val inputSize = 640
    private val numBoxes = 8400
    private val numClasses = 9

    private var interpreter: Interpreter? = null
    private val inputBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(3 * inputSize * inputSize * 4).order(ByteOrder.nativeOrder())
    private val pixels = IntArray(inputSize * inputSize)
    private val output = Array(1) { Array(4 + numClasses) { FloatArray(numBoxes) } }

    val isReady: Boolean get() = interpreter != null

    init {
        try {
            val bytes = context.applicationContext.assets.open(MODEL_FILE).use { it.readBytes() }
            val model = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
            model.put(bytes)
            model.rewind()
            interpreter = Interpreter(model, Interpreter.Options().apply { setNumThreads(4) })
            Log.d(TAG, "AI model loaded")

interpreter?.let { engine ->
    for (i in 0 until engine.inputTensorCount) {
        val tensor = engine.getInputTensor(i)
        Log.d(TAG, "INPUT $i: shape=${tensor.shape().contentToString()}, type=${tensor.dataType()}")
    }

    for (i in 0 until engine.outputTensorCount) {
        val tensor = engine.getOutputTensor(i)
        Log.d(TAG, "OUTPUT $i: shape=${tensor.shape().contentToString()}, type=${tensor.dataType()}")
    }
}
        } catch (e: Exception) {
            Log.e(TAG, "Could not load $MODEL_FILE. Is it in app/src/main/assets?", e)
        }
    }

    /** Runs the model on one image and returns up to 5 boxes, best first. */
    @Synchronized
    fun detectBoxes(
        bitmap: Bitmap,
        confidence: Float = 0.25f,
        iouThreshold: Float = 0.45f
    ): List<DetectedBox> {
        val tflite = interpreter ?: return emptyList()
        return try {
            val scale = min(
                inputSize.toFloat() / bitmap.width,
                inputSize.toFloat() / bitmap.height
            )

            val resizedWidth = bitmap.width * scale
            val resizedHeight = bitmap.height * scale

            val padX = (inputSize - resizedWidth) / 2f
            val padY = (inputSize - resizedHeight) / 2f

            val letterboxed = Bitmap.createBitmap(
                inputSize, inputSize, Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(letterboxed)
            canvas.drawColor(Color.rgb(114, 114, 114))

            canvas.drawBitmap(
                bitmap,
                null,
                RectF(padX, padY, padX + resizedWidth, padY + resizedHeight),
                Paint(Paint.FILTER_BITMAP_FLAG)
            )

            letterboxed.getPixels(
                pixels, 0, inputSize, 0, 0, inputSize, inputSize
            )

            letterboxed.recycle()

            // Channels first: all red values, then all green, then all blue.
            val area = inputSize * inputSize
            val floats = inputBuffer.asFloatBuffer()
            for (i in 0 until area) floats.put(i, ((pixels[i] shr 16) and 0xFF) / 255f)
            for (i in 0 until area) floats.put(area + i, ((pixels[i] shr 8) and 0xFF) / 255f)
            for (i in 0 until area) floats.put(2 * area + i, (pixels[i] and 0xFF) / 255f)

            inputBuffer.rewind()

val start = System.currentTimeMillis()

tflite.run(inputBuffer, output)

Log.d(TAG, "Inference time: ${System.currentTimeMillis() - start}ms")

for (c in 0 until numClasses) {
    val scores = output[0][4 + c]
    Log.d(TAG, "Class $c (${ClassInfo.label(c)}): max score=${scores.maxOrNull()}")
}

for (c in 0 until 4) {
    Log.d(TAG, "Coordinate $c: min=${output[0][c].minOrNull()}, max=${output[0][c].maxOrNull()}")
}
            decode(output[0], confidence, iouThreshold)
                .mapNotNull { box ->
                    val rect = box.rect

                    val corrected = RectF(
                        ((rect.left * inputSize - padX) / resizedWidth).coerceIn(0f, 1f),
                        ((rect.top * inputSize - padY) / resizedHeight).coerceIn(0f, 1f),
                        ((rect.right * inputSize - padX) / resizedWidth).coerceIn(0f, 1f),
                        ((rect.bottom * inputSize - padY) / resizedHeight).coerceIn(0f, 1f)
                    )

                    if (corrected.width() > 0f && corrected.height() > 0f) {
                        box.copy(rect = corrected)
                    } else {
                        null
                    }
                }
                .take(5)
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            emptyList()
        }
    }

    override suspend fun detect(photoPath: String): Detection? = withContext(Dispatchers.Default) {
        val bitmap = decodeUpright(photoPath) ?: return@withContext null
        val boxes = detectBoxes(bitmap)
        val top = boxes.firstOrNull() ?: return@withContext null
        Detection(
            type = ClassInfo.appType(top.classId),
            confidence = top.score,
            label = "${ClassInfo.label(top.classId)} (${ClassInfo.code(top.classId)})",
            boxes = boxes
        )
    }

    private fun decode(p: Array<FloatArray>, confidence: Float, iouThreshold: Float): List<DetectedBox> {
        val candidates = ArrayList<DetectedBox>()
        for (i in 0 until numBoxes) {
            var best = 0f
            var bestClass = -1
            for (c in 0 until numClasses) {
                val score = p[4 + c][i]
                if (score > best) {
                    best = score
                    bestClass = c
                }
            }
            if (bestClass >= 0 && best >= confidence) {
                val cx = p[0][i]
                val cy = p[1][i]
                val w = p[2][i]
                val h = p[3][i]
                candidates.add(
                    DetectedBox(
                        classId = bestClass,
                        score = best,
                        rect = RectF(
                            max(0f, cx - w / 2f),
                            max(0f, cy - h / 2f),
                            min(1f, cx + w / 2f),
                            min(1f, cy + h / 2f)
                        )
                    )
                )
            }
        }
        return nonMaxSuppression(candidates, iouThreshold)
    }

    private fun nonMaxSuppression(boxes: List<DetectedBox>, threshold: Float): List<DetectedBox> {
        val kept = ArrayList<DetectedBox>()
        for (box in boxes.sortedByDescending { it.score }) {
            if (kept.none { overlap(it.rect, box.rect) >= threshold }) kept.add(box)
        }
        return kept
    }

    private fun overlap(a: RectF, b: RectF): Float {
        val w = max(0f, min(a.right, b.right) - max(a.left, b.left))
        val h = max(0f, min(a.bottom, b.bottom) - max(a.top, b.top))
        val intersection = w * h
        val union = a.width() * a.height() + b.width() * b.height() - intersection
        return if (union <= 0f) 0f else intersection / union
    }

    private fun decodeUpright(path: String): Bitmap? {
        val options = BitmapFactory.Options().apply { inSampleSize = 1 }
        val bitmap = BitmapFactory.decodeFile(path, options) ?: return null
        val rotation = when (
            ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotation == 0f) return bitmap
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height,
            Matrix().apply { postRotate(rotation) }, true
        )
    }
}




