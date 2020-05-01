package com.switchonkannada.switchon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.switchonkannada.switchon.ui.login.LoginWithEmailActivity

class logInOption : AppCompatActivity() {

    lateinit var emailLogin:Button
    lateinit var googleLogin:Button
    lateinit var facebookLogin:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in_option)

        emailLogin = findViewById(R.id.emailLogin)
        googleLogin = findViewById(R.id.googleLogin)
        facebookLogin = findViewById(R.id.facebookLogin)


        emailLogin.setOnClickListener {

            val intent = Intent(this , LoginWithEmailActivity :: class.java)
            startActivity(intent)

        }

    }
}
