import java.awt.Color

/**
 *
 */


/**
 *
 */
interface ILogParser {

    fun parseLog(strText: String): LogInfo

    fun getColor(logInfo: LogInfo): Color

    fun getLogLV(logInfo: LogInfo): Int

    companion object {
        val TYPE_ANDROID_DDMS = 0
        val TYPE_ANDROID_LOGCAT = 1
    }
}
