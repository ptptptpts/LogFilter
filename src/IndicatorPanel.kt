import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.ItemListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.util.ArrayList
import javax.swing.JCheckBox
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.collections.HashMap

class IndicatorPanel(internal var m_LogFilterMain: LogFilterMain) : JPanel() {

    internal val INDICATRO_BOOK_X_POS = 5
    internal val INDICATRO_WIDTH = 12
    internal val INDICATRO_ERROR_X_POS = 23
    internal val INDICATRO_Y_POS = 22
    internal val INDICATRO_Y_GAP = 5

    internal var m_rcBookmark: Rectangle
    internal var m_rcError: Rectangle
    internal var m_chBookmark: JCheckBox = JCheckBox()
    internal var m_chError: JCheckBox = JCheckBox()
    internal var m_arLogInfo: ArrayList<LogInfo>? = null
    internal var m_hmBookmark: HashMap<Int, Int> = HashMap()
    internal var m_hmError: HashMap<Int, Int> = HashMap()
    internal lateinit var m_g: Graphics
    var m_bDrawFull: Boolean = false

    internal var PAGE_INDICATOR_WIDTH = 3
    internal var PAGE_INDICATOR_GAP = 2

    internal var m_itemListener: ItemListener = ItemListener { itemEvent ->
        if (itemEvent.source == m_chBookmark) {
            m_LogFilterMain.notiEvent(INotiEvent.EventParam(INotiEvent.EVENT_CLICK_BOOKMARK))
        } else if (itemEvent.source == m_chError) {
            m_LogFilterMain.notiEvent(INotiEvent.EventParam(INotiEvent.EVENT_CLICK_ERROR))
        }
    }


    init {
        //        m_chBookmark.setBackground( new Color( 555555 ) );
        m_chBookmark.addItemListener(m_itemListener)
        m_chBookmark.border = EmptyBorder(0, 0, 0, 0)

        m_chError.addItemListener(m_itemListener)
        m_chError.border = EmptyBorder(0, 0, 0, 0)

        m_rcBookmark = Rectangle()
        m_rcError = Rectangle()
        m_bDrawFull = true
        add(m_chBookmark)
        add(m_chError)

        addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent) {}
            override fun mousePressed(e: MouseEvent) {
                if (m_arLogInfo != null) {
                    val fRate = (e.y - m_rcBookmark.y).toFloat() / m_rcBookmark.height.toFloat()
                    val nIndex = (m_arLogInfo!!.size * fRate).toInt()
                    m_LogFilterMain.m_tbLogTable.showRow(nIndex, false)
                }
            }

            override fun mouseExited(e: MouseEvent) {}

            override fun mouseEntered(e: MouseEvent) {}
            override fun mouseClicked(e: MouseEvent) {}
        })
        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent) {}
            override fun mouseDragged(e: MouseEvent) {
                if (m_arLogInfo != null) {
                    val fRate = (e.y - m_rcBookmark.y).toFloat() / m_rcBookmark.height.toFloat()
                    val nIndex = (m_arLogInfo!!.size * fRate).toInt()
                    m_LogFilterMain.m_tbLogTable.showRow(nIndex, false)
                }
            }
        })
        addMouseWheelListener { e -> m_LogFilterMain.m_scrollVBar.dispatchEvent(e) }
    }

    fun testMsg(strMsg: String) {
        JOptionPane.showMessageDialog(this, strMsg)
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        m_g = g
        m_rcBookmark.setBounds(INDICATRO_BOOK_X_POS, INDICATRO_Y_POS, INDICATRO_WIDTH, height - INDICATRO_Y_POS - INDICATRO_Y_GAP)
        m_rcError.setBounds(INDICATRO_ERROR_X_POS, INDICATRO_Y_POS, INDICATRO_WIDTH, height - INDICATRO_Y_POS - INDICATRO_Y_GAP)
        if (m_bDrawFull) {
            drawIndicator(m_g)
        }
        drawBookmark(m_g)
        drawError(m_g)
        drawPageIndicator(m_g)

        m_bDrawFull = true
    }

    internal fun drawIndicator(g: Graphics) {
        if (m_arLogInfo == null) return

        val TOTAL_COUNT = m_arLogInfo!!.size

        if (TOTAL_COUNT > 0) {
            var HEIGHT = 1
            val MIN_HEIGHT = 1
            val fRate = m_rcBookmark.height.toFloat() / TOTAL_COUNT.toFloat()
            if (m_rcBookmark.height > TOTAL_COUNT)
                HEIGHT = m_rcBookmark.height / TOTAL_COUNT + 1

            //�ϸ�ũ indicator�� �׸���.
            for (nIndex in m_hmBookmark.keys) {
                if (m_LogFilterMain.m_nChangedFilter == LogFilterMain.STATUS_CHANGE || m_LogFilterMain.m_nChangedFilter == LogFilterMain.STATUS_PARSING)
                    break
                val nY1 = (INDICATRO_Y_POS + m_hmBookmark[nIndex]!!.times(fRate)).toInt()
                var nY2 = nY1 + HEIGHT
                if (nY2 - nY1 <= 0)
                    nY2 = nY1 + MIN_HEIGHT
                if (nY2 > m_rcBookmark.y + m_rcBookmark.height)
                    nY2 = m_rcBookmark.y + m_rcBookmark.height
                g.color = Color.BLUE
                g.fillRect(m_rcBookmark.x, nY1, m_rcBookmark.width, nY2 - nY1)
            }


            //���� indicator�� �׸���.
            for (nIndex in m_hmError.keys) {
                if (m_LogFilterMain.m_nChangedFilter == LogFilterMain.STATUS_CHANGE || m_LogFilterMain.m_nChangedFilter == LogFilterMain.STATUS_PARSING)
                    break
                val nY1 = (INDICATRO_Y_POS + m_hmError[nIndex]!!.times(fRate)).toInt()
                var nY2 = nY1 + HEIGHT
                if (nY2 - nY1 <= 0)
                    nY2 = nY1 + MIN_HEIGHT
                if (nY2 > m_rcError.y + m_rcError.height)
                    nY2 = m_rcError.y + m_rcError.height
                g.color = Color.RED
                g.fillRect(m_rcError.x, nY1, m_rcError.width, nY2 - nY1)
            }
        }
    }

    internal fun drawBookmark(g: Graphics) {
        g.color = Color.BLUE
        g.drawRect(m_rcBookmark.x, m_rcBookmark.y, m_rcBookmark.width, m_rcBookmark.height)
    }

    internal fun drawError(g: Graphics) {
        g.color = Color.RED
        g.drawRect(m_rcError.x, m_rcError.y, m_rcError.width, m_rcError.height)
    }

    internal fun drawPageIndicator(g: Graphics) {
        if (m_arLogInfo == null) return

        val TOTAL_COUNT = m_arLogInfo!!.size

        if (TOTAL_COUNT > 0) {
            val viewport = m_LogFilterMain.m_scrollVBar.viewport
            val viewRect = viewport.viewRect

            val nItemHeight = m_LogFilterMain.m_tbLogTable.rowHeight
            if (nItemHeight > 0) {
                val fRate = m_rcBookmark.height.toFloat() / TOTAL_COUNT.toFloat()

                val nFirst = m_LogFilterMain.m_tbLogTable.rowAtPoint(Point(0, viewRect.y))
                val nLast = m_LogFilterMain.m_tbLogTable.rowAtPoint(Point(0, viewRect.height - 1))
                val nY1 = (m_rcBookmark.y + nFirst * fRate).toInt()
                var nH = ((nLast + 1) * fRate).toInt()
                if (nH <= 0)
                    nH = 1
                if (nY1 + nH > m_rcBookmark.y + m_rcBookmark.height)
                    nH = m_rcBookmark.y + m_rcBookmark.height - nY1
                if (nLast == -1)
                    nH = m_rcBookmark.height

                g.drawRect(m_rcBookmark.x - PAGE_INDICATOR_WIDTH - PAGE_INDICATOR_GAP, nY1, PAGE_INDICATOR_WIDTH, nH)
                g.drawRect(m_rcError.x + m_rcError.width + PAGE_INDICATOR_GAP, nY1, PAGE_INDICATOR_WIDTH, nH)
            }
        }
    }

    fun setData(arLogInfo: ArrayList<LogInfo>, hmBookmark: HashMap<Int, Int>, hmError: HashMap<Int, Int>) {
        m_arLogInfo = arLogInfo
        m_hmBookmark = hmBookmark
        m_hmError = hmError
    }

    companion object {
        private val serialVersionUID = 1L
    }
}
