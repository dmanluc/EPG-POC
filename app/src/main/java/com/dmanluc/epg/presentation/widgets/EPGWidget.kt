package com.dmanluc.epg.presentation.widgets

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.DEFAULT
import android.graphics.Typeface.SANS_SERIF
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.text.TextPaint
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
import com.dmanluc.epg.app.drawMultilineText
import com.dmanluc.epg.app.px
import com.dmanluc.epg.app.toTimeFormat
import com.dmanluc.epg.domain.entity.Channel
import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.domain.entity.Schedule
import java.text.DecimalFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    30/7/18.
 */
class EPGWidget : ViewGroup {

    companion object {
        const val DAYS_MAX_HORIZONTAL_SCROLL_IN_MILLIS = 24 * 60 * 60 * 1000   // 1 day (API Data)
        const val HOURS_TIMELINE_IN_MILLIS: Int = 1 * 60 * 60 * 1000           // 1 hour
        const val TIME_SPACING_TIMELINE_IN_MILLIS: Int = 15 * 60 * 1000        // 15 minutes
        const val TIME_SCROLL_TO_CURRENT_TIME_IN_MILLIS: Int = 1000            // 1000 ms
        const val HEIGHT_WEEKDAY_BAR_DP = 48
        const val HEIGHT_NOW_BUTTON_DP = 52
        const val WIDTH_NOW_BUTTON_DP = 76
    }

    private val drawingRect = Rect()
    private val measuringRect = Rect()
    private val clipRect = Rect()
    private val paint = Paint().apply { typeface = Typeface.DEFAULT }
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

    private var epgData: EPG? = null
    private var epgClickListener: EPGClickListener? = null

    private var epgBackground: Int = 0
    private var channelMargin: Int = 0
    private var channelPadding: Int = 0
    private var channelHeight: Int = 0
    private var channelWidth: Int = 0
    private var channelBackground: Int = 0
    private var channelEventBackground: Int = 0
    private var channelCurrentEventBackground: Int = 0
    private var channelEventTitleColor: Int = 0
    private var channelEventScheduleColor: Int = 0
    private var channelEventTextSize: Int = 0
    private var timeBarLineWidth: Int = 0
    private var timeBarLineColor: Int = 0
    private var timeBarHeight: Int = 0
    private var timeBarTextSize: Int = 0
    private var nowButtonMargin: Int = 0
    private var nowButtonText: String = ""
    private var nowButtonHeight: Int = 0
    private var nowButtonWidth: Int = 0
    private var weekDayBarHeight: Int = 0

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
                channelEventTitleColor = getColor(R.styleable.EPG_epgChannelEventTitleColor,
                                                  ContextCompat.getColor(context, R.color.epgChannelEventTitleColor))
                channelEventScheduleColor = getColor(R.styleable.EPG_epgChannelEventScheduleColor,
                                                     ContextCompat.getColor(context,
                                                                            R.color.epgChannelEventScheduleColor))
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
                nowButtonMargin = getDimensionPixelSize(R.styleable.EPG_epgNowButtonMargin,
                                                        resources.getDimensionPixelSize(
                                                                R.dimen.margin_epg_nowButton_16dp))
                nowButtonText = resources.getString(
                        getResourceId(R.styleable.EPG_epgNowButtonText, R.string.now_button_text))

                nowButtonHeight = HEIGHT_NOW_BUTTON_DP.px()
                nowButtonWidth = WIDTH_NOW_BUTTON_DP.px()

                weekDayBarHeight = getDimensionPixelSize(HEIGHT_WEEKDAY_BAR_DP, resources.getDimensionPixelSize(
                        R.dimen.height_epg_timeBar_48dp))
            }
        } finally {
            attributes.recycle()
        }

        scroller = Scroller(context)
        scroller.setFriction(.2f)

        gestureDetector = GestureDetector(context, OnGestureListener())

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
                    drawChannelListItems(it, drawingRect)
                    drawSchedules(it, drawingRect)
                    drawTimeBar(it, drawingRect)
                    drawWeekDayBar(it, drawingRect)
                    drawTimeLine(it, drawingRect)
                    drawNowButton(it)
                    drawStarItem(it, drawingRect)
                }

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

    fun setEPGClickListener(listener: EPGClickListener) {
        this.epgClickListener = listener
    }

    fun resetView(withAnimation: Boolean) {
        epgData?.let {
            if (it.channels.isNotEmpty()) {
                initializeBoundaries()

                calculateMaxVerticalScroll()
                calculateMaxHorizontalScroll()

                scroller.startScroll(scrollX, scrollY,
                                     calculateHorizontalCoordinateAtHalfTimeLine() - scrollX,
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
            drawChannelItemsBottomStroke(pos, canvas, drawingRect)
        }

        drawChannelItemsRightStroke(canvas, drawingRect)
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

    private fun drawChannelItemsRightStroke(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX + channelWidth
        drawingRect.top = scrollY + weekDayBarHeight + timeBarHeight
        drawingRect.right = drawingRect.left + channelMargin
        drawingRect.bottom = height + scrollY

        paint.color = channelCurrentEventBackground
        canvas.drawRect(drawingRect, paint)
    }

    private fun drawChannelItemsBottomStroke(channelPosition: Int, canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX
        drawingRect.top = calculateVerticalCoordinateFromChannelPosition(channelPosition)
        drawingRect.right = drawingRect.left + channelWidth
        drawingRect.bottom = drawingRect.top + channelMargin

        paint.color = channelCurrentEventBackground
        canvas.drawRect(drawingRect, paint)
    }

    private fun drawSchedules(canvas: Canvas, drawingRect: Rect) {
        val firstPos = getFirstVisibleChannelPosition()
        val lastPos = getLastVisibleChannelPosition()

        for (pos in firstPos..lastPos) {
            clipRect.left = scrollX + channelWidth + channelMargin
            clipRect.top = calculateVerticalCoordinateFromChannelPosition(pos)
            clipRect.right = scrollX + width
            clipRect.bottom = clipRect.top + channelHeight

            canvas.save()
            canvas.clipRect(clipRect)

            var foundFirstVisibleChannelSchedule = false

            val channelSchedules = epgData?.channels?.get(pos)?.schedules ?: emptyList()

            for (schedule in channelSchedules) {
                if (isScheduleVisible(schedule.startTime, schedule.endTime)) {
                    drawChannelSchedules(canvas, pos, schedule, drawingRect)
                    foundFirstVisibleChannelSchedule = true
                } else if (foundFirstVisibleChannelSchedule) {
                    break
                }
            }
            canvas.restore()
        }
    }

    private fun drawChannelSchedules(canvas: Canvas, channelPosition: Int, schedule: Schedule, drawingRect: Rect) {
        setChannelScheduleDrawingRectangle(channelPosition, schedule.startTime, schedule.endTime, drawingRect)

        paint.color = if (schedule.isLive()) channelCurrentEventBackground else channelEventBackground
        canvas.drawRect(drawingRect, paint)

        drawingRect.left += channelPadding
        drawingRect.right -= channelPadding

        paint.isAntiAlias = true
        paint.textSize = channelEventTextSize.toFloat()

        val title = "${schedule.title}\n${schedule.timeInPrettyFormat}"
        drawChannelScheduleInfo(canvas, paint, title, drawingRect.left, drawingRect.top + 3 * channelPadding,
                                measuringRect)
    }

    private fun setChannelScheduleDrawingRectangle(channelPosition: Int, start: Long, end: Long, drawingRect: Rect) {
        drawingRect.left = calculateHorizontalCoordinateFromTime(start)
        drawingRect.top = calculateVerticalCoordinateFromChannelPosition(channelPosition) + channelMargin
        drawingRect.right = calculateHorizontalCoordinateFromTime(end) - channelMargin
        drawingRect.bottom = drawingRect.top + channelHeight + channelMargin
    }

    private fun drawChannelScheduleInfo(canvas: Canvas, paint: Paint, scheduleInfo: String, x: Int, y: Int,
                                        measuringRect: Rect) {
        val lines = scheduleInfo.split(regex = "\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        var yoff = 0

        for (i in lines.indices) {
            if (i == 1) {
                paint.typeface = Typeface.DEFAULT
                paint.color = channelEventScheduleColor
            } else {
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.color = channelEventTitleColor
            }
            canvas.drawText(lines[i], x.toFloat(), (y + yoff).toFloat(), paint)
            paint.getTextBounds(lines[i], 0, lines[i].length, measuringRect)
            yoff += measuringRect.height() + channelPadding
        }
    }

    private fun drawTimeBar(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX
        drawingRect.top = scrollY + weekDayBarHeight
        drawingRect.right = drawingRect.left + width
        drawingRect.bottom = drawingRect.top + timeBarHeight

        paint.color = channelBackground
        canvas.drawRect(drawingRect, paint)

        paint.color = channelEventTitleColor
        paint.textSize = timeBarTextSize.toFloat()
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        for (i in 0..HOURS_TIMELINE_IN_MILLIS / TIME_SPACING_TIMELINE_IN_MILLIS) {
            val time = TIME_SPACING_TIMELINE_IN_MILLIS * ((lowerTimeBound + TIME_SPACING_TIMELINE_IN_MILLIS * i) / TIME_SPACING_TIMELINE_IN_MILLIS)

            paint.color = channelEventTitleColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText(time.toTimeFormat("HH:mm"),
                            calculateHorizontalCoordinateFromTime(time).toFloat() - paint.measureText(
                                    time.toTimeFormat("HH:mm")) / 2,
                            (drawingRect.top + ((drawingRect.bottom - drawingRect.top) / 2 + timeBarTextSize / 2)).toFloat(),
                            paint)

            paint.color = channelCurrentEventBackground
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawRect(calculateHorizontalCoordinateFromTime(time).toFloat() - channelMargin,
                            drawingRect.bottom - 20f,
                            calculateHorizontalCoordinateFromTime(time).toFloat(),
                            drawingRect.bottom.toFloat(), paint)

        }

        drawTimeBarBottomStroke(canvas, drawingRect)
    }

    private fun drawTimeBarBottomStroke(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX
        drawingRect.top = scrollY + timeBarHeight + weekDayBarHeight
        drawingRect.right = drawingRect.left + width
        drawingRect.bottom = drawingRect.top + channelMargin

        paint.color = channelCurrentEventBackground
        canvas.drawRect(drawingRect, paint)
    }

    private fun drawWeekDayBar(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX + channelWidth
        drawingRect.top = scrollY
        drawingRect.right = scrollX + width
        drawingRect.bottom = drawingRect.top + weekDayBarHeight

        paint.color = channelBackground
        canvas.drawRect(drawingRect, paint)

        paint.color = channelEventTitleColor
        paint.typeface = Typeface.create(SANS_SERIF, BOLD)

        val days = calculateCurrentWeekDaysInPrettyFormat()

        val itemWidth = (drawingRect.width() / days.size).toFloat()

        days.mapIndexed { index, weekDayString ->
            drawWeekDayBarItem(weekDayString.first, weekDayString.second, index, drawingRect, itemWidth, canvas)
        }

        paint.typeface = DEFAULT
        drawWeekDayBarBottomStroke(canvas, drawingRect)
    }

    private fun drawWeekDayBarItem(weekDayString: String, isCurrentDay: Boolean, index: Int, drawingRect: Rect,
                                   itemWidth: Float, canvas: Canvas) {
        val newDrawingRect = drawingRect

        if (isCurrentDay) paint.color = channelEventTitleColor else paint.color = channelCurrentEventBackground

        canvas.drawMultilineText(weekDayString, TextPaint(paint), itemWidth.toInt(),
                                 (newDrawingRect.left + channelPadding + (itemWidth * index)),
                                 newDrawingRect.top.toFloat() + channelPadding,
                                 alignment = Layout.Alignment.ALIGN_CENTER)
    }

    private fun drawWeekDayBarBottomStroke(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX
        drawingRect.top = scrollY + weekDayBarHeight - channelMargin
        drawingRect.right = drawingRect.left + width
        drawingRect.bottom = drawingRect.top + channelMargin

        paint.color = channelCurrentEventBackground
        canvas.drawRect(drawingRect, paint)
    }

    private fun drawTimeLine(canvas: Canvas, drawingRect: Rect) {
        val now = System.currentTimeMillis()

        if (shouldTimeLineUpdate(now)) {
            drawingRect.left = calculateHorizontalCoordinateFromTime(now) - timeBarLineWidth - channelMargin
            drawingRect.top = scrollY + weekDayBarHeight + channelMargin
            drawingRect.right = drawingRect.left + 3 * timeBarLineWidth
            drawingRect.bottom = drawingRect.top + timeBarHeight - channelMargin

            paint.color = timeBarLineColor
            canvas.drawRect(drawingRect, paint)

            drawingRect.left = calculateHorizontalCoordinateFromTime(now) - channelMargin
            drawingRect.top = drawingRect.bottom
            drawingRect.right = drawingRect.left + timeBarLineWidth
            drawingRect.bottom = height + scrollY
            canvas.drawRect(drawingRect, paint)
        }
    }

    private fun drawNowButton(canvas: Canvas) {
        if (shouldNowButtonBeVisible()) {
            val newDrawingRect = calculateNowButtonClickArea()

            paint.color = timeBarLineColor
            canvas.drawRoundRect(RectF(newDrawingRect), 12f, 12f, paint)

            paint.color = channelEventTitleColor
            paint.typeface = Typeface.DEFAULT_BOLD

            val textBounds = Rect()
            paint.getTextBounds(nowButtonText, 0, nowButtonText.length - 1, textBounds)
            canvas.drawMultilineText(nowButtonText,
                                     TextPaint(paint),
                                     newDrawingRect.right - newDrawingRect.left,
                                     newDrawingRect.left.toFloat(),
                                     newDrawingRect.top.toFloat() + ((newDrawingRect.height() - 2 * textBounds.height()) / 2),
                                     alignment = Layout.Alignment.ALIGN_CENTER)
        }
    }

    private fun shouldNowButtonBeVisible() = abs(
            calculateHorizontalCoordinateAtHalfTimeLine() - scrollX) > (width / 3).toLong()

    private fun drawStarItem(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX
        drawingRect.top = scrollY
        drawingRect.right = drawingRect.left + channelWidth
        drawingRect.bottom = drawingRect.top + weekDayBarHeight - channelMargin

        val starDrawable = ContextCompat.getDrawable(context, R.drawable.ic_star)

        paint.color = channelBackground
        canvas.drawRect(drawingRect, paint)

        val drawableRect = drawingRect
        drawableRect.left += (2.75 * channelPadding).toInt()
        drawableRect.top += (1.25 * channelPadding).toInt()
        drawableRect.right -= (2.75 * channelPadding).toInt()
        drawableRect.bottom -= (1.25 * channelPadding).toInt()

        starDrawable?.setBounds(drawableRect.left, drawableRect.top, drawableRect.right, drawableRect.bottom)
        starDrawable?.draw(canvas)

        drawStarItemRightStroke(canvas, drawingRect)
    }

    private fun drawStarItemRightStroke(canvas: Canvas, drawingRect: Rect) {
        drawingRect.left = scrollX + channelWidth
        drawingRect.top = scrollY
        drawingRect.right = drawingRect.left + channelMargin
        drawingRect.bottom = weekDayBarHeight + scrollY

        paint.color = channelCurrentEventBackground
        canvas.drawRect(drawingRect, paint)
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
        return (((currentTimeInMillis - timeOffset) / millisPerPixel) + channelWidth + channelMargin).toInt()
    }

    private fun calculateVerticalCoordinateFromChannelPosition(position: Int): Int {
        return position * (channelHeight + channelMargin) + timeBarHeight + weekDayBarHeight
    }

    private fun calculateHorizontalCoordinateAtHalfTimeLine(): Int {
        val coordinate = calculateHorizontalCoordinateFromTime(
                System.currentTimeMillis() - (HOURS_TIMELINE_IN_MILLIS / 2) - ((channelWidth + 2 * channelMargin) * millisPerPixel))
        val coordinateLimit = calculateHorizontalCoordinateFromTime(timeOffset)
        return if (coordinateLimit >= coordinate) {
            coordinateLimit - channelWidth - channelMargin
        } else {
            coordinate
        }
    }

    private fun shouldTimeLineUpdate(currentTimeInMillis: Long): Boolean {
        return currentTimeInMillis in lowerTimeBound..upperTimeBound
    }

    private fun getFirstVisibleChannelPosition(): Int {
        val position = (scrollY - channelMargin - timeBarHeight - weekDayBarHeight) / (channelHeight + channelMargin)
        return if (position < 0) 0 else position
    }

    private fun getLastVisibleChannelPosition(): Int {
        val channelsCount = epgData?.channels?.size ?: 0
        var position = (scrollY + height + timeBarHeight + weekDayBarHeight - channelMargin) / (channelHeight + channelMargin)

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

    private fun calculateNowButtonClickArea(): Rect {
        measuringRect.left = scrollX + width - nowButtonWidth - nowButtonMargin
        measuringRect.top = scrollY + height - nowButtonHeight - nowButtonMargin
        measuringRect.right = measuringRect.left + nowButtonWidth
        measuringRect.bottom = measuringRect.top + nowButtonHeight
        return measuringRect
    }

    private fun calculateCurrentWeekDaysInPrettyFormat(): List<Pair<String, Boolean>> {
        val numberOfDaysToShow = 5

        val centralElement = (numberOfDaysToShow - 1) / 2

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -((numberOfDaysToShow - 1) / 2))
        }

        val weekDays = mutableListOf<Pair<String, Boolean>>()
        val decimalFormat = DecimalFormat("00")

        with(calendar) {
            for (dayIndexInWeek in 0 until numberOfDaysToShow) {
                weekDays.add(dayIndexInWeek,
                             Pair(getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.UK).capitalize()
                                  + "\n" + decimalFormat.format(get(Calendar.DAY_OF_MONTH)) + ".${decimalFormat.format(
                                     get(Calendar.MONTH) + 1)}.", dayIndexInWeek == centralElement))
                add(Calendar.DATE, 1)
            }
        }

        return weekDays
    }

    private inner class OnGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent): Boolean {

            val x = e.x.toInt()
            val y = e.y.toInt()

            val scrollX = scrollX + x
            val scrollY = scrollY + y

            epgClickListener?.let {
                if (calculateNowButtonClickArea().contains(scrollX, scrollY) && shouldNowButtonBeVisible()) {
                    it.onNowButtonClicked()
                }
            }

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

    interface EPGClickListener {

        fun onChannelClicked(channelPosition: Int, epgChannel: Channel)

        fun onEventClicked(channelPosition: Int, programPosition: Int, epgEvent: Schedule)

        fun onNowButtonClicked()
    }

}