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
    private var isStreaming = false
    private var firstFrameReceived = false
    private var reusableBitmap: Bitmap? = null
    private var handlerThread: android.os.HandlerThread? = null
    private var handler: android.os.Handler? = null

    var pendingResultCode: Int = -1
    var pendingData: Intent? = null

    var onFrameAvailable: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun initProjection(resultCode: Int, data: Intent, serviceContext: Context) {
        val projectionManager = serviceContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopStream()
            }
        }, null)
    }

    private var lastFrameTime: Long = 0L

    @SuppressLint("WrongConstant")
    fun startStream(serviceContext: Context) {
        if (isStreaming) return
        isStreaming = true
        firstFrameReceived = false
        lastFrameTime = 0L

        try {
            val metrics = serviceContext.resources.displayMetrics
            val width = 360 // Downscale significantly for fast stream
            val screenWidth = if (metrics.widthPixels > 0) metrics.widthPixels else 1080
            val screenHeight = if (metrics.heightPixels > 0) metrics.heightPixels else 1920
            
            var height = (width * screenHeight) / screenWidth
            if (height % 2 != 0) height++

            val density = if (metrics.densityDpi > 0) metrics.densityDpi else 400

            handlerThread = android.os.HandlerThread("ScreenCaptureThread")
            handlerThread?.start()
            handler = android.os.Handler(handlerThread!!.looper)

            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        
        imageReader?.setOnImageAvailableListener({ reader ->
            if (!isStreaming) return@setOnImageAvailableListener
            try {
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                
                // Throttle framerate to ~28 FPS (35ms) for instant real-time reflection
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFrameTime < 35L) {
                    image.close()
                    return@setOnImageAvailableListener
                }
                lastFrameTime = currentTime

                if (!firstFrameReceived) {
                    firstFrameReceived = true
                    onError?.invoke("DEBUG: First frame received from ImageReader!")
                }

                try {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * width
                    
                    val bitmapWidth = width + rowPadding / pixelStride
                    if (reusableBitmap == null || reusableBitmap?.width != bitmapWidth || reusableBitmap?.height != height) {
                        reusableBitmap?.recycle()
                        reusableBitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888)
                    }
                    
                    val targetBmp = reusableBitmap ?: return@setOnImageAvailableListener
                    buffer.position(0)
                    targetBmp.copyPixelsFromBuffer(buffer)
                    
                    val finalBitmap = if (rowPadding != 0) {
                        Bitmap.createBitmap(targetBmp, 0, 0, width, height)
                    } else {
                        targetBmp
                    }
                    
                    val baos = ByteArrayOutputStream()
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 18, baos)
                    val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                    
                    onFrameAvailable?.invoke(base64)
                    
                    if (finalBitmap != targetBmp) {
                        finalBitmap.recycle()
                    }
                } finally {
                    image.close()
                }
            } catch (e: Throwable) {
                Log.e("ScreenCapture", "Frame error", e)
                onError?.invoke("Frame error: ${e.message}")
            }
        }, handler)

        try {
            val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenStream",
                width, height, density,
                flags,
                imageReader?.surface, null, handler
            )
            onError?.invoke("DEBUG: VirtualDisplay created successfully!")
        } catch (e: Throwable) {
            onError?.invoke("VirtualDisplay error: ${e.message}")
        }
        } catch (e: Throwable) {
            Log.e("ScreenCapture", "startStream error", e)
            onError?.invoke("startStream error: ${e.message}")
        }
    }

    fun stopStream() {
        if (!isStreaming) return
        isStreaming = false
        
        handlerThread?.quitSafely()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        handlerThread = null
        handler = null
        virtualDisplay = null
        imageReader = null
        mediaProjection = null

        val stopIntent = Intent(context, ScreenCaptureService::class.java).apply {
            action = "STOP"
        }
        context.startService(stopIntent)
    }
}
