package geom

import java.awt.Point

data class Segment(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    val isHorizontal: Boolean
        get() = y1 == y2
}

fun Point.distTo(x0: Int, y0: Int)
        = Math.sqrt(((x - x0) * (x - x0) + (y - y0) * (y - y0)).toDouble()).toInt()

fun Segment.distanceToPoint(p: Point) =
        if (isHorizontal) {
            if (x1 <= p.x && p.x <= x2)
                Math.abs(p.y - y1)
            else
                Math.min(p.distTo(x1, y1), p.distTo(x2, y2))
        } else {
            if (y1 <= p.y && p.y <= y2)
                Math.abs(p.x - x1)
            else
                Math.min(p.distTo(x1, y1), p.distTo(x2, y2))
        }