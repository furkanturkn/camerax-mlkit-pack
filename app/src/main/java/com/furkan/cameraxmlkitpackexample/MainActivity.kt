package com.furkan.cameraxmlkitpackexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.furkan.camerax_mlkit_pack.CameraxManager
import com.furkan.camerax_mlkit_pack.core.ReaderType
import com.furkan.camerax_mlkit_pack.core.state.FlashStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val REQUIRED_PERMISSIONS =
        mutableListOf(
            Manifest.permission.CAMERA
        ).toTypedArray()

    private val REQUEST_CODE_PERMISSIONS = 10

    private var cameraxManager: CameraxManager? = null
    private lateinit var previewView: PreviewView
    private lateinit var focusRing: ImageView
    private lateinit var btnFlash: Button
    private lateinit var ivCapturePreview: ImageView
    lateinit var btnCapturePhoto: Button
    lateinit var btnChangeCameraType: Button
    private lateinit var btnStartCamera: Button
    private lateinit var btnStopCamera: Button
    private lateinit var btnStartReading: Button
    private lateinit var btnStopReading: Button
    private lateinit var tvReadResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        checkCameraPermission()

        btnFlash.setOnClickListener {
            cameraxManager?.changeFlashStatus()
        }

        btnChangeCameraType.setOnClickListener {
            cameraxManager?.changeCameraType()
        }

        btnCapturePhoto.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                cameraxManager?.capturePhoto()
            }
        }

        btnStartCamera.setOnClickListener {
            cameraxManager?.startCamera()
        }

        btnStopCamera.setOnClickListener {
            cameraxManager?.destroyReferences()
        }

        btnStartReading.setOnClickListener {
            cameraxManager?.startReading()
        }

        btnStopReading.setOnClickListener {
            cameraxManager?.stopReading()
        }

    }

    private fun checkCameraPermission() {
        if (allPermissionsGranted()) {
            initCameraManager()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        focusRing = findViewById(R.id.focusRing)
        btnFlash = findViewById(R.id.btnFlash)
        ivCapturePreview = findViewById(R.id.ivCapturePreview)
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto)
        btnChangeCameraType = findViewById(R.id.btnChangeCameraType)
        btnStartCamera = findViewById(R.id.btnStartCamera)
        btnStopCamera = findViewById(R.id.btnStopCamera)
        btnStartReading = findViewById(R.id.btnStartReading)
        btnStopReading = findViewById(R.id.btnStopReading)
        tvReadResult = findViewById(R.id.tvReadResult)
    }

    private fun initCameraManager() {
        cameraxManager = CameraxManager.getInstance(
            this,
            null,
            previewView,
            focusRing,
            1
        )
        cameraxManager?.startCamera()

        cameraxManager?.setReaderFormats(
            ReaderType.FORMAT_QR_CODE.value,
            ReaderType.FORMAT_EAN_8.value,
            ReaderType.FORMAT_EAN_13.value,
            ReaderType.FORMAT_UPC_E.value,
            ReaderType.FORMAT_UPC_A.value,
            ReaderType.FORMAT_AZTEC.value
        )
        cameraxManager?.startReading()


        cameraxManager?.apply {
            setQrReadSuccessListener { result ->
                println("QR RESULT ----------> $result")
                tvReadResult.text = result
            }

            setFlashStatusChangedListener { status ->
                when (status) {
                    FlashStatus.ENABLED -> {
                        btnFlash.setBackgroundResource(R.drawable.baseline_flash_on_24)
                    }
                    FlashStatus.DISABLED -> {
                        btnFlash.setBackgroundResource(R.drawable.baseline_flash_off_24)
                    }
                }
            }

            setPhotoCaptureResultListener { capturedBitmap ->
                runOnUiThread {
                    ivCapturePreview.setImageBitmap(capturedBitmap)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraxManager?.destroyReferences()
    }

    //[START] Permission Check
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                initCameraManager()

            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    //[END] Permission Check
}