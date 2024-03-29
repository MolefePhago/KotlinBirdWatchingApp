package com.example.birdview

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import com.example.birdview.databinding.ActivityBirdEntryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

class BirdEntry : AppCompatActivity() {
    private lateinit var binding: ActivityBirdEntryBinding
    lateinit var toggle: ActionBarDrawerToggle
    private val TAG = "Bird Entry"
    val PERMISSION_ID = 50
    private lateinit var firebaseAuth: FirebaseAuth
    var lat :Double=0.0
    var lng :Double=0.0
    val CAPTURE_CODE =1000
    private val PERMISSION_CODE = 1000
    private lateinit var progressDialog: ProgressDialog
    private lateinit var specieArrayList: ArrayList<BirdSpecieCategoryModel>
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var ImageUri: Uri? = null
    val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirdEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading..")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        binding.etCategory.setOnClickListener {
            categoryPick()
        }
        binding.Save.setOnClickListener {
            ValidateandSave()
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        binding.etLoaction.setOnClickListener {
            getLastLocation()
        }
        binding.Capture.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    val permission = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission,PERMISSION_CODE)
                }else{
                    takePicture()
                }
            }else{
                takePicture()
            }

        }

        //This code is for the side bar
        toggle = ActionBarDrawerToggle(this@BirdEntry, binding.drawerLayouts, 0, 0)
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
                    startActivity(Intent(this,HomePage::class.java))
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
        /*
        * How to use DatePickerDialog in Kotlin?
        * This code was sourced from stack overflow
        * Alexandr Kovalenko
        * https://stackoverflow.com/users/7697901/alexandr-kovalenko
        * https://stackoverflow.com/questions/45842167/how-to-use-datepickerdialog-in-kotlin*/
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val Myformat = "YYYY-MM-dd"
                val sdf = SimpleDateFormat(Myformat, Locale.US)
                binding.etDate.setText(sdf.format(calendar.time))
            }
        }
        binding.etDate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(
                    this@BirdEntry,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

        })

    }

    private var Name =""
    private var Description =""
    private var categories =""
    private var Date =""
    private var Location =""
    private fun ValidateandSave() {
        Log.d(TAG,"Validate and Save: validating data")

        Name = binding.etBirdName.text.toString().trim()
        Description = binding.etDescription.text.toString().trim()
        categories = binding.etCategory.text.toString().trim()
        Date = binding.etDate.text.toString().trim()
        Location = binding.etLoaction.text.toString().trim()

        if (Name.isEmpty()){
            Toast.makeText(this,"Enter Bird Name",Toast.LENGTH_SHORT).show()
        }else if(Description.isEmpty()){
            Toast.makeText(this,"Enter Bird Description",Toast.LENGTH_SHORT).show()
        }else if(categories.isEmpty()){
            Toast.makeText(this,"Select a Bird Specie",Toast.LENGTH_SHORT).show()
        }else if(Date.isEmpty()){
            Toast.makeText(this,"Enter Date of Site",Toast.LENGTH_SHORT).show()
        }else if(Location.isEmpty()){
            Toast.makeText(this,"Enter Bird Location",Toast.LENGTH_SHORT).show()
        }else{
            uploadBirdEntry()
        }
    }

    private fun uploadBirdEntry() {
        progressDialog.setMessage("Uploading Bird Entry")
        progressDialog.show()
        Log.d(TAG,"uploadToDatabase: Uploading to database ")
        val timestamp = System.currentTimeMillis()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val hashMap: HashMap<String,Any> = HashMap()
        hashMap["uid"]= userID.toString()
        hashMap["id"]= "$timestamp"
        hashMap["Name"]= Name
        hashMap["Description"]= Description
        hashMap["categoryId"]= selectedCategoryID
        hashMap["Date"]= Date
        hashMap["lat"]= lat
        hashMap["lng"]=lng
        hashMap["Location"]= Location
        hashMap["TimeImage"]= "$ImageUri"


        // Extract latitude and longitude from the selected location
       /* val selectedLocation = Location(LocationManager.GPS_PROVIDER)


        // Pass latitude and longitude to MapV2 activity
        val intent = Intent(this, MapV2::class.java)
        intent.putExtra("birdLatitude", selectedLocation.latitude)
        intent.putExtra("birdLongitude", selectedLocation.longitude)
        startActivity(intent)
*/
        val reference = FirebaseDatabase.getInstance().getReference("Bird")
        reference.child("$timestamp").setValue(hashMap).addOnSuccessListener {
            Log.d(TAG,"uploadToDatabase: Uploaded to database ")
            Toast.makeText(this, "Uploaded",Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()

        }.addOnFailureListener {N->
            Log.d(TAG,"uploadToDatabase: Uploading to database failed ${N.message} ")
            Toast.makeText(this,"Failed to upload ${N.message}",Toast.LENGTH_SHORT).show()
        }

    }
    private fun loadCategories(){
        Log.d(TAG,"loadCategories : Loading Specie categories")
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        specieArrayList = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Species")
        reference.orderByChild("uid").equalTo(userID.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                specieArrayList.clear()
                for (N in snapshot.children){
                    val model = N.getValue(BirdSpecieCategoryModel::class.java)
                    specieArrayList.add(model!!)
                    Log.d(TAG,"onDataChange: ${model.specie}")
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private var selectedCategoryID = ""
    private var selectedCategoryTitle = ""
    private fun categoryPick(){
        Log.d(TAG,"categoryPick: Showing Species category")
        progressDialog.show()
        val categoryArray = arrayOfNulls<String>(specieArrayList.size)
        for (N in specieArrayList.indices){
            categoryArray[N] = specieArrayList[N].specie
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick a Specie").setItems(categoryArray){ dialog, which ->
            selectedCategoryTitle = specieArrayList[which].specie
            selectedCategoryID = specieArrayList[which].id
            binding.etCategory.text = selectedCategoryTitle
            Log.d(TAG,"categoryPick: Selected Category ID: $selectedCategoryID")
            Log.d(TAG,"categoryPick: Selected Category Title $selectedCategoryTitle")
        }.show()
        progressDialog.dismiss()
    }
    private fun getLastLocation(){
        if (checkPermission()){
            if (isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task ->
                    var location = task.result
                    if (location ==null){
                        getNewLocation()
                    }else{
                        binding.etLoaction.setText(getCityName(location.latitude,location.longitude)+" "+getCountryName(location.latitude,location.longitude))
                        lat =location.latitude
                        lng = location.longitude
                    }
                }
            }else{
                Toast.makeText(this,"Enable Your Location services",Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermission()
        }
    }
    private fun getNewLocation(){
        var locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.priority =com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval =0
        locationRequest.fastestInterval= 0
        locationRequest.numUpdates = 2

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,locationCallback,Looper.myLooper()
        )
    }
    private val locationCallback = object:LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            var lastlocation : Location = p0.lastLocation!!
            binding.etLoaction.setText(getCityName(lastlocation.latitude,lastlocation.longitude)+" "+getCountryName(lastlocation.latitude,lastlocation.longitude))
        }
    }
    private  fun getCityName(lat:Double,Long:Double):String{
        var CityName =""
        var geocoder =Geocoder(this, Locale.getDefault())
        var addresses = geocoder.getFromLocation(lat,Long,1)
        CityName = addresses?.get(0)!!.locality
        return CityName
    }
    private fun getLngLat(lat:Double,Long:Double):String{
        var coordinates =""
        var geocoder =Geocoder(this, Locale.getDefault())
        var addresses = geocoder.getFromLocation(lat,Long,1)
        coordinates = addresses?.get(0)!!.locality
        return coordinates
    }
    private  fun getCountryName(lat:Double,Long:Double):String{
        var countryName =""
        var geocoder =Geocoder(this, Locale.getDefault())
        var addresses = geocoder.getFromLocation(lat,Long,1)
        countryName = addresses?.get(0)!!.locality
        return countryName
    }
    private fun checkPermission():Boolean{
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),PERMISSION_ID)
    }
    private fun isLocationEnabled():Boolean{
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    fun takePicture(){
        val cameraIn = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(cameraIn.resolveActivity(packageManager)!=null){
            startActivityForResult(cameraIn,CAPTURE_CODE)
        }
    }
    private fun uploadImage(bitmap: Bitmap){
        progressDialog.setMessage("Upload Image")
        progressDialog.show()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val timestamp = System.currentTimeMillis()
        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("$timestamp.jpg")

        // Create a reference to 'images/mountains.jpg'
        val ImagesRef = storageRef.child("images/$timestamp.jpg")

        // While the file names are the same, the references point to different files
        mountainsRef.name == ImagesRef.name // true
        mountainsRef.path == ImagesRef.path // false


        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()

        val uploadTask = ImagesRef.putBytes(data).continueWithTask { task ->
            if (!task.isSuccessful) {

                task.exception?.let {
                    throw it
                }
            }
            ImagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                progressDialog.dismiss()
                ImageUri = task.result
            } else {

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID){
            if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission","You have been granted permission")
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CAPTURE_CODE){

            val imageBitmap = data?.extras?.get("data")as Bitmap
            uploadImage(imageBitmap)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)

    }
}