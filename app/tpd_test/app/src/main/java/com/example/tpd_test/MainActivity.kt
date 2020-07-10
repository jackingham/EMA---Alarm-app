package com.example.tpd_test

import android.app.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmRecyclerView.layoutManager = LinearLayoutManager(this)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(AlarmViewModel::class.java)

        viewModel.getAllAlarms().observe(this, Observer {
            val sortedList = it.sortedBy {it.milliseconds}
            alarmRecyclerView.adapter = MyAdapter(sortedList)
            val adapter = alarmRecyclerView.adapter as? MyAdapter
            adapter?.alarms = sortedList
            adapter?.notifyDataSetChanged()
        })

        val receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
               val firedAlarmID = intent!!.getLongExtra("alarmID", -1)
                when (intent.action) {
                    "alarmTask" ->   handleAlarm(0, firedAlarmID)
                    "alarmTaskDaily" ->   handleAlarm(1, firedAlarmID)
                    "alarmTaskWeekly" ->   handleAlarm(2, firedAlarmID)

                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("alarmTask")
            addAction("alarmTaskDaily")
            addAction("alarmTaskWeekly")
        }
        registerReceiver(receiver, filter)





        fab_picker.setOnClickListener {
            val intent = Intent(this, AddAlarmActivity::class.java)
            startActivityForResult(intent,0)
        }

    }


    fun handleAlarm(typeCode : Int, alarmID: Long) {
        Toast.makeText(applicationContext,"Alarm triggered at ${SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)}",Toast.LENGTH_SHORT).show()

        val notification = Notification.Builder(this).setContentTitle("Alarm!")
            .setContentText("Time is now ${SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)}")
            .setSmallIcon(R.drawable.ic_alarm_black_24dp)
            .build()
        val nMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nMgr.notify(1, notification) // id is a unique ID for this notification
        val amgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val db = AlarmDatabase.getDatabase(application)

        if ((typeCode == 1) || (typeCode == 2)) {  //resetting the alarm
            var newAlarmMs = 0L
            val newAlarmIntent = Intent()

            if (typeCode ==1){
                newAlarmMs =(System.currentTimeMillis()+86400000)
                newAlarmIntent.setAction("alarmTaskDaily")
            } else if (typeCode ==2){
                newAlarmMs = (System.currentTimeMillis()+604800000)
                newAlarmIntent.setAction("alarmTaskWeekly")
            }

            newAlarmIntent.putExtra("alarmID", alarmID)
            val  pi  = PendingIntent.getBroadcast(this, alarmID.toInt(), newAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            Log.d("TAG","Alarm reset")
            amgr.setExact(AlarmManager.RTC_WAKEUP, newAlarmMs , pi)
            Toast.makeText(applicationContext,"Alarm reset",Toast.LENGTH_SHORT).show()
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    db.alarmDao().updateAlarmMs(alarmID, newAlarmMs)
                }
            }
        } else if (typeCode == 0){
            val pi = PendingIntent.getBroadcast(this, alarmID.toInt(), Intent("alarmTask"), PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.cancel(pi)
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                     db.alarmDao().deleteTriggeredAlarm(alarmID)
                }
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                val extras = intent?.extras
                val newAlarmMs = extras?.getLong("unroundedMs")
                val newAlarmFrequency = extras?.getString("frequency")
                if ((newAlarmMs != null) && (newAlarmFrequency != null)){
                    val amgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val db = AlarmDatabase.getDatabase(application)

                    val roundedMs = (newAlarmMs/60000)*60000//removes the seconds from the time
                    val alarmIntent = Intent()
                    when (newAlarmFrequency){
                        "Once" -> alarmIntent.setAction("alarmTask")
                        "Daily" ->  alarmIntent.setAction("alarmTaskDaily")
                        "Weekly" -> alarmIntent.setAction( "alarmTaskWeekly")
                        else -> null
                    }
                    var newAlarmID = 0L
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val alarm1 = Alarm(milliseconds = roundedMs, frequency = newAlarmFrequency)
                            newAlarmID =  db.alarmDao().insert(alarm1)
                            Log.d("TAG","ID updated to $newAlarmID")

                        }
                    }
                    Thread.sleep(100) // lets the coroutine catch up with the main program and change newAlarmID
                    //I tested this and the routine finishes 16ms too slow so this gives plenty of time without locking the UI too long
                    alarmIntent.putExtra("alarmID", newAlarmID)
                    val  pi  = PendingIntent.getBroadcast(this, newAlarmID.toInt(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    Log.d("TAG","Added alarm with ID $newAlarmID")
                    amgr.setExact(AlarmManager.RTC_WAKEUP, roundedMs , pi)
                    Toast.makeText(applicationContext,"Alarm set",Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

}