package ca.makakolabs.makakomusic.fragments

import android.content.ComponentName

import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.makakolabs.makakomusic.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import androidx.recyclerview.widget.GridLayoutManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.MediaBrowserCompat
import ca.makakolabs.makakomusic.model.Song
import ca.makakolabs.makakomusic.services.MakakoPlaybackService
import ca.makakolabs.makakomusic.viewholders.SongItem


class SongsFragment : MediaBrowserFragment() {

    companion object {
        val TAG = "Songs"
    }

    private val TAG = "MediaBrowserFragment"
    private val adapter = GroupAdapter<ViewHolder>()

    private lateinit var mediaBrowser: MediaBrowserCompat


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        var viewCL = inflater.inflate(ca.makakolabs.makakomusic.R.layout.songs_fragment_layout, container, false)
        val recycler = (viewCL as ConstraintLayout).findViewById<RecyclerView>(R.id.songs_fragment_recycler_view)


        recycler.layoutManager = GridLayoutManager(context, 2)


        recycler.adapter = adapter

        mediaBrowser = MediaBrowserCompat(
            context, ComponentName(context, MakakoPlaybackService::class.java),
            connectionCallbacks,
            null
        )




        return viewCL

    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()


    }



    fun onMediaItemSelected(item: MediaBrowserCompat.MediaItem) {
        Log.d(TAG, "onMediaItemSelected, mediaId=" + item.mediaId!!)
        if (item.isPlayable) {
            MediaControllerCompat.getMediaController(activity!!.parent).transportControls
                .playFromMediaId(item.mediaId, null)

        } else {
            Log.d(
                TAG, "Ignoring MediaItem that is neither browsable nor playable: mediaId=" + item.mediaId
            )
        }
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            var root = MakakoPlaybackService.SONGS_MEDIA_ROOT_ID
            mediaBrowser.subscribe(root,
                object : MediaBrowserCompat.SubscriptionCallback() {
                    override fun onChildrenLoaded(
                        parentId: String,
                        songs: List<MediaBrowserCompat.MediaItem>
                    ) {
                        if (songs == null || songs.isEmpty()) {
                            return
                        }
                        for (child in songs) {
                            Log.d(TAG, "" + child.description.title)
                        }

                        for (song in songs){
                            adapter.add(SongItem(song as Song))
                        }
                        // Play the first item?
                        // Probably should check firstItem.isPlayable()
//                        MediaControllerCompat.getMediaController(activity!!.parent)
//                            .transportControls
//                            .playFromMediaId(firstItem.mediaId, null)
                    }
                })

            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    activity, // Context
                    token
                )

                // Save the controller
                //MediaControllerCompat.setMediaController(context, mediaController)

            }
        }


    }


}



