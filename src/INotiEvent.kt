/**
 *
 */

/**
 *
 */
interface INotiEvent {

    fun notiEvent(param: EventParam)

    class EventParam(internal var nEventId: Int) {
        internal var param1: Any? = null
        internal var param2: Any? = null
        internal var param3: Any? = null
    }

    companion object {
        val EVENT_CLICK_BOOKMARK = 0
        val EVENT_CLICK_ERROR = 1
        val EVENT_CHANGE_FILTER_SHOW_TAG = 2
        val EVENT_CHANGE_FILTER_REMOVE_TAG = 3
        val EVENT_CHANGE_FILTER_FIND_WORD = 4
        val EVENT_CHANGE_FILTER_REMOVE_WORD = 5
    }
}
