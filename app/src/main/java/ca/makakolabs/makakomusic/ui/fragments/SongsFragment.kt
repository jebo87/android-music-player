package ca.makakolabs.makakomusic.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.services.MakakoPlaybackService
import ca.makakolabs.makakomusic.ui.activities.MediaBrowserProvider
import ca.makakolabs.makakomusic.ui.viewholders.SongItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class SongsFragment : MediaBrowserFragment() {

    companion object {
        val TAG = "SongsFragment"
    }
    var mAdapter= GroupAdapter<ViewHolder>()

    lateinit var recycler: RecyclerView
    lateinit var viewCL: View
    lateinit  var myActivity: MediaBrowserProvider


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewCL = inflater.inflate(ca.makakolabs.makakomusic.R.layout.songs_fragment_layout, container, false)
        recycler = (viewCL as ConstraintLayout).findViewById(R.id.songs_fragment_recycler_view)

        recycler.apply {
            layoutManager = GridLayoutManager(activity, 2)
        }

        recycler.adapter= mAdapter
        return viewCL
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)

        myActivity = (context as MediaBrowserProvider)


    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Mediabroswer is connected="+myActivity.getMediaBrowserCompat().isConnected)
        if(myActivity.getMediaBrowserCompat().isConnected)
            onConnected()

    }

    fun onConnected() {
        if (isDetached) {
            return
        }

        var root = MakakoPlaybackService.SONGS_MEDIA_ROOT_ID
        myActivity.getMediaBrowserCompat().unsubscribe(root)
        myActivity.getMediaBrowserCompat().subscribe(root,subscriptionCallback)
    }

    private var subscriptionCallback = object: MediaBrowserCompat.SubscriptionCallback(){
        override fun onChildrenLoaded(
            parentId: String,
            songs: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            if (songs == null || songs.isEmpty()) {
                return
            }

            //replace the contents of the adapter with the result sent from the MediaBrowserService
            mAdapter.clear()
            for (song in songs) {
                mAdapter.add(SongItem(song as Song))
            }
            mAdapter.notifyDataSetChanged()



        }

    }



}