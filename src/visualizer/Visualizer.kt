package visualizer

import geom.Segment
import geom.emptySegment
import retro.PartialRetroPriorityQueue
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

enum class OperationMode {
    ADD, EXTRACT
}

class Visualizer : JPanel() {
    private val retroQueue = PartialRetroPriorityQueue()

    private var cachedSegments = listOf<Segment>()
    private var nearestSeg = emptySegment

    private var cursorPoint = Point(0, 0)
    private val cpx: Int
        get() = cursorPoint.x
    private val cpy: Int
        get() = cursorPoint.y
    private val cpyi: Int
        get() = height - cursorPoint.y

    private val endLife: Int
        get() = width - 10

    private var insertMode = OperationMode.ADD
    private var isDrawAiming = false

    init {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                when (e.button) {
                    MouseEvent.BUTTON1 -> {
                        if (isDrawAiming) {
                            when (insertMode) {
                                OperationMode.ADD -> retroQueue.insertAddOperation(e.x, height - e.y)
                                OperationMode.EXTRACT -> retroQueue.insertExtractOperation(e.x)
                            }
                            cachedSegments = retroQueue.createSegments(0, endLife)
                        }

                        isDrawAiming = !isDrawAiming

                        if (!nearestSeg.isEmpty) {

                            if (nearestSeg.isHorizontal)
                                retroQueue.deleteAddOperation(nearestSeg.x1)
                            else
                                retroQueue.deleteExtractOperation(nearestSeg.x1)

                            cachedSegments = retroQueue.createSegments(0, width - 10)
                            nearestSeg = emptySegment
                            isDrawAiming = false
                        }
                    }
                }
                repaint()
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                cursorPoint = e.point
                nearestSeg = emptySegment

                if (!isDrawAiming) {
                    var minDistance = Int.MAX_VALUE
                    var mirroredPoint = Point(cpx, cpyi)

                    for (segment in cachedSegments) {
                        val curDistance = segment.distanceToPoint(mirroredPoint)

                        if (curDistance < 10 && (nearestSeg.isEmpty || curDistance < minDistance)) {
                            nearestSeg = segment
                            minDistance = curDistance
                        }
                    }
                }

                repaint()
            }
        })
    }

    private val defaultStroke = BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
    private val simpleBoldStroke = BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

    override fun getGraphics() = super.getGraphics()?.apply {
        this as Graphics2D
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    fun Graphics.drawSegment(s: Segment) = drawLine(s.x1, height - s.y1, s.x2, height - s.y2)

    override fun paint(g: Graphics) {
        g as Graphics2D

        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        g.color = Color.WHITE
        g.drawString("Point: ($cpx, $cpyi)", 0, 20)
        g.drawString("Mode: $insertMode key", 0, 40)

        // draw current time line
        g.color = Color.BLUE
        val curTimeDashingPattern = floatArrayOf(10f, 4f)
        val curTimeStroke = BasicStroke(4f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, curTimeDashingPattern, 0.0f)
        g.stroke = curTimeStroke
        g.drawLine(endLife, 0, endLife, height)

        if (!isDrawAiming) {
            // draw all segments (inserts and extracts) as rays
            g.color = Color.WHITE
            g.stroke = defaultStroke
            cachedSegments.forEach { g.drawSegment(it) }

            // draw nearest segment
            if (!nearestSeg.isEmpty) {
                g.color = Color.MAGENTA
                g.stroke = simpleBoldStroke
                g.drawSegment(nearestSeg)
            }

            return
        }

        val (newSegments, first) = updatedSegments()

        // draw active ray
        g.stroke = simpleBoldStroke
        when (insertMode) {
            OperationMode.ADD -> {
                val addEnd = first?.x1 ?: endLife

                g.color = Color.GREEN
                g.drawLine(cpx, cpy, addEnd, cpy)
            }
            OperationMode.EXTRACT -> {
                val extractEnd = first?.y1 ?: height

                g.color = Color.RED
                g.drawLine(cpx, cpy, cpx, height - extractEnd)
            }
        }

        g.color = Color.ORANGE
        newSegments.forEach { g.drawSegment(it) }
    }

    private fun findHorizontalSegment(xRay: Int, segments: MutableList<Segment>, isFirstRay: Boolean = false): Segment? {
        val meetSegmentIndexed = segments.withIndex().filter {
            it.value.isHorizontal && it.value.inXRange(xRay)
        }.minBy { it.value.y1 } ?: return null

        val ind = meetSegmentIndexed.index
        val seg = meetSegmentIndexed.value

        segments[ind] = seg.copy(x2 = xRay)

        if (seg.x2 != endLife) {
            val next = findVerticalSegment(seg.y1, segments)
            if (next != null && !isFirstRay) segments[ind] = seg.copy(x2 = next.x1)
        }

        return seg
    }

    private fun findVerticalSegment(yRay: Int, segments: MutableList<Segment>, isFirstRay: Boolean = false): Segment? {
        val meetSegmentIndexed = segments.withIndex().filter {
            !it.value.isHorizontal && it.value.inYRange(yRay)
        }.minBy { it.value.x1 } ?: return null

        val ind = meetSegmentIndexed.index
        val seg = meetSegmentIndexed.value

        segments[ind] = seg.copy(y2 = yRay)

        if (seg.y2 != 0) {
            val next = findHorizontalSegment(seg.x1, segments)
            if (next != null && !isFirstRay) segments[ind] = seg.copy(y2 = next.y1)
        }

        return seg
    }

    private fun updatedSegments(): Pair<List<Segment>, Segment?> {
        val segmentsBuffer = arrayListOf(*cachedSegments.toTypedArray())

        val firstSeg = when (insertMode) {
            OperationMode.ADD -> findVerticalSegment(cpyi, segmentsBuffer, true)
            OperationMode.EXTRACT -> findHorizontalSegment(cpx, segmentsBuffer, true)
        }

        return Pair(segmentsBuffer, firstSeg)
    }

    fun switchMode() {
        insertMode = when (insertMode) {
            OperationMode.ADD -> OperationMode.EXTRACT
            OperationMode.EXTRACT -> OperationMode.ADD
        }

        repaint()
    }
}