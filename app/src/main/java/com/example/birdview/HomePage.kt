package com.example.birdview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.birdview.databinding.ActivityHomePageBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog

//Shared preferences class
//url:https://kotlinlang.org/docs/classes.html

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunch() {
        sharedPreferences.edit().putBoolean("is_first_launch", false).apply()
    }
}


class HomePage : AppCompatActivity() {
    private lateinit var binding : ActivityHomePageBinding
    private lateinit var mMap: GoogleMap
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesHelper = SharedPreferencesHelper(this)

        if (sharedPreferencesHelper.isFirstLaunch()) {
            showWelcomeDialog()
            sharedPreferencesHelper.setFirstLaunch()
        }


        binding.btnAddBird.setOnClickListener {
            startActivity(Intent(this, BirdCategoryEntry::class.java))
        }
        binding.btnMap.setOnClickListener {
            startActivity(Intent(this, MapV2::class.java))
        }
        binding.Settings.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }
        binding.btnViewAllBirds.setOnClickListener {
            startActivity(Intent(this, AllBirds::class.java))
        }
        binding.info.setOnClickListener {
            startActivity(Intent(this,BirdInfo::class.java))
        }

        //This code is for the side bar
        toggle = ActionBarDrawerToggle(this@HomePage, binding.drawerLayouts, 0, 0)
        binding.drawerLayouts.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navViews.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Map -> {
                    val map = Intent(this, MapV2::class.java)
                    startActivity(map)
                }
                R.id.Entry -> {
                    val entry = Intent(this, BirdEntry::class.java)
                    startActivity(entry)
                }
                R.id.info ->{
                    startActivity(Intent(this,BirdInfo::class.java))
                }
                R.id.Homepage ->{
                    startActivity(Intent(this, ImagePickerActivity::class.java))
                }
                R.id.Category ->{
                    val category = Intent(this, SpecieCatgeory::class.java)
                    startActivity(category)
                }
                R.id.logout ->startActivity(Intent(this,Login::class.java))
            }
            true
            // The code ends here
        }
    }
    //On map ready google map
    //https://console.cloud.google.com/google/maps-apis/onboard;flow=free-trial-signup-flow?pli=1
    //(Google,2023).
    fun onMapReady(googleMap: GoogleMap) {
        val options = GoogleMapOptions()
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)

    }
    private fun showWelcomeDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Welcome to BirdView!")
        alertDialogBuilder.setMessage("Here are the steps to use the application:\n1. Set a distance filter\n2. Click on open Map\n3. View the hotspots around you and enjoy")
        alertDialogBuilder.setPositiveButton("Got it") { _, _ ->
            // Dismiss the dialog
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}