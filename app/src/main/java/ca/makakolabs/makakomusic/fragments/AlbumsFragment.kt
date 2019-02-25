package ca.makakolabs.makakomusic.fragments

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.model.Album
import ca.makakolabs.makakomusic.services.MakakoPlaybackService
import ca.makakolabs.makakomusic.viewholders.AlbumItem
import ca.makakolabs.makakomusic.viewmodels.AlbumViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class AlbumsFragment : MediaBrowserFragment() {

    companion object {
        val TAG = "Albums"
    }

    private val adapter = GroupAdapter<ViewHolder>()

    private lateinit var albumViewModel: AlbumViewModel

    private lateinit var mediaBrowser: MediaBrowserCompat


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //initialize view model
        albumViewModel = ViewModelProviders.of(this).get(AlbumViewModel::class.java)

        mediaBrowser = MediaBrowserCompat(
            context, ComponentName(context, MakakoPlaybackService::class.java),
            connectionCallbacks,
            null
        )

        mediaBrowser = MediaBrowserCompat(
            context, ComponentName(context, MakakoPlaybackService::class.java),
            connectionCallbacks,
            null
        )

        var constraintLayout = inflater.inflate(R.layout.albums_fragment_layout,container,false)
        var recycler = (constraintLayout as ConstraintLayout).findViewById<RecyclerView>(R.id.albums_fragment_recycler)

        recycler.layoutManager = GridLayoutManager(context,2)

        recycler.adapter = adapter



        return constraintLayout

    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()


    }


    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            var root = MakakoPlaybackService.ALBUMS_MEDIA_ROOT_ID
            mediaBrowser.subscribe(root,
                object : MediaBrowserCompat.SubscriptionCallback() {
                    override fun onChildrenLoaded(
                        parentId: String,
                        albums: List<MediaBrowserCompat.MediaItem>
                    ) {
                        if (albums == null || albums.isEmpty()) {
                            return
                        }
                        for (child in albums) {
                            Log.d(TAG, "" + child.description.title)
                        }

                        for (album in albums) {
                            adapter.add(AlbumItem(album as Album))
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