package logfilter.handler

import java.awt.event.WindowEvent
import java.awt.event.WindowListener

class WindowEventHandler : WindowListener {
    override fun windowClosing(e: WindowEvent) {
        e.window.isVisible = false
        e.window.dispose()
        System.exit(0)
    }

    override fun windowActivated(e: WindowEvent) {}
    override fun windowOpened(e: WindowEvent) {}
    override fun windowClosed(e: WindowEvent) {}

    override fun windowIconified(e: WindowEvent) {}

    override fun windowDeiconified(e: WindowEvent) {}
    override fun windowDeactivated(e: WindowEvent) {}
}
