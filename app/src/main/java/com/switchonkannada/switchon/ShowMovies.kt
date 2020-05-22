package com.switchonkannada.switchon

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.squareup.picasso.Picasso
import com.switchonkannada.switchon.ui.showMovie.ShowMovieViewModel
import org.json.JSONObject

class ShowMovies : AppCompatActivity(), PaymentResultListener {


    lateinit var showMoviesViewModel: ShowMovieViewModel
    private val co = Checkout()
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var photoId: String
    private val bd = Firebase.firestore.collection("Movies")
    private lateinit var rzpApi: String
    private lateinit var movieUrl : String
    private lateinit var trailer:String

    lateinit var buyButton: Button
    lateinit var backButton: FloatingActionButton
    lateinit var collaspingbar: CollapsingToolbarLayout
    lateinit var showTrailer: FloatingActionButton
    lateinit var moviePoster:ImageView
    lateinit var languageTxt:TextView
    lateinit var ageTxt:TextView
    lateinit var actionTxt:TextView
    lateinit var aboutMovie: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_movie)

        photoId = intent.getStringExtra("post_key")
        showMoviesViewModel = ViewModelProviders.of(this).get(ShowMovieViewModel::class.java)
        showMoviesViewModel.loadMovie(photoId)

        showMoviesViewModel.showMovieApiResult.observe(this, Observer {
            val api = it ?: return@Observer

            if (api.error != null) {
                //TODO :- add error
            }
            if (api.api != null) {
                rzpApi = api.api
            }
        })

        collaspingbar = findViewById(R.id.collaspingToolbar)
        showTrailer = findViewById(R.id.trailerButton)
        buyButton = findViewById(R.id.buyButton)
        backButton = findViewById(R.id.backButtonMovie)
        moviePoster = findViewById(R.id.moviePoster)
        languageTxt = findViewById(R.id.languageTxt)
        ageTxt = findViewById(R.id.ageTxt)
        actionTxt = findViewById(R.id.actionTxt)
        aboutMovie = findViewById(R.id.aboutMovie)



        showMoviesViewModel.showMovieDetailsResult.observe(this , Observer {
            val adapter = it ?:return@Observer

            if (adapter.error != null){
                //TODO :- add error
            }
            if(adapter.name != null){
                collaspingbar.title = adapter.name
            }

            if(adapter.about != null){
                aboutMovie.text = adapter.about
            }
            if(adapter.actionType != null){
                actionTxt.text = adapter.actionType
            }
            if(adapter.age != null){
                ageTxt.text != adapter.age
            }
            if(adapter.language != null){
                languageTxt.text != adapter.language
            }
            if(adapter.movieUrl != null){
                movieUrl = adapter.movieUrl
            }
            if (adapter.poster != null){
                val url = adapter.poster
                Picasso.get().load(url).placeholder(R.drawable.hourglass).error(R.drawable.error_icon)
                    .into(moviePoster)
            }

            if (adapter.trailer != null){
                trailer = adapter.trailer
            }

        })



        buyButton()

        supportActionBar?.hide()

        showTrailer.setOnClickListener {
            val intent = Intent(this, TrailerActivity::class.java)
            intent.putExtra("trailerUrl" , trailer)
            startActivity(intent)
        }
        buyButton.setOnClickListener {
            buttonPressed()
        }
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun buyButton() {

        showMoviesViewModel.buyButtonRef(photoId)

        showMoviesViewModel.buyButtonFalse.observe(this , Observer {
            val result = it ?: return@Observer

            if (result.error != null){
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error")
                    .setMessage(result.error)
                    .setPositiveButton("Ok", null).show()
            }
            if(result.result != null){
                val drawable = ContextCompat.getDrawable(this, R.drawable.money)
                buyButton.text = getString(R.string.buy_now)
                buyButton.setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
            }
        })


        showMoviesViewModel.buyButtonTrue.observe(this , Observer {
            val result = it ?: return@Observer

            if(result.error != null){
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error")
                    .setMessage(result.error)
                    .setPositiveButton("Ok", null).show()
            }

            if(result.result != null){
                val drawable = ContextCompat.getDrawable(this, R.drawable.play_arrow)
                buyButton.text = getString(R.string.play_now)
                buyButton.setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
            }

        })
    }

    private fun buttonPressed() {

        if (buyButton.text == getString(R.string.buy_now)) {
            startPayment()
        } else {
            val intent = Intent(this, PlayMoveActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("MovieId", photoId)
            startActivity(intent)
        }
    }

    private fun startPayment() {
        co.setKeyID(rzpApi)
        val activity: Activity = this
        try {
            val options = JSONObject()
            options.put("name", "Razorpay Corp")
            options.put("description", "Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency", "INR")
            options.put("amount", "100")

            val prefill = JSONObject()
            //  prefill.put("email","test@razorpay.com")
            //  prefill.put("contact","9876543210")

            //  options.put("prefill",prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(p1)
            .setPositiveButton("Ok", null).show()
    }

    override fun onPaymentSuccess(p0: String?) {

        showMoviesViewModel.paymentSuccessRef(photoId)
        showMoviesViewModel.showMovePaymentResult.observe(this, Observer {
            val result = it ?: return@Observer
            if (result.error != null) {
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error")
                    .setMessage(result.error)
                    .setPositiveButton("Ok", null).show()
            }
            if (result.success != null) {
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Success")
                    .setMessage(result.success)
                    .setPositiveButton("Ok", null).show()
            }
        })
    }
}
