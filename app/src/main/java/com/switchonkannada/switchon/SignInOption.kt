package com.switchonkannada.switchon

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*


class SignInOption : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var signInWithEmail:Button
    lateinit var signInWithGoogle:Button
    lateinit var mgoogleSignInClient: GoogleSignInClient
    lateinit var signInWithFacebook:Button
    lateinit var mAuth: FirebaseAuth
    lateinit var callbackManager: CallbackManager
    lateinit var gso:GoogleSignInOptions


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                AlertDialog.Builder(this@SignInOption , R.style.CustomDialogTheme).setTitle("Error").setMessage(e.message).setPositiveButton("Ok" , null).show()

            }
        }
    }




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

        mAuth = FirebaseAuth.getInstance()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mgoogleSignInClient = GoogleSignIn.getClient(this , gso)

        callbackManager = CallbackManager.Factory.create()

        signInWithEmail.setOnClickListener {
            val intent = Intent(this , signInActivity ::class.java)
            startActivity(intent)
        }

        signInWithGoogle.setOnClickListener {
            signInWithGoogle()
        }

        signInWithFacebook.setOnClickListener {
            signInWithFacebook()
        }
    }
    private fun signInWithGoogle() {
        val signInIntent = mgoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 101)
    }

    private fun signInWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this , Arrays.asList("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<com.facebook.login.LoginResult> {
            override fun onSuccess(loginResult: com.facebook.login.LoginResult) {
                // Log.d(FragmentActivity."TAG", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }
            override fun onCancel() {
                //  Log.d(FragmentActivity.TAG, "facebook:onCancel")
                // ...
            }
            override fun onError(error: FacebookException) {
                AlertDialog.Builder(this@SignInOption , R.style.CustomDialogTheme).setTitle("Error").setMessage(error.message).setPositiveButton("Ok" , null).show()
            }
        })
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Success").setMessage(user?.uid).setPositiveButton("Ok" , null).show()
                    // TODO : initiate successful logged in experience

                } else {
                    AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(task.exception?.message).setPositiveButton("Ok" , null).show()
                }

                // ...
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Success").setMessage(user?.uid).setPositiveButton("Ok" , null).show()
                    // TODO : initiate successful logged in experience
                } else {
                    AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(task.exception?.message).setPositiveButton("Ok" , null).show()
                }
            }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
