package ca.makakolabs.makakomusic.fragments

import android.Manifest

import android.content.pm.PackageManager

import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.model.Song
import ca.makakolabs.makakomusic.viewholders.SongItem
import ca.makakolabs.makakomusic.viewmodels.SongsViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import androidx.recyclerview.widget.GridLayoutManager





class SongsFragment : Fragment() {

    companion object {
        val TAG = "Songs"
    }

    private val adapter = GroupAdapter<ViewHolder>()

    private lateinit var mSongViewModel:SongsViewModel

    private var songs: List<Song> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        //initialize viewmodel
        mSongViewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)

        songs = mSongViewModel.getAllSongs() // TODO this is blocking the UI!!



        var viewCL = inflater.inflate(R.layout.songs_fragment_layout, container, false)
        val recycler = (viewCL as ConstraintLayout).findViewById<RecyclerView>(R.id.songs_fragment_recycler_view)


        recycler.layoutManager =GridLayoutManager(context, 2)


        recycler.adapter = adapter

        recycler.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
        recycler.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.HORIZONTAL))
/*
        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            // No explanation needed; request the permission

            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {

*/
            loadSongs()
/*        }

 */

        return viewCL
    }




    private fun loadSongs() {
        for (song in songs){
            adapter.add(SongItem(song))
        }

    }








}



