package ca.makakolabs.makakomusic.ui.viewholders


import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.utils.Utils
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.song_card.view.*

class SongItem(private val song: Song) : Item<ViewHolder>() {

    companion object {
        const val TAG = "SongItem"

    }
    private lateinit var mSong : Song

    init {
        mSong = song
    }


    override fun getLayout() = R.layout.song_card

    override fun bind(
        holder: ViewHolder,
        position: Int

    ) {
        holder.itemView.song_card_title.text = song.title
        holder.itemView.song_card_duration.text = Utils.convertToTime(song.duration)
        holder.itemView.song_card_artist.text = song.artist

    }


    fun getSong(): Song {
        return mSong
    }


}