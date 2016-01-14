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

    private var cachedSegments = emptyList<Segment>()
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
                            cachedSegments = retroQueue.createSegments(endLife)
                        }

                        isDrawAiming = !isDrawAiming

                        if (!nearestSeg.isEmpty) {

                            if (nearestSeg.isHorizontal)
                                retroQueue.deleteAddOperation(nearestSeg.x1)
                            else
                                retroQueue.deleteExtractOperation(nearestSeg.x1)

                            cachedSegments = retroQueue.createSegments(endLife)
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

        fun drawSimpleSegments(segments: List<Segment>) {
            g.color = Color.WHITE
            g.stroke = defaultStroke
            segments.forEach { g.drawSegment(it) }
        }

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
            drawSimpleSegments(cachedSegments)

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
                g.drawLine(cpx, height, cpx, height - extractEnd)
            }
        }

        val newSegmentsIds = newSegments.map(Segment::sid).toHashSet()
        val unchangedSegments = cachedSegments.filter { it.sid !in newSegmentsIds }

        g.color = Color.ORANGE
        newSegments.forEach { g.drawSegment(it) }
        drawSimpleSegments(unchangedSegments)
    }

    private fun traverseFromVertical(): Pair<List<Segment>, Segment?> {
        val meetSegment = cachedSegments.filter {
            it.isVertical && it.inYRange(cpyi) && it.x2 >= cpx
        }.minBy(Segment::x2) ?: return Pair(emptyList(), null)

        val updatedSegments = arrayListOf(meetSegment.copy(y2 = cpyi))

        var cur = meetSegment.nextOnAdd
        var prev = meetSegment
        while (cur != null) {
            val next = cur.nextOnAdd
            if (next == null) {
                val lastSegment = if (cur.isHorizontal)
                    cur.copy(x2 = endLife)
                else
                    cur.copy(y2 = Int.MAX_VALUE)

                updatedSegments.add(lastSegment)
                break
            }

            val cutCopy = if (next.isVertical)
                cur.copy(x2 = next.x2)
            else
                cur.copy(y2 = prev.y2)

            updatedSegments.add(cutCopy)
            prev = cur
            cur = next
        }

        return Pair(updatedSegments, meetSegment)
    }

    private fun traverseFromHorizontal(): Pair<List<Segment>, Segment?> {
        val meetSegment = cachedSegments.filter {
            it.isHorizontal && it.inXRange(cpx)
        }.minBy(Segment::y1) ?: return Pair(emptyList(), null)

        val updatedSegments = arrayListOf(meetSegment.copy(x2 = cpx))

        var cur = meetSegment.nextOnExtract
        var prev = meetSegment
        while (cur != null) {
            val next = cur.nextOnExtract
            if (next == null) {
                val lastSegment = if (cur.isHorizontal)
                    cur.copy(x2 = prev.x2)
                else
                    cur.copy(y2 = Int.MAX_VALUE)

                updatedSegments.add(lastSegment)
                break
            }

            val cutCopy = if (cur.isHorizontal)
                cur.copy(x2 = prev.x2)
            else
                cur.copy(y2 = next.y2)

            updatedSegments.add(cutCopy)
            prev = cur
            cur = next
        }

        return Pair(updatedSegments, meetSegment)
    }

    private fun updatedSegments() = when (insertMode) {
        OperationMode.ADD     -> traverseFromVertical()
        OperationMode.EXTRACT -> traverseFromHorizontal()
    }

    fun switchMode() {
        insertMode = when (insertMode) {
            OperationMode.ADD -> OperationMode.EXTRACT
            OperationMode.EXTRACT -> OperationMode.ADD
        }

        repaint()
    }
}