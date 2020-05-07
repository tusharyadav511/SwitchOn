package com.switchonkannada.switchon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()


        Handler().postDelayed({

            if(mAuth.currentUser != null){
                val intent = Intent(this , bottomNav::class.java)
                startActivity(intent)
                finish()
            }else {
                val intent = Intent(this , logInOption::class.java)
                startActivity(intent)
                finish()
            }


        },2000)
    }
}
