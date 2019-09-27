package by.kanber.fincontrol.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import android.view.View


class CustomItemDivider(context: Context?, orientation: Int) : RecyclerView.ItemDecoration() {
    private val ATTRS = intArrayOf(16843284)
    private var mDivider: Drawable? = null
    private var mOrientation: Int = 0
    private val mBounds = Rect()

    init {
        val a = context?.obtainStyledAttributes(ATTRS)
        this.mDivider = a?.getDrawable(0)
        a?.recycle()
        setOrientation(orientation)
    }

    private fun setOrientation(orientation: Int) {
        if (orientation != DividerItemDecoration.HORIZONTAL && orientation != DividerItemDecoration.VERTICAL) {
            throw IllegalArgumentException("Invalid orientation. It should be either HORIZONTAL or VERTICAL")
        } else {
            this.mOrientation = orientation
        }
    }

    fun setDrawable(@NonNull drawable: Drawable): CustomItemDivider {
        this.mDivider = drawable

        return this
    }

    override fun onDraw(@NonNull c: Canvas, @NonNull parent: RecyclerView, @NonNull state: RecyclerView.State) {
        if (parent.layoutManager != null && this.mDivider != null) {
            if (this.mOrientation == DividerItemDecoration.VERTICAL) {
                this.drawVertical(c, parent)
            } else {
                this.drawHorizontal(c, parent)
            }

        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        val childCount = parent.childCount

        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)

            parent.getDecoratedBoundsWithMargins(child, this.mBounds)
            val bottom = this.mBounds.bottom + Math.round(child.translationY)
            val top = bottom - this.mDivider!!.intrinsicHeight
            this.mDivider!!.setBounds(left, top, right, bottom)
            this.mDivider!!.draw(canvas)
        }

        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
        } else {
            top = 0
            bottom = parent.height
        }

        val childCount = parent.childCount

        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val manager = parent.layoutManager

            manager?.getDecoratedBoundsWithMargins(child, this.mBounds)

            val right = this.mBounds.right + Math.round(child.translationX)
            val left = right - this.mDivider!!.intrinsicWidth
            this.mDivider!!.setBounds(left, top, right, bottom)
            this.mDivider!!.draw(canvas)
        }

        canvas.restore()
    }

    override fun getItemOffsets(@NonNull outRect: Rect, @NonNull view: View, @NonNull parent: RecyclerView, @NonNull state: RecyclerView.State) {
        if (this.mDivider == null) {
            outRect.set(0, 0, 0, 0)
        } else {
            if (this.mOrientation == DividerItemDecoration.VERTICAL) {
                outRect.set(0, 0, 0, this.mDivider!!.intrinsicHeight)
            } else {
                outRect.set(0, 0, this.mDivider!!.intrinsicWidth, 0)
            }

        }
    }
}