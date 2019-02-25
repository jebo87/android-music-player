package ca.makakolabs.makakomusic.model

import android.content.ContentUris.withAppendedId
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat

data class Album(
    var id: Long,
    var title: String,
    var artist: String,
    var songNumber: String
) : MediaBrowserCompat.MediaItem(
MediaDescriptionCompat.Builder()
.setMediaId(id.toString())
.setTitle(title)
.setIconUri(withAppendedId(Uri.parse("content://media/external/audio/albumart"), id)!!)
.setSubtitle(artist)
.setExtras(Bundle().apply {
    this.putString("number_of_songs",artist)
})
.build(), FLAG_BROWSABLE
) {

}