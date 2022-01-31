package com.hover.stax.onboarding.slidingVariant

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager
import java.lang.ref.WeakReference

class StaxAutoScrollViewPager : ViewPager {

    private var interval = DEFAULT_INTERVAL.toLong()
    private var direction = RIGHT
    private var isCycle = true
    private var stopScrollWhenTouch = true
    private val slideBorderMode = SLIDE_BORDER_MODE_NONE
    private val isBorderAnimation = true
    private var autoScrollFactor = 1.0
    private var swipeScrollFactor = 1.0
    private lateinit var handler: MyHandler
    private var isAutoScroll = false
    private var isStopByTouch = false
    private var touchX = 0f
    private var downX = 0f
    private var scroller: CustomDurationScroller? = null

    constructor(paramContext: Context?) : super(paramContext!!) {
        init()
    }

    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?) : super(paramContext!!,
            paramAttributeSet) {
        init()
    }

    private fun init() {
        handler = MyHandler(this)
        setViewPagerScroller()
    }

    private fun startAutoScroll() {
        isAutoScroll = true
        sendScrollMessage((interval + scroller!!.duration / autoScrollFactor * swipeScrollFactor).toLong())
    }

    fun startAutoScroll(delayTimeInMills: Int) {
        isAutoScroll = true
        sendScrollMessage(delayTimeInMills.toLong())
    }

    private fun stopAutoScroll() {
        isAutoScroll = false
        handler.removeMessages(SCROLL_WHAT)
    }

    fun setSwipeScrollDurationFactor(scrollFactor: Double) {
        swipeScrollFactor = scrollFactor
    }

    fun setAutoScrollDurationFactor(scrollFactor: Double) {
        autoScrollFactor = scrollFactor
    }

    private fun sendScrollMessage(delayTimeInMills: Long) {
        handler.removeMessages(SCROLL_WHAT)
        handler.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills)
    }

    private fun setViewPagerScroller() {
        try {
            val scrollerField = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            val interpolatorField = ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolatorField.isAccessible = true
            scroller = CustomDurationScroller(context, interpolatorField[null] as Interpolator)
            scrollerField[this] = scroller
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scrollOnce() {
        val adapter = adapter
        var currentItem = currentItem
        var totalCount = 0
        if (adapter == null || adapter.count.also { totalCount = it } <= 1) {
            return
        }
        val nextItem = if (direction == LEFT) --currentItem else ++currentItem
        if (nextItem < 0) {
            if (isCycle) {
                setCurrentItem(totalCount - 1, isBorderAnimation)
            }
        } else if (nextItem == totalCount) {
            if (isCycle) {
                setCurrentItem(0, isBorderAnimation)
            }
        } else {
            setCurrentItem(nextItem, true)
        }
    }

    private fun pauseScrolling(ev: MotionEvent) {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN && isAutoScroll) {
            isStopByTouch = true
            stopAutoScroll()
        } else if (ev.action == MotionEvent.ACTION_UP && isStopByTouch) {
            startAutoScroll()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (stopScrollWhenTouch) pauseScrolling(ev)

        if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT || slideBorderMode == SLIDE_BORDER_MODE_CYCLE) {
            touchX = ev.x
            if (ev.action == MotionEvent.ACTION_DOWN) {
                downX = touchX
            }
            val currentItem = currentItem
            val adapter = adapter
            val pageCount = adapter?.count ?: 0
            if (currentItem == 0 && downX <= touchX || currentItem == pageCount - 1 && downX >= touchX) {
                if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT) {
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    if (pageCount > 1) {
                        setCurrentItem(pageCount - currentItem - 1, isBorderAnimation)
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                return super.dispatchTouchEvent(ev)
            }
        }
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

    private class MyHandler(staxAutoScrollViewPager: StaxAutoScrollViewPager) : Handler() {
        private val autoScrollViewPager: WeakReference<StaxAutoScrollViewPager> = WeakReference(staxAutoScrollViewPager)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == SCROLL_WHAT) {
                val pager = autoScrollViewPager.get()
                if (pager != null) {
                    pager.scroller!!.setScrollDurationFactor(pager.autoScrollFactor)
                    pager.scrollOnce()
                    pager.scroller!!.setScrollDurationFactor(pager.swipeScrollFactor)
                    pager.sendScrollMessage(pager.interval + pager.scroller!!.duration)
                }
            }
        }

    }

    fun setStopScrollWhenTouch(stopScrollWhenTouch: Boolean) {
        this.stopScrollWhenTouch = stopScrollWhenTouch
    }

    fun setCycle(isCycle: Boolean) {
        this.isCycle = isCycle
    }

    fun setDirection(direction: Int) {
        this.direction = direction
    }

    fun setInterval(interval: Long) {
        this.interval = interval
    }

    fun getDirection(): Int {
        return if (direction == LEFT) LEFT else RIGHT
    }

    private class CustomDurationScroller(context: Context?, interpolator: Interpolator?) :
            Scroller(context, interpolator) {
        private var scrollFactor = 1.0
        fun setScrollDurationFactor(scrollFactor: Double) {
            this.scrollFactor = scrollFactor
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, (duration * scrollFactor).toInt())
        }
    }

    companion object {
        const val DEFAULT_INTERVAL = 1500
        const val LEFT = 0
        const val RIGHT = 1
        const val SLIDE_BORDER_MODE_NONE = 0
        const val SLIDE_BORDER_MODE_CYCLE = 1
        const val SLIDE_BORDER_MODE_TO_PARENT = 2
        const val SCROLL_WHAT = 0
    }
}