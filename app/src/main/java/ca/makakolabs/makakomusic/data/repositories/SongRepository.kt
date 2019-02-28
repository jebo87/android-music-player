package ca.makakolabs.makakomusic.data.repositories

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaBrowserCompat
import ca.makakolabs.makakomusic.data.model.Song


class SongRepository (private val application: Application) : MusicRepository{


    private val songs: MutableList<MediaBrowserCompat.MediaItem>


    init {
        songs = getMediaFromCursor()
    }


    override fun getMediaFromCursor(): MutableList<MediaBrowserCompat.MediaItem>{

        var loadedSongs = mutableListOf<MediaBrowserCompat.MediaItem>()

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
            MusicRepository.uri, // Uri
            projection, // Projection
            selection, // Selection
            null, // Selection arguments
            sortOrder // Sort order
        )

        if (cursor != null && cursor.moveToFirst()) {
           val idColumn:Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val albumColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val artistColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val durationColumn : Int = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)


            var albumIdColumn :Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)



            // Now loop through the music files
            do {
                val audioId:String = cursor.getString(idColumn)
                val audioTitle: String = cursor.getString(titleColumn)
                val audioArtist: String = cursor.getString(artistColumn)
                val audioAlbum: String = cursor.getString(albumColumn)
                val audioAlbumId: Long = cursor.getLong(albumIdColumn)
                val audioDuration: Long = cursor.getLong(durationColumn)



                // Add the current music to the list


//                var mMediaMetadataCompat =MediaMetadataCompat.Builder()
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, audioId)
//                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audioAlbum)
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audioArtist)
//                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationColumn.toLong())
//                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "")
//                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "")
//                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioTitle)
//                    .build()

                var song = Song(audioId,audioTitle,audioArtist,audioAlbumId,audioAlbum,audioDuration)

                loadedSongs.add(song)




            } while (cursor.moveToNext())

            cursor.close()
        }

        return loadedSongs


    }

     override fun createMediaItem(metadata: MediaMetadataCompat): MediaBrowserCompat.MediaItem {

        return MediaBrowserCompat.MediaItem(
            metadata.description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )

    }
}