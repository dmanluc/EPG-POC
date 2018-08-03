package com.dmanluc.epg.presentation.widgets

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Scroller
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.dmanluc.epg.R
import com.dmanluc.epg.app.GlideApp
import com.dmanluc.epg.app.toTimeFormat
import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.domain.entity.Schedule
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.min

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    30/7/18.
 */
class EPGWidget : ViewGroup {

    companion object {
        const val DAYS_MAX_HORIZONTAL_SCROLL_IN_MILLIS = 24 * 60 * 60 * 1000
        const val HOURS_TIMELINE_IN_MILLIS: Int = 1 * 60 * 60 * 1000           // 1 hour
        const val TIME_SPACING_TIMELINE_IN_MILLIS: Int = 20 * 60 * 1000        // 15 minutes
        const val TIME_SCROLL_TO_CURRENT_TIME_IN_MILLIS: Int = 500              // 500 ms
    }

    private var epgData: EPG? = null

    private var drawingRect = Rect()
    private val measuringRect = Rect()
    private val clipRect = Rect()
    private val paint = Paint()
    private val channelImageCache: MutableMap<String, Bitmap> = HashMap()
    private val channelImageTargetCache: MutableMap<String, Target<Bitmap>> = HashMap()

    private val tickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action?.compareTo(Intent.ACTION_TIME_TICK) == 0) {
                reDraw()
            }
        }
    }

    private lateinit var scroller: Scroller
    private lateinit var gestureDetector: GestureDetector

    private var epgBackground: Int = 0
    private var channelMargin: Int = 0
    private var channelPadding: Int = 0
    private var channelHeight: Int = 0
    private var channelWidth: Int = 0
    private var channelBackground: Int = 0
    private var channelEventBackground: Int = 0
    private var channelCurrentEventBackground: Int = 0
    private var channelEventTextColor: Int = 0
    private var channelEventTextSize: Int = 0
    private var timeBarLineWidth: Int = 0
    private var timeBarLineColor: Int = 0
    private var timeBarHeight: Int = 0
    private var timeBarTextSize: Int = 0
    private var nowButtonSize: Int = 0
    private var nowButtonMargin: Int = 0

    private var nowButtonIcon: Bitmap? = null

    private var maxHorizontalScroll: Int = 0
    private var maxVerticalScroll: Int = 0
    private var millisPerPixel: Long = 0
    private var timeOffset: Long = 0
    private var lowerTimeBound: Long = 0
    private var upperTimeBound: Long = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeView(context)
    }

    private fun initializeView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        setWillNotDraw(false)

        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.EPG, 0, defStyleAttr)

        try {
            attributes.apply {
                epgBackground = getColor(R.styleable.EPG_epgBackgroundColor,
                                         ContextCompat.getColor(context, R.color.epgBackgroundColor))
                channelMargin = getDimensionPixelSize(R.styleable.EPG_epgChannelMargin,
                                                      resources.getDimensionPixelSize(R.dimen.margin_epg_channel_1dp))
                channelPadding = getDimensionPixelSize(R.styleable.EPG_epgChannelPadding,
                                                       resources.getDimensionPixelSize(R.dimen.padding_epg_channel_8dp))
                channelWidth = getDimensionPixelSize(R.styleable.EPG_epgChannelWidth,
                                                     resources.getDimensionPixelSize(R.dimen.width_epg_channel_70dp))
                channelHeight = getDimensionPixelSize(R.styleable.EPG_epgChannelHeight,
                                                      resources.getDimensionPixelSize(R.dimen.height_epg_channel_70dp))
                channelBackground = getColor(R.styleable.EPG_epgChannelBackgroundColor,
                                             ContextCompat.getColor(context, R.color.epgChannelBackgroundColor))
                channelEventBackground = getColor(R.styleable.EPG_epgChannelEventBackgroundColor,
                                                  ContextCompat.getColor(context,
                                                                         R.color.epgChannelEventBackgroundColor))
                channelCurrentEventBackground = getColor(R.styleable.EPG_epgChannelCurrentEventBackgroundColor,
                                                         ContextCompat.getColor(context,
                                                                                R.color.epgCurrentChannelEventBackgroundColor))
                channelEventTextColor = getColor(R.styleable.EPG_epgChannelEventTextColor,
                                                 ContextCompat.getColor(context, R.color.epgChannelEventTextColor))
                channelEventTextSize = getDimensionPixelSize(R.styleable.EPG_epgChannelEventTextSize,
                                                             resources.getDimensionPixelSize(
                                                                     R.dimen.textSize_epg_event_14sp))
                timeBarHeight = getResourceId(R.styleable.EPG_epgTimeBarHeight,
                                              resources.getDimensionPixelSize(R.dimen.height_epg_timeBar_48dp))
                timeBarLineWidth = getResourceId(R.styleable.EPG_epgTimeBarLineWidth,
                                                 resources.getDimensionPixelSize(R.dimen.width_epg_timeBarLine_1dp))
                timeBarLineColor = getColor(R.styleable.EPG_epgTimeBarLineColor,
                                            ContextCompat.getColor(context, R.color.epgCurrentTimeBarColor))
                timeBarTextSize = getResourceId(R.styleable.EPG_epgTimeBarTextSize,
                                                resources.getDimensionPixelSize(R.dimen.textSize_epg_timeBar_14sp))
                nowButtonSize = getDimensionPixelSize(R.styleable.EPG_epgNowButtonSize,
                                                      resources.getDimensionPixelSize(R.dimen.size_epg_nowButton_40dp))
                nowButtonMargin = getDimensionPixelSize(R.styleable.EPG_epgNowButtonMargin,
                                                        resources.getDimensionPixelSize(
                                                                R.dimen.margin_epg_nowButton_10dp))
            }
        } finally {
            attributes.recycle()
        }

        scroller = Scroller(context)
        scroller.setFriction(.2f)

        gestureDetector = GestureDetector(context, OnGestureListener())

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.outWidth = nowButtonSize
        bitmapOptions.outHeight = nowButtonSize

        // TODO Update correct drawable
        nowButtonIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground, bitmapOptions)

        initializeBoundaries()
        initializeMinuteTickChangeBroadcastReceiver(context)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetView(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {}

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        epgData?.let {
            if (it.channels.isNotEmpty()) {

                lowerTimeBound = calculateTimeFromHorizontalCoordinate(scrollX)
                upperTimeBound = calculateTimeFromHorizontalCoordinate(scrollX + width)

                val drawingRect = drawingRect
                drawingRect.left = scrollX
                drawingRect.top = scrollY
                drawingRect.right = drawingRect.left + width
                drawingRect.bottom = drawingRect.top + height

                canvas?.let {
                    drawChannelListItems(canvas, drawingRect)
                    drawSchedules(canvas, drawingRect)
                    drawTimeBar(canvas, drawingRect)
                    drawTimeLine(canvas, drawingRect)
                    //drawResetButton(canvas, drawingRect)
                }

                // If scroller is scrolling/animating do scroll. This applies when doing a fling.
                if (scroller.computeScrollOffset()) {
                    scrollTo(scroller.currX, scroller.currY)
                }
            }


        }

    }

    override fun onDetachedFromWindow() {
        context.unregisterReceiver(tickReceiver)
        super.onDetachedFromWindow()
    }

    fun setEPGData(epgData: EPG) {
        this.epgData = epgData
        resetView(false)
    }

    /**
     * This will recalculate boundaries, maximal scroll and scroll to start position which is current time.
     * To be used on device rotation etc since the device height and width will change.
     * @param withAnimation true if scroll to current position should be animated.
     */
    private fun resetView(withAnimation: Boolean) {
        epgData?.let {
            if (it.channels.isNotEmpty()) {
                initializeBoundaries()

                calculateMaxVerticalScroll()
                calculateMaxHorizontalScroll()

                scroller.startScroll(scrollX, scrollY,
                                     calculateHorizontalCoordinateAtVisibleTimeLineStartTime() - scrollX,
                                     0, if (withAnimation) TIME_SCROLL_TO_CURRENT_TIME_IN_MILLIS else 0)

                reDraw()
            }
        }
    }

    private fun reDraw() {
        invalidate()
        requestLayout()
    }

    private fun initializeBoundaries() {
        millisPerPixel = calculateMillisPerPixel()
        timeOffset = calculatedBaseLine()
        lowerTimeBound = calculateTimeFromHorizontalCoordinate(0)
        upperTimeBound = calculateTimeFromHorizontalCoordinate(width)
    }

    private fun initializeMinuteTickChangeBroadcastReceiver(context: Context) {

        context.registerReceiver(tickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    private fun drawChannelListItems(canvas: Canvas, drawingRect: Rect) {
        measuringRect.left = scrollX
        measuringRect.top = scrollY
        measuringRect.right = drawingRect.left + channelWidth
        measuringRect.bottom = measuringRect.top + height

        paint.color = channelBackground
        canvas.drawRect(measuringRect, paint)

        val firstPosition = getFirstVisibleChannelPosition()
        val lastPosition = getLastVisibleChannelPosition()

        for (pos in firstPosition..lastPosition) {
            drawChannelItem(canvas, pos)
        }
    }

    private fun drawChannelItem(canvas: Canvas, position: Int) {
        val imageURL = epgData?.channels?.get(position)?.logoUrl

        if (channelImageCache.containsKey(imageURL)) {
            val image = channelImageCache[imageURL]
            image?.let {
                canvas.drawBitmap(it, null, getDrawingRectForChannelImage(position, it), null)
            }
        } else {
            if (!channelImageTargetCache.containsKey(imageURL)) {
                channelImageTargetCache[imageURL.orEmpty()] = object : Target<Bitmap> {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        channelImageCache[imageURL.orEmpty()] = resource
                        reDraw()
                        channelImageTargetCache.remove(imageURL)
                    }

                    override fun onLoadStarted(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                    }

                    override fun getSize(cb: SizeReadyCallback) {
                    }

                    override fun getRequest(): Request? {
                        return null
                    }

                    override fun onStop() {
                    }

                    override fun setRequest(request: Request?) {
                    }

                    override fun removeCallback(cb: SizeReadyCallback) {
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onStart() {
                    }

                    override fun onDestroy() {
                    }
                }

            }

            GlideApp.with(context)
                    .asBitmap()
                    .load(imageURL)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(min(channelWidth, channelHeight))
                    .into(channelImageTargetCache[imageURL] as Target<Bitmap>)

        }
    }

    private fun getDrawingRectForChannelImage(position: Int, image: Bitmap): Rect {
        drawingRect.left = scrollX
        drawingRect.top = calculateVerticalCoordinateFromChannelPosition(position)
        drawingRect.right = drawingRect.left + channelWidth
        drawingRect.bottom = drawingRect.top + channelHeight

        drawingRect.left += channelPadding
        drawingRect.top += channelPadding
        drawingRect.right -= channelPadding
        drawingRect.bottom -= channelPadding

        val imageWidth = image.width
        val imageHeight = image.height
        val imageRatio = imageHeight / imageWidth.toFloat()

        val rectWidth = drawingRect.right - drawingRect.left
        val rectHeight = drawingRect.bottom - drawingRect.top

        // Keep aspect ratio.
        if (imageWidth > imageHeight) {
            val padding = (rectHeight - rectWidth * imageRatio).toInt() / 2
            drawingRect.top += padding
            drawingRect.bottom -= padding
        } else if (imageWidth <= imageHeight) {
            val padding = (rectWidth - rectHeight / imageRatio).toInt() / 2
            drawingRect.left += padding
            drawingRect.right -= padding
        }

        return drawingRect
    }

    private fun drawSchedules(canvas: Canvas, drawingRect: Rect) {
        val firstPos = getFirstVisibleChannelPosition()
        val lastPos = getLastVisibleChannelPosition()

        for (pos in firstPos..lastPos) {

            // Set clip rectangle
            clipRect.left = scrollX + channelWidth + channelMargin
            clipRect.top = calculateVerticalCoordinateFromChannelPosition(pos)
            clipRect.right = scrollX + width
            clipRect.bottom = clipRect.top + channelHeight

            canvas.save()
            canvas.clipRect(clipRect)

            // Draw each event
            var foundFirst = false

            val epgEvents = epgData?.channels?.get(pos)?.schedules ?: emptyList()

            for (schedule in epgEvents) {
                if (isScheduleVisible(schedule.startTime, schedule.endTime)) {
                    drawEvent(canvas, pos, schedule, drawingRect)
                    foundFirst = true
                } else if (foundFirst) {
                    break
                }
            }
            canvas.restore()
        }

    }

    private fun drawEvent(canvas: Canvas, channelPosition: Int, schedule: Schedule, drawingRect: Rect) {
        setEventDrawingRectangle(channelPosition, schedule.startTime, schedule.endTime, drawingRect)

        // Background
        paint.color = if (schedule.isLive()) channelCurrentEventBackground else channelEventBackground
        canvas.drawRect(drawingRect, paint)

        // Add left and right inner padding
        drawingRect.left += channelPadding
        drawingRect.right -= channelPadding

        // Text
        paint.isAntiAlias = true
        paint.color = channelEventTextColor
        paint.textSize = channelEventTextSize.toFloat()

        val title = "${schedule.title}\n${schedule.timeInPrettyFormat}"
        drawString(canvas, paint, title, drawingRect.left, drawingRect.top + 3*channelPadding, measuringRect)
    }

    private fun setEventDrawingRectangle(channelPosition: Int, start: Long, end: Long, drawingRect: Rect) {
        drawingRect.left = calculateHorizontalCoordinateFromTime(start)
        drawingRect.top = calculateVerticalCoordinateFromChannelPosition(channelPosition)
        drawingRect.right = calculateHorizontalCoordinateFromTime(end) - channelMargin
        drawingRect.bottom = drawingRect.top + channelHeight
    }

    private fun drawString(canvas: Canvas, paint: Paint, str: String, x: Int, y: Int, measuringRect: Rect) {
        val lines = str.split(regex = "\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        var yoff = 0

        for (i in lines.indices) {
            if (i == 1) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            } else {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(lines[i], x.toFloat(), (y + yoff).toFloat(), paint)
            paint.getTextBounds(lines[i], 0, lines[i].length, measuringRect)
            yoff += measuringRect.height() + channelPadding
        }
    }

    private fun drawTimeBarBottomStroke(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX
        drawingRect.top = scrollY + timeBarHeight
        drawingRect.right = drawingRect.left + width
        drawingRect.bottom = drawingRect.top + channelMargin

        // Bottom stroke
        paint.color = channelCurrentEventBackground
        canvas.drawRect(drawingRect, paint)
    }

    private fun drawTimeBar(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX
        drawingRect.top = scrollY
        drawingRect.right = drawingRect.left + width
        drawingRect.bottom = drawingRect.top + timeBarHeight

        clipRect.left = scrollX
        clipRect.top = scrollY
        clipRect.right = scrollX + width
        clipRect.bottom = clipRect.top + timeBarHeight

        canvas.save()
        canvas.clipRect(clipRect)

        // Background
        paint.color = channelBackground
        canvas.drawRect(drawingRect, paint)

        // Time stamps
        paint.color = channelEventTextColor
        paint.textSize = timeBarTextSize.toFloat()
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        for (i in 0..HOURS_TIMELINE_IN_MILLIS / TIME_SPACING_TIMELINE_IN_MILLIS) {
            // Get time and round to nearest half hour
            val time = TIME_SPACING_TIMELINE_IN_MILLIS * ((lowerTimeBound + TIME_SPACING_TIMELINE_IN_MILLIS * i + TIME_SPACING_TIMELINE_IN_MILLIS / 2) / TIME_SPACING_TIMELINE_IN_MILLIS)

            paint.color = channelEventTextColor

            canvas.drawText(time.toTimeFormat("HH:mm"),
                            calculateHorizontalCoordinateFromTime(time).toFloat() - paint.measureText(
                                    time.toTimeFormat("HH:mm")) / 2,
                            (drawingRect.top + ((drawingRect.bottom - drawingRect.top) / 2 + timeBarTextSize / 2)).toFloat(),
                            paint)

            paint.color = channelCurrentEventBackground

            canvas.drawLine(calculateHorizontalCoordinateFromTime(time).toFloat(),
                            drawingRect.bottom - 20f,
                            calculateHorizontalCoordinateFromTime(time).toFloat(),
                            drawingRect.bottom.toFloat(), paint)

        }

        canvas.restore()

        //drawTimebarDayIndicator(canvas, drawingRect)
        drawTimeBarBottomStroke(canvas, drawingRect)
    }

    //    private fun drawTimebarDayIndicator(canvas: Canvas, drawingRect: Rect) {
    //        drawingRect.left = scrollX
    //        drawingRect.top = scrollY
    //        drawingRect.right = drawingRect.left + mChannelLayoutWidth
    //        drawingRect.bottom = drawingRect.top + mTimeBarHeight
    //
    //        // Background
    //        paint.setColor(mChannelLayoutBackground)
    //        canvas.drawRect(drawingRect, mPaint)
    //
    //        // Text
    //        paint.setColor(mEventLayoutTextColor)
    //        paint.setTextSize(mTimeBarTextSize)
    //        paint.setTextAlign(Paint.Align.CENTER)
    //        canvas.drawText(EPGUtil.getWeekdayName(mTimeLowerBoundary),
    //                        (drawingRect.left + (drawingRect.right - drawingRect.left) / 2).toFloat(),
    //                        drawingRect.top + ((drawingRect.bottom - drawingRect.top) / 2 + mTimeBarTextSize / 2), paint)
    //
    //        paint.setTextAlign(Paint.Align.LEFT)
    //    }

    private fun drawTimeLine(canvas: Canvas, drawingRect: Rect) {
        val now = System.currentTimeMillis()

        if (shouldTimeLineUpdate(now)) {
            drawingRect.left = calculateHorizontalCoordinateFromTime(now) - timeBarLineWidth
            drawingRect.top = scrollY
            drawingRect.right = drawingRect.left + 3 * timeBarLineWidth
            drawingRect.bottom = drawingRect.top + timeBarHeight + channelMargin

            paint.color = timeBarLineColor
            canvas.drawRect(drawingRect, paint)

            drawingRect.left = calculateHorizontalCoordinateFromTime(now)
            drawingRect.top = drawingRect.bottom
            drawingRect.right = drawingRect.left + timeBarLineWidth
            drawingRect.bottom = height
            canvas.drawRect(drawingRect, paint)
        }
    }

    private fun calculateMillisPerPixel(): Long {
        return (HOURS_TIMELINE_IN_MILLIS / (resources.displayMetrics.widthPixels - channelWidth - channelMargin)).toLong()
    }

    private fun calculatedBaseLine(): Long {
        return GregorianCalendar().let {
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
            it.timeInMillis
        }
    }

    private fun calculateMaxHorizontalScroll() {
        maxHorizontalScroll = ((DAYS_MAX_HORIZONTAL_SCROLL_IN_MILLIS - HOURS_TIMELINE_IN_MILLIS) / millisPerPixel).toInt()
    }

    private fun calculateMaxVerticalScroll() {
        maxVerticalScroll = (calculateVerticalCoordinateFromChannelPosition(
                (epgData?.channels?.size ?: 0) - 2) + channelHeight).let {
            if (it < height) 0 else it - height
        }
    }

    private fun calculateTimeFromHorizontalCoordinate(xPixelCoordinate: Int): Long {
        return (xPixelCoordinate * millisPerPixel) + timeOffset
    }

    private fun calculateHorizontalCoordinateFromTime(currentTimeInMillis: Long): Int {
        return (((currentTimeInMillis - timeOffset) / millisPerPixel) + 2 * channelMargin + channelWidth).toInt()
    }

    private fun calculateVerticalCoordinateFromChannelPosition(position: Int): Int {
        return position * (channelHeight + 2 * channelMargin) + timeBarHeight
    }

    private fun calculateHorizontalCoordinateAtVisibleTimeLineStartTime(): Int {
        return calculateHorizontalCoordinateFromTime(System.currentTimeMillis() - HOURS_TIMELINE_IN_MILLIS / 2)
    }

    private fun shouldTimeLineUpdate(currentTimeInMillis: Long): Boolean {
        return currentTimeInMillis in lowerTimeBound..upperTimeBound
    }

    private fun getFirstVisibleChannelPosition(): Int {
        val position = (scrollY - channelMargin - timeBarHeight) / (channelHeight + channelMargin)
        return if (position < 0) 0 else position
    }

    private fun getLastVisibleChannelPosition(): Int {
        val channelsCount = epgData?.channels?.size ?: 0
        var position = (scrollY + height + timeBarHeight - channelMargin) / (channelHeight + channelMargin)

        if (position > channelsCount - 1) {
            position = channelsCount - 1
        }

        // Add one extra row if we don't fill screen with current channel position
        return if (scaleY + height > position * channelHeight && position < channelsCount - 1) position + 1 else position
    }

    private fun isScheduleVisible(start: Long, end: Long): Boolean {
        return (start in lowerTimeBound..upperTimeBound
                || end in lowerTimeBound..upperTimeBound
                || start <= lowerTimeBound && end >= upperTimeBound)
    }

    private inner class OnGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent): Boolean {

            // This is absolute coordinate on screen not taking scroll into account.
            val x = e.x.toInt()
            val y = e.y.toInt()

            // Adding scroll to clicked coordinate
            val scrollX = scrollX + x
            val scrollY = scrollY + y

            //            val channelPosition = getChannelPosition(scrollY)
            //            if (channelPosition != -1 && mClickListener != null) {
            //                if (calculateResetButtonHitArea().contains(scrollX, scrollY)) {
            //                    // Reset button clicked
            //                    mClickListener.onResetButtonClicked()
            //                } else if (calculateChannelsHitArea().contains(x, y)) {
            //                    // Channel area is clicked
            //                    mClickListener.onChannelClicked(channelPosition, epgData.getChannel(channelPosition))
            //                } else if (calculateProgramsHitArea().contains(x, y)) {
            //                    // Event area is clicked
            //                    val programPosition = getProgramPosition(channelPosition, getTimeFrom(
            //                            getScrollX() + x - calculateProgramsHitArea().left))
            //                    if (programPosition != -1) {
            //                        mClickListener.onEventClicked(channelPosition, programPosition,
            //                                                      epgData.getEvent(channelPosition, programPosition))
            //                    }
            //                }
            //            }

            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent,
                              distanceX: Float, distanceY: Float): Boolean {
            var dx = distanceX.toInt()
            var dy = distanceY.toInt()
            val x = scrollX
            val y = scrollY


            // Avoid over scrolling
            if (x + dx < 0) {
                dx = 0 - x
            }
            if (y + dy < 0) {
                dy = 0 - y
            }
            if (x + dx > maxHorizontalScroll) {
                dx = maxHorizontalScroll - x
            }
            if (y + dy > maxVerticalScroll) {
                dy = maxVerticalScroll - y
            }

            scrollBy(dx, dy)
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent,
                             vX: Float, vY: Float): Boolean {

            scroller.fling(scrollX, scrollY, -vX.toInt(),
                           -vY.toInt(), 0, maxHorizontalScroll, 0, maxVerticalScroll)

            reDraw()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            if (!scroller.isFinished) {
                scroller.forceFinished(true)
                return true
            }
            return true
        }
    }

}