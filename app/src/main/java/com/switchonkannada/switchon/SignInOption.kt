package com.switchonkannada.switchon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.Toolbar

class SignInOption : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var signInWithEmail:Button
    lateinit var signInWithGoogle:Button
    lateinit var signInWithFacebook:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_option)

        toolbar = findViewById(R.id.toolbarSignInOption)
        signInWithEmail = findViewById(R.id.emailSignIn)
        signInWithGoogle = findViewById(R.id.googleSignIn)
        signInWithFacebook = findViewById(R.id.facebookSignIn)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.toolbar_back)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        signInWithEmail.setOnClickListener {
            val intent = Intent(this , signInActivity ::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
