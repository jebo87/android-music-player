package ca.makakolabs.makakomusic.ui.activities

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import ca.makakolabs.makakomusic.data.model.Song

interface MediaBrowserProvider{
    fun getMediaBrowserCompat(): MediaBrowserCompat
    fun getMediaControllerCompat(): MediaControllerCompat
}

interface MediaActionListener : MediaBrowserProvider{
     fun onMediaItemSelected(song : Song)
     fun setMediaList(songs : MutableList<Song>)
}