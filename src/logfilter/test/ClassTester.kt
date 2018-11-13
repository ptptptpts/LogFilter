package logfilter.test

/**
 *
 */
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

object ClassTester {
    fun functionTest(cls: Class<*>, strMethod: String, vararg params: Any) {
        try {
            var method: Method?

            //테스트 값을 받은경우
            if (params.size > 0) {
                method = getMethod(cls, strMethod)
                method?.invoke(cls.getDeclaredConstructor().newInstance(), *params)
            } else {
                method = getMethod(cls, strMethod)
                val arParams = method!!.parameterTypes
                val arValues = arrayOfNulls<Any>(arParams.size)

                for (iIndex in arParams.indices) {
                    arValues[iIndex] = getDefaultValue(arParams[iIndex])
                }
                for (iIndex in arParams.indices) {
                    val param = createParam(arParams[iIndex])
                    if (param is ICheckValue) {
                        exeDefaultType(cls, method, param, iIndex, arValues)
                    } else {
                        disply(method, arValues, "user param")
                        val result = method.invoke(cls.getDeclaredConstructor().newInstance(), *arValues)
                        print("result = $result")
                        print("\n")
                    }
                }
            }

        } catch (e: Throwable) {
            print("e = $e")
            print("\n")
            e.printStackTrace()
            print("\n")
        }

    }

    fun disply(method: Method, params: Array<Any?>, strCheckValue: String) {
        print("=======================================================")
        print("\n")
        print("Test method [" + method.name + "()]")
        print("\n")
        print("Test value [$strCheckValue()]")
        print("\n")
        for (param in params) {
            print("param : $param")
            print("\n")
        }
        print("=======================================================")
        print("\n")
    }

    fun exeDefaultType(cls: Class<*>, method: Method, checkType: Any, iPosition: Int, arValues: Array<Any?>) {
        try {
            val checkMethod = ICheckValue::class.java.methods
            for (iIndex in checkMethod.indices) {
                arValues[iPosition] = checkMethod[iIndex].invoke(checkType)
                disply(method, arValues, checkMethod[iIndex].name)
                val result = method.invoke(cls.getDeclaredConstructor().newInstance(), *arValues)
                print("result = $result")
                print("\n")
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }

    }

    fun getDefaultValue(cls: Class<*>): Any? {
        var param: Any?
        try {
            param = createParam(cls)
            return (param as? ICheckValue)?.middleValue ?: cls.getDeclaredConstructor().newInstance()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }

        return null
    }

    fun createParam(cls: Class<*>): Any {
        if (cls.name == "java.lang.String")
            return CheckString()
        return if (cls.name == "java.lang.Byte" || cls.name == "byte")
            CheckByte()
        else if (cls.name == "java.lang.Short" || cls.name == "short")
            CheckShort()
        else if (cls.name == "java.lang.Long" || cls.name == "long")
            CheckLong()
        else if (cls.name == "java.lang.Integer" || cls.name == "int")
            CheckInteger()
        else if (cls.name == "java.lang.Float" || cls.name == "float")
            CheckFloat()
        else if (cls.name == "java.lang.Double" || cls.name == "double")
            CheckDouble()
        else if (cls.name == "java.lang.Booleane" || cls.name == "boolean")
            CheckBoolean()
        else if (cls.name == "java.lang.Character" || cls.name == "char")
            CheckCharacter()
        else
            cls
    }

    fun getMethod(cls: Class<*>, strMethod: String): Method? {
        val arMethod = cls.methods
        for (methodTemp in arMethod) {
            if (methodTemp.name == strMethod) {
                return methodTemp
            }
        }
        return null
    }
}

internal class CheckString : ICheckValue {
    override val maxValue: String
        get() = "aslkjf;alskjf;alskdjf;alskdjfaskldjf;laksjdf;laskjdf;aslkdfja;slkdfja;sldkfja;sldkfjas;ldkfja;sldkfja;sldkfjas;ldkfjasldfka;slkdfa;slkdfjasldkfas;ldkfjas;lkdfjas;ldkfjas;dlkfjsd;lfksa"
    override val middleValue: String
        get() = "asdfasdfasdfasdfasfafasdfasdfasdfasdfasdf"
    override val minValue: String
        get() = ""
    override val `null`: String?
        get() = null
    override val random: String
        get() = middleValue
}

internal class CheckByte : ICheckValue {
    override val maxValue: Byte?
        get() = java.lang.Byte.MAX_VALUE
    override val middleValue: Byte?
        get() = 0
    override val minValue: Byte?
        get() = java.lang.Byte.MIN_VALUE
    override val `null`: Byte?
        get() = -1
    override val random: Byte?
        get() = Random().nextInt(java.lang.Byte.MAX_VALUE.toInt()).toByte()
}

internal class CheckShort : ICheckValue {
    override val maxValue: Short?
        get() = java.lang.Short.MAX_VALUE
    override val middleValue: Short?
        get() = 0
    override val minValue: Short?
        get() = java.lang.Short.MIN_VALUE
    override val `null`: Short?
        get() = -1
    override val random: Short?
        get() = Random().nextInt(java.lang.Short.MAX_VALUE.toInt()).toShort()
}

internal class CheckInteger : ICheckValue {
    override val maxValue: Int?
        get() = Integer.MAX_VALUE
    override val middleValue: Int?
        get() = 0
    override val minValue: Int?
        get() = Integer.MIN_VALUE
    override val `null`: Int?
        get() = -1
    override val random: Int?
        get() = Random().nextInt()
}

internal class CheckLong : ICheckValue {
    override val maxValue: Long?
        get() = java.lang.Long.MAX_VALUE
    override val middleValue: Long?
        get() = 0.toLong()
    override val minValue: Long?
        get() = java.lang.Long.MIN_VALUE
    override val `null`: Long?
        get() = (-1).toLong()
    override val random: Long?
        get() = Random().nextLong()
}

internal class CheckFloat : ICheckValue {
    override val maxValue: Float?
        get() = java.lang.Float.MAX_VALUE
    override val middleValue: Float?
        get() = 0.toFloat()
    override val minValue: Float?
        get() = java.lang.Float.MIN_VALUE
    override val `null`: Float?
        get() = (-1).toFloat()
    override val random: Float?
        get() = Random().nextFloat()
}

internal class CheckDouble : ICheckValue {
    override val maxValue: Double?
        get() = java.lang.Double.MAX_VALUE
    override val middleValue: Double?
        get() = java.lang.Double.MAX_VALUE / 2
    override val minValue: Double?
        get() = java.lang.Double.MIN_VALUE
    override val `null`: Double?
        get() = 0.toDouble()
    override val random: Double?
        get() = Random().nextDouble()
}

internal class CheckBoolean : ICheckValue {
    override val maxValue: Boolean?
        get() = true
    override val middleValue: Boolean?
        get() = false
    override val minValue: Boolean?
        get() = false
    override val `null`: Boolean?
        get() = false
    override val random: Boolean?
        get() = Random().nextBoolean()
}

internal class CheckCharacter : ICheckValue {
    override val maxValue: Char?
        get() = Character.MAX_VALUE
    override val middleValue: Char?
        get() = (Character.MAX_VALUE.toInt() / 2).toChar()
    override val minValue: Char?
        get() = Character.MIN_VALUE
    override val `null`: Char?
        get() = null
    override val random: Char?
        get() = Random().nextInt(Character.MAX_VALUE.toInt()).toChar()
}

internal interface ICheckValue {
    val maxValue: Any?
    val middleValue: Any?
    val minValue: Any?
    val `null`: Any?
    val random: Any?
}
