package com.callguard.history

import android.content.Context
import androidx.room.*

@Database(
    entities = [CallLogEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CallHistoryDatabase : RoomDatabase() {
    abstract fun callHistoryDao(): CallHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CallHistoryDatabase? = null

        fun getInstance(context: Context): CallHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CallHistoryDatabase::class.java,
                    "callguard_history.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromCallType(value: CallType): String = value.name

    @TypeConverter
    fun toCallType(value: String): CallType = CallType.valueOf(value)

    @TypeConverter
    fun fromAIIntent(value: AIIntent?): String? = value?.name

    @TypeConverter
    fun toAIIntent(value: String?): AIIntent? = value?.let { AIIntent.valueOf(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString("|||")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split("|||")?.filter { it.isNotEmpty() }
    }
}
