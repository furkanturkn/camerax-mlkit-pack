package com.furkan.cameraxmlkitpackexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val btnQrReaderActivity: Button = findViewById(R.id.btnQrReaderActivity)
        val btnQrReaderFragment: Button = findViewById(R.id.btnQrReaderFragment)
        btnQrReaderActivity.setOnClickListener{
            val qrReaderActivityIntent =
                Intent(applicationContext, MainActivity::class.java)
            startActivity(qrReaderActivityIntent)
        }
        btnQrReaderFragment.setOnClickListener{
            val qrReaderFragment = QrDialogFragment()
            qrReaderFragment.show(supportFragmentManager, "QR_FRAGMENT")
        }
    }
}