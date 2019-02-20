package ca.makakolabs.makakomusic.viewholders

import android.net.Uri
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.model.Song
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.song_card.view.*


class SongItem(private val song: Song) : Item<ViewHolder>() {

    companion object {
        val TAG = "SongItem"
    }


//    override fun getLayout() = R.layout.song_horizontal

    override fun getLayout() = R.layout.song_card

//    var albumArtUri: Uri = Uri.parse("content://media/external/audio/albumart")
    override fun bind(holder: ViewHolder, position: Int) {
        holder.itemView.song_card_title.text = song.title
        holder.itemView.song_card_duration.text = song.album
        holder.itemView.song_card_artist.text = song.artist

        //In case we want to load an album art
//        if (song.albumArt != 0L) {
//            var uri = ContentUris.withAppendedId(albumArtUri, song.albumArt)
//            Picasso.get()
//                .load(uri)
//                .resize(70, 70)
//                .centerCrop()
//                .placeholder(R.mipmap.ic_launcher_round)
//                .error(R.mipmap.ic_launcher_round)
//                .into(holder.itemView.song_album_art)
//        }


    }

}