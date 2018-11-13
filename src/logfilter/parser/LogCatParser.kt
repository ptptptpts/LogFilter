package logfilter.parser

import logfilter.data.LogColor
import logfilter.data.LogInfo
import java.awt.Color
import java.util.*

/**
 *
 */

/**
 *
 */
class LogCatParser : ILogParser {
    internal val TOKEN_KERNEL = "<>[]"
    internal val TOKEN_SPACE = " "
    internal val TOKEN_SLASH = "/"
    internal val TOKEN = "/()"
    internal val TOKEN_PID = "/() "
    internal val TOKEN_MESSAGE = "'"

    override fun getColor(logInfo: LogInfo): Color {
        if (logInfo.m_strLogLV == null) return Color.BLACK

        if (logInfo.m_strLogLV == "FATAL" || logInfo.m_strLogLV == "F")
            return Color(LogColor.COLOR_FATAL)
        return if (logInfo.m_strLogLV == "ERROR" || logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "3")
            Color(LogColor.COLOR_ERROR)
        else if (logInfo.m_strLogLV == "WARN" || logInfo.m_strLogLV == "W" || logInfo.m_strLogLV == "4")
            Color(LogColor.COLOR_WARN)
        else if (logInfo.m_strLogLV == "INFO" || logInfo.m_strLogLV == "I" || logInfo.m_strLogLV == "6")
            Color(LogColor.COLOR_INFO)
        else if (logInfo.m_strLogLV == "DEBUG" || logInfo.m_strLogLV == "D" || logInfo.m_strLogLV == "7")
            Color(LogColor.COLOR_DEBUG)
        else if (logInfo.m_strLogLV == "0")
            Color(LogColor.COLOR_0)
        else if (logInfo.m_strLogLV == "1")
            Color(LogColor.COLOR_1)
        else if (logInfo.m_strLogLV == "2")
            Color(LogColor.COLOR_2)
        else if (logInfo.m_strLogLV == "5")
            Color(LogColor.COLOR_5)
        else
            Color.BLACK
    }

    override fun getLogLV(logInfo: LogInfo): Int {
        if (logInfo.m_strLogLV == null) return LogInfo.LOG_LV_VERBOSE

        if (logInfo.m_strLogLV == "FATAL" || logInfo.m_strLogLV == "F")
            return LogInfo.LOG_LV_FATAL
        return if (logInfo.m_strLogLV == "ERROR" || logInfo.m_strLogLV == "E")
            LogInfo.LOG_LV_ERROR
        else if (logInfo.m_strLogLV == "WARN" || logInfo.m_strLogLV == "W")
            LogInfo.LOG_LV_WARN
        else if (logInfo.m_strLogLV == "INFO" || logInfo.m_strLogLV == "I")
            LogInfo.LOG_LV_INFO
        else if (logInfo.m_strLogLV == "DEBUG" || logInfo.m_strLogLV == "D")
            LogInfo.LOG_LV_DEBUG
        else
            LogInfo.LOG_LV_VERBOSE
    }

    //04-17 09:01:18.910 D/LightsService(  139): BKL : 106
    fun isNormal(strText: String): Boolean {
        if (strText.length < 22) return false

        val strLevel = strText.substring(19, 21)
        return (strLevel == "D/"
                || strLevel == "V/"
                || strLevel == "I/"
                || strLevel == "W/"
                || strLevel == "E/"
                || strLevel == "F/")
    }

    //04-20 12:06:02.125   146   179 D BatteryService: update start
    fun isThreadTime(strText: String): Boolean {
        if (strText.length < 34) return false

        val strLevel = strText.substring(31, 33)
        return (strLevel == "D "
                || strLevel == "V "
                || strLevel == "I "
                || strLevel == "W "
                || strLevel == "E "
                || strLevel == "F ")
    }

    //    <4>[19553.494855] [DEBUG] USB_SEL(1) HIGH set USB mode
    fun isKernel(strText: String): Boolean {
        if (strText.length < 18) return false

        val strLevel = strText.substring(1, 2)
        return (strLevel == "0"
                || strLevel == "1"
                || strLevel == "2"
                || strLevel == "3"
                || strLevel == "4"
                || strLevel == "5"
                || strLevel == "6"
                || strLevel == "7")
    }

    fun getNormal(strText: String): LogInfo {
        val logInfo = LogInfo()

        val stk = StringTokenizer(strText, TOKEN_PID, false)
        if (stk.hasMoreElements())
            logInfo.m_strDate = stk.nextToken()
        if (stk.hasMoreElements())
            logInfo.m_strTime = stk.nextToken()
        if (stk.hasMoreElements())
            logInfo.m_strLogLV = stk.nextToken().trim { it <= ' ' }
        if (stk.hasMoreElements())
            logInfo.m_strTag = stk.nextToken()
        if (stk.hasMoreElements())
            logInfo.m_strPid = stk.nextToken().trim { it <= ' ' }
        if (stk.hasMoreElements()) {
            logInfo.m_strMessage = stk.nextToken(TOKEN_MESSAGE)
            while (stk.hasMoreElements()) {
                logInfo.m_strMessage += stk.nextToken(TOKEN_MESSAGE)
            }
            logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("\\): ".toRegex(), "")
        }
        logInfo.m_TextColor = getColor(logInfo)
        return logInfo
    }

    fun getThreadTime(strText: String): LogInfo {
        val logInfo = LogInfo()

        val stk = StringTokenizer(strText, TOKEN_SPACE, false)
        if (stk.hasMoreElements())
            logInfo.m_strDate = stk.nextToken()
        if (stk.hasMoreElements())
            logInfo.m_strTime = stk.nextToken()
        if (stk.hasMoreElements())
            logInfo.m_strPid = stk.nextToken().trim { it <= ' ' }
        if (stk.hasMoreElements())
            logInfo.m_strThread = stk.nextToken().trim { it <= ' ' }
        if (stk.hasMoreElements())
            logInfo.m_strLogLV = stk.nextToken().trim { it <= ' ' }
        if (stk.hasMoreElements())
            logInfo.m_strTag = stk.nextToken()
        if (stk.hasMoreElements()) {
            logInfo.m_strMessage = stk.nextToken(TOKEN_MESSAGE)
            while (stk.hasMoreElements()) {
                logInfo.m_strMessage += stk.nextToken(TOKEN_MESSAGE)
            }
            logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("\\): ".toRegex(), "")
        }
        logInfo.m_TextColor = getColor(logInfo)
        return logInfo
    }

    fun getKernel(strText: String): LogInfo {
        val logInfo = LogInfo()

        val stk = StringTokenizer(strText, TOKEN_KERNEL, false)
        if (stk.hasMoreElements())
            logInfo.m_strLogLV = stk.nextToken()
        if (stk.hasMoreElements())
            logInfo.m_strTime = stk.nextToken()
        if (stk.hasMoreElements()) {
            logInfo.m_strMessage = stk.nextToken(TOKEN_KERNEL)
            while (stk.hasMoreElements()) {
                logInfo.m_strMessage += " " + stk.nextToken(TOKEN_SPACE)
            }
            logInfo.m_strMessage = logInfo.m_strMessage.replaceFirst("  ".toRegex(), "")
        }
        logInfo.m_TextColor = getColor(logInfo)
        return logInfo
    }

    override fun parseLog(strText: String): LogInfo {
        if (isNormal(strText))
            return getNormal(strText)
        else if (isThreadTime(strText))
            return getThreadTime(strText)
        else if (isKernel(strText))
            return getKernel(strText)
        else {
            val logInfo = LogInfo()
            logInfo.m_strMessage = strText
            return logInfo
        }
    }
}
