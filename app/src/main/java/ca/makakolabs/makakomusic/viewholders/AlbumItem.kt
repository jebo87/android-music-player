package ca.makakolabs.makakomusic.viewholders

import android.graphics.*
import android.net.Uri
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.model.Album
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.album_card.view.*


class AlbumItem(private val album:Album) : Item<ViewHolder>(){



    private val albumArtUri: Uri = Uri.parse("content://media/external/audio/albumart")

    override fun getLayout() = R.layout.album_card


    override fun bind(viewHolder: ViewHolder, position: Int) {

////        In case we want to load an album art
        if (album.id != null) {
            var uri = Uri.withAppendedPath(albumArtUri,album.id)
            Picasso.get()
                .load(uri)
                .resize(126, 126)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .transform(CircleTransform())
                .into(viewHolder.itemView.album_card_art)
        }

        viewHolder.itemView.album_card_title.text = album.title
        viewHolder.itemView.album_card_artist.text = album.artist



    }
    inner class CircleTransform : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            val size = Math.min(source.width, source.height)

            val x = (source.width - size) / 2
            val y = (source.height - size) / 2

            val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
            if (squaredBitmap != source) {
                source.recycle()
            }

            val bitmap = Bitmap.createBitmap(size, size, source.config)

            val canvas = Canvas(bitmap)
            val paint = Paint()
            val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paint.setShader(shader)
            paint.setAntiAlias(true)

            val r = size / 2f
            canvas.drawCircle(r, r, r, paint)

            squaredBitmap.recycle()
            return bitmap
        }

        override fun key(): String {
            return "circle"
        }
    }

}



