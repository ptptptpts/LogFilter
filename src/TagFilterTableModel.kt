import java.util.*
import javax.swing.table.AbstractTableModel

class TagFilterTableModel : AbstractTableModel() {

    internal var m_arData: ArrayList<TagInfo>? = null


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

    fun getRow(row: Int): TagInfo {
        return m_arData!![row]
    }

    fun setData(arData: ArrayList<TagInfo>) {
        m_arData = arData
    }

    companion object {
        private val serialVersionUID = 1L

        var ColName = arrayOf("Tag", "Show", "Remove")
        var ColWidth = intArrayOf(120, 30, 30)
    }
}
