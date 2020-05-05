package com.switchonkannada.switchon

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.switchonkannada.switchon.ui.login.LoginWithEmailActivity
import kotlinx.android.synthetic.main.activity_log_in_option.*
import java.util.*

class logInOption : AppCompatActivity() {

    lateinit var emailLogin:Button
    lateinit var googleLogin:Button
    lateinit var facebookLogin:Button
    lateinit var register:Button
    lateinit var mAuth: FirebaseAuth
    lateinit var callbackManager: CallbackManager
    lateinit var mgoogleSignInClient: GoogleSignInClient
    lateinit var process:ProgressBar


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!! , account.email!!)
            } catch (e: Exception) {
                Toast.makeText(this , e.message , Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in_option)

        supportActionBar?.hide()

        emailLogin = findViewById(R.id.emailLogin)
        googleLogin = findViewById(R.id.googleLogin)
        facebookLogin = findViewById(R.id.facebookLogin)
        register = findViewById(R.id.signIn)
        process = findViewById(R.id.loginProcess)


        mAuth = FirebaseAuth.getInstance()




        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mgoogleSignInClient = GoogleSignIn.getClient(this , gso)

        callbackManager = CallbackManager.Factory.create()


        emailLogin.setOnClickListener {

            val intent = Intent(this , LoginWithEmailActivity :: class.java)
            startActivity(intent)

        }

        register.setOnClickListener {
            val intent = Intent(this , SignInOption :: class.java)
            startActivity(intent)
        }

        googleLogin.setOnClickListener {
            signInWithGoogle()
        }

        facebookLogin.setOnClickListener {
            signInWithFacebook()
        }

    }

    private fun signInWithGoogle() {
        val signInIntent = mgoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this , Arrays.asList("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: com.facebook.login.LoginResult) {

                val request = GraphRequest.newMeRequest(
                    loginResult.accessToken
                ) { me, response ->
                    if (response.error != null) {
                        AlertDialog.Builder(this@logInOption, R.style.CustomDialogTheme).setTitle("Error").setMessage(response.error.errorMessage).setPositiveButton("Ok" , null).show()

                    } else {
                        val user_email = response.jsonObject.optString("email")
                        handleFacebookAccessToken(loginResult.accessToken , user_email)

                    }
                }

                val parameters = Bundle()
                parameters.putString("fields", "last_name,first_name,email")
                request.parameters = parameters
                request.executeAsync()
            }
            override fun onCancel() {
                //  Log.d(FragmentActivity.TAG, "facebook:onCancel")
                // ...
            }
            override fun onError(error: FacebookException) {
                AlertDialog.Builder(this@logInOption, R.style.CustomDialogTheme).setTitle("Error").setMessage(error.message).setPositiveButton("Ok" , null).show()
            }
        })
    }

    private fun firebaseAuthWithGoogle(idToken: String , email:String) {

        process.visibility = View.VISIBLE

        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->

            val isNewUser: Boolean = task.result?.signInMethods!!.isEmpty().not()


            if (isNewUser) {
                Log.e("TAG", "Is New User!")
                val credential = GoogleAuthProvider.getCredential(idToken , null)
                mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            val user = mAuth.currentUser
                            val newUser = it.result!!.additionalUserInfo!!.isNewUser
                            
                            if (!newUser) {
                                nextScreen()
                            }
                        } else {
                            AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(it.exception?.message).setPositiveButton("Ok" , null).show()
                        } // ...
                    }
            } else {
                Log.e("TAG", "Is Old User!")
                AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage("There is no user record corresponding to this identifier. The user may have been deleted.").setPositiveButton("Ok" , null).show()
                mgoogleSignInClient.signOut()
            }
            process.visibility = View.GONE
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken, emailFacebook: String) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.fetchSignInMethodsForEmail(emailFacebook).addOnCompleteListener {
            val isNewUser: Boolean = it.result?.signInMethods!!.isEmpty().not()
            if (isNewUser) {
                Log.e("TAG", "Is New User!")
                mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth.currentUser
                            val newuser = task.result!!.additionalUserInfo!!.isNewUser
                            if (!newuser){
                                nextScreen()


                            }
                        } else {
                            AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(task.exception?.message).setPositiveButton("Ok" , null).show()
                        }
                    }
            } else {
                Log.e("TAG", "Is Old User!")
                AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage("There is no user record corresponding to this identifier. The user may have been deleted.").setPositiveButton("Ok" , null).show()
                LoginManager.getInstance().logOut()
            }
        }
    }

    fun nextScreen(){
        val intent = Intent(this , bottomNav :: class.java)
        startActivity(intent)
        finish()
    }



    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}
