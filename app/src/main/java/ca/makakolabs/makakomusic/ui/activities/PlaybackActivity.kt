package ca.makakolabs.makakomusic.ui.activities

import android.app.Activity
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.services.MakakoPlaybackService
import ca.makakolabs.makakomusic.ui.viewholders.CircleTransform
import ca.makakolabs.makakomusic.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playback_activity.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import android.provider.MediaStore






class PlaybackActivity : AppCompatActivity() {
    private val PROGRESS_UPDATE_INTERNAL: Long = 1000
    private val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100
    private var mediaController: MediaControllerCompat? = null
    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mLastPlaybackState: PlaybackStateCompat? = null
    private var mHandler = Handler()
    private lateinit var mMediaBrowser: MediaBrowserCompat
    private lateinit var song: Song
    private var percentage=0.0


    private val mUpdateProgressTask = Runnable { updateProgress() }


    companion object {
        val TAG = "PlaybackActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playback_activity)
//        song = intent.extras.getParcelable("song") as Song

        mMediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MakakoPlaybackService::class.java), mConnectionCallback, null
        )


    }

    override fun onStart() {
        super.onStart()
        mMediaBrowser.connect()

    }

    private fun updateUI(newMetaData: MediaMetadataCompat, pbState: PlaybackStateCompat, bundle: Bundle) {

       song = bundle.getParcelable("com.makakolabs.makakomusic.song") as Song
        playback_title.text = song.title
        playback_album.text = song.album
        playback_artist.text = song.artist
        playback_playtime.text = Utils.convertToTime(song.duration)
        Picasso.get().load(song.description.iconUri)
            .resize(250, 250)
            .centerCrop()
            .placeholder(R.drawable.ic_empty_album)
            .error(R.drawable.ic_empty_album)
            .transform(CircleTransform())
            .into(playback_imageview_album)
        mHandler = Handler()

        Thread(Runnable {

            mHandler.post {
                val myBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, song.description.iconUri)
                val blurredBackground = Utils.blurImage(this@PlaybackActivity,myBitmap, 250, 250)
                playback_constraint_layout.background=  BitmapDrawable(resources,blurredBackground)
            }
        }).start()




    }

//    inner class LoadBitmapTask : AsyncTask<Uri, Void, BitmapDrawable>(){
//        override fun doInBackground(vararg params: Uri?): BitmapDrawable {
//            var myBitmap = Picasso.get().load(params[0]).get()
//            val blurredBackground = Utils.blurImage(this@PlaybackActivity,myBitmap, 250, 250)
//            return  BitmapDrawable(resources,blurredBackground)
//
//        }
//
//    }

    private val mConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.v(TAG, "onConnected")
            try {
                connectToSession(mMediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                Log.e(TAG, "could not connect media controller")
            }

        }

        override fun onConnectionFailed() {
            Log.v(TAG, "connection failed")
        }
    }

    @Throws(RemoteException::class)
    private fun connectToSession(mToken: MediaSessionCompat.Token) {
        if (mediaController == null) {
            Log.d(TAG, "MEdia controller is null!")

            mToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@PlaybackActivity, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(this@PlaybackActivity, mediaController)

                // Display the initial state
                val metadata = mediaController.metadata
                val bundle = mediaController.extras
                bundle.classLoader = Song::class.java!!.classLoader
//                for (key in bundle.keySet()) {
//                    Log.d(TAG, "$key is a key in the bundle")
//                }

                val pbState = mediaController.playbackState

                updateUI(metadata, pbState, bundle)

                // Register a Callback to stay in sync
                mediaController.registerCallback(controllerCallback)

                val state = mediaController.playbackState

                updatePlaybackState(state)

            }
            scheduleSeekbarUpdate()
        }

    }


    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMediaBrowser.disconnect()

    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            //update the UI according to the new metadata


        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.d(TAG, "State changed" + state!!.state)

         }
    }

    private fun updatePlaybackState(stateCompat: PlaybackStateCompat){
        mLastPlaybackState = stateCompat
    }

    private fun updateProgress() {

        if (mLastPlaybackState == null) {
            return
        }
        var currentPosition = mLastPlaybackState!!.position
        if (mLastPlaybackState!!.state == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            val timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState!!.lastPositionUpdateTime
            currentPosition += (timeDelta.toInt() * mLastPlaybackState!!.playbackSpeed).toLong()
        }
        playback_playtime.text = Utils.convertToTime(currentPosition) +" / "+Utils.convertToTime(song.duration)
        percentage = ((currentPosition.toDouble() / song.duration.toDouble()))
        playback_imageview_bg_slider.rotate((percentage*360).toFloat())
    }

    private fun scheduleSeekbarUpdate() {
        stopSeekbarUpdate()
        if (!mExecutorService.isShutdown) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                { mHandler.post(mUpdateProgressTask) }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS
            )
        }
    }

    private fun stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture!!.cancel(false)
        }
    }

}