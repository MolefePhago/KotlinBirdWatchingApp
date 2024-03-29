package com.example.birdview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.birdview.databinding.ActivityMapV2Binding
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.birdview.databinding.ActivityBirdEntryBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import kotlin.concurrent.thread

var userLocation: LatLng? = null
var selectedDistance = 20
var instructions: String? = null

//Interface for ebirdservice
interface EBirdService{
    //Will change endpoint to a more user sentric on for sure
    //https://console.cloud.google.com/google/maps-apis/onboard;flow=just-ask-flow;step=just_ask
    //(Google,2023).
    //Forgot too call the birdsighting method in the map ready method
    //I'll implement UI threading so the baby doesnt have too load for too long
    //https://api.ebird.org/v2/data/obs/geo/recent?lat=38.897957&lng=-77.036560&dist=20
    @GET("/v2/data/obs/geo/recent")
    fun getBirdSighting(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("dist") dist: Int,
        @Query("key") apiKey: String
    ): Call<List<BirdSighting>>
    //the code was source from Ebird Postman
    //the Data class
    //Ebird service
    //https://documenter.getpostman.com/view/664302/S1ENwy59

    data class BirdSighting(
        val speciesCode: String,
        val comName: String,
        val sciName: String,
        val locId: String,
        val locName: String,
        val obsDt: String,
        val howMany: Int,
        val lat: Double,
        val lng: Double,
        val obsValid: Boolean,
        val obsReviewed: Boolean,
        val locationPrivate: Boolean,
        val subId: String,
        val exoticCategory: String?
    )
}
   //Class for most stuff
    //Run on UI thread too boost performance

    class MapV2 : AppCompatActivity(), OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,TextToSpeech.OnInitListener {
        // Firebase database reference for bird entries
        private lateinit var birdReference: DatabaseReference

        // List to store bird entries
        private val birdList: MutableList<SpecieViewModel> = mutableListOf()

        //Ebird service
        //https://documenter.getpostman.com/view/664302/S1ENwy59
        private lateinit var mMap: GoogleMap
        lateinit var toggle: ActionBarDrawerToggle
        private lateinit var binding: ActivityMapV2Binding
        private lateinit var eBirdService: EBirdService
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var textToSpeech: TextToSpeech



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityMapV2Binding.inflate(layoutInflater)
            setContentView(binding.root)
            val distanceSeekBar = findViewById<SeekBar>(R.id.distanceSeekBar)
            val metricsReceive = intent.getStringExtra("metrics")
            val receive = intent.getIntExtra("DistanceValue", 50)
            binding.minDistance.text = "0" + metricsReceive.toString().trim()
            binding.maxDistance.text = receive.toString().trim() + metricsReceive.toString().trim()
            distanceSeekBar.max = receive
            //


            textToSpeech = TextToSpeech(this, this)

            // Initialize Firebase database reference for bird entries
            birdReference = FirebaseDatabase.getInstance().getReference("Bird")

            // ...

            // Call method to retrieve bird entries and display markers on the map
            getBirdEntriesAndDisplayMarkers()




            distanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    //
                    selectedDistance = progress
                    // New Map stuff added
                    refreshMapWithNewDistance()
                    getBirdEntriesAndDisplayMarkers()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })
            // Initialize FusedLocationProviderClient
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // Request location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            } else {

                initializeMap()
                initializeEBirdService()

            }


        }

        override fun onInit(status: Int) {
            if (status == TextToSpeech.SUCCESS) {
                // TextToSpeech is successfully initialized
                // You can set the language or other configurations here if needed
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }

        override fun onDestroy() {
            if (::textToSpeech.isInitialized) {
                textToSpeech.stop()
                textToSpeech.shutdown()
            }
            super.onDestroy()
        }
        private fun refreshMapWithNewDistance() {
            // Check if the map is already aight
            if (::mMap.isInitialized) {
                // Clear the map
                mMap.clear()

                // Request the location
                requestLocation()
            }
        }



        private fun initializeMap() {

               val mapFragment = supportFragmentManager
                   .findFragmentById(R.id.map) as SupportMapFragment
               mapFragment.getMapAsync(this)

       }


       private fun initializeEBirdService() {
           val retrofit:Retrofit = Retrofit.Builder()
               .baseUrl("https://api.ebird.org")
               .addConverterFactory(GsonConverterFactory.create())
               .build()

           eBirdService = retrofit.create(EBirdService::class.java)
       }

       //Apply Run on UI threads here
       // The brain
        //
       override fun onMapReady(googleMap: GoogleMap) {

            val options = GoogleMapOptions()
           mMap = googleMap
           mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
           mMap.isTrafficEnabled = false

           // Enable the My Location layer
            //
           if (ActivityCompat.checkSelfPermission(
                   this, Manifest.permission.ACCESS_FINE_LOCATION
               ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                   this,
                   Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Handle permission request
               return
           }
           mMap.isMyLocationEnabled = true
           // Set click listeners
           mMap.setOnMyLocationButtonClickListener(this)
           mMap.setOnMyLocationClickListener(this)
           //Solution? Yes it was

           userLocation?.let {
               getBirdSightings(it)
                    }
                        ?: run {

                            //will implement -- Changes
                            }
                    //getBirdSightings(userLocation)
                    requestLocation()

       }

        private fun requestLocation() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                        getBirdSightings(userLocation!!)
                    }
                }
        }
        private fun getBirdEntriesAndDisplayMarkers() {
            // Get the current user's UID
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

            birdReference.orderByChild("uid").equalTo(currentUserUid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        birdList.clear()

                        for (birdSnapshot in snapshot.children) {
                            val bird = birdSnapshot.getValue(SpecieViewModel::class.java)
                            if (bird != null) {
                                birdList.add(bird)
                                displayMarkerForBird(bird)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }
            )
        }

        private fun displayMarkerForBird(bird: SpecieViewModel) {
            val birdLatLng = LatLng(bird.lat, bird.lng)
            val birdMarker = MarkerOptions()
                .position(birdLatLng)
                .title(bird.Name)
                .snippet(bird.Description)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            val marker = mMap.addMarker(birdMarker)

            if (marker != null) {
                marker.tag = bird
            }
        }

        //Ebird service
        //https://documenter.getpostman.com/view/664302/S1ENwy59
        private fun getBirdSightings(userLocation: LatLng) {
            val apiKey = "9riis08rlgc2" //  eBird API key

            eBirdService.getBirdSighting(
                userLocation.latitude, userLocation.longitude, selectedDistance, apiKey
            ).enqueue(object : Callback<List<EBirdService.BirdSighting>> {
                    override fun onResponse(
                        call: Call<List<EBirdService.BirdSighting>>,
                        response: Response<List<EBirdService.BirdSighting>>
                    ) {
                        if (response.isSuccessful) {
                            val birdSightings = response.body()

                            if (birdSightings != null) {
                                for (sighting in birdSightings) {
                                    val birdLatLng = LatLng(sighting.lat, sighting.lng)
                                    val birdMarker = MarkerOptions()
                                        .position(birdLatLng)
                                        .title(sighting.comName)
                                        .snippet(sighting.locName)

                                    val marker = mMap.addMarker(birdMarker)

                                    if (marker != null) {
                                        marker.setTag(sighting)
                                    }
                                }
                            }
                        }
                    }


                    override fun onFailure(
                       call: Call<List<EBirdService.BirdSighting>>,
                       t: Throwable
                   ) {
                       // Handle API call failure
                       // You can display an error message or handle it as needed
                   }
               })

           // Set a click listener for the markers
            ////Ebird service
            //    //https://documenter.getpostman.com/view/664302/S1ENwy59
           mMap.setOnMarkerClickListener { marker ->
               // Retrieve the sighting data from the marker's tag
               val sighting = marker.tag as? EBirdService.BirdSighting
               sighting?.let {
                   navigateToBirdSighting(it)
               }

               // Return 'false' to allow default behavior (opening marker info window)
               false
           }
       }
        //Ebird service
        //https://documenter.getpostman.com/view/664302/S1ENwy59
       private fun navigateToBirdSighting(sighting: EBirdService.BirdSighting) {


            val context = GeoApiContext.Builder()
                //Changed second version of the API that includes Google Directions Api and Maps API
                .apiKey("AIzaSyAcnPpN2K87hgK2IIjDyqReQlIPjU41kvo")
                .build()

            val request = DirectionsApi.newRequest(context)
                .origin(userLocation!!.latitude.toString() + "," + userLocation!!.longitude.toString())
                .destination(sighting.lat.toString() + "," + sighting.lng.toString())
                .mode(TravelMode.DRIVING)


            Thread {
                try {
                    val result = request.await()
                    val route = result.routes[0]

                    runOnUiThread {
                        val polylineOptions = PolylineOptions()
                            .color(Color.BLUE)
                            .width(5f)

                        for (leg in route.legs) {
                            for (step in leg.steps) {
                                val points = step.polyline.decodePath()

                                for (point in points) {
                                    polylineOptions.add(LatLng(point.lat, point.lng))
                                }

                                val instructions = step.htmlInstructions?.replace(Regex("<.*?>"), "") ?: ""


                                // Show instructions in a Snackbar
                                val snackbar: Snackbar = Snackbar.make(
                                    findViewById(android.R.id.content),
                                    instructions,
                                    Snackbar.ANIMATION_MODE_SLIDE


                                )
                                    snackbar.show()
                                    textToSpeech.speak(instructions, TextToSpeech.QUEUE_ADD, null, null)
                            }
                        }

                        mMap.addPolyline(polylineOptions)
                    }
                } catch (e: Exception) {
                    Log.e("DirectionsAPI", "Failed to get directions: ${e.message}")
                }
            }.start()
        }

       override fun onMyLocationClick(location: Location) {
           //userLocation = LatLng(location.latitude, location.longitude)
           Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
               .show()
       }

       override fun onMyLocationButtonClick(): Boolean {
           Toast.makeText(this, "Locating your phone ", Toast.LENGTH_SHORT)
               .show()
           return false
       }

       companion object {
           const val REQUEST_LOCATION_PERMISSION = 1
       }


       }




