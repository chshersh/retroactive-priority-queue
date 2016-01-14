package visualizer

import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class MainWindow : JFrame("Retro queue : awesome visualization") {
    init {
        val retroPanel = Visualizer()

        contentPane = retroPanel
        size = Dimension(700, 600)
        isVisible = true
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setLocationRelativeTo(null)
    }

    companion object {
        fun start() = SwingUtilities.invokeLater { MainWindow() }
    }
}