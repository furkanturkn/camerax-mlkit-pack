package com.furkan.cameraxmlkitpackexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.furkan.camerax_mlkit_pack.CameraxManager
import com.furkan.camerax_mlkit_pack.core.ReaderType
import com.furkan.camerax_mlkit_pack.core.state.FlashStatus
import com.furkan.cameraxmlkitpackexample.MainActivity.Companion.REQUEST_CODE_PERMISSIONS
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.btnCapturePhotoFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.btnChangeCameraTypeFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.btnFlashFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.btnStartCameraFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.btnStartReadingFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.btnStopCameraFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.btnStopReadingFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.focusRingFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.ivCapturePreviewFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.previewViewFragment
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.tvReadResultFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class QrDialogFragment : DialogFragment(R.layout.fragment_qr_dialog) {
    private lateinit var rootView: View

    private val requiredPermissions = mutableListOf(
        Manifest.permission.CAMERA
    ).toTypedArray()

    private var cameraxManager: CameraxManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_qr_dialog, container, false)
        requireDialog().setTitle(resources.getString(R.string.app_name))

        checkCameraPermission()

        rootView.btnStartCameraFragment.setOnClickListener {
            cameraxManager?.startCamera()
        }

        rootView.btnStopCameraFragment.setOnClickListener {
            cameraxManager?.destroyReferences()
        }

        rootView.btnStartReadingFragment.setOnClickListener {
            cameraxManager?.startReading()
        }

        rootView.btnStopReadingFragment.setOnClickListener {
            cameraxManager?.stopReading()
        }

        rootView.btnCapturePhotoFragment.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                cameraxManager?.capturePhoto()
            }
        }

        rootView.btnChangeCameraTypeFragment.setOnClickListener {
            cameraxManager?.changeCameraType()
        }

        rootView.btnFlashFragment.setOnClickListener {
            cameraxManager?.changeFlashStatus()
        }

        return rootView
    }


    private fun initCameraManager(qrDialogFragment: QrDialogFragment) {
        cameraxManager = CameraxManager.getInstance(
            requireContext(),
            qrDialogFragment,
            rootView.previewViewFragment,
            rootView.focusRingFragment,
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
                rootView.tvReadResultFragment.text = result
            }

            setFlashStatusChangedListener { status ->
                when (status) {
                    FlashStatus.ENABLED -> {
                        rootView.btnFlashFragment.setBackgroundResource(R.drawable.baseline_flash_on_24)
                    }

                    FlashStatus.DISABLED -> {
                        rootView.btnFlashFragment.setBackgroundResource(R.drawable.baseline_flash_off_24)
                    }
                }
            }

            setPhotoCaptureResultListener { capturedBitmap ->
                activity?.runOnUiThread {
                    rootView.ivCapturePreviewFragment.setImageBitmap(capturedBitmap)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraxManager?.destroyReferences()
    }

    //region Permission Check
    private fun checkCameraPermission() {
        if (allPermissionsGranted()) {
            initCameraManager(this)
        } else {
            requestPermissions(
                requiredPermissions, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                initCameraManager(this)

            } else {
                Toast.makeText(
                    requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    //endregion Permission Check

}