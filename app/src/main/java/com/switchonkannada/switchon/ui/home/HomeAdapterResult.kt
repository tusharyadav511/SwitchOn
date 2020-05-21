package com.switchonkannada.switchon.ui.home

import android.content.Context
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.switchonkannada.switchon.HomeModel
import com.switchonkannada.switchon.R
import com.switchonkannada.switchon.SongHomeModel

data class HomeAdapterResult (
    val adapter : FirestoreRecyclerAdapter<HomeModel, ProductViewHolder>?= null
)

class ProductViewHolder (itemVIew: View) : RecyclerView.ViewHolder(itemVIew) {
    var poster = itemVIew?.findViewById<View>(R.id.moviePoster) as ImageButton

}

