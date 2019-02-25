package ca.makakolabs.makakomusic.model

import android.content.ContentUris.withAppendedId
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat



data class Song(
    val id: String,
    val title: String,
    val artist: String = "",
    val albumId:Long = 0,
    val album: String = "",
    val duration: Long = 0
) : MediaBrowserCompat.MediaItem(
    MediaDescriptionCompat.Builder()
        .setMediaId(id)
        .setTitle(title)
        .setIconUri(withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)!!)
        .setSubtitle(artist)
        .setExtras(Bundle().apply {
            this.putString("artist",artist)
            this.putLong("album_id",albumId)
            this.putString("album",album)
            this.putLong("duration",duration)
        })
        .build(), FLAG_PLAYABLE
) {


}