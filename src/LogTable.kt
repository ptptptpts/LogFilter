import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.util.*
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableColumnModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableCellRenderer

class LogTable(tablemodel: LogFilterTableModel, internal var m_LogFilterMain: LogFilterMain) : JTable(tablemodel), FocusListener, ActionListener {
    internal lateinit var m_iLogParser: ILogParser
    internal var m_strHighlight: String
    internal var m_strPidShow: String
    internal var m_strTidShow: String
    internal var m_strTagShow: String
    internal var m_strTagRemove: String
    internal var m_strFilterRemove: String
    internal var m_strFilterFind: String
    var fontSize: Float = 0.toFloat()
        internal set
    internal var m_bAltPressed: Boolean = false
    internal var m_nTagLength: Int = 0
    internal var m_arbShow: BooleanArray

    internal val visibleRowCount: Int
        get() = visibleRect.height / getRowHeight()

    init {
        m_strHighlight = ""
        m_strPidShow = ""
        m_strTidShow = ""
        m_strTagShow = ""
        m_strTagRemove = ""
        m_strFilterRemove = ""
        m_strFilterFind = ""
        m_nTagLength = 0
        m_arbShow = BooleanArray(LogFilterTableModel.COMUMN_MAX)
        init()
        setColumnWidth()
    }

    override fun changeSelection(rowIndex: Int, columnIndex: Int, toggle: Boolean, extend: Boolean) {
        var rowIndex = rowIndex
        if (rowIndex < 0) rowIndex = 0
        if (rowIndex > rowCount - 1) rowIndex = rowCount - 1
        super.changeSelection(rowIndex, columnIndex, toggle, extend)
        //        if(getAutoscrolls())
        showRow(rowIndex)
    }

    fun changeSelection(rowIndex: Int, columnIndex: Int, toggle: Boolean, extend: Boolean, bMove: Boolean) {
        var rowIndex = rowIndex
        if (rowIndex < 0) rowIndex = 0
        if (rowIndex > rowCount - 1) rowIndex = rowCount - 1
        super.changeSelection(rowIndex, columnIndex, toggle, extend)
        //        if(getAutoscrolls())
        if (bMove)
            showRow(rowIndex)
    }

    private fun init() {
        val copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false)
        registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED)

        addFocusListener(this)
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
        //        setTableHeader(createTableHeader());
        //        getTableHeader().setReorderingAllowed(false);
        fontSize = 12f
        isOpaque = false
        autoscrolls = false
        //        setRequestFocusEnabled(false);

        //        setGridColor(TABLE_GRID_COLOR);
        intercellSpacing = Dimension(0, 0)
        // turn off grid painting as we'll handle this manually in order to paint
        // grid lines over the entire viewport.
        setShowGrid(false)

        for (iIndex in 0 until columnCount) {
            getColumnModel().getColumn(iIndex).cellRenderer = LogCellRenderer()
        }

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                val p = e!!.point
                val row = rowAtPoint(p)
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.clickCount == 2) {
                        val logInfo = (model as LogFilterTableModel).getRow(row)
                        logInfo.m_bMarked = !logInfo.m_bMarked
                        m_LogFilterMain.bookmarkItem(row, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked)
                    } else if (m_bAltPressed) {
                        val colum = columnAtPoint(p)
                        if (colum == LogFilterTableModel.COMUMN_TAG) {
                            val logInfo = (model as LogFilterTableModel).getRow(row)
                            if (m_strTagShow.contains("|" + logInfo.getData(colum)!!))
                                m_strTagShow = m_strTagShow.replace("|" + logInfo.getData(colum)!!, "")
                            else if (m_strTagShow.contains(logInfo.getData(colum) as String))
                                m_strTagShow = m_strTagShow.replace(logInfo.getData(colum) as String, "")
                            else
                                m_strTagShow += "|" + logInfo.getData(colum)!!
                            m_LogFilterMain.notiEvent(INotiEvent.EventParam(INotiEvent.EVENT_CHANGE_FILTER_SHOW_TAG))
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    val colum = columnAtPoint(p)
                    T.d("m_bAltPressed = $m_bAltPressed")
                    if (m_bAltPressed) {
                        if (colum == LogFilterTableModel.COMUMN_TAG) {
                            T.d()
                            val logInfo = (model as LogFilterTableModel).getRow(row)
                            m_strTagRemove += "|" + logInfo.getData(colum)!!
                            m_LogFilterMain.notiEvent(INotiEvent.EventParam(INotiEvent.EVENT_CHANGE_FILTER_REMOVE_TAG))
                        }
                    } else {
                        T.d()
                        val logInfo = (model as LogFilterTableModel).getRow(row)
                        val data = StringSelection(logInfo.getData(colum) as String)
                        toolkit
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(data, data)
                    }
                }
            }
        })
        getTableHeader().addMouseListener(ColumnHeaderListener())
    }

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return column == LogFilterTableModel.COMUMN_BOOKMARK
    }

    internal fun isInnerRect(parent: Rectangle, child: Rectangle): Boolean {
        return parent.y <= child.y && parent.y + parent.height >= child.y + child.height
    }

    internal fun GetFilterFind(): String {
        return m_strFilterFind
    }

    internal fun GetFilterRemove(): String {
        return m_strFilterRemove
    }

    internal fun GetFilterShowPid(): String {
        return m_strPidShow
    }

    internal fun GetFilterShowTid(): String {
        return m_strTidShow
    }

    internal fun GetFilterShowTag(): String {
        return m_strTagShow
    }

    internal fun GetHighlight(): String {
        return m_strHighlight
    }

    internal fun GetFilterRemoveTag(): String {
        return m_strTagRemove
    }

    internal fun gotoNextBookmark() {
        val nSeletectRow = selectedRow
        val parent = visibleRect

        var logInfo: LogInfo
        for (nIndex in nSeletectRow + 1 until rowCount) {
            logInfo = (model as LogFilterTableModel).getRow(nIndex)
            if (logInfo.m_bMarked) {
                changeSelection(nIndex, 0, false, false)
                var nVisible = nIndex
                if (!isInnerRect(parent, getCellRect(nIndex, 0, true)))
                    nVisible = nIndex + visibleRowCount / 2
                showRow(nVisible)
                return
            }
        }

        for (nIndex in 0 until nSeletectRow) {
            logInfo = (model as LogFilterTableModel).getRow(nIndex)
            if (logInfo.m_bMarked) {
                changeSelection(nIndex, 0, false, false)
                var nVisible = nIndex
                if (!isInnerRect(parent, getCellRect(nIndex, 0, true)))
                    nVisible = nIndex - visibleRowCount / 2
                showRow(nVisible)
                return
            }
        }
    }

    internal fun gotoPreBookmark() {
        val nSeletectRow = selectedRow
        val parent = visibleRect

        var logInfo: LogInfo
        for (nIndex in nSeletectRow - 1 downTo 0) {
            logInfo = (model as LogFilterTableModel).getRow(nIndex)
            if (logInfo.m_bMarked) {
                changeSelection(nIndex, 0, false, false)
                var nVisible = nIndex
                if (!isInnerRect(parent, getCellRect(nIndex, 0, true)))
                    nVisible = nIndex - visibleRowCount / 2
                showRow(nVisible)
                return
            }
        }

        for (nIndex in rowCount - 1 downTo nSeletectRow + 1) {
            logInfo = (model as LogFilterTableModel).getRow(nIndex)
            if (logInfo.m_bMarked) {
                changeSelection(nIndex, 0, false, false)
                var nVisible = nIndex
                if (!isInnerRect(parent, getCellRect(nIndex, 0, true)))
                    nVisible = nIndex + visibleRowCount / 2
                showRow(nVisible)
                return
            }
        }
    }

    fun hideColumn(nColumn: Int) {
        getColumnModel().getColumn(nColumn).width = 0
        getColumnModel().getColumn(nColumn).minWidth = 0
        getColumnModel().getColumn(nColumn).maxWidth = 0
        getColumnModel().getColumn(nColumn).preferredWidth = 0
        getColumnModel().getColumn(nColumn).resizable = false
    }

    override fun processKeyBinding(ks: KeyStroke, e: KeyEvent, condition: Int, pressed: Boolean): Boolean {
        m_bAltPressed = e.isAltDown
        //        if(e.getID() == KeyEvent.KEY_RELEASED)
        run {
            when (e.keyCode) {
                KeyEvent.VK_END -> {
                    changeSelection(rowCount - 1, 0, false, false)
                    return true
                }
                KeyEvent.VK_HOME -> {
                    changeSelection(0, 0, false, false)
                    return true
                }
                KeyEvent.VK_F2 -> {
                    if (e.isControlDown && e.id == KeyEvent.KEY_PRESSED) {
                        val arSelectedRow = selectedRows
                        for (nIndex in arSelectedRow) {
                            val logInfo = (model as LogFilterTableModel).getRow(nIndex)
                            logInfo.m_bMarked = !logInfo.m_bMarked
                            m_LogFilterMain.bookmarkItem(nIndex, Integer.parseInt(logInfo.m_strLine) - 1, logInfo.m_bMarked)
                        }
                        repaint()
                    } else if (!e.isControlDown && e.id == KeyEvent.KEY_PRESSED)
                        gotoPreBookmark()
                    return true
                }
                KeyEvent.VK_F3 -> {
                    if (e.id == KeyEvent.KEY_PRESSED)
                        gotoNextBookmark()
                    return true
                }
                KeyEvent.VK_F -> if (e.id == KeyEvent.KEY_PRESSED && e.modifiers and InputEvent.CTRL_MASK == InputEvent.CTRL_MASK) {
                    m_LogFilterMain.setFindFocus()
                    return true
                }
            }//                case KeyEvent.VK_O:
            //                    if(e.getID() == KeyEvent.KEY_RELEASED)
            //                    {
            //                        m_LogFilterMain.openFileBrowser();
            //                        return true;
            //                    }
        }
        return super.processKeyBinding(ks, e, condition, pressed)
    }

    fun packColumn(vColIndex: Int, margin: Int) {
        val colModel = getColumnModel() as DefaultTableColumnModel
        val col = colModel.getColumn(vColIndex)
        var width = 0

        // Get width of column header
        var renderer: TableCellRenderer? = col.headerRenderer
        if (renderer == null) {
            renderer = getTableHeader().defaultRenderer
        }
        var comp: Component
        //        Component comp = renderer.getTableCellRendererComponent(
        //            this, col.getHeaderValue(), false, false, 0, 0);
        //        width = comp.getPreferredSize().width;

        val viewport = m_LogFilterMain.m_scrollVBar.viewport
        val viewRect = viewport.viewRect
        val nFirst = m_LogFilterMain.m_tbLogTable.rowAtPoint(Point(0, viewRect.y))
        var nLast = m_LogFilterMain.m_tbLogTable.rowAtPoint(Point(0, viewRect.height - 1))

        if (nLast < 0)
            nLast = m_LogFilterMain.m_tbLogTable.rowCount
        // Get maximum width of column data
        for (r in nFirst until nFirst + nLast) {
            renderer = getCellRenderer(r, vColIndex)
            comp = renderer!!.getTableCellRendererComponent(
                    this, getValueAt(r, vColIndex), false, false, r, vColIndex)
            width = Math.max(width, comp.preferredSize.width)
        }

        // Add margin
        width += 2 * margin

        // Set the width
        col.preferredWidth = width
    }

    fun getColumnWidth(nColumn: Int): Int {
        return getColumnModel().getColumn(nColumn).width
    }

    fun showColumn(nColumn: Int, bShow: Boolean) {
        m_arbShow[nColumn] = bShow
        if (bShow) {
            getColumnModel().getColumn(nColumn).resizable = true
            getColumnModel().getColumn(nColumn).maxWidth = LogFilterTableModel.ColWidth[nColumn] * 1000
            getColumnModel().getColumn(nColumn).minWidth = 1
            getColumnModel().getColumn(nColumn).width = LogFilterTableModel.ColWidth[nColumn]
            getColumnModel().getColumn(nColumn).preferredWidth = LogFilterTableModel.ColWidth[nColumn]
        } else
            hideColumn(nColumn)
    }

    fun setColumnWidth() {
        for (iIndex in 0 until columnCount) {
            showColumn(iIndex, true)
        }
        showColumn(LogFilterTableModel.COMUMN_BOOKMARK, false)
        //        showColumn(LogFilterTableModel.COMUMN_THREAD, false);
    }

    internal fun setFilterFind(strFind: String) {
        m_strFilterFind = strFind
    }

    internal fun SetFilterRemove(strRemove: String) {
        m_strFilterRemove = strRemove
    }

    internal fun SetFilterShowTag(strShowTag: String) {
        m_strTagShow = strShowTag
    }

    internal fun SetFilterShowPid(strShowPid: String) {
        m_strPidShow = strShowPid
    }

    internal fun SetFilterShowTid(strShowTid: String) {
        m_strTidShow = strShowTid
    }

    internal fun SetHighlight(strHighlight: String) {
        m_strHighlight = strHighlight
    }

    internal fun SetFilterRemoveTag(strRemoveTag: String) {
        m_strTagRemove = strRemoveTag
    }

    fun setFontSize(nFontSize: Int) {
        fontSize = nFontSize.toFloat()
        setRowHeight(nFontSize + 4)
    }

    fun setLogParser(iLogParser: ILogParser) {
        m_iLogParser = iLogParser
    }

    override fun setValueAt(aValue: Any, row: Int, column: Int) {
        val logInfo = (model as LogFilterTableModel).getRow(row)
        if (column == LogFilterTableModel.COMUMN_BOOKMARK) {
            logInfo.m_strBookmark = aValue as String
            m_LogFilterMain.setBookmark(Integer.parseInt(logInfo.m_strLine) - 1, aValue)
        }
    }

    inner class LogCellRenderer : DefaultTableCellRenderer() {
        internal var m_bChanged: Boolean = false

        override fun getTableCellRendererComponent(table: JTable?,
                                                   value: Any?,
                                                   isSelected: Boolean,
                                                   hasFocus: Boolean,
                                                   row: Int,
                                                   column: Int): Component {
            var value = value
            if (value != null)
                value = remakeData(column, value as String)
            val c = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column)
            val logInfo = (model as LogFilterTableModel).getRow(row)
            c.font = font.deriveFont(fontSize)
            c.foreground = logInfo.m_TextColor
            if (isSelected) {
                if (logInfo.m_bMarked)
                    c.background = Color(LogColor.COLOR_BOOKMARK2)
            } else if (logInfo.m_bMarked)
                c.background = Color(LogColor.COLOR_BOOKMARK)
            else
                c.background = Color.WHITE

            return c
        }

        internal fun remakeData(nIndex: Int, strText: String): String {
            var strText = strText
            if (nIndex != LogFilterTableModel.COMUMN_MESSAGE && nIndex != LogFilterTableModel.COMUMN_TAG) return strText

            val strFind = if (nIndex == LogFilterTableModel.COMUMN_MESSAGE) GetFilterFind() else GetFilterShowTag()
            m_bChanged = false

            strText = strText.replace(" ", "\u00A0")
            if (LogColor.COLOR_HIGHLIGHT != null && LogColor.COLOR_HIGHLIGHT!!.size > 0)
                strText = remakeFind(strText, GetHighlight(), LogColor.COLOR_HIGHLIGHT!![0], true)
            else
                strText = remakeFind(strText, GetHighlight(), "#00FF00", true)
            strText = remakeFind(strText, strFind, "#FF0000", false)
            if (m_bChanged)
                strText = "<html><nobr>$strText</nobr></html>"

            return strText.replace("\t", "    ")
        }

        internal fun remakeFind(strText: String, strFind: String?, arColor: Array<String>?, bUseSpan: Boolean): String {
            var strText = strText
            var strFind = strFind
            if (strFind == null || strFind.length <= 0) return strText

            strFind = strFind.replace(" ", "\u00A0")
            val stk = StringTokenizer(strFind, "|")
            var newText: String
            var strToken: String
            var nIndex = 0

            while (stk.hasMoreElements()) {
                if (nIndex >= arColor!!.size)
                    nIndex = 0
                strToken = stk.nextToken()

                if (strText.toLowerCase().contains(strToken.toLowerCase())) {
                    if (bUseSpan)
                        newText = "<span style=\"background-color:#" + arColor[nIndex] + "\"><b>"
                    else
                        newText = "<font color=#" + arColor[nIndex] + "><b>"
                    newText += strToken
                    if (bUseSpan)
                        newText += "</b></span>"
                    else
                        newText += "</b></font>"
                    strText = strText.replace(strToken, newText)
                    m_bChanged = true
                    nIndex++
                }
            }
            return strText
        }

        internal fun remakeFind(strText: String, strFind: String?, strColor: String?, bUseSpan: Boolean): String {
            var strText = strText
            var strFind = strFind
            if (strFind == null || strFind.length <= 0) return strText

            strFind = strFind.replace(" ", "\u00A0")
            val stk = StringTokenizer(strFind, "|")
            var newText: String
            var strToken: String

            while (stk.hasMoreElements()) {
                strToken = stk.nextToken()

                if (strText.toLowerCase().contains(strToken.toLowerCase())) {
                    if (bUseSpan)
                        newText = "<span style=\"background-color:$strColor\"><b>"
                    else
                        newText = "<font color=$strColor><b>"
                    newText += strToken
                    if (bUseSpan)
                        newText += "</b></span>"
                    else
                        newText += "</b></font>"
                    strText = strText.replace(strToken, newText)
                    m_bChanged = true
                }
            }
            return strText
        }
    }

    fun showRow(row: Int) {
        var row = row
        if (row < 0) row = 0
        if (row > rowCount - 1) row = rowCount - 1

        val rList = visibleRect
        val rCell = getCellRect(row, 0, true)
        if (rList != null && rCell != null) {
            val scrollToRect = Rectangle(rList.getX().toInt(), rCell.getY().toInt(), rList.getWidth().toInt(), rCell.getHeight().toInt())
            scrollRectToVisible(scrollToRect)
        }
    }

    fun showRow(row: Int, bCenter: Boolean) {
        val nLastSelectedIndex = selectedRow

        changeSelection(row, 0, false, false)
        var nVisible = row
        if (nLastSelectedIndex <= row || nLastSelectedIndex == -1)
            nVisible = row + visibleRowCount / 2
        else
            nVisible = row - visibleRowCount / 2
        if (nVisible < 0)
            nVisible = 0
        else if (nVisible > rowCount - 1) nVisible = rowCount - 1
        showRow(nVisible)
    }

    inner class ColumnHeaderListener : MouseAdapter() {
        override fun mouseClicked(evt: MouseEvent?) {

            if (SwingUtilities.isLeftMouseButton(evt!!) && evt.clickCount == 2) {
                val table = (evt.source as JTableHeader).table
                val colModel = table.columnModel

                // The index of the column whose header was clicked
                val vColIndex = colModel.getColumnIndexAtX(evt.x)

                if (vColIndex == -1) {
                    T.d("vColIndex == -1")
                    return
                }
                packColumn(vColIndex, 1)
            }
        }
    }

    override fun focusGained(arg0: FocusEvent) {}

    override fun focusLost(arg0: FocusEvent) {
        m_bAltPressed = false
    }

    override fun actionPerformed(arg0: ActionEvent) {
        var system = Toolkit.getDefaultToolkit().systemClipboard
        val sbf = StringBuffer()
        val numrows = selectedRowCount
        val rowsselected = selectedRows
        //        if ( !( ( numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length )
        //                && ( numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] && numcols == colsselected.length ) ) )
        //        {
        //            JOptionPane.showMessageDialog( null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE );
        //            return;
        //        }

        for (i in 0 until numrows) {
            for (j in m_arbShow.indices) {
                if (j != LogFilterTableModel.COMUMN_LINE && m_arbShow[j]) {
                    val strTemp = StringBuffer(getValueAt(rowsselected[i], j) as String)
                    if (j == LogFilterTableModel.COMUMN_TAG) {
                        val strTag = strTemp.toString()
                        for (k in 0 until m_nTagLength - strTag.length)
                            strTemp.append(" ")
                    } else if (j == LogFilterTableModel.COMUMN_THREAD || j == LogFilterTableModel.COMUMN_PID) {
                        val strTag = strTemp.toString()
                        for (k in 0 until 8 - strTag.length)
                            strTemp.append(" ")
                    }
                    strTemp.append(" ")
                    sbf.append(strTemp)
                }
            }
            sbf.append("\n")
        }
        val stsel = StringSelection(sbf.toString())
        system = Toolkit.getDefaultToolkit().systemClipboard
        system.setContents(stsel, stsel)
    }

    fun setTagLength(nLength: Int) {
        if (m_nTagLength < nLength) {
            m_nTagLength = nLength
            T.d("m_nTagLength = $m_nTagLength")
        }
    }

    companion object {
        private val serialVersionUID = 1L
    }
}
