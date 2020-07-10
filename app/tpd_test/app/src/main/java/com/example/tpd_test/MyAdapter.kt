package com.example.tpd_test

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*


import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

class MyAdapter(var alarms: List<Alarm>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CoroutineScope {

    var job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAlarmTime = itemView.findViewById(R.id.alarmTime) as TextView
        val tvAlarmFrequency = itemView.findViewById(R.id.alarmFrequency) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int) : RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(layoutInflater.inflate(R.layout.list_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, index: Int) {
        val myViewHolder = holder as MyViewHolder
        val sdf = SimpleDateFormat("HH:mm EEEE")
        val context = myViewHolder.itemView.context
        myViewHolder.tvAlarmTime.text = sdf.format(alarms[index].milliseconds)
        myViewHolder.tvAlarmFrequency.text = alarms[index].frequency
        myViewHolder.itemView.setOnClickListener {
            launch {
                withContext(Dispatchers.IO) {
                    val db = AlarmDatabase.getDatabase(context)
                    db.alarmDao().deleteTriggeredAlarm((alarms[index].id))
                }

            }


            val context = myViewHolder.itemView.context
            val pi = PendingIntent.getBroadcast(context, (alarms[index].id).toInt(), Intent("alarmTask"), PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.cancel(pi)

        }
    }



    override fun getItemCount(): Int {
        return alarms.size
    }
}

