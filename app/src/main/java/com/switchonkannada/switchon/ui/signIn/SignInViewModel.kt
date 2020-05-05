package com.switchonkannada.switchon.ui.signIn

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.switchonkannada.switchon.R

class SignInViewModel(): ViewModel() {

    private val _signFormState = MutableLiveData<SignInFromState>()
    val signFormState : LiveData<SignInFromState> = _signFormState

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase:DatabaseReference


    private val _signInResult = MutableLiveData<SignInResult>()
    val signInResult : LiveData<SignInResult> = _signInResult


    fun signIn(email: String , verifyPassword: String , name: String){
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child("users")


        if (email != ""){
            auth.createUserWithEmailAndPassword(email , verifyPassword).addOnCompleteListener {
                if (it.isSuccessful){
                    val currentUser = it.result!!.user!!.uid
                    val imageUrl = "https://firebasestorage.googleapis.com/v0/b/switch-on-39001.appspot.com/o/DefaultProfileIcon%20%2FdefaultProfileIcon.png?alt=media&token=d2e5ff1b-36d3-4ba2-9447-254c4dcc1300"
                    val map:Map<String , String> = mapOf( "Name" to name , "Email" to email , "ProfileImage" to imageUrl , "uid" to currentUser )
                    Firebase.firestore.collection("users").document(currentUser).set(map).addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            mDatabase.child(currentUser).setValue(map)
                            _signInResult.value = SignInResult(success = "You have created an account")
                        }else {
                            _signInResult.value = SignInResult(error = task.exception?.message)
                        }
                    }

                    _signInResult.value = SignInResult(success = "You have created an account")
                }else {
                    _signInResult.value = SignInResult(error = it.exception?.message)
                }
            }
        }
    }



    fun SignInDataChange(name:String , email:String , passowrd:String , verifyPassword:String){

        if(!isEmailValid(email)){
            _signFormState.value = SignInFromState(emailError = R.string.error_invalid_email)
        } else if(name.trim { it <= ' ' } == ""){
            _signFormState.value = SignInFromState(nameError = R.string.error_field_required)
        } else if (!isPasswordValid(passowrd)){
            _signFormState.value = SignInFromState(passwordError = R.string.invalid_password)
        }else if (verifyPassword.trim(){ it <= ' ' } != passowrd ){
            _signFormState.value = SignInFromState(verifyPasswordError = R.string.incorrect_password_verification)
        }else {
            _signFormState.value = SignInFromState(isDataValid = true)
        }

    }

    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isNotBlank()
        }
    }
    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

}