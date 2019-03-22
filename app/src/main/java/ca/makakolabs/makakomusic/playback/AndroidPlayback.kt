package ca.makakolabs.makakomusic.playback

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import ca.makakolabs.makakomusic.data.model.Song
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.source.ExtractorMediaSource


class AndroidPlayback(context: Context) {

    private lateinit var player: SimpleExoPlayer
    private var mContext: Context = context
    private var songs = mutableListOf<Song>()
    private var currentIndex = -1

    fun addQueueItem(song: MediaDescriptionCompat) {
        songs.add(Song(song.mediaId!!))
    }


    companion object {
        val TAG = "AndroidPlayback"
    }

    private fun findSong(mediaId: String): Int {
        for ((index, song) in songs.withIndex()) {
            if (song.id.equals(mediaId))
                return index
        }
        return -1
    }

    fun playFromId(mediaId: String) {

        currentIndex = findSong(mediaId)
        Log.d(TAG, "Index to play is $currentIndex")

        if (!::player.isInitialized)
            initializePlayer()

        Log.d(TAG, "playing!!")
        val dataSourceFactory = DefaultDataSourceFactory(
            mContext,
            Util.getUserAgent(mContext, "makakomusic")
        )

        val source = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse("content://media/external/audio/media/" + mediaId))
        player.prepare(source)
        player.playWhenReady = true

    }

    fun stop() {
        if (!::player.isInitialized)
            return
        player.playWhenReady = false
        player.stop()

    }

    fun pause() {
        if (!::player.isInitialized)
            return
        player.playWhenReady = false

    }

    fun play() {
        Log.d(TAG, "Preparing playback")
        if (!::player.isInitialized)
            return
        player.playWhenReady = true

    }

    fun skipToNext(): String? {
        if (!::player.isInitialized)
            return null
        playFromId(songs[currentIndex + 1].id)
        //the current index is modified in playfromId, so we return the id related to the currentIndex which is the new song
        return songs[currentIndex].id

    }
    fun skipToPrevious(): String? {
        if (!::player.isInitialized)
            return null
        if (currentIndex == 0){
            currentIndex = songs.size
        }
        playFromId(songs[currentIndex - 1].id)
        //the current index is modified in playfromId, so we return the id related to the currentIndex which is the new song
        return songs[currentIndex].id

    }

    fun seekTo(position: Long) {
        if (!::player.isInitialized)
            return
        player.seekTo(position)


    }

    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(mContext)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(CONTENT_TYPE_MUSIC)
            .setUsage(USAGE_MEDIA)
            .build()
        player.setAudioAttributes(audioAttributes, true)


    }

}