package com.switchonkannada.switchon

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.ui.userProfile.UserProfileViewModel
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UserProfileActivity : AppCompatActivity() {

    lateinit var userProfileViewModel: UserProfileViewModel
    lateinit var backButton:ImageButton
    lateinit var uploadButton: FloatingActionButton
    lateinit var userProfileImage:ImageButton
    lateinit var userName:TextView
    lateinit var userEmail:TextView
    lateinit var editUserName:EditText
    lateinit var editUserEmail:TextView





    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getPhoto()
            }
        }

    }


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

        userProfileViewModel.userProfileImageResult.observe(this , Observer {
            val result = it ?: return@Observer

            if(result.success != null){
                puttingImage()
            }
            if (result.error != null){
                displayErrorMessage(result.error)
            }

            if(result.onTick != null){
                uploadButton?.visibility = View.GONE

                showProgress(true)
            }

            if(result.onFinish != null){
                uploadButton?.visibility = View.VISIBLE

                //  progress.visibility = View.GONE

                showProgress(false)

                Toast.makeText(this,"Profile Changed!",Toast.LENGTH_LONG).show()
                finish()
            }
        })

        userProfileViewModel.puttingImageResult.observe(this , Observer {
            val result = it ?: return@Observer

            if (result.showProcess != null){
                when (result.showProcess){
                    true -> {
                        showProgress(true)
                    }else -> {
                    showProgress(false)
                }
                }
            }
            if(result.error != null){
                displayErrorMessage(result.error)
            }
        })



        supportActionBar?.hide()
        backButton.setOnClickListener {
            onBackPressed()
        }
        editUserEmail.setOnClickListener {
            Toast.makeText(this , "You are not allowed to change Email Address." , Toast.LENGTH_LONG).show()
        }

        userProfileImage.setOnClickListener {
            uploadProfilePhoto()
        }

        uploadButton.setOnClickListener {
            saveProfile()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==1 && resultCode== Activity.RESULT_OK && data!=null){
            try {
                var selectedImage: Uri =data.data
                var bitmappic: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImage)
                showProgress(true)
                //Uploading image to database
                val bao = ByteArrayOutputStream()
                bitmappic.compress(Bitmap.CompressFormat.JPEG, 15, bao)
                val data = bao.toByteArray()

                Toast.makeText(this,"Image(s) Uploading",Toast.LENGTH_LONG).show()

                userProfileViewModel.uploadPhoto(data)

            }catch (e:Exception){
                showProgress(false)
                e.printStackTrace()
            }
        }

    }


    private fun uploadProfilePhoto(){
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
        else{
            getPhoto()
        }
    }

    private fun getPhoto(){
        var intent2:Intent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent2,1)
    }


    private fun puttingImage(){
        userProfileViewModel.putImage(userProfileImage)
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            userLayoutDetails.visibility = if (show) View.GONE else View.VISIBLE
            userLayoutDetails.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        userLayoutDetails.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

            process_Bar.visibility = if (show) View.VISIBLE else View.GONE
            process_Bar.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        process_Bar.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            process_Bar.visibility = if (show) View.VISIBLE else View.GONE
            userLayoutDetails.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    private fun saveProfile(){
        editUserName.error = null
        val nameString = editUserName.text.toString()

        var cancel= false
        var focusView:View ?=null

        if(TextUtils.isEmpty(nameString)){
            editUserName.error = getString(R.string.error_field_required)
            focusView = editUserName
            cancel = true
        }

        if(cancel){
            focusView?.requestFocus()
        }else{
            updateUser()
        }
    }

    private fun updateUser(){
        hideKeyboard()
        userProfileViewModel.updateUser(editUserName)
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

    private fun displayErrorMessage(error: String){
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun hideKeyboard() {
        try {
            val activityView = this?.window?.decorView?.rootView
            activityView?.let {
                val imm =
                    this?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(it.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

