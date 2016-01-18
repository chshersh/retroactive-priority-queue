package algo.geom

import algo.retro.Operation.*
import algo.retro.RetroPriorityQueue
import java.awt.Point
import java.util.*

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

fun RetroPriorityQueue.createSegments(maxLifeTime: Int): List<Segment> {
    var curId = 0
    val deadSegments = arrayListOf<Segment>()
    val queue = PriorityQueue<Segment>(y2Comparator)
    var extractedKeys = arrayListOf<Segment>()

    fun setNextOnAdd(extractSegment: Segment) {
        val (lower, higher) = extractedKeys.partition { it.y2 <= extractSegment.y2 }
        lower.forEach { it.nextOnAdd = extractSegment }
        extractedKeys = higher.toArrayList()
    }

    val operationsMap = operations
    for ((time, ops) in operationsMap) {
        for (operation in ops) {
            when (operation) {
                is Add -> queue.add(Segment(curId++, time, operation.key, maxLifeTime, operation.key))
                Extract ->
                    if (queue.isEmpty()) {
                        val extractRay = Segment(curId++, time, 0, time, Int.MAX_VALUE)
                        setNextOnAdd(extractRay)
                        deadSegments.add(extractRay)
                    } else {
                        val addSegment = queue.poll()
                        val extractSegment = Segment(curId++, time, 0, time, addSegment.y1)

                        addSegment.x2 = time
                        addSegment.nextOnExtract = extractSegment

                        extractSegment.nextOnAdd = addSegment
                        extractSegment.nextOnExtract = queue.peek()

                        setNextOnAdd(extractSegment)

                        deadSegments.add(extractSegment)
                        deadSegments.add(addSegment)
                        extractedKeys.add(addSegment)
                    }
            }
        }
    }

    return deadSegments + queue.toList()
}