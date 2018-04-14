package com.driveu.mapsmarker

import com.android.volley.VolleyError

/**
 * Created by emil on 12/4/18.
 */
interface NetworkListener {

    fun onSuccess(tag : String, response : String)
    fun failure(tag : String, error : VolleyError)
}