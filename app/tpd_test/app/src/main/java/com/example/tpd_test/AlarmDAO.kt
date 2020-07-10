package com.example.tpd_test

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms WHERE id=:id")
    fun getAlarmById(id: Long): Alarm?

    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): LiveData<List<Alarm>>

    @Query("UPDATE alarms SET milliseconds=:milliseconds WHERE id=:id")
    fun updateAlarmMs(id: Long, milliseconds: Long)

    @Query("DELETE FROM alarms WHERE id=:id")
    fun deleteTriggeredAlarm(id: Long)

    @Insert
    fun insert(alarm: Alarm) : Long

    @Delete
    fun delete(alarm: Alarm)
}
