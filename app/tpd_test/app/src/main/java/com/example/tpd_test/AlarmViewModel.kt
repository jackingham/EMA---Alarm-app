package com.example.tpd_test

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class AlarmViewModel(app: Application): AndroidViewModel(app) {

    var db = AlarmDatabase.getDatabase(app)
    var alarms: LiveData<List<Alarm>>

    init {
        alarms = db.alarmDao().getAllAlarms()
    }

    fun insert(alarm: Alarm) {
        db.alarmDao().insert(alarm)
    }

    fun updateAlarmMs(id: Long, milliseconds: Long){
        db.alarmDao().updateAlarmMs(id, milliseconds)
    }

    fun deleteTriggeredAlarm(id: Long){
        db.alarmDao().deleteTriggeredAlarm(id)
    }

    fun getAllAlarms(): LiveData<List<Alarm>> {
        return alarms
    }
}