package ca.makakolabs.makakomusic.ui.viewmodels

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.AndroidViewModel
import ca.makakolabs.makakomusic.data.repositories.SongRepository

class SongsViewModel (application: Application): AndroidViewModel(application){
    private val songRepository: SongRepository

    private val allSongs:List<MediaBrowserCompat.MediaItem>

    init {
        songRepository= SongRepository(application)
        allSongs = songRepository.getMediaFromCursor()
    }





}