package com.furkan.camerax_mlkit_pack.core

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.furkan.camerax_mlkit_pack.core.Constants.EMPTY_IMAGE_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object Utils {

    internal fun ImageView.setViewLocation(x: Float, y: Float) {
        val width = this.width
        val height = this.height
        this.x = x - width / 2
        this.y = y - height / 2
    }

    internal fun ImageView.visible() {
        this.visibility = View.VISIBLE
        this.alpha = 1f
    }

    internal fun ImageView.inVisible() {
        this.visibility = View.INVISIBLE
    }

    internal fun ObjectAnimator.initAnimation() {
        this.duration = 500
        this.repeatCount = ValueAnimator.RESTART
        this.repeatMode = ValueAnimator.REVERSE
    }


    internal suspend fun ImageCapture.takePhoto(executor: Executor): File {
        val photoFile = withContext(Dispatchers.IO) {
            kotlin.runCatching {
                File.createTempFile("image", "jpg")
            }.getOrElse { ex ->
                Log.e("take photo err", ex.toString())
                File(EMPTY_IMAGE_URI)
            }
        }

        return suspendCoroutine { continuation ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            takePicture(
                outputOptions, executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                        continuation.resume(photoFile)
                    }

                    override fun onError(ex: ImageCaptureException) {
                        Log.e("take-photo suspend", "Image capture failed")
                        continuation.resumeWithException(ex)
                    }
                }
            )
        }
    }

    internal fun Uri.convertToBitmap(contentResolver: ContentResolver, cameraType: Int): Bitmap {
        return MediaStore.Images.Media.getBitmap(contentResolver, this).rotateBitmap(cameraType)
    }

    private fun Bitmap.rotateBitmap(cameraType: Int): Bitmap {
        val rotationMatrix = Matrix()

        if (this.width >= this.height) {
            if (cameraType == CameraSelector.LENS_FACING_FRONT) {
                rotationMatrix.setRotate(-90F)
            } else {
                rotationMatrix.setRotate(90F)
            }

        }

        return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            rotationMatrix,
            true
        )
    }

}
