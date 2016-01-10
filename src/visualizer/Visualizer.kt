package visualizer

import retro.PartialRetroPriorityQueue
import retro.Segment
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
                            cachedSegments = retroQueue.createSegments()
                        }

                        isDrawAiming = !isDrawAiming
                    }
                }
                repaint()
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                cursorPoint = e.point
                repaint()
            }
        })
    }

    override fun getGraphics() = super.getGraphics()?.apply {
        this as Graphics2D
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    override fun paint(g: Graphics) {
        g as Graphics2D

        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        g.color = Color.WHITE

        g.drawString("Point: (${cursorPoint.x}, ${height - cursorPoint.y})", 0, 20)
        g.drawString("Mode: $insertMode key", 0, 40)

        val curTime = width - 10

        for ((key, start, end) in cachedSegments) {
            val drawKey = height - key

            if (end == Int.MAX_VALUE) {
                g.drawLine(start, drawKey, curTime, drawKey)
            } else {
                g.drawLine(start, drawKey, end, drawKey)
                g.drawLine(end, height, end, drawKey)
            }
        }


        // draw current time line
        g.color = Color.BLUE
        val curTimeDashingPattern = floatArrayOf(10f, 4f)
        val curTimeStroke = BasicStroke(3f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, curTimeDashingPattern, 0.0f)
        g.stroke = curTimeStroke
        g.drawLine(curTime, 0, curTime, height)

        if (isDrawAiming) {
            g.stroke = BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

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