package com.switchonkannada.switchon

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.switchonkannada.switchon.ui.userProfile.UserProfileViewModel

class UserProfileActivity : AppCompatActivity() {

    lateinit var userProfileViewModel: UserProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        userProfileViewModel = ViewModelProviders.of(this).get(UserProfileViewModel::class.java)
        userProfileViewModel.setUserName()

        userProfileViewModel.userProfileResults.observe(this , Observer {
            val nameResult = it ?: return@Observer
            if (nameResult.name != null){
                supportActionBar?.title = nameResult.name
            }
            if (nameResult.error != null){
                userNameError(nameResult.error)
            }
            if (nameResult.noDataError != null){
                noDataError(nameResult.noDataError)
            }
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.toolbar_back)
    }


    fun userNameError (error : String){
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    fun noDataError(error: String){
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
