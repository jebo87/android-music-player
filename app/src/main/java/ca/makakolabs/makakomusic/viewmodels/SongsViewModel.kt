package ca.makakolabs.makakomusic.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ca.makakolabs.makakomusic.model.Song
import ca.makakolabs.makakomusic.repositories.SongRepository

class SongsViewModel (application: Application): AndroidViewModel(application){
    private val songRepository: SongRepository

    private val allSongs:List<Song>

    init {
        songRepository= SongRepository(application)
        allSongs = songRepository.getAllSongs()
    }

    fun getAllSongs(): List<Song>{
        return allSongs
    }


}