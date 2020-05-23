package com.switchonkannada.switchon

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_password_reset.*

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var email: TextView
    private lateinit var send: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        supportActionBar?.hide()
        mAuth=FirebaseAuth.getInstance()

        email=findViewById(R.id.passwordResetEmail)
        send=findViewById(R.id.sendEmail)

        send.setOnClickListener {
            if (TextUtils.isEmpty(email.text.toString())){
                email.error = "This field is required"
            }else{
                resetPasswordProgress.visibility = View.VISIBLE
                send.visibility = View.GONE
                sendRequest()
            }
        }
        resetBackButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun sendRequest(){

        //This code is used for sending password request email
        var focusView: View? = null
        mAuth.sendPasswordResetEmail(email.text.toString()).addOnCompleteListener { task ->
            if(task.isSuccessful){
                openAlert()
                Toast.makeText(this,"Success! We've sent a password reset link to email address",Toast.LENGTH_LONG).show()
                email.text = null
                resetPasswordProgress.visibility = View.GONE
                send.visibility = View.VISIBLE
            }else{
                email.error="Please enter a valid email address."
                focusView = email
                resetPasswordProgress.visibility = View.GONE
                send.visibility = View.VISIBLE
            }
        }
    }

    private fun openAlert(){
        AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(getString(R.string.password_reset_success))
            .setPositiveButton("Ok", null).show()
    }
}
