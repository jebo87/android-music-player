package ca.makakolabs.makakomusic.data.model

import android.content.ContentUris.withAppendedId
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Song(
     var id: String ="",
     var title: String = "",
     var artist: String = "",
     var albumId:Long = 0,
     var album: String = "",
     var duration: Long = 0
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
), Parcelable{


    fun toMetaData(): MediaMetadataCompat{

        return MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,id)
            putString(MediaMetadataCompat.METADATA_KEY_TITLE,title)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist)
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM,album)
            putString(MediaMetadataCompat.METADATA_KEY_ART_URI,"content://media/external/audio/albumart/$albumId")
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration)

        }.build()
    }

    companion object {
        fun fromMetaData(metadataCompat: MediaMetadataCompat): Song{

            var st = StringTokenizer(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ART_URI),"/",false)
            var tempId:String = ""
            while (st.hasMoreTokens()){
                tempId = st.nextToken()
            }



            return Song().apply{
                id=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                title=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                artist=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                album=metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
                albumId = tempId.toLong()
                Log.d("SONG",this.title)

            }
        }
    }

}