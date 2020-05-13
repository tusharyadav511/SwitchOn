package com.switchonkannada.switchon

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)


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
