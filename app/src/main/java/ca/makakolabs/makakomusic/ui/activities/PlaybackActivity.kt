package ca.makakolabs.makakomusic.ui.activities

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.services.MakakoPlaybackService

class PlaybackActivity : Activity() {
    private lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var song: Song

    companion object {
        val TAG = "PlaybackActivity"
    }

    fun playMedia(item: Song){
        val mediaController=MediaControllerCompat.getMediaController(this@PlaybackActivity)
            mediaController.transportControls.playFromMediaId(item.id, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser = MediaBrowserCompat(
            this, ComponentName(this, MakakoPlaybackService::class.java),
            connectionCallbacks,
            null
        )




    }

    override fun onStart() {
        super.onStart()
         song = intent.extras.getParcelable("song") as Song




    }

    override fun onPause() {
        super.onPause()
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    applicationContext, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(this@PlaybackActivity, mediaController)

            }

            if(MediaControllerCompat.getMediaController(this@PlaybackActivity) != null){
                Log.d(TAG,"Playing ${song.title}")
                playMedia(song)
            }
        }


    }
}