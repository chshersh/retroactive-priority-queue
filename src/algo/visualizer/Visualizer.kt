package algo.visualizer

import algo.geom.Segment
import algo.geom.emptySegment
import algo.retro.NaivePartialRetroPriorityQueue
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

class Visualizer : JPanel() {
    private val retroQueue = NaivePartialRetroPriorityQueue()

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

    private enum class AimingMode {
        EMPTY, ADD, EXTRACT
    }

    private var aimingMode = AimingMode.EMPTY

    init {
        addMouseListener(object : MouseAdapter() {
            fun updateGlobals() {
                cachedSegments = retroQueue.createSegments(endLife)
                aimingMode = AimingMode.EMPTY
                nearestSeg = emptySegment
            }

            override fun mouseClicked(e: MouseEvent) {
                when (e.button) {
                    MouseEvent.BUTTON1 -> when (aimingMode) {
                        AimingMode.ADD -> {
                            retroQueue.insertAddOperation(e.x, height - e.y)
                            updateGlobals()
                        }
                        else -> aimingMode = AimingMode.ADD
                    }

                    MouseEvent.BUTTON3 -> when (aimingMode) {
                        AimingMode.EXTRACT -> {
                            retroQueue.insertExtractOperation(e.x)
                            updateGlobals()
                        }
                        else -> aimingMode = AimingMode.EXTRACT
                    }

                    MouseEvent.BUTTON2 -> if (!nearestSeg.isEmpty) {
                        if (nearestSeg.isHorizontal)
                            retroQueue.deleteAddOperation(nearestSeg.x1)
                        else
                            retroQueue.deleteExtractOperation(nearestSeg.x1)

                        updateGlobals()
                    }
                }
                repaint()
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                cursorPoint = e.point
                nearestSeg = emptySegment

                if (aimingMode == AimingMode.EMPTY) {
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

    private val defaultStroke    = BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
    private val simpleBoldStroke = BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
    private val deleteOpStroke   = BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

    private val DARK_BLUE   = Color(30, 35, 146)
    private val DARK_ORANGE = Color(255, 129, 0)

    override fun getGraphics() = super.getGraphics()?.apply {
        this as Graphics2D
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    fun Graphics.drawSegment(s: Segment) = drawLine(s.x1, height - s.y1, s.x2, height - s.y2)

    override fun paint(g: Graphics) {
        g as Graphics2D

        fun drawCustomSegment(s: Segment, segColor: Color, segStroke: Stroke) {
            g.color = segColor
            g.stroke = segStroke
            g.drawSegment(s)
        }

        fun drawSegmentsList(segments: List<Segment>, segColor: Color, segStroke: Stroke) {
            g.color = segColor
            g.stroke = segStroke
            segments.forEach { g.drawSegment(it) }
        }

        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        g.color = Color.WHITE
        g.drawString("Point: ($cpx, $cpyi)", 0, 20)
        g.drawString("Press LeftMB to insert `ADD` operation", 0, 40)
        g.drawString("Press RightMB to insert `EXTRACT` operation", 0, 60)
        g.drawString("Press Middle(Wheel) to delete operation", 0, 80)

        // draw current time line
        g.color = Color.BLUE
        val curTimeDashingPattern = floatArrayOf(10f, 4f)
        val curTimeStroke = BasicStroke(4f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, curTimeDashingPattern, 0.0f)
        g.stroke = curTimeStroke
        g.drawLine(endLife, 0, endLife, height)

        val (newSegments, first) = updatedSegments()

        when (aimingMode) {
            AimingMode.EMPTY -> if (!nearestSeg.isEmpty) drawCustomSegment(nearestSeg, DARK_BLUE, deleteOpStroke)
            AimingMode.ADD -> { // draw green `add` ray
                val addEnd = first?.x1 ?: endLife
                drawCustomSegment(Segment(cpx, cpyi, addEnd, cpyi), Color.GREEN, simpleBoldStroke)
            }
            AimingMode.EXTRACT -> { // draw red `extract` ray
                val extractEnd = first?.y1 ?: height
                drawCustomSegment(Segment(cpx, 0, cpx, extractEnd), Color.RED, simpleBoldStroke)
            }
        }

        val newSegmentsIds = newSegments.map(Segment::sid).toHashSet()
        val unchangedSegments = cachedSegments.filter { it.sid !in newSegmentsIds }

        drawSegmentsList(newSegments, DARK_ORANGE, simpleBoldStroke)
        drawSegmentsList(unchangedSegments, Color.WHITE, defaultStroke)
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
                    cur.copy(y2 = prev.y2)

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

    private fun updatedSegments() = when (aimingMode) {
        AimingMode.EMPTY   -> Pair(emptyList<Segment>(), null)
        AimingMode.ADD     -> traverseFromVertical()
        AimingMode.EXTRACT -> traverseFromHorizontal()
    }
}