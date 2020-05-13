package com.switchonkannada.switchon.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser!!.uid


    private val _homeLoadProfileResult = MutableLiveData<HomeLoadProfileResult>().apply {
        db.collection("users").document(currentUser).addSnapshotListener(
            MetadataChanges.INCLUDE,
            EventListener { document, e ->

                try {
                    if (document != null) {
                        value = if (e != null){
                            HomeLoadProfileResult(error = e.message)
                        }else{
                            if (document.exists()){
                                val imageUrl = document.getString("ProfileImage")
                                HomeLoadProfileResult(imageUrl = imageUrl.toString())
                            }else {
                                HomeLoadProfileResult(noDataError = "No Profile Image Found")

                            }
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            })
    }

     val homeLoadProfileResult : LiveData<HomeLoadProfileResult> = _homeLoadProfileResult


}