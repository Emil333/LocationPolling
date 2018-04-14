package com.driveu.mapsmarker

import android.app.Application

/**
 * Created by emil on 12/4/18.
 */
class ExploreApplication : Application(){

    var applicationInstance : Application? = null

    override fun onCreate() {
        super.onCreate()
        applicationInstance = this
    }

}