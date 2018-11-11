import java.awt.Color

class LogInfo {

    internal var m_bMarked: Boolean = false
    internal var m_strBookmark = ""
    internal var m_strDate = ""
    internal var m_strLine = ""
    internal var m_strTime = ""
    internal var m_strLogLV = ""
    internal var m_strPid = ""
    internal var m_strThread = ""
    internal var m_strTag = ""
    internal var m_strMessage = ""
    internal var m_TextColor: Color? = null

    fun display() {
        T.d("=============================================")
        T.d("m_bMarked      = $m_bMarked")
        T.d("m_strBookmark  = $m_strBookmark")
        T.d("m_strDate      = $m_strDate")
        T.d("m_strLine      = $m_strLine")
        T.d("m_strTime      = $m_strTime")
        T.d("m_strLogLV     = $m_strLogLV")
        T.d("m_strPid       = $m_strPid")
        T.d("m_strThread    = $m_strThread")
        T.d("m_strTag       = $m_strTag")
        T.d("m_strMessage   = $m_strMessage")
        T.d("=============================================")
    }

    fun getData(nColumn: Int): Any? {
        when (nColumn) {
            LogFilterTableModel.COMUMN_LINE -> return m_strLine
            LogFilterTableModel.COMUMN_DATE -> return m_strDate
            LogFilterTableModel.COMUMN_TIME -> return m_strTime
            LogFilterTableModel.COMUMN_LOGLV -> return m_strLogLV
            LogFilterTableModel.COMUMN_PID -> return m_strPid
            LogFilterTableModel.COMUMN_THREAD -> return m_strThread
            LogFilterTableModel.COMUMN_TAG -> return m_strTag
            LogFilterTableModel.COMUMN_BOOKMARK -> return m_strBookmark
            LogFilterTableModel.COMUMN_MESSAGE -> return m_strMessage
        }
        return null
    }

    companion object {
        val LOG_LV_VERBOSE = 1
        val LOG_LV_DEBUG = LOG_LV_VERBOSE shl 1
        val LOG_LV_INFO = LOG_LV_DEBUG shl 1
        val LOG_LV_WARN = LOG_LV_INFO shl 1
        val LOG_LV_ERROR = LOG_LV_WARN shl 1
        val LOG_LV_FATAL = LOG_LV_ERROR shl 1
        val LOG_LV_ALL = (LOG_LV_VERBOSE or LOG_LV_DEBUG or LOG_LV_INFO
                or LOG_LV_WARN or LOG_LV_ERROR or LOG_LV_FATAL)
    }
}
