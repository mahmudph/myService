package com.example.myservice

import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myservice.service.MyBoundService
import com.example.myservice.service.MyJobIntentService
import com.example.myservice.service.MyService

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var mServiceBound = false
    private lateinit var mBounceService: MyBoundService

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mServiceBound = true
            val myBinder = service as MyBoundService.MyBinder
            mBounceService = myBinder.getService

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartService = findViewById<Button>(R.id.btn_start_service)

        btnStartService.setOnClickListener {
            // service
            val mStartServiceIntent = Intent(this, MyService::class.java)
            startService(mStartServiceIntent)
        }

        val btnStartJobIntentService = findViewById<Button>(R.id.btn_start_job_intent_service)

        btnStartJobIntentService.setOnClickListener {
            // start jobs
            val mStartJobService = Intent(this, MyJobIntentService::class.java)
            mStartJobService.putExtra(MyJobIntentService.EXTRA_DURATION, 5000L)
            MyJobIntentService.enqueueWork(this, mStartJobService)
        }

        val btnStartBoundService = findViewById<Button>(R.id.btn_start_bound_service)

        btnStartBoundService.setOnClickListener {
            // bind service
            val mBoundServiceIntent = Intent(this, MyBoundService::class.java)
            bindService(mBoundServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
        }

        val btnStopBoundService = findViewById<Button>(R.id.btn_stop_bound_service)

        btnStopBoundService.setOnClickListener {
            unbindService(mServiceConnection)
        }

        val btn = findViewById<Button>(R.id.toScheduleActivity)
        btn.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mServiceBound) {
            unbindService(mServiceConnection)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.toScheduleActivity -> {
                val intent = Intent(this, SchedulerActivity::class.java)
                startActivity(intent)
            }
        }
    }
}