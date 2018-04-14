package com.driveu.mapsmarker

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.android.volley.VolleyError
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, NetworkListener, View.OnClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var context: Context
    private lateinit var locationButton: FloatingActionButton
    private lateinit var handler: Handler
    private var timerStarted: Boolean = false
    private lateinit var networkHelper: NetworkHelper
    private lateinit var locationIntent: Intent
    private lateinit var broadCastReceiver: LocationBroadCastReceiver


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private val LOCATION_API_URL: String = "http://10.0.2.2:8080/explore"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        context = this

        initViews()
        initHelper()
        initMapFragment()
        registerLocationBroadcastReceiver()
    }

    private fun registerLocationBroadcastReceiver() {
        broadCastReceiver = LocationBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("Location")
        registerReceiver(broadCastReceiver, intentFilter)
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadCastReceiver)
    }

    override fun onPause() {
        super.onPause()
        if (timerStarted)
            startLocationService()

    }

    override fun onResume() {
        super.onResume()
        if (timerStarted)
            stopService(locationIntent)
    }

    private fun startLocationService() {
        if (context != null) {
            startService(locationIntent)
        }
    }

    private fun initViews() {
        locationButton = findViewById(R.id.myLocationButton)
        locationButton.setOnClickListener(this)
        locationIntent = Intent(this, LocationUpdateService::class.java)
    }

    private fun initMapFragment() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initHelper() {
        networkHelper = NetworkHelper(context, this)
        handler = Handler()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

        } else {

            setMyLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocation() {
        map.isMyLocationEnabled = true
        val mapSettings = map?.uiSettings
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    setMyLocation()
                } else {
                    //If user presses deny
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerClickListener(this)
        setUpMap()
    }

    private fun addMarker(latitude: Double, longitude: Double) {

        val handler: Handler = Handler()
        handler.postDelayed(Runnable {

            val myPlace = LatLng(latitude, longitude)  // this is New York
            map.addMarker(MarkerOptions().position(myPlace).title("My Favorite City"))
            map.moveCamera(CameraUpdateFactory.newLatLng(myPlace))

        }, 100)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return true
    }

    private fun placeMarkerOnMap(location: LatLng) {

        val markerOptions = MarkerOptions().position(location)
        map.addMarker(markerOptions)
    }

    override fun onSuccess(tag: String, response: String) {
        when (tag) {
            "location_data" ->
                parseDataAndPlotMarker(response)
        }
    }

    private fun parseDataAndPlotMarker(response: String) {
        val gson = Gson()
        val topic = gson.fromJson(response, ResponseModel::class.java)
        addMarker(topic.latitude, topic.longitude)
    }

    override fun failure(tag: String, error: VolleyError) {

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.myLocationButton ->
                if (!timerStarted) {
                    locationButton.setImageResource(R.drawable.ic_stop_button)
                    requestLocation()
                    timerStarted = true
                } else {
                    locationButton.setImageResource(R.drawable.ic_play_button)
                    handler.removeCallbacksAndMessages(null)
                    timerStarted = false
                }

        }
    }

    private fun requestLocation() {
        timerStarted = true
        handler.apply {
            val runnable = object : Runnable {
                override fun run() {
                    networkHelper.getLocationData(LOCATION_API_URL, "location_data")
                    postDelayed(this, 15000)
                }
            }
            postDelayed(runnable, 1000)
        }

    }

    internal inner class LocationBroadCastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            try {
                val latitude: Double = intent.getDoubleExtra("latitude", 0.0)
                val longitude: Double = intent.getDoubleExtra("longitude", 0.0)
                addMarker(latitude, longitude)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
    }
}
