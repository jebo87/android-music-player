package ca.makakolabs.makakomusic.ui.viewholders

import android.graphics.*
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Album
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.album_card.view.*


class AlbumItem(private val album:Album) : Item<ViewHolder>(){


    override fun getLayout() = R.layout.album_card


    override fun bind(viewHolder: ViewHolder, position: Int) {

        //Code to load the album art
        if (album.id != null) {
            Picasso.get()
                .load(album.description.iconUri)
                .resize(126, 126)
                .centerCrop()
                .placeholder(R.drawable.ic_empty_album)
                .error(R.drawable.ic_empty_album)
                .transform(CircleTransform())
                .into(viewHolder.itemView.album_card_art)
        }

        viewHolder.itemView.album_card_title.text = album.title
        viewHolder.itemView.album_card_artist.text = album.artist



    }

}



