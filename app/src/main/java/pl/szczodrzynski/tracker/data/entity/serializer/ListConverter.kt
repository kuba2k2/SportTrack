package pl.szczodrzynski.tracker.data.entity.serializer

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import androidx.room.util.joinIntoString
import androidx.room.util.splitToIntList

class ListConverter {

	@TypeConverter
	@SuppressLint("RestrictedApi")
	fun toListInt(value: String?) = splitToIntList(value)

	@TypeConverter
	@SuppressLint("RestrictedApi")
	fun fromListInt(value: List<Int>?) = joinIntoString(value)
}
