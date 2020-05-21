package com.switchonkannada.switchon

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class ShowMovies : AppCompatActivity() , PaymentResultListener  {


    lateinit var backButton: FloatingActionButton
    private val co = Checkout()
    private lateinit var collaspingbar: CollapsingToolbarLayout
    lateinit var showTrailer : FloatingActionButton
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    lateinit var buyButton:Button
    lateinit var photoId: String
    private val bd = Firebase.firestore.collection("Movies")
    lateinit var rzpApi:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_movie)


        Firebase.firestore.collection("paymentApi").document("rzpApi").addSnapshotListener { documentSnapshot, exception ->
            if (exception != null){
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(exception.message)
                    .setPositiveButton("Ok", null).show()
            }else{
                val key = documentSnapshot?.getString("apiKeyId")
                if (key != null) {
                    rzpApi = key
                }
            }
        }



        collaspingbar = findViewById(R.id.collaspingToolbar)
        showTrailer = findViewById(R.id.trailerButton)
        buyButton = findViewById(R.id.buyButton)
        photoId = intent.getStringExtra("post_key")
        backButton = findViewById(R.id.backButtonMovie)


        buyButton()
        collaspingbar.title = "My name is Tushar Yadav"

        supportActionBar?.hide()

        showTrailer.setOnClickListener {
            val intent = Intent(this , TrailerActivity::class.java)
            startActivity(intent)
        }
        buyButton.setOnClickListener {
            buttonPressed()
        }
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun buyButton(){
        bd.document(photoId)
            .collection("users").whereEqualTo(currentUser!! , false).addSnapshotListener {
                    snapshot, exception ->

                if (exception != null){
                    AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(exception.message)
                        .setPositiveButton("Ok", null).show()
                }else{
                    if (snapshot?.isEmpty == false){
                        val drawable = ContextCompat.getDrawable(this, R.drawable.money)
                        buyButton.text = getString(R.string.buy_now)
                        buyButton.setCompoundDrawablesWithIntrinsicBounds(drawable , null ,null ,null)
                    }
                }
            }

        bd.document(photoId).collection("users").whereEqualTo(currentUser!! , true).addSnapshotListener { querySnapshot, firestoreException ->
            if (firestoreException != null){
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(firestoreException.message)
                    .setPositiveButton("Ok", null).show()
            }else{
                if (querySnapshot?.isEmpty == false){
                    val drawable = ContextCompat.getDrawable(this, R.drawable.play_arrow)
                    buyButton.text = getString(R.string.play_now)
                    buyButton.setCompoundDrawablesWithIntrinsicBounds(drawable , null ,null ,null)
                }
            }
        }
    }

    private fun buttonPressed(){

        if(buyButton.text == getString(R.string.buy_now) ){
            startPayment()
        }else{
            val intent = Intent(this , PlayMoveActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("MovieId" , photoId)
            startActivity(intent)
        }
    }

    private fun startPayment(){
        co.setKeyID(rzpApi)
        val activity:Activity = this
        try {
            val options = JSONObject()
            options.put("name","Razorpay Corp")
            options.put("description","Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency","INR")
            options.put("amount","100")

            val prefill = JSONObject()
          //  prefill.put("email","test@razorpay.com")
          //  prefill.put("contact","9876543210")

          //  options.put("prefill",prefill)
            co.open(activity,options)
        }catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(p1)
            .setPositiveButton("Ok", null).show()
    }

    override fun onPaymentSuccess(p0: String?) {
        val data = hashMapOf(
            currentUser to true
        )
        bd.document(photoId).collection("users").document(currentUser!!).set(data).addOnCompleteListener {
            if(it.isSuccessful){
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Success").setMessage("Payment Successful!")
                    .setPositiveButton("Ok", null).show()
            }else{
                AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(it.exception?.message)
                    .setPositiveButton("Ok", null).show()
            }
        }

    }
}
