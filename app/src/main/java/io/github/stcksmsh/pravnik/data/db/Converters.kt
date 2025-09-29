package io.github.stcksmsh.pravnik.data.db

import androidx.room.TypeConverter
import java.time.LocalDate

object Converters {
    @TypeConverter
    @JvmStatic
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    @JvmStatic
    fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
