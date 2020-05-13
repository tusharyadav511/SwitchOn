package com.switchonkannada.switchon.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.CircleTransform
import com.switchonkannada.switchon.HomeModel
import com.switchonkannada.switchon.R
import com.switchonkannada.switchon.UserProfileActivity


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var target : com.squareup.picasso.Target? = null
    private lateinit var mRecycler:RecyclerView
    private var adapter : FirestoreRecyclerAdapter<HomeModel, ProductViewHolder>?= null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)

        mRecycler = root.findViewById(R.id.homeRecycler)
        val layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity)

        mRecycler.setHasFixedSize(true)
        mRecycler.layoutManager = layoutManager

        userFeed()

        return root
    }


    private fun userFeed(){
        val query: Query = Firebase.firestore.collection("Movies")
        val options = FirestoreRecyclerOptions.Builder<HomeModel>().setQuery(query , HomeModel::class.java).build()

        adapter = object : FirestoreRecyclerAdapter<HomeModel , ProductViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.home_video_layout , parent , false)
                return ProductViewHolder(v)
            }
            override fun onBindViewHolder(
                holder: ProductViewHolder,
                position: Int,
                model: HomeModel
            ) {

                val url = model.postUrl
                Picasso.get().load(url).placeholder(R.drawable.hourglass).error(R.drawable.error_icon).into(holder.poster)
            }

        }
        mRecycler.adapter = adapter
        adapter?.startListening()
    }


    private inner class ProductViewHolder(itemVIew:View) : RecyclerView.ViewHolder(itemVIew) {
        var poster = itemVIew?.findViewById<View>(R.id.moviePoster) as ImageButton
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        setUserProfile(menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar_menu , menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return  when(item.itemId) {
             R.id.homeMenuItem -> {
                 val intent = Intent(activity, UserProfileActivity::class.java)
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                 startActivity(intent)
                 true
             }
             else -> {
                 super.onOptionsItemSelected(item)
             }
         }
    }

    private fun setUserProfile(menu: Menu) {

        try {
            activity?.let { activity ->
                homeViewModel.homeLoadProfileResult.observe(activity, Observer {
                    val homeResult = it ?: return@Observer

                    if (homeResult.error != null) {
                        userProfileError(homeResult.error)
                    }
                    if (homeResult.imageUrl != null){
                        menu.findItem(R.id.homeMenuItem)?.let { menuItem ->
                            try {
                                target = object : com.squareup.picasso.Target {
                                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                        menuItem.setIcon(R.drawable.person_icon)
                                    }

                                    override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                                        menuItem.setIcon(R.drawable.person_icon)
                                    }

                                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                        try {
                                            menuItem.icon = BitmapDrawable(resources, CircleTransform.getCroppedBitmap(bitmap!!))
                                        }catch (e:Exception){
                                            e.printStackTrace()
                                        }

                                    }
                                }.picassoLoad(homeResult.imageUrl, resources)
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                    }

                    if(homeResult.noDataError != null){
                        noDataError(homeResult.noDataError)
                    }
                })
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun userProfileError (error : String){
        AlertDialog.Builder(activity , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    fun noDataError(error: String){
        AlertDialog.Builder(activity , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    fun com.squareup.picasso.Target.picassoLoad(url: String, resources: Resources): com.squareup.picasso.Target {
        Picasso.get().load(url)
            .resize(resources.getDimension(R.dimen.menuIconSize).toInt(),
                resources.getDimension(R.dimen.menuIconSize).toInt())
            .into(this)

        return this
    }
}


