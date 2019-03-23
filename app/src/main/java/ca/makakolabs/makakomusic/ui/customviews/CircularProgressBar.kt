package ca.makakolabs.makakomusic.ui.customviews

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import ca.makakolabs.makakomusic.R
import android.graphics.Shader.TileMode
import android.graphics.RadialGradient


class CircularProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var objPaint = Paint()
    private var bounds = RectF()
    private var imageHeight = 0
    private var imageWidth = 0
    private var posEnd = 0f
    private var posStart = 0f
    private var density = 1F
    private var atr: TypedArray

    init {
        objPaint = Paint()
        objPaint.isAntiAlias = true
        objPaint.style = Paint.Style.STROKE


        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inPreferredConfig = Bitmap.Config.RGB_565
        BitmapFactory.decodeResource(resources, R.drawable.ic_albumart_bg, options)
        imageWidth = options.outWidth
        imageHeight = options.outHeight

        atr = context.theme.obtainStyledAttributes(attrs, R.styleable.torta, 0, 0)
//




        //objPaint.shader = LinearGradient(0f, 0f, 290f, 290f, Color.RED, Color.CYAN, TileMode.MIRROR)


    }

    companion object {
        val TAG = "CircularProgressBar"
    }

    fun setDensity(density: Float) {

        objPaint.strokeWidth = 4 * density
        var positions = arrayOf(
            0.0f,
            0.125f,
            0.25f,
            0.375f,
            0.5f,
            0.625f,
            0.75f,
            0.875f,
            1.0f)
        var colors = arrayOf(
            Color.rgb(228, 239, 14),
            Color.rgb(211, 248, 19),
            Color.rgb(143, 236, 40),
            Color.rgb(80, 224, 60),
            Color.rgb(35, 200, 186),
            Color.rgb(74, 88, 200),
            Color.rgb(127, 51, 141),
            Color.rgb(180, 14, 81),
            Color.rgb(222, 49, 79)
            )

            objPaint.shader = SweepGradient(290*density/ 2, 290*density / 2, colors.toIntArray(), positions.toFloatArray())
    }

    fun setWidthHeight(width: Float, height: Float) {
        bounds.set(1 * density, 1 * density, width - 4, height - 4)


    }

    var lienzo = Canvas()

    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)

        lienzo = canvas

        lienzo.drawArc(bounds, posStart, posEnd, false, objPaint)
        //Log.d(TAG, "RGB ${(255 * (posEnd / 360))}");


    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        atr.recycle()
    }


    fun setPosEnd(posE: Float) {
        this.posEnd = posE
//        Log.d(TAG, "Moving progress bar to $posE")
        requestLayout()


    }


}