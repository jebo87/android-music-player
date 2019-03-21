package ca.makakolabs.makakomusic.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import androidx.core.graphics.drawable.DrawableCompat
import android.os.Build
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable


class Utils{

    companion object {
        fun convertToTime(milliseconds: Long): String {



            var segs = "" + (milliseconds / 1000).toInt() % 60
            var mins = "" + (milliseconds / (1000 * 60) % 60).toInt()
            var hours = "" + (milliseconds / (1000 * 60 * 60) % 24).toInt()
            if (segs.length == 1) {
                segs = "0$segs"
            }
            if (mins.length == 1) {
                mins = "0$mins"
            }
            if (hours.length == 1) {
                hours = "0$hours"
            }
            return if ((milliseconds / (1000 * 60 * 60) % 24).toInt() == 0) {
                mins + ":" + segs
            } else hours + ":" + mins + ":" + segs
        }

        fun blurImage(context: Context, albumart: Bitmap, width: Int, height: Int): Bitmap? {
             val BITMAP_SCALE = 0.2f
             val BLUR_RADIUS = 15f
             var inputBitmap: Bitmap? = null
             var outputBitmap: Bitmap? = null

            inputBitmap = Bitmap.createScaledBitmap(
                albumart,
                Math.round(albumart.width * BITMAP_SCALE),
                Math.round(albumart.height * BITMAP_SCALE),
                false
            )
            outputBitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true)
            if (inputBitmap == null)
                return null

            val rs = RenderScript.create(context)

            val input = Allocation.createFromBitmap(
                rs,
                outputBitmap,
                Allocation.MipmapControl.MIPMAP_FULL,
                Allocation.USAGE_SHARED
            )
            val output = Allocation.createTyped(rs, input.getType())

            val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            theIntrinsic.setInput(input)

            theIntrinsic.setRadius(BLUR_RADIUS)

            theIntrinsic.forEach(output)
            output.copyTo(outputBitmap)
            inputBitmap.recycle()

            return outputBitmap

        }

        fun decodeSampledBitmapFromFile(
            resId: String,
            reqWidth: Int, reqHeight: Int
        ): Bitmap {

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            options.inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeFile(resId, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(resId, options)
        }

        fun decodeSampledBitmapFromResource(
            res: Resources, resId: Int,
            reqWidth: Int, reqHeight: Int
        ): Bitmap {

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            options.inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeResource(res, resId, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeResource(res, resId, options)
        }

        fun getBitmapFromVectorDrawableWithId(context: Context, drawableId: Int): Bitmap {
            var drawable = ContextCompat.getDrawable(context, drawableId)

            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }

        private fun getBitmapFromVectorDrawable( drawable: Drawable): Bitmap {


            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }


        fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }

            return inSampleSize
        }

        fun isClickableArea(x: Float, y: Float, vectorDrawable: Drawable, option: Int): Boolean {
            val bm = getBitmapFromVectorDrawable(vectorDrawable)
            val newX:Float = if (x <= 0) 0f else x
            val newY:Float = if (y <= 0) 0f else y
            Log.d("Utils", " $newX $newY")
            when (option) {
                1 -> if (bm.getPixel(newX.toInt(), newY.toInt()) != 0) {
                    bm.recycle()
                    return true
                }
                2 -> {
                    Log.d("Utils", "One " + bm.getPixel(newX.toInt(), newY.toInt()))
                    var result = Color.alpha(bm.getPixel(newX.toInt(), newY.toInt())) >= 0
                    Log.d("Utils", "Two $result")
                    bm.recycle()
                    return result
                }
                else -> return false
            }
            return false

        }
    }

}
