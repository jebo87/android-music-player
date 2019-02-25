package ca.makakolabs.makakomusic.repositories

import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat

interface MusicRepository{
    companion object {
         val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    fun getMediaFromCursor(): MutableList<MediaBrowserCompat.MediaItem>
    fun createMediaItem(metadata: MediaMetadataCompat): MediaBrowserCompat.MediaItem
}