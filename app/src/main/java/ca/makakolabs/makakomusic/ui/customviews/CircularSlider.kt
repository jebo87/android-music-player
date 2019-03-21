package ca.makakolabs.makakomusic.ui.customviews

import android.content.Context
import android.graphics.drawable.VectorDrawable
import android.os.Handler
import android.support.v4.media.session.MediaControllerCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.ui.activities.PlaybackActivity
import ca.makakolabs.makakomusic.utils.Utils
import java.io.FileNotFoundException

class CircularSlider(context: Context, attrs: AttributeSet) : ImageView(context, attrs), View.OnTouchListener {
    private var oldAngle = 0f
    var duration:Long = 0
    var pivx = 0
    var pivy = 0
    private var angle = 0f
    var percentage =0f
    private var mHandler = Handler()


    private lateinit var rotate: RotateAnimation
    var mediaController: MediaControllerCompat? = null

    companion object {
        val TAG = "CircularSlider"
    }

    init {
        this.setOnTouchListener(this)

    }




    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                if(Utils.isClickableArea(x,y,this.background,1)) {
                    PlaybackActivity.isUILocked = true
                    angle = Math.toDegrees(Math.atan2((y - pivy).toDouble(), (x - pivx).toDouble())).toFloat()
                    if (angle < 0)
                        angle += 360f
                    rotate(angle, 10)
                }else{
                    return false
                }


            }

            MotionEvent.ACTION_MOVE->{

                    PlaybackActivity.isUILocked = true
                    angle = Math.toDegrees(Math.atan2((y - pivy).toDouble(), (x - pivx).toDouble())).toFloat()
                    if (angle < 0)
                        angle += 360f
                    rotate(angle, 10)

            }

            MotionEvent.ACTION_UP ->{
                if(PlaybackActivity.isUILocked) {
                    PlaybackActivity.isUILocked = false
                    oldAngle = angle
                    percentage = angle / 360
                    mediaController?.transportControls?.seekTo((duration * percentage).toLong())
                }

            }
        }
        return true
    }


    fun rotate(newAngle: Float, duration: Long): Float {

        angle = newAngle
        rotate = RotateAnimation(
            oldAngle,
            angle,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        );
        rotate.duration = duration
        rotate.interpolator = LinearInterpolator()

        startAnimation(rotate)
        rotate.fillAfter = true

        oldAngle = angle

        return angle


    }






}
