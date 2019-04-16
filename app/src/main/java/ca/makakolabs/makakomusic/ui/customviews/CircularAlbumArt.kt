package ca.makakolabs.makakomusic.ui.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ImageView

class CircularAlbumArt(context: Context, attrs: AttributeSet): ImageView (context,attrs){
     var  source: Bitmap? = null
    var  bitmap: Bitmap? = null

    var density = 1f



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        if(source == null) {
//            super.onDraw(canvas)
//            return
//        }
//
//
//        val x = (250*density.toInt()) / 2
//        val y = (250*density.toInt()) / 2
//
//        val squaredBitmap = Bitmap.createBitmap(source, x, y, 250*density.toInt(), 250*density.toInt())
//        if (squaredBitmap != source) {
//            source!!.recycle()
//        }
////        bitmap= Bitmap.createBitmap(size, size, source!!.config)
////        canvas.setBitmap(bitmap)
//        val paint = Paint()
//        val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
//        paint.shader = shader
//        paint.isAntiAlias=true
//
//
//
//
//        val r = 250*density.toInt() / 2f
//        canvas.drawCircle(r, r, r, paint)
//        //canvas.drawBitmap(bitmap,0f,0f,paint)
//
//
//        //In case we need to paint a hole in the middle like in old vinyls
//        //paintHoleInMiddle(canvas,r)
//
//
//
//
//
//
//        squaredBitmap.recycle()
    }

}