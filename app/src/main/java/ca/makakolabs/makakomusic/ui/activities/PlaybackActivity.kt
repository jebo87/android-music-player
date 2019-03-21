package ca.makakolabs.makakomusic.ui.activities

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import android.view.Window
import android.view.WindowManager
import java.io.FileNotFoundException


class PlaybackActivity : AppCompatActivity() {


    private val PROGRESS_UPDATE_INTERNAL: Long = 250
    private val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 10
    private var mediaController: MediaControllerCompat? =null
    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mLastPlaybackState: PlaybackStateCompat? = null
    private var mHandler = Handler()
    private lateinit var mMediaBrowser: MediaBrowserCompat
    private var percentage = 0.0
    private var pivX = 0
    private var pivY = 0
    private var tempBitmap: Bitmap? = null
    private var songs = mutableListOf<Song>()


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

                    //load the albumart bitmap to calculate the pivot points for the slider.

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

    private fun updateUI(metadataCompat: MediaMetadataCompat) {

        //Load the song info into the different UI elements
        playback_title.text = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        playback_album.text = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
        playback_artist.text = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

        playback_playtime.text = Utils.convertToTime(metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))
        Picasso.get().load(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
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
                    myBitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        Uri.parse(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
                    )
                    val blurredBackground = Utils.blurImage(this@PlaybackActivity, myBitmap, 250, 250)
                    playback_constraint_layout.background = BitmapDrawable(resources, blurredBackground)
//

                } catch (e: FileNotFoundException) {
                    playback_constraint_layout.setBackgroundResource(R.drawable.black_gradient)
                }


            }
        }).start()




        playback_imageview_bg_slider.pivx = pivX
        playback_imageview_bg_slider.pivy = pivY

        playback_imageview_bg_slider.duration = metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)


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
               val  mediaController = MediaControllerCompat(
                    this@PlaybackActivity, // Context
                    token
                )


                // Save the controller
                MediaControllerCompat.setMediaController(this@PlaybackActivity, mediaController)

                // Display the initial state
                val metadata = mediaController.metadata

                val pbState = mediaController.playbackState


                // Register a Callback to stay in sync
                mediaController.registerCallback(controllerCallback)
                updatePlaybackState(pbState)
                updateUI(metadata!!)
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

                playback_next_button.setOnClickListener {
                    Log.d(TAG, "Skipping to next song")
                    mediaController!!.transportControls!!.skipToNext()
                }


            }
            scheduleSeekbarUpdate()
            mediaController =  MediaControllerCompat.getMediaController(this@PlaybackActivity)
        }

    }


    override fun onStop() {
        super.onStop()
        mMediaBrowser.disconnect()
        mExecutorService.shutdown()

    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (metadata == null)
                return
            updateUI(metadata!!)


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
                    Log.d(TAG, "changed to paused ${state.position}")

                    playback_pause_button.setImageResource(R.drawable.ic_play_button)
//                    playback_imageview_bg_slider.clearAnimation()

                    percentage = (mLastPlaybackState!!.position!!.toDouble() / mediaController?.metadata!!.getLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION
                    ).toDouble())
                    playback_imageview_bg_slider.rotate((percentage * 360).toFloat(), 10)
                    stopSeekbarUpdate()


                }
                PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {
                    Log.d(TAG, "Skipping to next song")
                    scheduleSeekbarUpdate()


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
            Log.d(TAG, "lastPlaybackState is null")
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

        Log.d(TAG,"updating progress $currentPosition")
        //display  elapsed time / total time
        playback_playtime.text = "${Utils.convertToTime(currentPosition)}  /  ${Utils.convertToTime(
            mediaController!!.metadata!!.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        )}"

        //calculate the percentage played of the song
        percentage =
            (currentPosition.toDouble() / mediaController!!.metadata!!.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toDouble())

        if (!isUILocked) {
            //Rotate the dial depending on the progress of the song.
            playback_imageview_bg_slider.rotate((percentage * 360).toFloat(), PROGRESS_UPDATE_INTERNAL)
        }

        if (percentage >= 1)
            stopSeekbarUpdate()


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

