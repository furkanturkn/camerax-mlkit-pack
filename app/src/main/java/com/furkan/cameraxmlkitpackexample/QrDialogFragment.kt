package com.furkan.cameraxmlkitpackexample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.furkan.camerax_mlkit_pack.CameraxManager
import com.furkan.camerax_mlkit_pack.core.ReaderType
import kotlinx.android.synthetic.main.fragment_qr_dialog.view.*


class QrDialogFragment : DialogFragment(R.layout.fragment_qr_dialog) {
    private lateinit var rootView: View

    private var cameraxManager: CameraxManager? = null

    private var qrReaderDialogDismissListener: ((Boolean) -> Unit)? = null
    fun setQrReaderDialogDismissListener(listener: (Boolean) -> Unit) {
        qrReaderDialogDismissListener = listener
    }

    private var qrReadSuccessListener: ((String) -> Unit)? = null
    fun mSetQrReadSuccessListener(listener: (String) -> Unit) {
        qrReadSuccessListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_qr_dialog, container, false)
        requireDialog().setTitle("QR")


        rootView.btnBarcodeReaderBackButtonFragment.setOnClickListener {
            sendQrReaderDialogDismissedBroadcast()
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

        cameraxManager?.setReaderFormats(ReaderType.FORMAT_QR_CODE.value)
        cameraxManager?.startReading()

        cameraxManager?.setQrReadSuccessListener { result ->
            Log.e("QR", "Result ------------------------------------- $result")
            sendQrReaderSuccessBroadcast(result)
        }

    }

    private fun sendQrReaderSuccessBroadcast(result: String) {
        qrReadSuccessListener?.let { qrReaderSuccess ->
            qrReaderSuccess(result)
        }
    }

    override fun onStart() {
        super.onStart()
        initCameraManager(this)
    }

    private fun sendQrReaderDialogDismissedBroadcast() {
        qrReaderDialogDismissListener?.let { dialogDismiss ->
            dialogDismiss(true)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraxManager?.destroyReferences()
    }

}