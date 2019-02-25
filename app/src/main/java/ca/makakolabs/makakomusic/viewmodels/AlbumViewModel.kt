package ca.makakolabs.makakomusic.viewmodels

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.AndroidViewModel
import ca.makakolabs.makakomusic.model.Album
import ca.makakolabs.makakomusic.repositories.AlbumRepository

class AlbumViewModel (application: Application): AndroidViewModel(application){
    private val albumRepository: AlbumRepository

    private val allAlbums: List<MediaBrowserCompat.MediaItem>

    init{
        albumRepository = AlbumRepository(application)
        allAlbums = albumRepository.getMediaFromCursor()
    }


}
