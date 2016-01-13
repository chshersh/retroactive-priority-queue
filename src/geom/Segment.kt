package geom

import java.awt.Point

fun Point.distTo(x0: Int, y0: Int)
        = Math.sqrt(((x - x0) * (x - x0) + (y - y0) * (y - y0)).toDouble()).toInt()

data class Segment(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    val isHorizontal: Boolean
        get() = y1 == y2

    val isEmpty: Boolean
        get() = this == emptySegment

    fun inXRange(x: Int) = x1 <= x && x <= x2
    fun inYRange(y: Int) = y1 <= y && y <= y2

    fun distanceToPoint(p: Point) =
            if (isHorizontal) {
                if (inXRange(p.x))
                    Math.abs(p.y - y1)
                else
                    Math.min(p.distTo(x1, y1), p.distTo(x2, y2))
            } else {
                if (inYRange(p.y))
                    Math.abs(p.x - x1)
                else
                    Math.min(p.distTo(x1, y1), p.distTo(x2, y2))
            }
}

val emptySegment = Segment(-1, -1, -1, -1)