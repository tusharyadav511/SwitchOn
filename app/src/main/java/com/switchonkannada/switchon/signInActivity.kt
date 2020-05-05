package com.switchonkannada.switchon

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.switchonkannada.switchon.ui.login.LoggedInUserView
import com.switchonkannada.switchon.ui.login.afterTextChanged
import com.switchonkannada.switchon.ui.signIn.SignInViewModel

class signInActivity : AppCompatActivity() {

    lateinit var signInViewMode: SignInViewModel

    lateinit var name:EditText
    lateinit var email:EditText
    lateinit var password:EditText
    lateinit var verifyPassword:EditText
    lateinit var loading:ProgressBar
    lateinit var register:Button
    lateinit var back:FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        name = findViewById(R.id.enterName)
        email = findViewById(R.id.enterEmail)
        password = findViewById(R.id.enterPassword)
        verifyPassword = findViewById(R.id.verifyPassword)
        loading = findViewById(R.id.signUpLoading)
        register = findViewById(R.id.register)
        back = findViewById(R.id.backFloationRegister)


        back.setOnClickListener {
            onBackPressed()
        }

        supportActionBar?.hide()

        signInViewMode = ViewModelProviders.of(this).get(SignInViewModel ::class.java)

        signInViewMode.signInResult.observe(this , Observer {
            val signInResult = it ?: return@Observer

            loading.visibility = View.GONE

            if(signInResult.error != null){
                showLoginFailed(signInResult.error)
            }
            if(signInResult.success != null){
                updateUiWithUser()
            }
            setResult(Activity.RESULT_OK)
        })

        signInViewMode.signFormState.observe(this , Observer {
            val signInState = it ?: return@Observer

            register.isEnabled = signInState.isDataValid

            if(signInState.nameError != null){
                name.error = getString(signInState.nameError)
            }
            if (signInState.emailError !=  null){
                email.error = getString(signInState.emailError)
            }
            if (signInState.passwordError != null){
                password.error = getString(signInState.passwordError)
            }
            if (signInState.verifyPasswordError != null){
                verifyPassword.error = getString(signInState.verifyPasswordError)
            }
        })

        email.apply {
            afterTextChanged {
                signInViewMode.SignInDataChange(name.text.toString() , email.text.toString() , password.text.toString() , verifyPassword.text.toString())
            }
        }

        name.apply {
            afterTextChanged {
                signInViewMode.SignInDataChange(name.text.toString() , email.text.toString() , password.text.toString() , verifyPassword.text.toString())
            }
        }
        password.apply {
            afterTextChanged {
                signInViewMode.SignInDataChange(name.text.toString() , email.text.toString() , password.text.toString() , verifyPassword.text.toString())
            }
        }

        verifyPassword.apply {
            afterTextChanged {
                signInViewMode.SignInDataChange(name.text.toString() , email.text.toString() , password.text.toString() , verifyPassword.text.toString())
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        if(name.text.toString() != "" && email.text.toString() != "" && password.text.toString() != "" && verifyPassword.text.toString() != ""){
                            loading.visibility = View.VISIBLE
                            signInViewMode.signIn(email.text.toString() , verifyPassword.text.toString() , name.text.toString())
                        }
                }
                false
            }
        }

        register.setOnClickListener {
            hideKeyBoard()
            loading.visibility = View.VISIBLE
            signInViewMode.signIn(email.text.toString() , verifyPassword.text.toString(), name.text.toString())
        }



    }



    private fun updateUiWithUser() {
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Success").setMessage("Nooo").setPositiveButton("Ok" , null).show()
        // TODO : initiate successful logged in experience
    }

    private fun showLoginFailed(errorString: String) {
        // Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(errorString).setPositiveButton("Ok" , null).show()
    }

    fun hideKeyBoard(){
        val inputManager: InputMethodManager? =
            this?.getSystemService(Context.INPUT_METHOD_SERVICE) as?
                    InputMethodManager
        // check if no view has focus:
        val v = this?.currentFocus ?: return
        inputManager?.hideSoftInputFromWindow(v.windowToken, 0)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}


