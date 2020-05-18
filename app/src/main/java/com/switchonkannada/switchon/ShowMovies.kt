package com.switchonkannada.switchon

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    private val co = Checkout()
    private lateinit var collaspingbar: CollapsingToolbarLayout
    lateinit var showTrailer : FloatingActionButton
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    lateinit var buyButton:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_movie)

        collaspingbar = findViewById(R.id.collaspingToolbar)
        showTrailer = findViewById(R.id.trailerButton)
        buyButton = findViewById(R.id.buyButton)
        buyButton()
        collaspingbar.title = "My name is Tushar Yadav"

        supportActionBar?.hide()

        showTrailer.setOnClickListener {
            val intent = Intent(this , TrailerActivity::class.java)
            startActivity(intent)
        }



      // startPayment()
    }

    private fun buyButton(){
        Firebase.firestore.collection("Movies").document("JWXJWQVGLGlq8QIE3M7H")
            .collection("users").whereEqualTo(currentUser!! , false).addSnapshotListener { snapshot, exception ->
                if (exception != null){
                    AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Error").setMessage(exception.message)
                        .setPositiveButton("Ok", null).show()
                }else{
                    if (snapshot!!.isEmpty){
                        val drawable = ContextCompat.getDrawable(this, R.drawable.money)
                        buyButton.text = getString(R.string.buy_now)
                        buyButton.setCompoundDrawablesWithIntrinsicBounds(drawable , null ,null ,null)
                    }else{
                        val drawable = ContextCompat.getDrawable(this, R.drawable.play_arrow)
                        buyButton.text = getString(R.string.play_now)
                        buyButton.setCompoundDrawablesWithIntrinsicBounds(drawable , null ,null ,null)
                    }
                }
            }
    }

    private fun startPayment(){
        co.setKeyID("rzp_test_d5h4wAHU4Ax9y7")
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
        Toast.makeText(this , p1 , Toast.LENGTH_LONG).show()
        Log.i("Fuck" , p1)
    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this , p0 , Toast.LENGTH_LONG).show()
        AlertDialog.Builder(this, R.style.CustomDialogTheme).setTitle("Success").setMessage("YoYOYOYO")
            .setPositiveButton("Ok", null).show()
    }
}
