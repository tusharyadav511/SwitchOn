package com.switchonkannada.switchon.ui.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.switchonkannada.switchon.ui.home.HomeLoadProfileResult

class UserProfileViewModel():ViewModel() {

    private val _userProfileResult = MutableLiveData<UserProfileResults>()
    val userProfileResults : LiveData<UserProfileResults> = _userProfileResult

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser!!.uid

    private val reference =
        db.collection("users").document(currentUser).addSnapshotListener {document, e ->

            if (e != null){
                _userProfileResult.value = UserProfileResults(error = e.message)
            }else{
                if (document!!.exists()){
                    val name = document?.getString("Name")
                    _userProfileResult.value = UserProfileResults(name = name)
                }else {
                    _userProfileResult.value = UserProfileResults(noDataError = "No Profile Image Found")

                }
            }
        }

    fun setUserName() {
        reference
    }

    override fun onCleared() {
        super.onCleared()
        reference.remove()
    }
}