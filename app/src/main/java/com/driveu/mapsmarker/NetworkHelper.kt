package com.driveu.mapsmarker

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

/**
 * Created by emil on 12/4/18.
 */
class NetworkHelper(context : Context, networkListener: NetworkListener){

    private var requestQueue = Volley.newRequestQueue(context)
    lateinit var stringRequest : StringRequest
    var networkListener : NetworkListener = networkListener

    fun getLocationData(url : String, tag : String){

        stringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener { s ->
            networkListener.onSuccess(tag, s)
        }, Response.ErrorListener { e ->
            networkListener.failure(tag, e)
        })
        {

        }
        requestQueue.add(stringRequest)
    }

}