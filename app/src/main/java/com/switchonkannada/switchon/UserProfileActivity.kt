package com.switchonkannada.switchon

import android.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.ui.userProfile.UserProfileViewModel

class UserProfileActivity : AppCompatActivity() {

    lateinit var userProfileViewModel: UserProfileViewModel
    lateinit var backButton:ImageButton
    lateinit var uploadButton: FloatingActionButton
    lateinit var userProfileImage:ImageButton
    lateinit var userName:TextView
    lateinit var userEmail:TextView
    lateinit var editUserName:EditText
    lateinit var editUserEmail:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        backButton = findViewById(R.id.profileBackButton)
        uploadButton = findViewById(R.id.profileUploadButton)
        userProfileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.profileName)
        userEmail = findViewById(R.id.profileEmailAddress)
        editUserName = findViewById(R.id.editName)
        editUserEmail = findViewById(R.id.editEmail)

        userProfileViewModel = ViewModelProviders.of(this).get(UserProfileViewModel::class.java)
        userProfileViewModel.setUserName()

        userProfileViewModel.userProfileResults.observe(this , Observer {
            val nameResult = it ?: return@Observer
            if (nameResult.name != null){
                userName.text = nameResult.name
                editUserName.setText(nameResult.name)
            }
            if(nameResult.email != null){
                userEmail.text = nameResult.email
                editUserEmail.text = nameResult.email
            }
            if (nameResult.imageUrl != null){
                setUserProfile(nameResult.imageUrl)
            }

            if (nameResult.error != null){
                userNameError(nameResult.error)
            }
            if (nameResult.noDataError != null){
                noDataError(nameResult.noDataError)
            }
        })

        supportActionBar?.hide()
        backButton.setOnClickListener {
            onBackPressed()
        }
        editUserEmail.setOnClickListener {
            Toast.makeText(this , "You are not allowed to change Email Address." , Toast.LENGTH_LONG).show()
        }
    }

    private fun setUserProfile(url:String){
        Picasso.get().load(url).transform( CircleTransform()).placeholder(R.drawable.person_icon).error(R.drawable.error).into(userProfileImage)
    }


    private fun userNameError (error : String){
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    private fun noDataError(error: String){
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

