package de.nulide.shiftcal.ui.helper.ExpandableLinearLayout

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.animation.addListener
import androidx.core.view.children
import de.nulide.shiftcal.R
import de.nulide.shiftcal.utils.Runner

open class ExpandableLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle) {

    private var isExpanded = false

    private var hideItems = true

    private var hideChildren = true

    private var useCollapseSize = true

    private var onExpandableLinearLayoutExpandedListener: OnExpandableLinearLayoutExpandedListener? =
        null

    private var onExpandableLinearLayoutCollapsedListener: OnExpandableLinearLayoutCollapsedListener? =
        null

    private var sizeOnCollapse = 0
    private var lastSizeOnCollapse = 0

    // Initialize the layout
    init {
        if (visibility != GONE) {
            isExpanded = true
        }
        Runner.run {
            sizeOnCollapse = layoutParams.height
        }
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.ExpandableLinearLayout, 0, 0)
            try {
                hideChildren =
                    typedArray.getBoolean(R.styleable.ExpandableLinearLayout_hideChildren, true)
                useCollapseSize =
                    typedArray.getBoolean(R.styleable.ExpandableLinearLayout_useCollapseSize, false)
            } finally {
                typedArray.recycle()
            }
        }
    }

    // Expand the layout
    fun expand() {
        if (isExpanded) {
            adaptSize()
            return
        }
        if (hideChildren) {
            setChildrenVisibility(VISIBLE)
        }

        var targetHeight = getMeasuredTrueHeight()
        if (useCollapseSize) {
            targetHeight = sizeOnCollapse
        }

        layoutParams.height = 0
        visibility = View.VISIBLE

        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            layoutParams.height = animatedValue
            requestLayout()
        }
        animator.addListener(onEnd = {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            requestLayout()
            onExpandableLinearLayoutExpandedListener?.onExpanded(this)
        })
        animator.duration = 200 // duration in milliseconds
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()

        isExpanded = true
    }

    fun expandNow() {
        if (isExpanded) {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            requestLayout()
            return
        }
        if (hideChildren) {
            setChildrenVisibility(VISIBLE)
        }
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        visibility = View.VISIBLE
        requestLayout()
        isExpanded = true
    }

    private fun adaptSize() {
        val targetHeight = getMeasuredTrueHeight()

        val animator = ValueAnimator.ofInt(layoutParams.height, targetHeight)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            layoutParams.height = animatedValue
            requestLayout()
        }
        animator.addListener(onEnd = {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            requestLayout()
        })

        animator.duration = 200 // duration in milliseconds
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun getMeasuredTrueHeight(): Int {
        var childHeight = 0
        for (child in children) {
            val parentWidth = width - paddingLeft - paddingRight
            val measureSpecWidth = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.AT_MOST)
            val measureSpecHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            child.measure(measureSpecWidth, measureSpecHeight)
            childHeight += child.measuredHeight
        }
        return childHeight + paddingTop + paddingBottom
    }

    // Collapse the layout
    fun collapse() {
        if (!isExpanded) return

        lastSizeOnCollapse = sizeOnCollapse
        sizeOnCollapse = layoutParams.height
        if (sizeOnCollapse < lastSizeOnCollapse) {
            sizeOnCollapse = lastSizeOnCollapse
        }

        if (hideItems) {
            setChildrenVisibility(GONE)
        }

        val initialHeight = measuredHeight

        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            layoutParams.height = animatedValue
            requestLayout()
        }
        animator.duration = 200 // duration in milliseconds
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(onEnd = {
            onExpandableLinearLayoutCollapsedListener?.onCollapsed(this)
            visibility = View.GONE
        })
        animator.start()

        isExpanded = false
    }

    private fun setChildrenVisibility(visibility: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).visibility = visibility
        }
    }

    fun hideItems(shouldHide: Boolean) {
        hideItems = shouldHide
    }

    fun setOnExpandableLinerLayoutExpandedListener(onExpandableLinearLayoutExpandedListener: OnExpandableLinearLayoutExpandedListener) {
        this.onExpandableLinearLayoutExpandedListener = onExpandableLinearLayoutExpandedListener
    }

    fun setOnExpandableLinerLayoutCollapsedListener(onExpandableLinearLayoutCollapsedListener: OnExpandableLinearLayoutCollapsedListener) {
        this.onExpandableLinearLayoutCollapsedListener = onExpandableLinearLayoutCollapsedListener
    }
}
