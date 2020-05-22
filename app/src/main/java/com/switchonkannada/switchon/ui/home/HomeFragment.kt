package com.switchonkannada.switchon.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.android.billingclient.api.BillingClient
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var target: com.squareup.picasso.Target? = null
    private lateinit var mRecycler: RecyclerView
    private lateinit var songRecycler: RecyclerView
   // private lateinit var billingClient: BillingClient

    //  private val skuList = listOf("android.test.purchased")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        homeViewModel.setUserProfile()
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        setHasOptionsMenu(true)

        mRecycler = root.findViewById(R.id.homeRecycler)
        songRecycler = root.findViewById(R.id.songRecycler)

        val snapHelper: SnapHelper = LinearSnapHelper()

        val layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                activity,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        val songLayoutManager = LinearLayoutManager(activity , LinearLayoutManager.HORIZONTAL , false)

        mRecycler.setHasFixedSize(true)
        mRecycler.layoutManager = layoutManager

        songRecycler.setHasFixedSize(true)
        songRecycler.layoutManager = songLayoutManager



        homeViewModel.homeAdapterResult.observe(viewLifecycleOwner, Observer {
            val adapter = it ?: return@Observer
            mRecycler.adapter = adapter.adapter
            snapHelper.attachToRecyclerView(mRecycler)
        })

        homeViewModel.homeSongAdapterPostResult.observe(viewLifecycleOwner , Observer {
            val adapter = it ?: return@Observer
            songRecycler.adapter = adapter.songAdapter
        })

        homeViewModel.homeAdapterPostKey.observe(viewLifecycleOwner, Observer {
            val key = it ?: return@Observer
            if (key.key != null) {
                val intent = Intent(activity, ShowMovies::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("post_key", key.key)
                startActivity(intent)
            }
        })

        homeViewModel.homeSongUrl.observe(viewLifecycleOwner , Observer {
            val key = it ?: return@Observer

            if (key.songUrl != null){
                val intent = Intent(activity , SongActivity ::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("song_url", key.songUrl)
                startActivity(intent)
            }
        })

        //     billingClient()

        return root
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        setUserProfile(menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
                    if (homeResult.imageUrl != null) {
                        menu.findItem(R.id.homeMenuItem)?.let { menuItem ->
                            try {
                                target = object : com.squareup.picasso.Target {
                                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                        menuItem.setIcon(R.drawable.person_icon)
                                    }

                                    override fun onBitmapFailed(
                                        e: java.lang.Exception?,
                                        errorDrawable: Drawable?
                                    ) {
                                        menuItem.setIcon(R.drawable.person_icon)
                                    }

                                    override fun onBitmapLoaded(
                                        bitmap: Bitmap?,
                                        from: Picasso.LoadedFrom?
                                    ) {
                                        try {
                                            menuItem.icon = BitmapDrawable(
                                                resources,
                                                CircleTransform.getCroppedBitmap(bitmap!!)
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                    }
                                }.picassoLoad(homeResult.imageUrl, resources)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    if (homeResult.noDataError != null) {
                        noDataError(homeResult.noDataError)
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun userProfileError(error: String) {
        AlertDialog.Builder(activity, R.style.CustomDialogTheme).setTitle("Error").setMessage(error)
            .setPositiveButton("Ok", null).show()
    }

    private fun noDataError(error: String) {
        AlertDialog.Builder(activity, R.style.CustomDialogTheme).setTitle("Error").setMessage(error)
            .setPositiveButton("Ok", null).show()
    }

    private fun com.squareup.picasso.Target.picassoLoad(
        url: String,
        resources: Resources
    ): com.squareup.picasso.Target {
        Picasso.get().load(url)
            .resize(
                resources.getDimension(R.dimen.menuIconSize).toInt(),
                resources.getDimension(R.dimen.menuIconSize).toInt()
            )
            .into(this)

        return this
    }

    /*  private fun billingClient() {
          billingClient =
              BillingClient.newBuilder(requireActivity()).enablePendingPurchases().setListener(this)
                  .build()

          billingClient.startConnection(object : BillingClientStateListener {
              override fun onBillingSetupFinished(billingResult: BillingResult) {
                  if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                      // The BillingClient is ready. You can query purchases here.
                      Toast.makeText(activity , "Successful" , Toast.LENGTH_LONG).show()
                      loadAllSKUs()
                  }
              }

              override fun onBillingServiceDisconnected() {
                  // Try to restart the connection on the next request to
                  // Google Play by calling the startConnection() method.
                  Toast.makeText(activity , "Nooooo" , Toast.LENGTH_LONG).show()

              }
          })
      }




      override fun onPurchasesUpdated(p0: BillingResult?, p1: MutableList<Purchase>?) {
          if (p0?.responseCode == BillingClient.BillingResponseCode.OK && p1 != null) {
              for (purchase in p1) {
                     acknowledgePurchase(purchase)


              }
          } else if (p0?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
              // Handle an error caused by a user cancelling the purchase flow.

          } else if (p0?.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

                      Toast.makeText(activity , "purchase.developerPayload", Toast.LENGTH_LONG).show()
          } else {
              // Handle any other error codes.
          }

      }



      private fun loadAllSKUs() = if (billingClient.isReady) {
          val params = SkuDetailsParams
              .newBuilder()
              .setSkusList(skuList)
              .setType(BillingClient.SkuType.INAPP)
              .build()
          billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
              // Process the result.
              if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                  for (skuDetails in skuDetailsList) {
                      if (skuDetails.sku == "android.test.purchased")
                          buttonBuyProduct.setOnClickListener {
                              val billingFlowParams = BillingFlowParams
                                  .newBuilder()
                                  .setSkuDetails(skuDetails)
                                  .build()
                              billingClient.launchBillingFlow(activity, billingFlowParams)
                           //   Toast.makeText(activity , "Puschase" , Toast.LENGTH_LONG).show()
                          }
                  }
              }
          }

      } else {
          println("Billing Client not ready")
      }

      private fun acknowledgePurchase( purchase: Purchase) {

          val consumeParams = ConsumeParams.newBuilder()
              .setPurchaseToken(purchase.purchaseToken)
              .setDeveloperPayload(purchase.developerPayload).build()


          if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
              if (!purchase.isAcknowledged){
                  billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                          Toast.makeText(activity, "Yoo" , Toast.LENGTH_LONG).show()

                      }
                  }
              }
          }
      }*/
}



