package com.switchonkannada.switchon

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_trailer.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TrailerActivity : AppCompatActivity() {

    lateinit var backButton: FloatingActionButton
    lateinit var trailerView:PlayerView
    lateinit var simpleExoPlayer: SimpleExoPlayer

    val videoUrl:String = "https://firebasestorage.googleapis.com/v0/b/switch-on-39001.appspot.com/o/Movie%20on%2021-10-2019%20at%2000.15.mp4?alt=media&token=a1b13ab8-f744-44f7-8074-6d03c6e2d0da"

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        backButton.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_trailer)
        fullscreen_content.setOnClickListener { toggle() }
        supportActionBar?.hide()

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        trailerView = findViewById(R.id.trailer_movie_view)


        backButton = findViewById(R.id.trailerBackButton)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.

        inExoPlayer()

        trailerView.setControllerVisibilityListener { visibility ->
                when (visibility) {
                    View.GONE -> {
                        backButton.visibility = View.GONE
                        hide()
                    }
                    View.VISIBLE -> {
                        backButton.visibility = View.VISIBLE
                        show()

                    }
                }

        }


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        backButton.visibility = View.GONE
        mVisible = false
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }



    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }


    private fun inExoPlayer(){

        simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        trailerView.player = simpleExoPlayer

        val dataSourceFactory: DataSource.Factory =  DefaultDataSourceFactory(this , Util.getUserAgent(this , "Switch On"))

        val videoSource : MediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(videoUrl))
        simpleExoPlayer.prepare(videoSource)
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.shuffleModeEnabled = true

    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer.release()
    }
}
