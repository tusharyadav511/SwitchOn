package com.switchonkannada.switchon

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class ShowMovies : AppCompatActivity() , PaymentResultListener  {

    private val TAG = MainActivity::class.java.simpleName
    private val co = Checkout()
    lateinit var collaspingbar: CollapsingToolbarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_movie)

        collaspingbar = findViewById(R.id.collaspingToolbar)
        collaspingbar.title = "My name is Tushar Yadav"

        supportActionBar?.hide()

      // startPayment()
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
