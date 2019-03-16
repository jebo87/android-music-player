package ca.makakolabs.makakomusic.ui.activities

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.support.v4.media.MediaBrowserCompat
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
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import java.io.FileNotFoundException


class PlaybackActivity : AppCompatActivity() {


    private val PROGRESS_UPDATE_INTERNAL: Long = 250
    private val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 10
    private var mediaController: MediaControllerCompat? = null
    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mLastPlaybackState: PlaybackStateCompat? = null
    private var mHandler = Handler()
    private lateinit var mMediaBrowser: MediaBrowserCompat
    private lateinit var song: Song
    private var percentage = 0.0
    private var pivX = 0
    private var pivY = 0
    private var tempBitmap: Bitmap? = null


    private val mUpdateProgressTask = Runnable { updateProgress() }


    companion object {
        val TAG = "PlaybackActivity"
        var isUILocked = false

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.playback_activity)

        mMediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MakakoPlaybackService::class.java), mConnectionCallback, null
        )


        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)


    }

    override fun onStart() {
        super.onStart()
        mMediaBrowser.connect()
        Thread(Runnable {

            mHandler.post {
                try {

                    tempBitmap = Utils.getBitmapFromVectorDrawableWithId(this, R.drawable.ic_albumart_bg)
                    pivX = tempBitmap!!.width / 2
                    pivY = tempBitmap!!.height / 2
                    tempBitmap!!.recycle()
                } catch (e: FileNotFoundException) {
                    //no album art
                }


            }
        }).start()

    }

    private fun updateUI(newMetaData: MediaMetadataCompat, pbState: PlaybackStateCompat, bundle: Bundle) {

        song = bundle.getParcelable("com.makakolabs.makakomusic.song") as Song

        //Load the song info into the different UI elements
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


                var myBitmap: Bitmap
                try {

                    //load the blurred background
                    myBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, song.description.iconUri)
                    val blurredBackground = Utils.blurImage(this@PlaybackActivity, myBitmap, 250, 250)
                    playback_constraint_layout.background = BitmapDrawable(resources, blurredBackground)

                } catch (e: FileNotFoundException) {
                    //no album art
                }


            }
        }).start()




        playback_imageview_bg_slider.pivx = pivX
        playback_imageview_bg_slider.pivy = pivY

        playback_imageview_bg_slider.duration = song.duration


    }


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



                updatePlaybackState(pbState)
                playback_imageview_bg_slider.mediaController = mediaController

                playback_pause_button.setOnClickListener {

                    when (mLastPlaybackState?.state) {
                        PlaybackStateCompat.STATE_PLAYING -> {
                            Log.d(TAG, "Is Playing, going to pause now")

                            mediaController!!.transportControls!!.pause()

                        }
                        PlaybackStateCompat.STATE_PAUSED -> {
                            Log.d(TAG, "Is paused, going to play now")
                            mediaController!!.transportControls!!.play()
                        }

                    }
                }


            }
            scheduleSeekbarUpdate()
        }

    }


    override fun onStop() {
        super.onStop()
        mMediaBrowser.disconnect()
        mExecutorService.shutdown()

    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            //update the UI according to the new metadata
            Log.d(TAG, "Metadata changed" + metadata!!.keySet().size)


        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            updatePlaybackState(state)

            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    Log.d(TAG, "changed to playing")
                    playback_pause_button.setImageResource(R.drawable.ic_pause_button)
                    scheduleSeekbarUpdate()


                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    Log.d(TAG, "changed to paused")

                    playback_pause_button.setImageResource(R.drawable.ic_play_button)
//                    playback_imageview_bg_slider.clearAnimation()
                    stopSeekbarUpdate()
                    percentage = (mLastPlaybackState!!.position!!.toDouble() / song.duration.toDouble())
                    playback_imageview_bg_slider.rotate((percentage * 360).toFloat(), 10)


                }

            }


        }


    }

    private fun updatePlaybackState(stateCompat: PlaybackStateCompat?) {
        if (stateCompat != null)
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
        playback_playtime.text = Utils.convertToTime(currentPosition) + " / " + Utils.convertToTime(song.duration)
        percentage = (currentPosition.toDouble() / song.duration.toDouble())

        if (!isUILocked) {
            //Rotate the dial depending on the progres of the song.
            playback_imageview_bg_slider.rotate((percentage * 360).toFloat(),PROGRESS_UPDATE_INTERNAL )
        }


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

