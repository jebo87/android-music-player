package ca.makakolabs.makakomusic.viewholders

import android.content.ContentUris
import android.net.Uri
import android.util.Log
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.model.Song
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.song.view.*
import java.net.URI


class SongItem( val song: Song) : Item<ViewHolder>(){

    companion object {
        val TAG = "SongItem"
    }


    override fun getLayout()=R.layout.song


    override fun bind(holder: ViewHolder, position: Int) {
        holder.itemView.song_title.text = song.title
        holder.itemView.song_album.text = song.album
        holder.itemView.song_artist.text = song.artist
        if(song.albumArt != 0L) {
            Log.d(TAG,"AlbumArt: "+song.albumArt)

            var uri: Uri = Uri.parse("content://media/external/audio/albumart")
            uri = ContentUris.withAppendedId(uri,song.albumArt)
        Picasso.get().load(uri).into(holder.itemView.song_album_art)}



    }

}