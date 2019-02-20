package ca.makakolabs.makakomusic.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.model.Album
import ca.makakolabs.makakomusic.viewholders.AlbumItem
import ca.makakolabs.makakomusic.viewmodels.AlbumViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class AlbumsFragment : Fragment() {

    companion object {
        val TAG = "Albums"
    }

    private val adapter = GroupAdapter<ViewHolder>()

    private lateinit var albumViewModel: AlbumViewModel

    private var albums: List<Album> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //initialize view model
        albumViewModel = ViewModelProviders.of(this).get(AlbumViewModel::class.java)

        albums = albumViewModel.getAlbums() // TODO change to ASYNC

        var constraintLayout = inflater.inflate(R.layout.albums_fragment_layout,container,false)
        var recycler = (constraintLayout as ConstraintLayout).findViewById<RecyclerView>(R.id.albums_fragment_recycler)

        recycler.layoutManager = GridLayoutManager(context,2)

        recycler.adapter = adapter

        for (album in albums){
            adapter.add(AlbumItem(album))
        }

        return constraintLayout

    }

}