### SurfaceView介绍
在Android中更新UI必须保证操作都在主线程中，如果画面经常刷新变动就会导致主线程阻塞，影响用户体验或更严重的ANR，这时就需要使用SurfaceView进行处理。
SurfaceView可以另起一个线程单独进行画面更新的，通过对cavans进行绘制来实现画面显示，在重复的动画中可以对cavans绘制的bitmap用LruCache进行缓存来优化性能。

### 简单的使用（kotlin实现）
#### 1.需要继承SurfaceView以及对SurfaceHolder.Callback接口方法进行重写
```kotlin
class ResignView : SurfaceView, SurfaceHolder.Callback {
    constructor(context: Context?) : super(context) {holder.addCallback(this)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {holder.addCallback(this)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {holder.addCallback(this)}

    override fun surfaceCreated(p0: SurfaceHolder) {}

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}

    override fun surfaceDestroyed(p0: SurfaceHolder) {}
}
```
holder.addCallback(this):绑定接口实现。
surfaceCreated：SurfaceView刚创建时的回调，这个方法里我们可以对Surface进行一些设定、以及动画方法的开始。
surfaceChanged：在画面有变动时的回调，比如屏幕旋转，小窗口模式等，这时候我们就需要对绘制动画的坐标、比例等进行修改。
surfaceDestroyed：在画面销毁时的操作，需要手动释放的资源等都在这里进行处理，防止内存泄漏。
ps：如果需要透明无遮挡的画布，需要在surfaceCreated回调中添加以下方法：
```kotlin
        this.setZOrderOnTop(true)
        holder?.setFormat(PixelFormat.TRANSPARENT)
```


#### 2.对cavans进行绘制
在surfaceCreated之后的调用中可以起一个线程，通过holder.lockCanvas()获取canvas，进行相应的绘制，绘制完成后再通过holder.unlockCanvasAndPost(canvas)进行显示
```kotlin
    private var isRun = false
    private val paint = Paint()
    private val mPaint = Paint()
    private val list = mutableListOf<MutableMap<String, Float>>()
    private fun pushMax() {
        isRun = true
        paint.style = Paint.Style.FILL
        paint.color = Color.RED
        paint.isAntiAlias = true

        mPaint.color = Color.WHITE
        mPaint.textSize = dpToPx(16)
        Thread {
            paint.color = Color.argb(0xff, 0x01, 0xB8, 0xD1)
            while (isRun) {
                val map = mutableMapOf<String, Float>()
                holder?.lockCanvas()?.let { canvas ->
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                    map["pointX"] = Random.nextInt(0, canvas.width/Random.nextInt(1,3))*1f
                    map["pointY"] = Random.nextInt(0, canvas.height/Random.nextInt(1,3))*1f

                    list.add(map)

                    list.forEach {
                        pointX = it["pointX"] ?:0f
                        pointY = it["pointY"] ?:0f

                        canvas.drawRoundRect(
                            pointX,
                            pointY,
                            pointX+dpToPx(88),
                            pointY+dpToPx(44),
                            8f, 8f, paint)

                        canvas.drawText(
                            context.getString(R.string.agree),
                            pointX+dpToPx(28),
                            pointY+dpToPx(28), mPaint)

                    }

                    holder?.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(500)
            }
        }.start()
    }
```
通过while 进行循环来实现动画的绘制；canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)实现画布清空，背景色透明的实现；isRun 对循环进行处理（简单的实现，最好用线程池来管理），list、map对绘制的坐标进行缓存（之后的点击事件会用到）。

#### 3.点击事件的检测
需要重写onTouchEvent方法，并在MotionEvent.ACTION_DOWN事件中进行检测。需要在MotionEvent.ACTION_UP事件中对performClick()方法进行执行。
```kotlin
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> performClick()
            MotionEvent.ACTION_DOWN -> {
                list.forEach {
                        val pointX = it["pointX"]?:0f
                        val pointY = it["pointY"]?:0f
                        val rect = Rect(pointX.toInt(), pointY.toInt(), pointX.toInt() + dpToPx(88).toInt(), pointY.toInt() + dpToPx(44).toInt())
                        when {
                            rect.contains(event.x.toInt(), event.y.toInt()) -> {
                                Toast.makeText(context, context.getString(R.string.thank), Toast.LENGTH_SHORT).show()
                                (context as Activity).finish()
                            }
                            else -> {}
                        }
                    }
            }
            else -> {}
        }
        return super.onTouchEvent(event)
    }
```
我们绘制的是矩形，将缓存的坐标list遍历，通过Rect类进行判断即可确定是否在绘制范围内。

#### 4.使用方法
可通过xml布局文件，或者通过addview方法加入。
