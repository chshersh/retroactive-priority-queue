package geom

import java.awt.Point

fun Point.distTo(x0: Int, y0: Int)
        = Math.sqrt(((x - x0) * (x - x0) + (y - y0) * (y - y0)).toDouble()).toInt()

// something went wrong here after creating simple immutable segment class
data class Segment(
        val sid: Int, // TODO: make default to remove secondary constructor?
        val x1: Int, val y1: Int,
        var x2: Int, var y2: Int,  // this ends can change their value
        var nextOnAdd: Segment? = null,
        var nextOnExtract: Segment? = null
) {

    constructor(ax: Int, ay: Int, bx: Int, by: Int) : this(-1, ax, ay, bx, by)

    val isHorizontal: Boolean
        get() = y1 == y2

    val isVertical: Boolean
        get() = x1 == x2

    val isEmpty: Boolean
        get() = sid == -1

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

val emptySegment = Segment(-1, -1, -1, -1, -1)
val y2Comparator = { s1: Segment, s2: Segment ->
    if (s1.y2 == s2.y2)
        s1.sid.compareTo(s2.sid)
    else
        s1.y2.compareTo(s2.y2)
}