package ca.makakolabs.makakomusic.ui.viewholders

import android.graphics.*
import com.squareup.picasso.Transformation

//    Class to paint the album art with masks and strokes.
class CircleTransform : Transformation {
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
        paint.shader = shader
        paint.isAntiAlias=true


        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)


        //In case we need to paint a hole in the middle like in old vinyls
//        paintHoleInMiddle(canvas,r)






        squaredBitmap.recycle()
        return bitmap
    }

    private fun paintHoleInMiddle(canvas : Canvas, r :Float){
        val paint2 = Paint()
        paint2.style = Paint.Style.STROKE
        paint2.strokeWidth = 2F
        paint2.isAntiAlias=true
        paint2.color = Color.rgb(100,100,100)



        canvas.drawCircle(r, r, r-paint2.strokeWidth/2, paint2)

        val paint3 = Paint()
        paint3.style = Paint.Style.FILL
        paint3.isAntiAlias=true
        paint3.color = Color.rgb(41,41,41)
        canvas.drawCircle(r, r, r*0.20f, paint3)

        val paint4 = Paint()
        paint4.style = Paint.Style.STROKE
        paint4.strokeWidth = 2F
        paint4.isAntiAlias=true
        paint4.color = Color.rgb(100,100,100)
        canvas.drawCircle(r, r, r*0.20f-paint4.strokeWidth/2, paint4)

        val paint5 = Paint()
        paint5.style = Paint.Style.FILL
        paint5.isAntiAlias=true
        paint5.color = Color.argb(60,40,40,40)
        canvas.drawCircle(r, r, r*0.30f, paint5)

    }

    override fun key(): String {
        return "circle"
    }
}