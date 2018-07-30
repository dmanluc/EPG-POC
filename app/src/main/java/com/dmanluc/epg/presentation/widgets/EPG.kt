package com.dmanluc.epg.presentation.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Scroller
import com.dmanluc.epg.R


/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    30/7/18.
 */
class EPG : ViewGroup {

    private val drawingRect = Rect()
    private val measuringRect = Rect()
    private val clipRect = Rect()
    private val paint = Paint()

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
                epgBackground = getResourceId(R.styleable.EPG_epgBackgroundColor, R.color.epgBackgroundColor)
                channelMargin = getResourceId(R.styleable.EPG_epgChannelMargin, R.dimen.margin_epg_channel_3dp)
                channelPadding = getResourceId(R.styleable.EPG_epgChannelPadding, R.dimen.padding_epg_channel_8dp)
                channelWidth = getResourceId(R.styleable.EPG_epgChannelWidth, R.dimen.width_epg_channel_70dp)
                channelHeight = getResourceId(R.styleable.EPG_epgChannelHeight, R.dimen.height_epg_channel_70dp)
                channelBackground = getResourceId(R.styleable.EPG_epgChannelBackgroundColor,
                                                  R.color.epgChannelBackgroundColor)
                channelEventBackground = getResourceId(R.styleable.EPG_epgChannelEventBackgroundColor,
                                                       R.color.epgChannelEventBackgroundColor)
                channelCurrentEventBackground = getResourceId(R.styleable.EPG_epgChannelCurrentEventBackgroundColor,
                                                              R.color.epgCurrentChannelEventBackgroundColor)
                channelEventTextColor = getResourceId(R.styleable.EPG_epgChannelEventTextColor,
                                                      R.color.epgChannelEventTextColor)
                channelEventTextSize = getResourceId(R.styleable.EPG_epgChannelEventTextSize,
                                                     R.dimen.textSize_epg_event_20sp)
                timeBarHeight = getResourceId(R.styleable.EPG_epgTimeBarHeight, R.dimen.height_epg_timeBar_30dp)
                timeBarLineWidth = getResourceId(R.styleable.EPG_epgTimeBarLineWidth, R.dimen.width_epg_timeBarLine_2dp)
                timeBarLineColor = getResourceId(R.styleable.EPG_epgTimeBarLineColor, R.color.epgCurrentTimeBarColor)
                timeBarTextSize = getResourceId(R.styleable.EPG_epgTimeBarTextSize, R.dimen.textSize_epg_timeBar_14sp)
                nowButtonSize = getResourceId(R.styleable.EPG_epgNowButtonSize, R.dimen.size_epg_nowButton_40dp)
                nowButtonMargin = getResourceId(R.styleable.EPG_epgNowButtonMargin, R.dimen.margin_epg_nowButton_10dp)
            }
        } finally {
            attributes.recycle()
        }

        scroller = Scroller(context)
        scroller.setFriction(.2f)

        gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {

            override fun onShowPress(p0: MotionEvent?) {}

            override fun onSingleTapUp(p0: MotionEvent?): Boolean = false

            override fun onDown(p0: MotionEvent?): Boolean = false

            override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float,
                                  distanceY: Float): Boolean = false

            override fun onLongPress(e: MotionEvent?) {}

        })

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.outWidth = nowButtonSize
        bitmapOptions.outHeight = nowButtonSize

        // TODO Update correct drawable
        nowButtonIcon = BitmapFactory.decodeResource(resources, R.drawable.abc_btn_colored_material, bitmapOptions)

    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {

    }

}