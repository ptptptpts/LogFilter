class TagInfo {

    internal var m_strTag: String? = null
    internal var m_bShow: Boolean = false
    internal var m_bRemove: Boolean = false

    fun getData(nColumn: Int): Any? {
        when (nColumn) {
            COMUMN_TAG -> return m_strTag
            COMUMN_SHOW -> return m_bShow
            COMUMN_REMOVE -> return m_bRemove
        }
        return null
    }

    companion object {
        internal val COMUMN_TAG = 0
        internal val COMUMN_SHOW = 1
        internal val COMUMN_REMOVE = 2
    }
}
