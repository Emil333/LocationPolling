package com.driveu.mapsmarker

import com.google.gson.annotations.SerializedName

/**
 * Created by emil on 13/4/18.
 */
class ResponseModel{
    @SerializedName("latitude")
    var latitude : Double = 0.0

    @SerializedName("longitude")
    var longitude : Double = 0.0


}