package com.example.myservice.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myservice.R
import com.example.myservice.model.Response
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.text.DecimalFormat

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        private val TAG = MyWorker::class.java.simpleName
        const val APP_ID = "YOUR_KEY_HERE"
        const val EXTRA_CITY = "city"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "channel_01"
        const val CHANNEL_NAME = "dicoding channel"
    }

    private var resultStatus: Result? = null

    override fun doWork(): Result {
        val dataCity = inputData.getString(EXTRA_CITY);
        return getCurrentWeather(dataCity)
    }

    private fun getCurrentWeather(city: String?): Result {
        Log.d(TAG, "getCurrentWeather: Mulai.....")
        Looper.prepare()

        val client = SyncHttpClient()
        val url =
            "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=a26a4d1db3a3a17717988610bd476e6a"
        Log.d(TAG, "getCurrentWeather: $url")

        client.post(url, object : AsyncHttpResponseHandler() {

            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                val result = String(responseBody!!)
                Log.d(TAG, "onSuccess: $result")

                try {
                    // val responseJson = JSONObject(result)
                    // val currentWeather = responseJson.getJSONArray("weather").getJSONObject(0).getString("main")
                    // val description: String = responseJson.getJSONArray("weather").getJSONObject(0)
                    //    .getString("description")
                    // val tempInKelvin = responseJson.getJSONObject("main").getDouble("temp")

//                    val tempInCelsius = tempInKelvin - 273
//                    val temperature: String = DecimalFormat("##.##").format(tempInCelsius)
//
//                    val title = "Current Weather in $city"
//                    val message = "$currentWeather, $description with $temperature celsius"

                    val moshi = Moshi.Builder()
                        .addLast(KotlinJsonAdapterFactory())
                        .build()

                    val jsonAdapter = moshi.adapter(Response::class.java)
                    val response = jsonAdapter.fromJson(result)

                    response?.let {

                        val currentWeather = it.weatherList[0].main
                        val description = it.weatherList[0].description
                        val tempInKelvin = it.main.temperature

                        val tempInCelsius = tempInKelvin - 273
                        val temperature: String = DecimalFormat("##.##").format(tempInCelsius)

                        val title = "Current Weather in $city"

                        val message = "$currentWeather, $description with $temperature celsius"
                        showNotification(title, message)
                    }

                    Log.d(TAG, "onSuccess: Selesai.....")
                    resultStatus = Result.success()

                } catch (e: Exception) {
                    showNotification("Get Current Weather Not Success", e.message!!)
                    Log.d(TAG, "onSuccess: Gagal.....")
                    resultStatus = Result.failure()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                Log.d(TAG, "onFailure: Gagal.....")
                showNotification("Get Current Weather Failed", error?.message!!)
                resultStatus = Result.failure()
            }
        })

        return resultStatus as Result
    }

    private fun showNotification(title: String, msg: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_circle_notifications)
                .setContentText(msg)
                .setContentTitle(title)
                .setPriority(NotificationCompat.DEFAULT_ALL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notification.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }
}
