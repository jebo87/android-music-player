package ca.makakolabs.makakomusic.repositories

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import ca.makakolabs.makakomusic.model.Song

class SongRepository (private val application: Application){
    private val songs: MutableList<Song>

    companion object {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    init {
        songs = getAllSongs()
    }


    fun getAllSongs(): MutableList<Song>{
        val loadedSongs: MutableList<Song> = mutableListOf()

        //val uri: Uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI

        // IS_MUSIC : Non-zero if the audio file is music
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"

        // Sort the musics
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        //val sortOrder = MediaStore.Audio.Media.TITLE + " DESC"

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Query the external storage for music files
        val cursor: Cursor? = application.contentResolver?.query(
            uri, // Uri
            projection, // Projection
            selection, // Selection
            null, // Selection arguments
            sortOrder // Sort order
        )

        if (cursor != null && cursor.moveToFirst()) {
//            val title:Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val id: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val album: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val artist: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)


            var albumId :Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)



            // Now loop through the music files
            do {
//                val audioId:Long = cursor.getLong(id)
                val audioTitle: String = cursor.getString(id)
                val audioArtist: String = cursor.getString(artist)
                val audioAlbum: String = cursor.getString(album)
                val audioArt: Long = cursor.getLong(albumId)
                Log.d("SongsFragment title", audioTitle)

                // Add the current music to the list
                loadedSongs.add(Song(audioTitle,audioArtist,audioAlbum,0,"",audioArt))
            } while (cursor.moveToNext())

            cursor.close()
        }

        return loadedSongs


    }
}