package com.switchonkannada.switchon

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.facebook.*
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class SignInOption : AppCompatActivity() {

    lateinit var signInWithEmail:Button
    lateinit var signInWithGoogle:Button
    lateinit var mgoogleSignInClient: GoogleSignInClient
    lateinit var signInWithFacebook:Button
    lateinit var mAuth: FirebaseAuth
    lateinit var callbackManager: CallbackManager
    lateinit var progress:ProgressBar
    lateinit var back:FloatingActionButton


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
        setContentView(R.layout.activity_sign_in_option)

        signInWithEmail = findViewById(R.id.emailSignIn)
        signInWithGoogle = findViewById(R.id.googleSignIn)
        signInWithFacebook = findViewById(R.id.facebookSignIn)
        progress = findViewById(R.id.signInProgess)
        back = findViewById(R.id.backFloatingSignOption)

        back.setOnClickListener {
            onBackPressed()
        }

        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)


        mAuth = FirebaseAuth.getInstance()




        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this , Arrays.asList("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<com.facebook.login.LoginResult> {
            override fun onSuccess(loginResult: com.facebook.login.LoginResult) {

                val request = GraphRequest.newMeRequest(
                    loginResult.accessToken
                ) { me, response ->
                    if (response.error != null) {
                        AlertDialog.Builder(this@SignInOption , R.style.CustomDialogTheme).setTitle("Error").setMessage(response.error.errorMessage).setPositiveButton("Ok" , null).show()

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
                AlertDialog.Builder(this@SignInOption , R.style.CustomDialogTheme).setTitle("Error").setMessage(error.message).setPositiveButton("Ok" , null).show()
            }
        })
    }

    private fun firebaseAuthWithGoogle(idToken: String , email:String) {

        progress.visibility = View.VISIBLE

        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener {

            val isNewUser: Boolean = it.result?.signInMethods?.isEmpty()!!

            if (isNewUser) {
                Log.e("TAG", "Is New User!")
                val credential = GoogleAuthProvider.getCredential(idToken , null)
                mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            val newuser = task.result!!.additionalUserInfo!!.isNewUser
                            val uid = user?.uid
                            val imageUrl = user?.photoUrl.toString()
                            val name = user?.displayName
                            val email = user?.email
                            if (newuser){
                                if (name != null && email != null && uid != null && imageUrl != null) {
                                    nextScreen(imageUrl , name , email , uid)
                                } else {
                                    AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage("Something is wrong with your profile details").setPositiveButton("Ok" , null).show()
                                    mgoogleSignInClient.signOut()
                                    mAuth.signOut()
                                    progress.visibility = View.GONE
                                }
                            }
                        } else {
                            AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(task.exception?.message).setPositiveButton("Ok" , null).show()
                            progress.visibility = View.GONE
                        } // ...
                    }
            } else {
                Log.e("TAG", "Is Old User!")
                AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage("The email address is already in use by another account.").setPositiveButton("Ok" , null).show()
                mgoogleSignInClient.signOut()
                progress.visibility = View.GONE
            }



        }

    }

    private fun handleFacebookAccessToken(token: AccessToken , emailFacebook: String) {
        progress.visibility = View.VISIBLE
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.fetchSignInMethodsForEmail(emailFacebook).addOnCompleteListener {
            val isNewUser: Boolean = it.result?.signInMethods?.isEmpty()!!
            if (isNewUser) {
                Log.e("TAG", "Is New User!")
                mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth.currentUser
                            val newuser = task.result!!.additionalUserInfo!!.isNewUser
                            val uid = user?.uid
                            val imageUrl = user?.photoUrl.toString()
                            val name = user?.displayName
                            val email = user?.email
                            if (newuser){
                                if (name != null && email != null && uid != null && imageUrl != null) {
                                    nextScreen(imageUrl , name , email , uid)
                                } else {
                                    progress.visibility = View.GONE
                                    AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage("Something is wrong with your profile details").setPositiveButton("Ok" , null).show()
                                    LoginManager.getInstance().logOut()
                                    mAuth.signOut()
                                }
                            }
                        } else {
                            progress.visibility = View.GONE
                            AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(task.exception?.message).setPositiveButton("Ok" , null).show()
                        }
                    }
            } else {
                progress.visibility = View.GONE
                Log.e("TAG", "Is Old User!")
                AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage("The email address is already in use by another account.").setPositiveButton("Ok" , null).show()
                LoginManager.getInstance().logOut()
            }
        }
    }

    private fun nextScreen(imageUrl: String , name:String , email:String , uid:String){

        val userData = hashMapOf("Name" to name , "Email" to email , "ProfileImage" to imageUrl , "uid" to uid)
        val intent = Intent(this , bottomNav :: class.java)

        Firebase.firestore.collection("users").document(uid).set(userData).addOnCompleteListener {
            if (it.isSuccessful){
                FirebaseDatabase.getInstance().reference.child("users").child(uid).setValue(userData)
                progress.visibility = View.GONE
                startActivity(intent)
                finish()
            }else{
                progress.visibility = View.GONE
                AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(it.exception?.message).setPositiveButton("Ok" , null).show()
            }
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}
