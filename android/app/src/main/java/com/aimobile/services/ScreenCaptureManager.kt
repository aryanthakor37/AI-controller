package com.aimobile.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureJob: Job? = null
    private var isStreaming = false

    var onFrameAvailable: ((String) -> Unit)? = null

    fun startProjection(resultCode: Int, data: Intent, serviceContext: Context) {
        val projectionManager = serviceContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopStream()
            }
        }, null)
        startStream(serviceContext)
    }

    @SuppressLint("WrongConstant")
    private fun startStream(serviceContext: Context) {
        if (isStreaming) return
        isStreaming = true

        val metrics = context.resources.displayMetrics
        val width = 720 // Downscale for stream performance
        val height = (width * metrics.heightPixels) / metrics.widthPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenStream",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        captureJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && isStreaming) {
                try {
                    val image: Image? = imageReader?.acquireLatestImage()
                    if (image != null) {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width
                        
                        val bitmap = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        buffer.position(0)
                        bitmap.copyPixelsFromBuffer(buffer)
                        val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                        
                        val baos = ByteArrayOutputStream()
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos) // Low quality for speed
                        val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                        
                        onFrameAvailable?.invoke(base64)
                        
                        image.close()
                    }
                    kotlinx.coroutines.delay(200) // ~5 FPS
                } catch (e: Exception) {
                    Log.e("ScreenCapture", "Frame error", e)
                }
            }
        }
    }

    fun stopStream() {
        if (!isStreaming) return
        isStreaming = false
        captureJob?.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
        captureJob = null

        val stopIntent = Intent(context, ScreenCaptureService::class.java).apply {
            action = "STOP"
        }
        context.startService(stopIntent)
    }
}
