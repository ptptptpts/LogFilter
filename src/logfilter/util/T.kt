package logfilter.util

import java.text.SimpleDateFormat
import java.util.*

/*
***************************************************************************
**          WiseStone Co. Ltd. CONFIDENTIAL AND PROPRIETARY
**        This source is the sole property of WiseStone Co. Ltd.
**      Reproduction or utilization of this source in whole or in part
**    is forbidden without the written consent of WiseStone Co. Ltd.
***************************************************************************
**                 Copyright (c) 2007 WiseStone Co. Ltd.
**                           All Rights Reserved
***************************************************************************
** Revision History:
** Author                 Date          Version      Description of Changes
** ------------------------------------------------------------------------
** dhwoo     2010. 3. 12.        1.0              Created
*/

object T {
    //	private final static String PREFIX = "LogFilter";
    private val POSTFIX = "[iookill]"
    private var misEnabled: Boolean? = true

    val currentTime: String
        get() {
            val time = System.currentTimeMillis()

            val dayTime = SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS")

            return dayTime.format(Date(time))

        }

    fun enable(isEnable: Boolean?) {
        misEnabled = isEnable
    }

    fun e() {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]")
        }
    }

    fun e(strMsg: Any) {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]" +
                    strMsg)
        }
    }

    fun w() {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]")
        }
    }

    fun w(strMsg: Any) {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]" +
                    strMsg)
        }
    }

    fun i() {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]")
        }
    }

    fun i(strMsg: Any) {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]" +
                    strMsg)
        }
    }

    fun d() {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]")
        }
    }

    fun d(strMsg: Any) {
        if (misEnabled!!) {
            val e = Exception()
            val callerElement = e.stackTrace[1]
            println(currentTime +
                    POSTFIX + "[" +
                    callerElement.fileName + ":" +
                    callerElement.methodName + ":" +
                    callerElement.lineNumber + "]" +
                    strMsg)
        }
    }
}
