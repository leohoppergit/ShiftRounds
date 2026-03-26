package de.nulide.shiftcal.data.factory

import android.content.Context
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.io.File

//JSON IO
class JIO {

    companion object {

        fun <T> saveAsJSON(context: Context, obj: T, filepath: String) {
            val dir = context.filesDir
            val file = File(dir, filepath)
            saveAsJSON(obj, file)
        }

        fun <T> saveAsJSON(obj: T, file: File) {
            val objectMapper = getObjectMapper()
            objectMapper.writeValue(file, obj)
        }

        fun <T> readAsJSON(context: Context, classType: Class<T>, filepath: String): T {
            val dir = context.filesDir
            val file = File(dir, filepath)
            return readAsJSON(classType, file)
        }

        fun <T> readAsJSON(classType: Class<T>, file: File): T {
            var res = classType.getDeclaredConstructor().newInstance()
            if (!file.exists()) {
                file.createNewFile()
                saveAsJSON(res, file)
            } else {
                val objectMapper = getObjectMapper()
                try {
                    val toAssign = objectMapper.readValue(file, classType)
                    if (toAssign != null) {
                        res = toAssign
                    }
                } catch (mie: MismatchedInputException) {
                    mie.printStackTrace()
                }
            }
            return res
        }

        fun <T> toJSON(obj: T): String {
            val objectMapper = getObjectMapper()
            return objectMapper.writeValueAsString(obj)
        }

        fun <T> fromJSON(json: String, classType: Class<T>): T {
            val objectMapper = getObjectMapper()
            return objectMapper.readValue(json, classType)
        }

        fun getObjectMapper(): ObjectMapper {
            val objectMapper = ObjectMapper()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return objectMapper
        }

    }
}