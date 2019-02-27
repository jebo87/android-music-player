package ca.makakolabs.makakomusic.ui.viewmodels

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.AndroidViewModel
import ca.makakolabs.makakomusic.data.repositories.AlbumRepository

class AlbumViewModel (application: Application): AndroidViewModel(application){
    private val albumRepository: AlbumRepository

    private val allAlbums: List<MediaBrowserCompat.MediaItem>

    init{
        albumRepository = AlbumRepository(application)
        allAlbums = albumRepository.getMediaFromCursor()
    }


}
