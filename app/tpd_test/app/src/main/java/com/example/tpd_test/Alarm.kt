
package com.example.tpd_test
import android.hardware.ConsumerIrManager
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="alarms")

data class Alarm(@PrimaryKey(autoGenerate = true) val id: Long = 0, val milliseconds: Long, val frequency: String)