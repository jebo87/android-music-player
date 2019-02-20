package ca.makakolabs.makakomusic.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ca.makakolabs.makakomusic.model.Album
import ca.makakolabs.makakomusic.repositories.AlbumRepository

class AlbumViewModel (application: Application): AndroidViewModel(application){
    private val albumRepository: AlbumRepository

    private val allAlbums: List<Album>

    init{
        albumRepository = AlbumRepository(application)
        allAlbums = albumRepository.getAlbums()
    }

    fun getAlbums(): List<Album> {
        return allAlbums
    }

}
