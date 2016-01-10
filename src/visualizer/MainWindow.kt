package visualizer

import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class MainWindow : JFrame("Retro queue : awesome visualization") {
    init {
        val retroPanel = Visualizer()

        contentPane = retroPanel
        size = Dimension(700, 500)
        isVisible = true
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setLocationRelativeTo(null)

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent): Unit = when (e.keyCode) {
                KeyEvent.VK_S -> retroPanel.switchMode()
                else -> Unit
            }
        })
    }

    companion object {
        fun start() = SwingUtilities.invokeLater { MainWindow() }
    }
}