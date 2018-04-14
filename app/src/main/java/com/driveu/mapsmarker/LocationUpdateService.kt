package com.driveu.mapsmarker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.provider.SyncStateContract
import android.widget.Toast
import com.android.volley.VolleyError
import com.google.gson.Gson
import java.util.*

/**
 * Created by emil on 13/4/18.
 */
class LocationUpdateService : Service(), NetworkListener{


    val MY_ACTION = "MY_ACTION"
    private lateinit var context: Context
    private var timer: Timer? = null
    var Data: String? = null
    private lateinit var handler: Handler
    private lateinit var networkHelper : NetworkHelper


    override fun onBind(arg0: Intent): IBinder? {
        // TODO Auto-generated method stub
        return null
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        handler = Handler()
        networkHelper = NetworkHelper(context, this)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        handler.apply {
            val runnable = object : Runnable {
                override fun run() {
                    networkHelper.getLocationData("http://10.0.2.2:8080/explore", "location_data")
                    postDelayed(this, 15000)
                }
            }
            postDelayed(runnable, 1000)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        stopSelf()
    }

    override fun onSuccess(tag: String, response: String) {
        parseResponse(response)
    }

    private fun parseResponse(response: String) {
        val gson = Gson()
        val topic = gson.fromJson(response, ResponseModel::class.java)
        val intent = Intent()
        intent.action = "Location"
        intent.putExtra("latitude", topic.latitude)
        intent.putExtra("longitude", topic.longitude)
        sendBroadcast(intent)

    }

    override fun failure(tag: String, error: VolleyError) {

    }


}