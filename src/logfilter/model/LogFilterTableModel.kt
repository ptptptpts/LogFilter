package logfilter.model

import logfilter.data.LogInfo
import logfilter.util.T
import java.util.*
import javax.swing.table.AbstractTableModel

class LogFilterTableModel : AbstractTableModel() {

    internal var m_arData: ArrayList<LogInfo>? = null

    override fun getColumnCount(): Int {
        return ColName.size
    }

    override fun getRowCount(): Int {
        return if (m_arData != null)
            m_arData!!.size
        else
            0
    }

    override fun getColumnName(col: Int): String {
        return ColName[col]
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return m_arData!![rowIndex].getData(columnIndex)
    }

    fun getRow(row: Int): LogInfo {
        return m_arData!![row]
    }

    fun setData(arData: ArrayList<LogInfo>) {
        m_arData = arData
    }

    companion object {
        val COMUMN_LINE = 0
        val COMUMN_DATE = 1
        val COMUMN_TIME = 2
        val COMUMN_LOGLV = 3
        val COMUMN_PID = 4
        val COMUMN_THREAD = 5
        val COMUMN_TAG = 6
        val COMUMN_BOOKMARK = 7
        val COMUMN_MESSAGE = 8
        val COMUMN_MAX = 9

        private val serialVersionUID = 1L

        var ColName = arrayOf("Line", "Date", "Time", "LogLV", "Pid", "Thread", "Tag", "Bookmark", "Message")
        var ColWidth = intArrayOf(50, 50, 100, 20, 50, 50, 100, 100, 600)
        var DEFULT_WIDTH = intArrayOf(50, 50, 100, 20, 50, 50, 100, 100, 600)

        fun setColumnWidth(nColumn: Int, nWidth: Int) {
            T.d("nWidth = $nWidth")
            if (nWidth >= DEFULT_WIDTH[nColumn])
                ColWidth[nColumn] = nWidth
        }
    }
}
