package com.switchonkannada.switchon.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.switchonkannada.switchon.R
import com.switchonkannada.switchon.logInOption


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var logoutButton:Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mgoogleSignInClient: GoogleSignInClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        logoutButton = root.findViewById(R.id.buttonDashboard)
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mgoogleSignInClient = activity?.let {
            GoogleSignIn.getClient(it, gso)
        }!!

        logoutButton.setOnClickListener {
            logout()
        }

        return root
    }

    fun  logout(){
        val intent = Intent(activity , logInOption ::class.java)
        mAuth.currentUser?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)

                when (profile.providerId) {
                    "facebook.com" -> {
                        try {
                            LoginManager.getInstance().logOut()
                            mAuth.signOut()
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            activity?.finish()

                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                    "google.com" -> {
                        try {
                            val account =
                                GoogleSignIn.getLastSignedInAccount(activity)
                            if (account != null){
                                mgoogleSignInClient.signOut()
                                mAuth.signOut()
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                activity?.finish()
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                    else -> {
                        try {

                            mAuth.signOut()
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            activity?.finish()

                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

    }
}
