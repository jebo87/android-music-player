package ca.makakolabs.makakomusic.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.data.repositories.AlbumRepository
import ca.makakolabs.makakomusic.data.repositories.MusicRepository
import ca.makakolabs.makakomusic.data.repositories.SongRepository
import ca.makakolabs.makakomusic.playback.AndroidPlayback


class MakakoPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        const val MY_MEDIA_ROOT_ID = "media_root_id"
        const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        const val SONGS_MEDIA_ROOT_ID = "songs_root_id"
        const val ALBUMS_MEDIA_ROOT_ID = "albums_root_id"
        const val TAG = "MakakoPlaybackService"

    }

    private lateinit var repository: MusicRepository
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var controller: MediaControllerCompat

    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
    //private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    //private lateinit var myPlayerNotification: MediaStyleNotification
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: AndroidPlayback
    private lateinit var audioFocusRequest: AudioFocusRequest


    override fun onCreate() {
        super.onCreate()


        //Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MakakoPlaybackService").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(mediaSessionCallback)

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }

        controller = mediaSession.controller

        player = AndroidPlayback(this)
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onStop() {
            super.onStop()
            player.stop()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                }.build()
            )
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d(TAG, "Seeking to $pos ")
            player.seekTo(pos)
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

            player.pause()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        mediaSession.controller.playbackState.position,
                        1f
                    )
                }.build()
            )

        }

        override fun onPlay() {
//            super.onPlay()
            Log.d(TAG, "Entered onPlay")
            player.play()
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().apply {

                    setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        mediaSession.controller.playbackState.position,
                        1f
                    )
                }.build()
            )
        }

        override fun onPlayFromMediaId(mediaId: String?, bundle: Bundle?) {
            Log.d(TAG, "OnPlayMediaId MediaSession" + mediaId)
            val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Request audio focus for playback, this registers the afChangeListener

            //TODO: implement a new way that is not deprecated
            @Suppress("DEPRECATION")
            var result = am.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                //Start the service
                startService(Intent(applicationContext, MakakoPlaybackService::class.java))
                // Set the session active  (and update metadata and state)
                mediaSession.isActive = true

                //set the song metadata and the additional info (artist, duration, etc.)
                val songToPlay = bundle!!.getParcelable("com.makakolabs.makakomusic.song") as Song
                mediaSession.setMetadata(songToPlay.toMetaData())
                mediaSession.setExtras(Bundle().apply {
                    classLoader = Song::class.java!!.classLoader
                    putParcelable("com.makakolabs.makakomusic.song", songToPlay)
                })

                // start the player (custom call)
                //var test = mediaSession.controller.queue[0].description.title


                startService(Intent(applicationContext, MakakoPlaybackService::class.java))
                player.playFromId(mediaId!!)
                // Register BECOME_NOISY BroadcastReceiver
                //registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                // Put the service in the foreground, post notification
                //service.startForeground(id, myPlayerNotification)


                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder().apply {

                        setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                    }.build()
                )




                showNotification()


            }


        }


    }

    fun showNotification() {

        val mediaMetadata = controller.metadata
        val description = mediaMetadata.description

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("MakakoPlaybackService", "Makako Playback")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val builder = NotificationCompat.Builder(applicationContext, channelId).apply {
            // Add the metadata for the currently playing track
            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)
            setLargeIcon(description.iconBitmap)

            // Enable launching the player by clicking the notification
            setContentIntent(controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    applicationContext,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(R.drawable.ic_albumart_bg)
            color = ContextCompat.getColor(applicationContext, R.color.primary_dark_material_dark)

            // Add a pause button
            addAction(
                androidx.core.app.NotificationCompat.Action(
                    R.drawable.exo_icon_pause,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }

// Display the notification and place the service in the foreground
        startForeground(1, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {


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

        var mediaItems = mutableListOf<MediaItem>()
        // Check root menu and return the items depending on the parentMediaId requested:
        when (parentMediaId) {
            SONGS_MEDIA_ROOT_ID -> {
                repository = SongRepository(application)

                //Get the songs from the repository and send them back to the subscriber
                result.sendResult(repository.getMediaFromCursor())

            }
            ALBUMS_MEDIA_ROOT_ID -> {
                repository = AlbumRepository(application)

                //Get the albums from the repository and send them back to the subscriber
                result.sendResult(repository.getMediaFromCursor())
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


}