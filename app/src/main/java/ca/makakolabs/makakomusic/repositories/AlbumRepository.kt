package ca.makakolabs.makakomusic.repositories

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import ca.makakolabs.makakomusic.model.Album

class AlbumRepository(private val application: Application) {
    private var albums: MutableList<Album> = mutableListOf()

    companion object {
        val TAG = "AlbumRepository"
    }

    init {
        albums = getAlbums()
    }

    fun getAlbums(): MutableList<Album> {

        val sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"


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

                albums.add(Album(albumId, albumTitle, albumArtist, albumSongNumber))


            } while (cursor.moveToNext())

            cursor.close()


        }

        return albums


    }


}