package com.furkan.camerax_mlkit_pack

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.ImageView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.furkan.camerax_mlkit_pack.core.Constants
import com.furkan.camerax_mlkit_pack.core.Constants.CAMERA_RESOLUTION_HEIGHT
import com.furkan.camerax_mlkit_pack.core.Constants.CAMERA_RESOLUTION_WIDTH
import com.furkan.camerax_mlkit_pack.core.Constants.MLKIT_READER_MANAGER_TAG
import com.furkan.camerax_mlkit_pack.core.Constants.NO_BARCODE_RESULT
import com.furkan.camerax_mlkit_pack.core.Utils.convertToBitmap
import com.furkan.camerax_mlkit_pack.core.Utils.inVisible
import com.furkan.camerax_mlkit_pack.core.Utils.initAnimation
import com.furkan.camerax_mlkit_pack.core.Utils.setViewLocation
import com.furkan.camerax_mlkit_pack.core.Utils.takePhoto
import com.furkan.camerax_mlkit_pack.core.Utils.visible
import com.furkan.camerax_mlkit_pack.core.state.FlashStatus
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.BarcodeFormat
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraxManager(
    context: Context,
    previewView: PreviewView,
    focusRing: ImageView,
    cameraSelectorType: Int? = CameraSelector.LENS_FACING_BACK,
    accuracyLevel: Int = Constants.BARCODE_ACCURACY_DEFAULT_COUNT
) {

    private var mContext = context
    private var mPreviewView = previewView
    private var mFocusRing = focusRing
    private var cameraControl: CameraControl? = null
    private var accuracyCounter = 0
    private var tempBarcodeResult = NO_BARCODE_RESULT
    private var continueToRead = true
    private var mAccuracyLevel = accuracyLevel
    private lateinit var barcodeScannerOptions: BarcodeScannerOptions
    private var flashStatus = FlashStatus.DISABLED

    private var isAnimationFinished = true
    private var focusAnimation: ObjectAnimator? = null

    private var cameraResolutionWidth = CAMERA_RESOLUTION_WIDTH
    private var cameraResolutionHeight = CAMERA_RESOLUTION_HEIGHT

    private var mCameraSelectorType = cameraSelectorType
    private var cameraSelector: CameraSelector? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var barcodeScanner: BarcodeScanner? = null
    private var cameraExecutor: ExecutorService
    private val imageCaptureBuilder: ImageCapture

    //[START] Listeners
    private var qrReadSuccessListener: ((String) -> Unit)? = null
    fun setQrReadSuccessListener(listener: (String) -> Unit) {
        qrReadSuccessListener = listener
    }

    private var flashStatusChangedListener: ((FlashStatus) -> Unit)? = null
    fun setFlashStatusChangedListener(listener: (FlashStatus) -> Unit) {
        flashStatusChangedListener = listener
    }

    private var photoCaptureResultListener: ((Bitmap) -> Unit)? = null
    fun setPhotoCaptureResultListener(listener: (Bitmap) -> Unit) {
        photoCaptureResultListener = listener
    }

    private fun sendQrReaderSuccess(result: String) {
        qrReadSuccessListener?.let { qrReaderSuccess ->
            qrReaderSuccess(result)
        }
    }

    private fun sendFlashStatusChanged(flashStatus: FlashStatus) {
        flashStatusChangedListener?.let { flashStatusChanged ->
            flashStatusChanged(flashStatus)
        }
    }

    private fun sendPhotoCaptureResult(captureResult: Bitmap) {
        photoCaptureResultListener?.let { capturedPhotoBitmap ->
            capturedPhotoBitmap(captureResult)
        }
    }
    //[END] Listeners

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: CameraxManager? = null

        fun getInstance(
            context: Context,
            previewView: PreviewView,
            focusRing: ImageView,
            cameraSelectorType: Int? = CameraSelector.LENS_FACING_BACK,
            accuracyLevel: Int = Constants.BARCODE_ACCURACY_DEFAULT_COUNT
        ) = INSTANCE
            ?: synchronized(this) {
                INSTANCE ?: CameraxManager(
                    context,
                    previewView,
                    focusRing,
                    cameraSelectorType,
                    accuracyLevel
                ).also {
                    INSTANCE = it
                }
            }
    }

    init {
        initAnimation()
        addPreviewTouchListener()
        cameraExecutor = Executors.newSingleThreadExecutor()
        imageCaptureBuilder = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    fun startCamera() {
        addCameraProviderFeatureListener()
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        stopReading()
    }

    fun startReading() {
        continueToRead = true
    }

    fun stopReading() {
        continueToRead = false
    }

    fun destroyReferences() {
        stopReading()
        stopCamera()
        cameraExecutor.shutdown()
        barcodeScanner?.close()
    }

    fun changeCameraType() {
        when (mCameraSelectorType) {
            CameraSelector.LENS_FACING_BACK -> {
                mCameraSelectorType = CameraSelector.LENS_FACING_FRONT
            }
            CameraSelector.LENS_FACING_FRONT -> {
                mCameraSelectorType = CameraSelector.LENS_FACING_BACK
            }
        }
        addCameraProviderFeatureListener()
    }

    fun setReaderFormats(@BarcodeFormat vararg moreFormats: Int) {
        val firstBarcodeFormat = moreFormats[0]
        if (firstBarcodeFormat == Barcode.FORMAT_QR_CODE) {
            setReadingAccuracyLevel(1)
        }
        moreFormats.drop(1)
        barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                firstBarcodeFormat,
                *moreFormats
            ).build()
    }

    fun setReadingAccuracyLevel(accuracyLevel: Int) {
        if (accuracyLevel < 1) {
            mAccuracyLevel = 1
            return
        }
        if (accuracyLevel > 3) {
            mAccuracyLevel = 3
            return
        }
        mAccuracyLevel = accuracyLevel
    }

    fun setCameraResolution(width: Int, height: Int) {
        cameraResolutionWidth = width
        cameraResolutionHeight = height
    }

    fun changeFlashStatus() {
        when (flashStatus) {
            FlashStatus.ENABLED -> {
                flashStatus = FlashStatus.DISABLED
                cameraControl?.enableTorch(false)
                sendFlashStatusChanged(FlashStatus.DISABLED)
            }
            FlashStatus.DISABLED -> {
                flashStatus = FlashStatus.ENABLED
                cameraControl?.enableTorch(true)
                sendFlashStatusChanged(FlashStatus.ENABLED)
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun processImageProxy(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            val image: Image? = imageProxy.image
            barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

            if (image == null) return

            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner?.process(inputImage)
                ?.addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0]
                        val barcodeResult = barcode.rawValue
                            ?: return@addOnSuccessListener
                        if (continueToRead) {
                            setAccurateBarcodeResult(barcodeResult)
                        }
                    }
                }?.addOnFailureListener {
                    Log.e(MLKIT_READER_MANAGER_TAG, it.stackTrace.toString())
                }
                ?.addOnCompleteListener {
                    imageProxy.image!!.close()
                    imageProxy.close()
                }
        }
    }

    private fun setAccurateBarcodeResult(
        barcodeResult: String
    ) {
        if (accuracyCounter >= mAccuracyLevel) {
            Log.w(
                MLKIT_READER_MANAGER_TAG,
                "consecutive reading SUCCESS...Barcode result: ${barcodeResult}]"
            )
            accuracyCounter = 0
            tempBarcodeResult = NO_BARCODE_RESULT
            continueToRead = false
            startReading()
            sendQrReaderSuccess(barcodeResult)
        } else {
            if (barcodeResult == tempBarcodeResult) {
                Log.w(
                    MLKIT_READER_MANAGER_TAG,
                    "times matched: ${accuracyCounter + 1}",
                )
                accuracyCounter++
            } else {
                Log.w(
                    MLKIT_READER_MANAGER_TAG,
                    """
                    different data received, temp data updated ...
                    Old Temp: $tempBarcodeResult
                    """.trimIndent() + "\n"
                            + "New Temp: " + barcodeResult
                )
                tempBarcodeResult = barcodeResult
                accuracyCounter = 0
            }
        }
    }

    private fun addCameraProviderFeatureListener() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture?.addListener(Runnable {
            try {
                cameraProvider =
                    cameraProviderFuture?.get()
                cameraProvider?.let {
                    bindImageAnalysis(it)
                }
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(mContext))
    }

    private fun bindImageAnalysis(cameraProvider: ProcessCameraProvider) {
        val imageAnalysisUseCase = ImageAnalysis.Builder()
            .setTargetResolution(
                Size(
                    cameraResolutionWidth,
                    cameraResolutionHeight
                )
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysisUseCase.setAnalyzer(
            cameraExecutor
        ) { imageProxy: ImageProxy? ->
            imageProxy?.let {
                processImageProxy(
                    it
                )
            }
        }
        val orientationEventListener: OrientationEventListener =
            object : OrientationEventListener(mContext) {
                override fun onOrientationChanged(orientation: Int) {
                    val rotation: Int = when (orientation) {
                        in 45..134 -> Surface.ROTATION_270
                        in 135..224 -> Surface.ROTATION_180
                        in 225..314 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }
                    imageCaptureBuilder.targetRotation = rotation
                }
            }
        orientationEventListener.enable()

        val preview = Preview.Builder().build()
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(mCameraSelectorType!!)
            .build()
        preview.setSurfaceProvider(mPreviewView.surfaceProvider)

        cameraProvider.unbindAll()
        val camera: Camera = cameraProvider.bindToLifecycle(
            (mContext as LifecycleOwner), cameraSelector!!,
            imageAnalysisUseCase, preview, imageCaptureBuilder
        )
        cameraControl = camera.cameraControl
    }

    suspend fun capturePhoto() = withContext(Dispatchers.IO) {
        val fileUri = Uri.fromFile(imageCaptureBuilder.takePhoto(cameraExecutor))
        sendPhotoCaptureResult(
            fileUri!!.convertToBitmap(
                mContext.contentResolver,
                mCameraSelectorType!!
            )
        )
    }

    //[START] Focus related
    @SuppressLint("ClickableViewAccessibility")
    private fun addPreviewTouchListener() {
        mPreviewView.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    mFocusRing.setViewLocation(event.x, event.y)
                    fadeOutFocusAnimation()
                    isAnimationFinished = false

                    if (cameraSelector == null) return@setOnTouchListener true
                    val factory: MeteringPointFactory =
                        mPreviewView.getMeteringPointFactory()
                    val point = factory.createPoint(event.x, event.y)
                    val action =
                        FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                            .setAutoCancelDuration(5, TimeUnit.SECONDS)
                            .build()
                    cameraControl?.startFocusAndMetering(action)
                    return@setOnTouchListener true
                }
                else -> {
                    Log.d(MLKIT_READER_MANAGER_TAG, "other action")
                    return@setOnTouchListener false
                }
            }
        }
    }

    private fun fadeOutFocusAnimation() {
        mFocusRing.visible()
        if (!isAnimationFinished) {
            return
        }
        focusAnimation?.start()
        mFocusRing.animate()
            .setStartDelay(1000)
            .setDuration(500)
            .alpha(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animator: Animator) {
                    mFocusRing.inVisible()
                    isAnimationFinished = true
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
    }

    private fun initAnimation() {
        focusAnimation = ObjectAnimator.ofPropertyValuesHolder(
            mFocusRing,
            PropertyValuesHolder.ofFloat("scaleX", 0.7f),
            PropertyValuesHolder.ofFloat("scaleY", 0.7f)
        )
        focusAnimation?.initAnimation()
    }
    //[END] Focus related
}