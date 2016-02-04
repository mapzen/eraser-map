package com.mapzen.erasermap.util

/**
 * An axis-aligned bounding box
 *
 * Useful class for finding the axis-aligned bounds of a set of 2D points
 *
 * @author Matt Blair
 */

public class AxisAlignedBoundingBox {

    /**
     * 2D point container like PointF, but with double precision
     */
    public class PointD(x: Double = 0.0, y: Double = 0.0) {
        var x: Double = x
        var y: Double = y
    }

    var min = PointD()
    var max = PointD()

    var center: PointD
        get() {
            return PointD(0.5 * (max.x + min.x), 0.5 * (max.y + min.y))
        }
        set(c: PointD) {
            val hw = 0.5 * this.width
            val hh = 0.5 * this.height
            max = PointD(c.x + hw, c.y + hh)
            min = PointD(c.x - hw, c.y - hh)
        }

    var width: Double
        get() {
            return max.x - min.x
        }
        set(w: Double) {
            val c = this.center
            max.x = c.x + 0.5 * w
            min.x = c.x - 0.5 * w
        }

    var height: Double
        get() {
            return max.y - min.y
        }
        set(h: Double) {
            val c = this.center
            max.y = c.y + 0.5 * h
            min.y = c.y - 0.5 * h
        }

    /**
     * If (x, y) is outside the box, expands the box to reach it
     */
    public fun expandTo(x: Double, y: Double) {
        max.x = Math.max(max.x, x)
        max.y = Math.max(max.y, y)
        min.x = Math.min(min.x, x)
        min.y = Math.min(min.y, y)
    }

}
