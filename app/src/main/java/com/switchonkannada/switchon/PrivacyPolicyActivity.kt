package com.switchonkannada.switchon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_privacy_policy.*

class PrivacyPolicyActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        supportActionBar?.hide()

        privacyWebView.settings.javaScriptEnabled = true
        privacyWebView.webViewClient = WebViewClient()
        privacyWebView.loadUrl("https://www.google.com")

        privacyBack.setOnClickListener {
            onBackPressed()
        }
    }
}
