package com.switchonkannada.switchon.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.HomeModel
import com.switchonkannada.switchon.R
import com.switchonkannada.switchon.ShowMovies
import java.lang.Exception

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser!!.uid
    private val query: Query = Firebase.firestore.collection("Movies")
    private val options = FirestoreRecyclerOptions.Builder<HomeModel>().setQuery(query , HomeModel::class.java).build()



    private val _homeLoadProfileResult = MutableLiveData<HomeLoadProfileResult>()
    val homeLoadProfileResult : LiveData<HomeLoadProfileResult> = _homeLoadProfileResult

    private val _homeAdapterPostKey = MutableLiveData<HomeAdapterPostKey>()
    val homeAdapterPostKey : LiveData<HomeAdapterPostKey> = _homeAdapterPostKey


    private val reference =
         db.collection("users").document(currentUser).addSnapshotListener {document, e ->
            try {
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
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    private val adapter = HomeAdapterResult(adapter = object : FirestoreRecyclerAdapter<HomeModel , ProductViewHolder>(options){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.home_video_layout , parent , false)
            return ProductViewHolder(v)
        }

        override fun onBindViewHolder(
            holder: ProductViewHolder,
            position: Int,
            model: HomeModel
        ) {

            model.setKey(snapshots.getSnapshot(position).id)
            val item = snapshots[position]
            val key = item?.getKey()


            val url = model.moviePoster
            Picasso.get().load(url).placeholder(R.drawable.hourglass).error(R.drawable.error_icon).into(holder.poster)

            holder.poster.setOnClickListener {
                _homeAdapterPostKey.value = HomeAdapterPostKey(key = key)
            }
        }

    })

    private val _homeAdapterResult = MutableLiveData<HomeAdapterResult>().apply {
        value = adapter
        adapter.adapter?.startListening()
    }
    val homeAdapterResult : LiveData<HomeAdapterResult> = _homeAdapterResult


    fun setUserProfile() {
        reference
    }

    override fun onCleared() {
        super.onCleared()
        reference.remove()
        adapter.adapter?.stopListening()
    }



}