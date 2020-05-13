package com.switchonkannada.switchon.ui.home

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.CircleTransform
import com.switchonkannada.switchon.R

class HomeViewModel : ViewModel() {

    private val _homeLoadProfileResult = MutableLiveData<HomeLoadProfileResult>()
    val homeLoadProfileResult : LiveData<HomeLoadProfileResult> = _homeLoadProfileResult


    // Access a Cloud Firestore instance from your Activity
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser!!.uid

    private val reference =
        db.collection("users").document(currentUser).addSnapshotListener {document, e ->

            if (e != null){
                _homeLoadProfileResult.value = HomeLoadProfileResult(error = e.message)
            }else{
                if (document!!.exists()){
                    val imageUrl = document?.getString("ProfileImage")
                    _homeLoadProfileResult.value = HomeLoadProfileResult(imageUrl = imageUrl.toString())
                }else {
                    _homeLoadProfileResult.value = HomeLoadProfileResult(noDataError = "No Profile Image Found")

                }
            }
        }

    fun setUserProfile() {
        reference
    }

    override fun onCleared() {
        super.onCleared()
        reference.remove()
    }
}