package com.example.shimoradar.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
private const val PHOTO_LONG_SIDE = 600     // photos are shrunk so their long side is about this many pixels
private const val PHOTO_CONFIDENCE = 0.20f  // minimum confidence for saved photos

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
            val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
            if (scaled !== bitmap) scaled.recycle()

            // Channels first: all red values, then all green, then all blue.
            val area = inputSize * inputSize
            val floats = inputBuffer.asFloatBuffer()
            for (i in 0 until area) floats.put(i, ((pixels[i] shr 16) and 0xFF) / 255f)
            for (i in 0 until area) floats.put(area + i, ((pixels[i] shr 8) and 0xFF) / 255f)
            for (i in 0 until area) floats.put(2 * area + i, (pixels[i] and 0xFF) / 255f)

            tflite.run(inputBuffer, output)
            var topScore = 0f
            for (c in 0 until numClasses) {
                for (i in 0 until numBoxes) {
                    if (output[0][4 + c][i] > topScore) topScore = output[0][4 + c][i]
                }
            }
            Log.d(TAG, "Highest class score in this image: ${"%.2f".format(topScore)}")
            decode(output[0], confidence, iouThreshold).take(5)
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            emptyList()
        }
    }

    override suspend fun detect(photoPath: String): Detection? = withContext(Dispatchers.Default) {
        val bitmap = decodeUpright(photoPath) ?: return@withContext null
        val boxes = detectBoxes(bitmap, confidence = PHOTO_CONFIDENCE)
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
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val longSide = max(bounds.outWidth, bounds.outHeight)
        if (longSide <= 0) return null

        // Decode at a power-of-two size that is still at least PHOTO_LONG_SIDE, then scale to it.
        var sample = 1
        while (longSide / (sample * 2) >= PHOTO_LONG_SIDE) sample *= 2
        val decoded = BitmapFactory.decodeFile(
            path, BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: return null

        val scale = PHOTO_LONG_SIDE.toFloat() / max(decoded.width, decoded.height)
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            decoded
        }

        val rotation = when (
            ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotation == 0f) return resized
        return Bitmap.createBitmap(
            resized, 0, 0, resized.width, resized.height,
            Matrix().apply { postRotate(rotation) }, true
        )
    }
}