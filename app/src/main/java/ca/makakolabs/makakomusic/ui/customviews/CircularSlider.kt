package ca.makakolabs.makakomusic.ui.customviews

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import ca.makakolabs.makakomusic.utils.Utils

class CircularSlider (context: Context, attrs: AttributeSet): ImageView(context, attrs), View.OnTouchListener {

     private var theMatrix= Matrix()
     var pivx = 0
     var pivy = 0
     private  var angle = 0f

    companion object {
        val TAG = "CircularSlider"
    }

    init {

        setOnTouchListener(this)
    }



    fun rotate(angle: Float) {

        scaleType = ImageView.ScaleType.MATRIX
            //theMatrix.postRotate(angle, pivx.toFloat(), pivy.toFloat())
       theMatrix.setRotate(angle, pivx.toFloat(), pivy.toFloat())
        imageMatrix = theMatrix

        requestLayout()
        invalidate()





    }


    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val x = motionEvent.x
        val y = motionEvent.y


        when (motionEvent.action) {

            MotionEvent.ACTION_DOWN -> {
                angle = Math.toDegrees(Math.atan2((y - pivy).toDouble(), (x - pivx).toDouble())).toFloat()
                if (angle < 0)
                    angle += 360f
//                if (Utils.isClickableArea(motionEvent.x, motionEvent.y, this.background as VectorDrawable, 1)) {
                    //The user clicked somewhere in the circle, we have to move the controller to that exact point and forward or rewind the song accordingly
                   rotate(angle)

//                } else
//                    return false
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "Entro a onTouch")

                //The user is dragging the controller, we should move it so it stays on his finger.
                angle = Math.toDegrees(Math.atan2((y - pivy).toDouble(), (x - pivx).toDouble())).toFloat()
                if (angle < 0)
                    angle += 360f
                rotate(angle)



            }
            MotionEvent.ACTION_UP ->
            {

                //Seek to the new position
                //                notifyPlaybackChange(PlaybackObserver.SEEK)

            }


        }

        return true
    }


}
