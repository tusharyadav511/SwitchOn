package com.switchonkannada.switchon.ui.userProfile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import java.net.HttpURLConnection
import java.net.URL

class UserProfileViewModel():ViewModel() {

    private val _userProfileResult = MutableLiveData<UserProfileResults>()
    val userProfileResults : LiveData<UserProfileResults> = _userProfileResult

    private val _userProfileImageResult = MutableLiveData<UploadProfileImageResult>()
    val userProfileImageResult : LiveData<UploadProfileImageResult> = _userProfileImageResult

    private val _puttingImageResult = MutableLiveData<PuttingImageResult>()
    val puttingImageResult : LiveData<PuttingImageResult> = _puttingImageResult

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser!!.uid
    private val mStorage =  FirebaseStorage.getInstance().reference.child("Profile Images")
        .child(currentUser).child("profilePhoto.jpg")

    private val reference =
        db.collection("users").document(currentUser).addSnapshotListener {document, e ->

            if (e != null){
                _userProfileResult.value = UserProfileResults(error = e.message)
            }else{
                if (document!!.exists()){
                    val name = document?.getString("Name")
                    val email = document?.getString("Email")
                    val profileImage = document?.getString("ProfileImage")
                    _userProfileResult.value = UserProfileResults(name = name)
                    _userProfileResult.value = UserProfileResults(email = email)
                    _userProfileResult.value = UserProfileResults(imageUrl = profileImage)
                }else {
                    _userProfileResult.value = UserProfileResults(noDataError = "No Profile Image Found")

                }
            }
        }

    fun setUserName() {
        reference
    }

    fun uploadPhoto(data : ByteArray){

        var metadata = storageMetadata {
            contentType = "image/jpg"
        }
        mStorage.putBytes(data , metadata).addOnFailureListener {
            _userProfileImageResult.value = UploadProfileImageResult(error = it.message)
        }.addOnSuccessListener {
            _userProfileImageResult.value = UploadProfileImageResult(success = "Successful")
        }

    }

    fun putImage(image : ImageView){

        mStorage?.downloadUrl.addOnSuccessListener {

            var url = it.toString()
            val task = DownloadImage()
            val myImage: Bitmap
            try {
                myImage = task.execute(url).get()
                image.setImageBitmap(myImage)
                db.collection("users").document(currentUser).update("ProfileImage" , url)

            } catch (e: Exception) {
                e.printStackTrace()
                _puttingImageResult.value = PuttingImageResult(error = e.message)
                _puttingImageResult.value = PuttingImageResult(showProcess = false)
            }

        }.addOnFailureListener {
            _puttingImageResult.value = PuttingImageResult(showProcess = false)
            _puttingImageResult.value = PuttingImageResult(error = it.message)

        }
    }

    override fun onCleared() {
        super.onCleared()
        reference.remove()
    }


    inner class DownloadImage : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            return try {
                val url = URL(urls[0])
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val `in` = connection.inputStream
                BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
                _puttingImageResult.value = PuttingImageResult(showProcess = false)
                null
            }
        }
        override fun onPreExecute() {
            super.onPreExecute()
            _puttingImageResult.value = PuttingImageResult(showProcess = true)

        }
        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
            _puttingImageResult.value = PuttingImageResult(showProcess = true)

        }
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            _puttingImageResult.value = PuttingImageResult(showProcess = false)

        }
    }
}