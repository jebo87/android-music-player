package ca.makakolabs.makakomusic.playback

import android.os.Parcelable
import android.support.v4.media.session.MediaSessionCompat
import ca.makakolabs.makakomusic.data.model.Song
import kotlinx.android.parcel.Parcelize

@Parcelize
class QueueManager : Parcelable {
    private var songs = mutableListOf<MediaSessionCompat.QueueItem>()

    fun setQueue(newQueue : ArrayList<MediaSessionCompat.QueueItem>){
        songs = newQueue

    }

}