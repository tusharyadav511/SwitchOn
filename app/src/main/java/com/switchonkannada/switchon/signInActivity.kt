package com.switchonkannada.switchon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class signInActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        toolbar = findViewById(R.id.toolbarSignIn)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.toolbar_back)
        supportActionBar?.setDisplayShowTitleEnabled(false);
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
