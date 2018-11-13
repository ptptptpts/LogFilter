package logfilter.data

import logfilter.LogFilterMain
import logfilter.model.LogFilterTableModel
import logfilter.model.TagFilterTableModel
import logfilter.util.T
import java.awt.Component
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JCheckBox
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableColumnModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableCellRenderer

class TagTable(tablemodel: TagFilterTableModel, internal var m_LogFilterMain: LogFilterMain) : JTable(tablemodel) {

    internal val visibleRowCount: Int
        get() = visibleRect.height / getRowHeight()

    init {
        init()
        setColumnWidth()
    }

    private fun init() {
        //        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //        setTableHeader(createTableHeader());
        //        getTableHeader().setReorderingAllowed(false);
        isOpaque = false
        autoscrolls = false
        //        setRequestFocusEnabled(false);

        //        setGridColor(TABLE_GRID_COLOR);
        intercellSpacing = Dimension(0, 0)
        // turn off grid painting as we'll handle this manually in order to paint
        // grid lines over the entire viewport.
        setShowGrid(false)

        for (iIndex in 0 until columnCount) {
            getColumnModel().getColumn(iIndex).cellRenderer = TagCellRenderer()
        }

        getTableHeader().addMouseListener(ColumnHeaderListener())
    }

    fun showColumn(nColumn: Int, bShow: Boolean) {
        if (bShow) {
            getColumnModel().getColumn(nColumn).resizable = true
            getColumnModel().getColumn(nColumn).maxWidth = TagFilterTableModel.ColWidth[nColumn] * 1000
            getColumnModel().getColumn(nColumn).minWidth = 1
            getColumnModel().getColumn(nColumn).width = TagFilterTableModel.ColWidth[nColumn]
            getColumnModel().getColumn(nColumn).preferredWidth = TagFilterTableModel.ColWidth[nColumn]
        } else
            hideColumn(nColumn)
    }

    fun hideColumn(nColumn: Int) {
        getColumnModel().getColumn(nColumn).width = 0
        getColumnModel().getColumn(nColumn).minWidth = 0
        getColumnModel().getColumn(nColumn).maxWidth = 0
        getColumnModel().getColumn(nColumn).preferredWidth = 0
        getColumnModel().getColumn(nColumn).resizable = false
    }

    private fun setColumnWidth() {
        for (iIndex in 0 until columnCount) {
            showColumn(iIndex, true)
        }
    }

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return column == LogFilterTableModel.COMUMN_BOOKMARK
    }

    internal fun isInnerRect(parent: Rectangle, child: Rectangle): Boolean {
        return parent.y <= child.y && parent.y + parent.height >= child.y + child.height
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
        //        Component comp;
        //        Component comp = renderer.getTableCellRendererComponent(
        //            this, col.getHeaderValue(), false, false, 0, 0);
        //        width = comp.getPreferredSize().width;

        //        JViewport viewport = (JViewport)m_LogFilterMain.m_scrollVBar.getViewport();
        //        Rectangle viewRect = viewport.getViewRect();
        //        int nFirst = m_LogFilterMain.m_tbTagTable.rowAtPoint(new Point(0, viewRect.y));
        //        int nLast = m_LogFilterMain.m_tbTagTable.rowAtPoint(new Point(0, viewRect.height - 1));
        //
        //        if(nLast < 0)
        //            nLast = m_LogFilterMain.m_tbTagTable.getRowCount();
        // Get maximum width of column logfilter.data
        //        for (int r=nFirst; r<nFirst + nLast; r++) {
        //            renderer = getCellRenderer(r, vColIndex);
        //            comp = renderer.getTableCellRendererComponent(
        //                this, getValueAt(r, vColIndex), false, false, r, vColIndex);
        //            width = Math.max(width, comp.getPreferredSize().width);
        //        }

        // Add margin
        width += 2 * margin

        // Set the width
        col.preferredWidth = width
    }

    inner class TagCellRenderer : DefaultTableCellRenderer() {
        internal var m_bChanged: Boolean = false

        override fun getTableCellRendererComponent(table: JTable?,
                                                   value: Any,
                                                   isSelected: Boolean,
                                                   hasFocus: Boolean,
                                                   row: Int,
                                                   column: Int): Component {
            val c = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column)

            return if (column == TagInfo.COMUMN_TAG)
                c
            else
                JCheckBox()
        }
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

    companion object {
        private val serialVersionUID = 1L
    }
}
