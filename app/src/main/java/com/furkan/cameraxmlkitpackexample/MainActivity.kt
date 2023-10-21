package com.furkan.cameraxmlkitpackexample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnQrReaderActivity: Button = findViewById(R.id.btnQrReaderActivity)
        val btnQrReaderFragment: Button = findViewById(R.id.btnQrReaderFragment)

        btnQrReaderActivity.setOnClickListener {
            val qrReaderActivityIntent =
                Intent(applicationContext, QrActivity::class.java)
            startActivity(qrReaderActivityIntent)
        }

        btnQrReaderFragment.setOnClickListener {
            val qrReaderFragment = QrDialogFragment()
            qrReaderFragment.show(supportFragmentManager, "QR_FRAGMENT")
        }


    }
}