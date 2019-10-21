package ca.makakolabs.makakomusic.ui.activities

import android.content.ComponentName
import android.content.Intent
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
import ca.makakolabs.makakomusic.MakakoPlaybackService
import ca.makakolabs.makakomusic.utils.Utils
import kotlinx.android.synthetic.main.playback_activity.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
    private var percentage = 0.0
    private var pivX = 0
    private var pivY = 0
    private var tempBitmap: Bitmap? = null
    var density = 1f
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




    }


    override fun onStart() {
        super.onStart()

        if (!mMediaBrowser.isConnected) {
            mMediaBrowser.connect()
            Thread(Runnable {

                mHandler.post {


                    //load the albumart bitmap to calculate the pivot points for the slider.

                    tempBitmap = Utils.getBitmapFromVectorDrawableWithId(this, R.drawable.ic_albumart_bg)
                    pivX = tempBitmap!!.width / 2
                    pivY = tempBitmap!!.height / 2
                    tempBitmap!!.recycle()

                    var metrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(metrics)

                    density = metrics.density

                    playback_progress_bar.setDensity(density)
                    playback_imageview_album.density = density
                    //TODO: make sure it works for all screen sizes
                    playback_progress_bar.setWidthHeight(294 * density, 294 * density)


                }
            }).start()
        }

    }

    private fun updateUI(metadataCompat: MediaMetadataCompat) {

        //Load the song info into the different UI elements
        playback_title.text = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        playback_album.text = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
        playback_artist.text = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

        if (playback_title.lineCount == 1) {
            playback_title.text = "\n${playback_title.text}"
        }

        playback_playtime.text =
            "00:00  /  ${Utils.convertToTime(metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))}"

        Log.d(TAG, metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))




            //In case we want to load the album art blurred in the background
            loadArtImages(metadataCompat)
            //TODO("Leave and option to show blurred background from settings, paid option?")

        playback_imageview_bg_slider.pivx = pivX
        playback_imageview_bg_slider.pivy = pivY

        playback_imageview_bg_slider.duration = metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)


    }

    private fun loadArtImages(metadataCompat: MediaMetadataCompat) {
        mHandler = Handler()
        Thread(Runnable {

            mHandler.post {
                var options = RequestOptions()
                options.override(400,400)
                var myBitmap: Bitmap
                try {

                    Glide.with(applicationContext)
                        .load(Uri.parse(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI)))
                        .placeholder(R.drawable.ic_empty_album)
                        .circleCrop()
                        .into(playback_imageview_album)

                    Log.d(TAG,metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
                    myBitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        Uri.parse(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
                    )
//                    playback_imageview_album.source = myBitmap
//                    playback_imageview_album.background =BitmapDrawable(resources,myBitmap)
                    Log.d(TAG, metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
                    val blurredBackground = Utils.blurImage(this@PlaybackActivity, myBitmap, 250, 250)
                    playback_constraint_layout.background = BitmapDrawable(resources, blurredBackground)
                } catch (e: FileNotFoundException) {
                    playback_constraint_layout.setBackgroundResource(R.drawable.black_gradient)
                }


            }
        }).start()

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
                            mediaController.transportControls.pause()
                        }
                        PlaybackStateCompat.STATE_PAUSED -> {
                            Log.d(TAG, "Is paused, going to play now")
                            mediaController.transportControls.play()
                        }

                    }
                }

                playback_next_button.setOnClickListener {
                    Log.d(TAG, "Skipping to next song")
                    mediaController.transportControls.skipToNext()
                }

                playback_back_button.setOnClickListener {
                    Log.d(TAG, "Skipping to previous song")
                    mediaController.transportControls.skipToPrevious()
                }
            }
            scheduleSeekbarUpdate()
            mediaController = MediaControllerCompat.getMediaController(this@PlaybackActivity)
        }
    }


    override fun onDestroy() {

        mMediaBrowser.disconnect()
        stopSeekbarUpdate()
        mExecutorService.shutdown()
        Log.d(TAG, "called on destroy")
        super.onDestroy()


    }

    override fun onStop() {

        Log.d(TAG, "called on stop")
        stopSeekbarUpdate()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        scheduleSeekbarUpdate()
    }

    override fun onRestart() {
        super.onRestart()
        scheduleSeekbarUpdate()
    }


    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (metadata == null)
                return
            updateUI(metadata)


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

                    //Since we will stop the real time update, we must set the rotation manually so the progress
                    //will still display on the dial and progress bar.
//                    percentage = (mLastPlaybackState!!.position!!.toDouble() / mediaController?.metadata!!.getLong(
//                        MediaMetadataCompat.METADATA_KEY_DURATION
//                    ).toDouble())
//                    playback_imageview_bg_slider.rotate((percentage * 360).toFloat(), 10)
                    stopSeekbarUpdate()


                }
                PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {
                    Log.d(TAG, "Skipping to next song")
                    scheduleSeekbarUpdate()


                }
                PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {
                    Log.d(TAG, "Skipping to previous song")
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

        if(!mScheduleFuture!!.isCancelled) {



            var currentPosition = mLastPlaybackState!!.position
            if (mLastPlaybackState!!.state == PlaybackStateCompat.STATE_PLAYING) {
                // Calculate the elapsed time between the last position update and now and unless
                // paused, we can assume (delta * speed) + current position is approximately the
                // latest position. This ensure that we do not repeatedly call the getPlaybackState()
                // on MediaControllerCompat.
                val timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState!!.lastPositionUpdateTime
                currentPosition += (timeDelta.toInt() * mLastPlaybackState!!.playbackSpeed).toLong()
            }
            //if we reach 100% of the song, we should prevent the slider of rotating indefinitely





            //calculate the percentage played of the song
            percentage =
                (currentPosition.toDouble() / mediaController!!.metadata!!.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toDouble())

            if (percentage >= 1) {
                stopSeekbarUpdate()
                return
            }

            //display  elapsed time / total time
            playback_playtime.text = "${Utils.convertToTime(currentPosition)}  /  ${Utils.convertToTime(
                mediaController!!.metadata!!.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            )}"

            if (!isUILocked) {
                //Rotate the dial depending on the progress of the song.
                playback_imageview_bg_slider.rotate((percentage * 360).toFloat(), PROGRESS_UPDATE_INTERNAL)

                //Draw the progress bar to match the dial
                playback_progress_bar.setPosEnd((percentage * 360).toFloat())


            }




        }else{
            return
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
            mScheduleFuture!!.cancel(true)
        }
    }

//    override fun onBackPressed() {
//        val intent  = Intent(this,MainActivity::class.java)
//        startActivity(intent)
//    }

}

