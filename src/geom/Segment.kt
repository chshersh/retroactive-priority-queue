package geom

import java.awt.Point

data class Segment(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    val isHorizontal: Boolean
        get() = y1 == y2
}

fun Segment.distanceToPoint(p: Point) =
        if (isHorizontal) {
            if (p.y == y1 && (p.x < x1 || x2 < p.x))
                Math.min(Math.abs(p.x - x1), Math.abs(p.x - x2))
            else
                Math.abs(p.y - y1)
        } else {
            if (p.x == x1 && (p.y < y1 || y2 < p.y))
                Math.min(Math.abs(p.y - y1), Math.abs(p.y - y2))
            else
                Math.abs(p.x - x1)
        }