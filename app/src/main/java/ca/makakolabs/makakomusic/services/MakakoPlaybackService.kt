package ca.makakolabs.makakomusic.services

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.data.repositories.AlbumRepository
import ca.makakolabs.makakomusic.data.repositories.MusicRepository
import ca.makakolabs.makakomusic.data.repositories.SongRepository




class MakakoPlaybackService : MediaBrowserServiceCompat() {

    companion object {
         const val MY_MEDIA_ROOT_ID = "media_root_id"
         const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
         const val SONGS_MEDIA_ROOT_ID = "songs_root_id"
        const val ALBUMS_MEDIA_ROOT_ID = "albums_root_id"

    }

    private var mediaSession : MediaSessionCompat? = null
    private lateinit var repository:MusicRepository
    private lateinit var stateBuilder: PlaybackStateCompat.Builder


    override fun onCreate() {
        super.onCreate()


        //Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MakakoPlaybackService").apply{

            // Enable callbacks from MediaButtons and TransportControls

            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(MySessionCallback())

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)



        }
    }

    class MySessionCallback : MediaSessionCompat.Callback(){

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
        when(parentMediaId) {
            SONGS_MEDIA_ROOT_ID -> {
                repository = SongRepository(application)

                //Get the songs from the repository and load them into the array



                result.sendResult(repository.getMediaFromCursor())

//                for (item in items) {
//                    mediaItems.add(item as Song)
//                }


            }
            ALBUMS_MEDIA_ROOT_ID ->{
                //Get the songs from the repository and load them into the array
                repository = AlbumRepository(application)


                var metaDataItems = repository.getMediaFromCursor()
                mediaItems.clear()
                for (item in metaDataItems) {
                    mediaItems.add(item)
                }
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