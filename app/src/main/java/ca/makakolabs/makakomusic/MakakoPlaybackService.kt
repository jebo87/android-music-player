package ca.makakolabs.makakomusic


import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.data.repositories.AlbumRepository
import ca.makakolabs.makakomusic.data.repositories.SongRepository
import ca.makakolabs.makakomusic.playback.AndroidPlayback
import ca.makakolabs.makakomusic.receivers.MusicNotificationManager
import ca.makakolabs.makakomusic.ui.activities.MainActivity
import ca.makakolabs.makakomusic.ui.activities.PlaybackActivity
import ca.makakolabs.makakomusic.utils.Utils
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import java.io.FileNotFoundException


class MakakoPlaybackService : MediaBrowserServiceCompat(), Player.EventListener {

    companion object {
        const val MY_MEDIA_ROOT_ID = "media_root_id"
        const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        const val SONGS_MEDIA_ROOT_ID = "songs_root_id"
        const val ALBUMS_MEDIA_ROOT_ID = "albums_root_id"
        const val TAG = "MakakoPlaybackService"

    }

    private lateinit var songRepository: SongRepository
    private lateinit var albumRepository: AlbumRepository
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    lateinit var controller: MediaControllerCompat

    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
    //private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    //private lateinit var myPlayerNotification: MediaStyleNotification
    lateinit var mediaSession: MediaSessionCompat
    private lateinit var androidPlayback: AndroidPlayback
    private lateinit var audioFocusRequest: AudioFocusRequest

    private lateinit var musicNotificationManager: MusicNotificationManager

    private lateinit var channelId: String


    override fun onCreate() {
        super.onCreate()

        initMediaSession()

        controller = mediaSession.controller

        androidPlayback = AndroidPlayback(this)
        val player = androidPlayback.getPlayer()
        player.addListener(this)

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        mediaSession.setMediaButtonReceiver(pendingIntent)
        channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(this, "MakakoPlaybackService", "Makako Playback")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }


//        musicNotificationManager = MusicNotificationManager(this)


    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        Log.d(TAG, "onSTART!! ${mediaSession.isActive}")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(this, MediaButtonReceiver::class.java)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingMediaButtonIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);


        //Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(application, "MakakoPlaybackService", mediaButtonReceiver, null).apply {

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(mediaSessionCallback)

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
            )

            //isActive=true

            //pre lolipop code needed for the media buttons
            setMediaButtonReceiver(pendingMediaButtonIntent)

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the androidPlayback
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )


            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)


            setSessionActivity(pendingIntent)


        }

    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        //Code needed to handle the media button and notification actions
        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            val event = mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

            when (event?.keyCode) {
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    controller.transportControls.skipToNext()
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    controller.transportControls.skipToPrevious()
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    if (androidPlayback.isPlaying)
                        controller.transportControls.pause()
                    else
                        controller.transportControls.play()
                }
            }


            return true
        }

        override fun onStop() {
            super.onStop()
            androidPlayback.stop()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                }.build()
            )
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d(TAG, "Seeking to $pos ")
            androidPlayback.seekTo(pos)
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(
                        mediaSession.controller.playbackState.state,
                        pos,
                        1f
                    )
                }.build()
            )

        }


        override fun onPause() {
            super.onPause()
            Log.d(TAG, "Pausing in position${mediaSession.controller.playbackState.position}")

            androidPlayback.pause()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        mediaSession.controller.playbackState.position,
                        1f
                    )
                }.build()
            )
            showPausedNotification()
        }

        override fun onPlay() {
//            super.onPlay()
            Log.d(TAG, "Entered onPlay")
            androidPlayback.play()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        mediaSession.controller.playbackState.position,
                        1f
                    )
                }.build()
            )

            showPlayingNotification()
        }

        override fun onSkipToPrevious() {
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {
                    setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                        0,
                        1f
                    )
                }.build()
            )

            //play previous song
            var previousSongId = androidPlayback.skipToPrevious()

            //Load new song info to make it available for the media session
            var previousSong = songRepository.getSongFromId(previousSongId)
            mediaSession.setMetadata(previousSong!!.toMetaData())
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        mediaSession.controller.playbackState.position,
                        1f
                    )
                }.build()
            )


            showPlayingNotification()

        }

        override fun onSkipToNext() {

            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {
                    setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                        0,
                        1f
                    )
                }.build()
            )

            //Play next song
            var nextSongId = androidPlayback.skipToNext()

            //Load new song information and make it available for the media session metadata
            var nextSong = songRepository.getSongFromId(nextSongId)
            mediaSession.setMetadata(nextSong!!.toMetaData())
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {
                    setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        mediaSession.controller.playbackState.position,
                        1f
                    )
                }.build()
            )
            showPlayingNotification()
        }


        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            androidPlayback.addQueueItem(description!!)
        }

        override fun onPlayFromMediaId(mediaId: String?, bundle: Bundle?) {
            Log.d(TAG, "OnPlayMediaId MediaSession" + mediaId)

            val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Request audio focus for playback, this registers the afChangeListener

            //TODO: implement a new way that is not deprecated for the audio focus
            @Suppress("DEPRECATION")
            var result = am.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaSession.isActive = true

                //Start the service
                startService(Intent(applicationContext, MakakoPlaybackService::class.java))
                // Set the session active  (and update metadata and state)

                //set the song metadata and the additional info (artist, duration, etc.)
                val songToPlay = bundle!!.getParcelable("com.makakolabs.makakomusic.song") as Song
                val queue = bundle!!.getParcelableArrayList<Song>("com.makakolabs.makakomusic.songList")
                mediaSession.setMetadata(songToPlay.toMetaData())

                startService(Intent(applicationContext, MakakoPlaybackService::class.java))
                androidPlayback.playFromId(mediaId!!)
                // Register BECOME_NOISY BroadcastReceiver
                //registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                // Put the service in the foreground, post notification
                //service.startForeground(id, myPlayerNotification)


                //update the state
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder().apply {

                        setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1f)
                    }.build()
                )

                //show the notification
                showNotification()
                showPlayingNotification()
            }
        }
    }


    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<List<MediaItem>>
    ) {
        //  Browsing not allowed
        if (MY_EMPTY_MEDIA_ROOT_ID == parentMediaId) {
            result.sendResult(mutableListOf())
            return
        }

        // Assume for example that the music catalog is already loaded/cached.

        // Check root menu and return the items depending on the parentMediaId requested:
        when (parentMediaId) {
            SONGS_MEDIA_ROOT_ID -> {
                songRepository = SongRepository(application)

                //Get the songs from the repository and send them back to the subscriber
                result.sendResult(songRepository.getMediaFromCursor())

            }
            ALBUMS_MEDIA_ROOT_ID -> {
                albumRepository = AlbumRepository(application)

                //Get the albums from the repository and send them back to the subscriber
                result.sendResult(albumRepository.getMediaFromCursor())
            }
        }


    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): MediaBrowserServiceCompat.BrowserRoot {

        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.


        /*

        return if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            MediaBrowserServiceCompat.BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }

        */

        return MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {

        //        when (it) {
//            AudioManager.AUDIOFOCUS_LOSS -> {
//                if (androidPlayback.isPlaying()) {
//                    androidPlayback.stop()
//                }
//            }
//            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                androidPlayback.pause()
//            }
//            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
//                if (androidPlayback != null) {
////                    androidPlayback.setVolume(0.3f, 0.3f)
//                }
//            }
//            AudioManager.AUDIOFOCUS_GAIN -> {
//                if (androidPlayback != null) {
//                    if (!androidPlayback.isPlaying()) {
////                        androidPlayback.start()
//                    }
////                    mMediaPlayer.setVolume(1.0f, 1.0f)
//                }
//            }
//        }

    }

    fun showNotification() {

        val mediaMetadata = controller.metadata
        val description = mediaMetadata.description
        val pkg = packageName


        val builder = createBuilder()

        // Display the notification and place the service in the foreground
        startForeground(1, builder.build())



    }

    //Function to show the actions for the notification when a song is playing
    private fun showPlayingNotification() {
        val builder = createBuilder()

        builder.apply {
            addAction(
                androidx.core.app.NotificationCompat.Action(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

            addAction(
                androidx.core.app.NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
            addAction(
                androidx.core.app.NotificationCompat.Action(
                    android.R.drawable.ic_media_next,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2).setMediaSession(
                    mediaSession.sessionToken
                )
            )
        }


        val mNotificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, builder.build())
    }


    //function to show the notification when a song is paused.
    private fun showPausedNotification() {
        val builder = createBuilder()

        builder.apply {
            addAction(
                androidx.core.app.NotificationCompat.Action(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

            addAction(
                androidx.core.app.NotificationCompat.Action(
                    android.R.drawable.ic_media_play,
                    "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
            addAction(
                androidx.core.app.NotificationCompat.Action(
                    android.R.drawable.ic_media_next,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2).setMediaSession(
                    mediaSession.sessionToken
                )
            )
        }


        val mNotificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, builder.build())
    }

    private fun createBuilder(): NotificationCompat.Builder {
//        val color = ContextCompat.getColor(context, R.color.primary_dark_material_dark)
        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata
        val description = mediaMetadata.description


        val builder = NotificationCompat.Builder(this, channelId)


        var myBitmap: Bitmap?
        try {


            myBitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                description.iconUri
            )
        } catch (e: FileNotFoundException) {
            myBitmap = Utils.getBitmapFromVectorDrawableWithId(this, R.drawable.ic_albumart_bg)

        }


        builder

            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setSubText(description.description)
            .setLargeIcon(myBitmap)
            .setContentIntent(controller.sessionActivity)
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext, PlaybackStateCompat.ACTION_STOP)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            .setSmallIcon(R.drawable.ic_albumart_bg)
// Take advantage of MediaStyle features
//                .setStyle(
//                    androidx.media.app.NotificationCompat.MediaStyle()
//                        .setMediaSession(mediaSession.sessionToken)
//                        .setShowActionsInCompactView(0)
//
//                    // Add a cancel button
////                    .setShowCancelButton(true)
////                    .setCancelButtonIntent(
////                        MediaButtonReceiver.buildMediaButtonPendingIntent(
////                            applicationContext,
////                            PlaybackStateCompat.ACTION_STOP
////                        )
////                    )
//                )
        return builder
    }


    //Handles de events coming fronm exoplayer
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == Player.STATE_ENDED) {
            mediaSessionCallback.onSkipToNext()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(service: Service, channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


}