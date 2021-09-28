package com.example.myservice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.work.*
import com.example.myservice.service.MyWorker
import java.util.concurrent.TimeUnit

class SchedulerActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var periodicWorkRequest: PeriodicWorkRequest
    private lateinit var workManager: WorkManager
    private lateinit var btnClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduler)
        val btn = findViewById<Button>(R.id.btnOneTimeTask)
        val btnPeriodic = findViewById<Button>(R.id.btnPeriodicTask)
        btnClose = findViewById<Button>(R.id.btnCancelTask)

        btn.setOnClickListener(this)
        btnPeriodic.setOnClickListener(this)
        btnClose.setOnClickListener(this)

        workManager = WorkManager.getInstance(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnOneTimeTask -> startOneTimeTask()
            R.id.btnPeriodicTask -> startPeriodicTask()
            R.id.btnCancelTask -> cancelPeriodicTask()
        }
    }

    private fun startOneTimeTask() {
        val textStatus = findViewById<TextView>(R.id.textStatus)
        val editText = findViewById<EditText>(R.id.editCity)

        textStatus.text = getString(R.string.status)

        val data = Data.Builder()
            .putString(MyWorker.EXTRA_CITY, editText.text.toString())
            .build()

        // add event work manager
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val onTimeWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setInputData(data)
            .setConstraints(constraint)
            .build()


        workManager.enqueue(onTimeWorkRequest)
        workManager.getWorkInfoByIdLiveData(onTimeWorkRequest.id)
            .observe(this@SchedulerActivity, { workInfo ->
                val status = workInfo.state.name
                textStatus.text = "${textStatus.text} \n $status"
            })
    }

    private fun startPeriodicTask() {
        val textStatus = findViewById<TextView>(R.id.textStatus)
        val editText = findViewById<EditText>(R.id.editCity)

        textStatus.text = getString(R.string.status)

        val data = Data.Builder()
            .putString(MyWorker.EXTRA_CITY, editText.text.toString())
            .build()

        // add event work manager
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        periodicWorkRequest =
            PeriodicWorkRequest.Builder(MyWorker::class.java, 15, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(constraint)
                .build()

        workManager.enqueue(periodicWorkRequest)
        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(this@SchedulerActivity, { workInfo ->
                val status = workInfo.state.name
                textStatus.text = "${textStatus.text} \n $status"

                btnClose.isEnabled = false
                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                    btnClose.isEnabled = true
                }
            })
    }

    private fun cancelPeriodicTask() {
        workManager.cancelWorkById(periodicWorkRequest.id)
    }
}