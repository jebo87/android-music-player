package ca.makakolabs.makakomusic.repositories

import android.R
import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import ca.makakolabs.makakomusic.model.Album

class AlbumRepository(private val application: Application) : MusicRepository{
    private var albums: MutableList<MediaBrowserCompat.MediaItem> = mutableListOf()

    companion object {
        val TAG = "AlbumRepository"
    }

    init {
        albums = getMediaFromCursor()
    }

    override fun getMediaFromCursor(): MutableList<MediaBrowserCompat.MediaItem> {
        val sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"

        var loadedAlbums = mutableListOf<MediaBrowserCompat.MediaItem>()


//        projection: columns that we need
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
        )



        val cursor: Cursor? = application.contentResolver?.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder

        )


        if (cursor != null && cursor.moveToFirst()) {

            val id = cursor.getColumnIndex(MediaStore.Audio.Albums._ID)
            val album = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
            val artist = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)
            val number_songs = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

            //now loop though the different albums

            do {
                val albumId = cursor.getString(id)
                val albumTitle = cursor.getString(album)
                val albumArtist = cursor.getString(artist)
                val albumSongNumber = cursor.getString(number_songs)

//                var mMediaMetadataCompat =MediaMetadataCompat.Builder()
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, albumId)
//                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumTitle)
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, albumArtist)
//                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, albumSongNumber)
//                    .build()

                var album = Album(albumId.toLong(),albumTitle,albumArtist,albumSongNumber)
                loadedAlbums.add(album)

            } while (cursor.moveToNext())

            cursor.close()


        }

        return loadedAlbums     }

    override fun createMediaItem(metadata: MediaMetadataCompat): MediaBrowserCompat.MediaItem {

        return MediaBrowserCompat.MediaItem(
            metadata.description,
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )

    }




}