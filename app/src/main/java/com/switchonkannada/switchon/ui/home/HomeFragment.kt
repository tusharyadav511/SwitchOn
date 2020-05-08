package com.switchonkannada.switchon.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.CircleTransform
import com.switchonkannada.switchon.R
import com.switchonkannada.switchon.UserProfileActivity

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var target : com.squareup.picasso.Target? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)

        homeViewModel.setUserProfile()

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar_menu , menu)
        setUserProfile(menu)
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
        activity?.let { activity ->
            homeViewModel.homeLoadProfileResult.observe(activity, Observer {
                val homeResult = it ?: return@Observer

                if (homeResult.error != null) {
                    userProfileError(homeResult.error)
                }
                if (homeResult.imageUrl != null){
                    menu.findItem(R.id.homeMenuItem)?.let { menuItem ->
                        target = object : com.squareup.picasso.Target {
                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                menuItem.setIcon(R.drawable.person_icon)
                            }

                            override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                                menuItem.setIcon(R.drawable.person_icon)
                            }

                            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                menuItem.icon =
                                    BitmapDrawable(resources, CircleTransform.getCroppedBitmap(bitmap!!))
                            }
                        }.picassoLoad(homeResult.imageUrl, resources)
                    }
                }

                if(homeResult.noDataError != null){
                    noDataError(homeResult.noDataError)
                }
            })
        }


    }
    fun userProfileError (error : String){
        AlertDialog.Builder(activity , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }

    fun noDataError(error: String){
        AlertDialog.Builder(activity , R.style.CustomDialogTheme).setTitle("Error").setMessage(error).setPositiveButton("Ok" , null).show()
    }
}

fun com.squareup.picasso.Target.picassoLoad(url: String, resources: Resources): com.squareup.picasso.Target {
    Picasso.get().load(url)
        .resize(resources.getDimension(R.dimen.menuIconSize).toInt(),
            resources.getDimension(R.dimen.menuIconSize).toInt())
        .into(this)

    return this
}
