package com.switchonkannada.switchon

import android.app.AlertDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_play_move.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class PlayMoveActivity : AppCompatActivity() , Player.EventListener {

    lateinit var backButton: FloatingActionButton
    lateinit var movieView: PlayerView
    lateinit var simpleExoPlayer: SimpleExoPlayer
    private var playbackPosition = 0L
    private lateinit var videoUrl:String
    private lateinit var url : Uri

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreen_content_controls.visibility = View.VISIBLE
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

        setContentView(R.layout.activity_play_move)
        fullscreen.setOnClickListener { toggle() }
        supportActionBar?.hide()

        videoUrl = intent.getStringExtra("MovieUrl")
        url = Uri.parse(videoUrl)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        movieView = findViewById(R.id.movie_view)
        backButton = findViewById(R.id.movieBackButton)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.

        inExoPlayer()

        movieView.setControllerVisibilityListener { visibility ->
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


    override fun onStop() {
        simpleExoPlayer.playWhenReady = false
        simpleExoPlayer.playbackState
        super.onStop()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
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
        fullscreen.systemUiVisibility =
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
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        AlertDialog.Builder(this , R.style.CustomDialogTheme).setTitle("Error").setMessage(error.sourceException.message).setPositiveButton("Ok" , null).show()
    }


    private fun releaseExoplayer(){
        playbackPosition = simpleExoPlayer.currentPosition
        simpleExoPlayer.release()
    }

    private fun inExoPlayer(){



        try {

            //Initialize the player
            simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
            movieView.player = simpleExoPlayer

            //Initialize the player
            val dataSourceFactory: DataSource.Factory =
                DefaultHttpDataSourceFactory(Util.getUserAgent(this, getString(R.string.app_name)))
            val videoSource: MediaSource = DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(url)

            simpleExoPlayer.prepare(videoSource)
            simpleExoPlayer.addListener(this)
            simpleExoPlayer.seekTo(playbackPosition)
            simpleExoPlayer.playWhenReady = true

        }catch (e:Exception){
            e.printStackTrace()

        }
    }

    override fun onDestroy() {
        releaseExoplayer()
        super.onDestroy()
    }
}
