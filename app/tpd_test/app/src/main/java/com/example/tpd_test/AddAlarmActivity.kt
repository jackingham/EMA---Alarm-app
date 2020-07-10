package com.example.tpd_test

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_addalarm.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddAlarmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addalarm)

        var alarmTimeMillis = 0L

        btn_pick.setOnClickListener {
            val calendarObject = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                calendarObject.set(Calendar.HOUR_OF_DAY, hour)
                calendarObject.set(Calendar.MINUTE, minute)
                tv_time.text = SimpleDateFormat("HH:mm").format(calendarObject.time)

                alarmTimeMillis = calendarObject.timeInMillis;
                if (alarmTimeMillis < System.currentTimeMillis()){
                    alarmTimeMillis += 86400000 // adds 24 hours for alarms at times already passed in current day
                }


            }
            TimePickerDialog(this, timeSetListener, calendarObject.get(Calendar.HOUR_OF_DAY), calendarObject.get(
                Calendar.MINUTE), true).show()
        }

        btn_add.setOnClickListener{
            val intent = Intent()
            val bundle = Bundle()
            Log.d("testAct", sp_repetition.selectedItem.toString())
            bundle.putLong("unroundedMs", alarmTimeMillis )
            bundle.putString("frequency", sp_repetition.selectedItem.toString())
            intent.putExtras(bundle)
            setResult(RESULT_OK,intent)
            finish()
        }
    }
}