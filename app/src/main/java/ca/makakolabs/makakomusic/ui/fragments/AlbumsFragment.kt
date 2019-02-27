package ca.makakolabs.makakomusic.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Album
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.services.MakakoPlaybackService
import ca.makakolabs.makakomusic.ui.activities.MediaBrowserProvider
import ca.makakolabs.makakomusic.ui.viewholders.AlbumItem
import ca.makakolabs.makakomusic.ui.viewholders.SongItem
import ca.makakolabs.makakomusic.ui.viewmodels.AlbumViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class AlbumsFragment : MediaBrowserFragment() {

    companion object {
        val TAG = "Albums"
    }

    private val mAdapter = GroupAdapter<ViewHolder>()

    private lateinit var albumViewModel: AlbumViewModel

    private lateinit var mediaBrowser: MediaBrowserCompat

    lateinit  var myActivity: MediaBrowserProvider



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //initialize view model
        albumViewModel = ViewModelProviders.of(this).get(AlbumViewModel::class.java)

//        mediaBrowser = MediaBrowserCompat(
//            context, ComponentName(context, MakakoPlaybackService::class.java),
//            connectionCallbacks,
//            null
//        )


        var constraintLayout = inflater.inflate(R.layout.albums_fragment_layout,container,false)
        var recycler = (constraintLayout as ConstraintLayout).findViewById<RecyclerView>(R.id.albums_fragment_recycler)

        recycler.apply{

            layoutManager = GridLayoutManager(context,2)
            adapter = mAdapter

        }





        return constraintLayout

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = (context as MediaBrowserProvider)
    }

    override fun onStart() {
        super.onStart()
        if(myActivity.getMediaBrowserCompat().isConnected)
            onConnected()


    }

    fun onConnected() {
        if (isDetached) {
            return
        }

        var root = MakakoPlaybackService.ALBUMS_MEDIA_ROOT_ID
        myActivity.getMediaBrowserCompat().unsubscribe(root)
        myActivity.getMediaBrowserCompat().subscribe(root,subscriptionCallback)
    }

    private var subscriptionCallback = object: MediaBrowserCompat.SubscriptionCallback(){
        override fun onChildrenLoaded(
            parentId: String,
            albums: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            if (albums == null || albums.isEmpty()) {
                return
            }

            //replace the contents of the adapter with the result sent from the MediaBrowserService
            mAdapter.clear()
            for (album in albums) {
                mAdapter.add(AlbumItem(album as Album))
            }
            mAdapter.notifyDataSetChanged()



        }

    }


//    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
//        override fun onConnected() {
//
//            var root = MakakoPlaybackService.ALBUMS_MEDIA_ROOT_ID
//            mediaBrowser.subscribe(root,
//                object : MediaBrowserCompat.SubscriptionCallback() {
//                    override fun onChildrenLoaded(
//                        parentId: String,
//                        albums: List<MediaBrowserCompat.MediaItem>
//                    ) {
//                        if (albums == null || albums.isEmpty()) {
//                            return
//                        }
//
//
//                        mAdapter.clear()
//
//
//                        for (album in albums) {
//                            mAdapter.add(AlbumItem(album as Album))
//                        }
//
//                        mAdapter.notifyDataSetChanged()
//                        // Play the first item?
//                        // Probably should check firstItem.isPlayable()
////                        MediaControllerCompat.getMediaController(activity!!.parent)
////                            .transportControls
////                            .playFromMediaId(firstItem.mediaId, null)
//                    }
//                })
//
//            // Get the token for the MediaSession
//            mediaBrowser.sessionToken.also { token ->
//
//                // Create a MediaControllerCompat
//                val mediaController = MediaControllerCompat(
//                    activity, // Context
//                    token
//                )
//
//                // Save the controller
//                //MediaControllerCompat.setMediaController(context, mediaController)
//
//            }
//        }
//
//
//    }

}