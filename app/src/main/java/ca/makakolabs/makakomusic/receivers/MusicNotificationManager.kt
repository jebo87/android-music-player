package ca.makakolabs.makakomusic.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import ca.makakolabs.makakomusic.MakakoPlaybackService
import ca.makakolabs.makakomusic.R

class MusicNotificationManager (mService : MakakoPlaybackService): BroadcastReceiver(){
    companion object {
        val TAG="MusicNotificationMgr"
    }
    val service :MakakoPlaybackService
    val ACTION_PAUSE = "com.makakolabs.makakomusic.pause"
    val ACTION_PLAY = "com.makakolabs.makakomusic.play"
    val ACTION_PREV = "com.makakolabs.makakomusic.prev"
    val ACTION_NEXT = "com.makakolabs.makakomusic.next"
    val ACTION_STOP = "com.makakolabs.makakomusic.stop"
    val ACTION_STOP_CASTING = "com.makakolabs.makakomusic.stop_cast"
    private val REQUEST_CODE = 1987

    init {
        service = mService
    }
    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action

        when (action){
            ACTION_PAUSE ->{
                Log.d(TAG,"Pause from notification received")


            }
        }
    }

    fun showNotification() {

        val mediaMetadata = service.controller.metadata
        val description = mediaMetadata.description
        val pkg = service.packageName

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("MakakoPlaybackService", "Makako Playback")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val builder = NotificationCompat.Builder(service.applicationContext, channelId).apply {
            // Add the metadata for the currently playing track
            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)
            setLargeIcon(description.iconBitmap)

            // Enable launching the androidPlayback by clicking the notification
            setContentIntent(service.controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    service.applicationContext,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            //set the media session

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(R.drawable.ic_albumart_bg)
            color = ContextCompat.getColor(service.applicationContext, R.color.primary_dark_material_dark)

            // Add a pause button

            val pauseIntent = PendingIntent.getBroadcast(service,REQUEST_CODE,Intent(ACTION_PAUSE).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT)

            addAction(
                androidx.core.app.NotificationCompat.Action(
                    R.drawable.exo_icon_pause,
                    "Pause",
                    //pauseIntent
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service.applicationContext,
                    PlaybackStateCompat.ACTION_PAUSE
                )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(service.mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service.applicationContext,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }

// Display the notification and place the service in the foreground
        service.startForeground(1, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
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