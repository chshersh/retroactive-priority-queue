package visualizer

import geom.Segment
import geom.distanceToPoint
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

    private var cachedSegments: List<Segment> = listOf()
    private var closestSegment: Segment? = null

    private var cursorPoint = Point(0, 0)

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
                            cachedSegments = retroQueue.createSegments(0, width - 10)
                        }

                        isDrawAiming = !isDrawAiming

                        if (closestSegment != null) {
                            val cs = closestSegment

                            if (cs.isHorizontal)
                                retroQueue.deleteAddOperation(cs.x1)
                            else
                                retroQueue.deleteExtractOperation(cs.x1)

                            cachedSegments = retroQueue.createSegments(0, width - 10)
                            closestSegment = null
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
                closestSegment = null

                if (!isDrawAiming) {
                    var minDistance = Int.MAX_VALUE
                    var mirroredPoint = cursorPoint.inverse()

                    for (segment in cachedSegments) {
                        val curDistance = segment.distanceToPoint(mirroredPoint)

                        if (curDistance < 10 && (closestSegment == null || curDistance < minDistance)) {
                            closestSegment = segment
                            minDistance = curDistance
                        }
                    }
                }

                repaint()
            }
        })
    }

    private val simpleBoldStroke = BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

    override fun getGraphics() = super.getGraphics()?.apply {
        this as Graphics2D
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    fun Graphics.drawSegment(s: Segment) = drawLine(s.x1, height - s.y1, s.x2, height - s.y2)
    fun Point.inverse() = Point(this.x, height - this.y)

    override fun paint(g: Graphics) {
        g as Graphics2D

        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        g.color = Color.WHITE

        g.drawString("Point: (${cursorPoint.x}, ${height - cursorPoint.y})", 0, 20)
        g.drawString("Mode: $insertMode key", 0, 40)

        val curTime = width - 10

        cachedSegments.map {
            if (it.x2 == Int.MAX_VALUE) it.copy(x2 = curTime) else it
        }.forEach { g.drawSegment(it) }

        if (closestSegment != null) {
            val cs = closestSegment
            g.color = Color.MAGENTA
            g.stroke = simpleBoldStroke
            g.drawSegment(cs)
        }


        // draw current time line
        g.color = Color.BLUE
        val curTimeDashingPattern = floatArrayOf(10f, 4f)
        val curTimeStroke = BasicStroke(4f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, curTimeDashingPattern, 0.0f)
        g.stroke = curTimeStroke
        g.drawLine(curTime, 0, curTime, height)

        if (isDrawAiming) {
            g.stroke = simpleBoldStroke

            when (insertMode) {
                OperationMode.ADD -> {
                    g.color = Color.GREEN
                    g.drawLine(cursorPoint.x, cursorPoint.y, curTime, cursorPoint.y)
                }
                OperationMode.EXTRACT -> {
                    g.color = Color.RED
                    g.drawLine(cursorPoint.x, cursorPoint.y, cursorPoint.x, 0)
                }
            }
        }

    }

    fun switchMode() {
        insertMode = when (insertMode) {
            OperationMode.ADD -> OperationMode.EXTRACT
            OperationMode.EXTRACT -> OperationMode.ADD
        }

        repaint()
    }
}