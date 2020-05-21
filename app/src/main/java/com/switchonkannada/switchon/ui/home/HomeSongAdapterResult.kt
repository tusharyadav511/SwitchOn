package com.switchonkannada.switchon.ui.home

import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.switchonkannada.switchon.R
import com.switchonkannada.switchon.SongHomeModel

data class HomeSongAdapterResult (
    val songAdapter : FirestoreRecyclerAdapter<SongHomeModel, SongViewHolder>? = null
)

class SongViewHolder (itemVIew: View) : RecyclerView.ViewHolder(itemVIew){
    var poster = itemVIew?.findViewById<View>(R.id.songPoster) as ImageButton
}