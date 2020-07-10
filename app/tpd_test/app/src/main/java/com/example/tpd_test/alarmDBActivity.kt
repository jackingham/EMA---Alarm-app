package com.example.tpd_test

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Alarm::class), version = 1, exportSchema = false)
public abstract class AlarmDatabase: RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        private var instance: AlarmDatabase? = null

        fun getDatabase(ctx:Context) : AlarmDatabase {
            var tmpInstance = instance
            if(tmpInstance == null) {
                tmpInstance = Room.databaseBuilder(
                    ctx.applicationContext,
                    AlarmDatabase::class.java,
                    "alarmDatabase"
                ).build()
                instance = tmpInstance
            }
            return tmpInstance
        }
    }
}