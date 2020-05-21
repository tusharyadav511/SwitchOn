package com.switchonkannada.switchon.ui.showMovie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ShowMovieViewModel : ViewModel() {


    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser!!.uid

    lateinit var loadMovie: ListenerRegistration
    lateinit var buyButtonFalseRef: ListenerRegistration
    lateinit var buyButtonTrueRef: ListenerRegistration

    private val _showMovieDetailsResult = MutableLiveData<ShowMovieDetailsResult>()
    val showMovieDetailsResult: LiveData<ShowMovieDetailsResult> = _showMovieDetailsResult

    private val _showMovieAPiResult = MutableLiveData<ShowMoveApiResult>()
    val showMovieApiResult : LiveData<ShowMoveApiResult> = _showMovieAPiResult

    private val _buyButtonFalse = MutableLiveData<BuyButtonFalse>()
    val buyButtonFalse : LiveData<BuyButtonFalse> = _buyButtonFalse

    private val _buyButtonTrue = MutableLiveData<BuyButtonTrue>()
    val buyButtonTrue : LiveData<BuyButtonTrue> = _buyButtonTrue

    private val _showMoviePaymentResult = MutableLiveData<ShowMovePaymentResult>()
    val showMovePaymentResult : LiveData<ShowMovePaymentResult> = _showMoviePaymentResult



    fun loadMovie(movieId: String) {
        loadMovie = Firebase.firestore.collection("Movies").document(movieId)
            .addSnapshotListener { documentSnapshot, exception ->
                if (exception != null) {
                    _showMovieDetailsResult.value =
                        ShowMovieDetailsResult(error = exception.message)
                } else {
                    if (documentSnapshot!!.exists()) {

                        val name = documentSnapshot.getString("name")
                        val about = documentSnapshot.getString("about")
                        val actionType = documentSnapshot.getString("actionType")
                        val age = documentSnapshot.getString("age")
                        val language = documentSnapshot.getString("language")
                        val movieUrl = documentSnapshot.getString("movieUrl")
                        val poster = documentSnapshot.getString("poster")

                        _showMovieDetailsResult.value = ShowMovieDetailsResult(
                            name = name, about = about,
                            actionType = actionType,
                            age = age,
                            language = language,
                            movieUrl = movieUrl,
                            poster = poster
                        )
                    }
                }
            }
    }

    private val paymentApiRef = Firebase.firestore.collection("paymentApi").document("rzpApi")
        .addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                _showMovieAPiResult.value = ShowMoveApiResult(error = exception.message)
            } else {
                val key = documentSnapshot?.getString("apiKeyId")
                if (key != null) {
                    _showMovieAPiResult.value = ShowMoveApiResult(api = key)
                }
            }
        }

    private fun buyButtonRef(movieId: String) {
        buyButtonFalseRef =
            Firebase.firestore.collection("Movies").document(movieId).collection("users")
                .whereEqualTo(currentUser!!, false).addSnapshotListener { snapshot, exception ->

                    if (exception != null) {
                        _buyButtonFalse.value = BuyButtonFalse(error = exception.message)
                    } else {
                        if (snapshot?.isEmpty == false) {
                           _buyButtonFalse.value = BuyButtonFalse(result = "Success")
                        }
                    }
                }

        buyButtonTrueRef =
            Firebase.firestore.collection("Movies").document(movieId).collection("users")
                .whereEqualTo(currentUser!!, true)
                .addSnapshotListener { querySnapshot, firestoreException ->
                    if (firestoreException != null) {
                        _buyButtonTrue.value = BuyButtonTrue(error = firestoreException.message)
                    } else {
                        if (querySnapshot?.isEmpty == false) {
                            _buyButtonTrue.value = BuyButtonTrue(result = "Success")
                        }
                    }
                }
    }

    private fun paymentSuccessRef(movieId: String) {

        val data = hashMapOf(
            currentUser to true
        )

        Firebase.firestore.collection("Movies").document(movieId).collection("users")
            .document(currentUser).set(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    _showMoviePaymentResult.value = ShowMovePaymentResult(success = "Payment Successful!")
                } else {
                    _showMoviePaymentResult.value = ShowMovePaymentResult(error = it.exception?.message)
                }
            }
    }


    override fun onCleared() {
        loadMovie?.remove()
        paymentApiRef.remove()
        buyButtonFalseRef.remove()
        buyButtonTrueRef.remove()
        super.onCleared()
    }

}