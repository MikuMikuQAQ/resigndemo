package com.rainblog.resigndemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class ResignView : SurfaceView, SurfaceHolder.Callback {

    @Volatile private var isRun = false
    private var pointX: Float = 0f
    private var pointY: Float = 0f
    private val paint = Paint()
    private val mPaint = Paint()
    private var clickNum = 0

    constructor(context: Context?) : super(context) {initd()}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {initd()}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initd()}

    private fun initd() {
        paint.style = Paint.Style.FILL
        paint.color = Color.RED
        paint.isAntiAlias = true

        mPaint.color = Color.WHITE
        mPaint.textSize = dpToPx(16)

        holder.addCallback(this)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        this.setZOrderOnTop(true)
        holder?.setFormat(PixelFormat.TRANSPARENT)
        Thread { draw() }.start()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        isRun = false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
//                if (clickNum <= 5) {
                    val rect = Rect(pointX.toInt(), pointY.toInt(), pointX.toInt() + dpToPx(88).toInt(), pointY.toInt() + dpToPx(44).toInt())
                    when {
                        rect.contains(event.x.toInt(), event.y.toInt()) -> {
//                            if (clickNum >= 5) {
//                                pullMax()
//                            } else {
                                clickNum ++
                                Thread { draw() }.start()
//                            }
                        }
                        else -> {}
                    }
//                } else {}
            }
            else -> {}
        }
        return super.onTouchEvent(event)
    }

    private fun draw() {
        holder?.lockCanvas()?.let { canvas ->
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            pointX = Random.nextInt(0, canvas.width/Random.nextInt(1,3))*1f
            pointY = Random.nextInt(0, canvas.height/Random.nextInt(1,3))*1f

            canvas.drawRoundRect(
                pointX,
                pointY,
                pointX+dpToPx(88),
                pointY+dpToPx(44),
                8f, 8f, paint)

            canvas.drawText(
                context.getString(R.string.no_agree),
                pointX+dpToPx(20),
                pointY+dpToPx(28), mPaint)

            holder?.unlockCanvasAndPost(canvas)
        }
    }

    private fun pullMax() {
        isRun = true
        Thread {
            val list = mutableListOf<MutableMap<String, Float>>()
            while (isRun) {
                val map = mutableMapOf<String, Float>()
                holder?.lockCanvas()?.let { canvas ->
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                    map["pointX"] = Random.nextInt(0, canvas.width/Random.nextInt(1,3))*1f
                    map["pointY"] = Random.nextInt(0, canvas.height/Random.nextInt(1,3))*1f

                    list.add(map)

                    list.forEach {
                        canvas.drawRoundRect(
                            it["pointX"] ?:0f,
                            it["pointY"] ?:0f,
                            it["pointX"] ?:0f+dpToPx(88),
                            it["pointY"] ?:0f+dpToPx(44),
                            8f, 8f, paint)

                        canvas.drawText(
                            context.getString(R.string.agree),
                            it["pointX"] ?:0f+dpToPx(20),
                            it["pointY"] ?:0f+dpToPx(28), mPaint)

                    }

                    holder?.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun dpToPx(dp: Int): Float {
        val dpi = context.resources.displayMetrics.density
        return dp * dpi + 0.5f
    }

}