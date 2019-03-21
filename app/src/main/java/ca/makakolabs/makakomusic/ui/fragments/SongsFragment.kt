package ca.makakolabs.makakomusic.ui.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.services.MakakoPlaybackService
import ca.makakolabs.makakomusic.ui.activities.MediaActionListener
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
    lateinit  var myActivity: MediaActionListener
    private  var songs = mutableListOf<Song>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewCL = inflater.inflate(ca.makakolabs.makakomusic.R.layout.songs_fragment_layout, container, false)
        recycler = (viewCL as ConstraintLayout).findViewById(R.id.songs_fragment_recycler_view)

        recycler.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = mAdapter.apply {
                setOnItemClickListener { item, view ->
                    var songToPlay = (item as SongItem).getSong()
                    myActivity.setMediaList(songs)
                    myActivity.onMediaItemSelected(songToPlay)



                }
            }

        }

        return viewCL
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)

        myActivity = (context as MediaActionListener)


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
        //subscribe to the songs root
        //unsubsribe and subscribe again, this is supposed to be an android bug that will be corrected in the future
        //once this is corrected, only the subscribe line will be necessary
        var root = MakakoPlaybackService.SONGS_MEDIA_ROOT_ID
        myActivity.getMediaBrowserCompat().unsubscribe(root)
        myActivity.getMediaBrowserCompat().subscribe(root,subscriptionCallback)

    }

    private var subscriptionCallback = object: MediaBrowserCompat.SubscriptionCallback(){
        override fun onChildrenLoaded(
            parentId: String,
            loadedSongs: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            if (loadedSongs == null || loadedSongs.isEmpty()) {
                return
            }

            //replace the contents of the adapter with the result sent from the MediaBrowserService
            mAdapter.clear()
            for (song in loadedSongs) {
                mAdapter.add(SongItem(song as Song))
                songs.add(song)
            }

            mAdapter.notifyDataSetChanged()
            MediaControllerCompat.getMediaController(myActivity as Activity).transportControls.prepare()


        }

    }



}