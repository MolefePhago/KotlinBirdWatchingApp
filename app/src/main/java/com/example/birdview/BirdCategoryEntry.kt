package com.example.birdview

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.birdview.databinding.ActivityBirdCategoryEntryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BirdCategoryEntry : AppCompatActivity() {
    private lateinit var binding: ActivityBirdCategoryEntryBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var specieArrayList: ArrayList<BirdSpecieCategoryModel>
    lateinit var toggle: ActionBarDrawerToggle
    private val TAG = "Load Species"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBirdCategoryEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.AddSpecie.setOnClickListener {
            addDataValidate()
        }
        binding.Proceed.setOnClickListener {
            val birdEntry = Intent(this, BirdEntry::class.java)
            startActivity(birdEntry)
        }
        binding.CheckSpecie.setOnClickListener {
            val checkSpecie = Intent(this, SpecieCatgeory::class.java)
            startActivity(checkSpecie)
        }
        /*
        Navigation Drawer and Drawer Layout in Kotlin
        This code was sourced from
        Reyhaneh
        https://androidgeek.co/
        https://androidgeek.co/navigation-drawer-and-drawer-layout-in-kotlin-in-depth-guide-103ce411416d
        */
        toggle = ActionBarDrawerToggle(this@BirdCategoryEntry, binding.drawerLayouts, 0, 0)
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
        }


    }
    /*
    Book App Firebase | 03 Add Book Category | Android Studio | Kotlin
    This code was sourced from Youtube
    Atif Pervaiz
    https://www.youtube.com/@AtifSayings
    https://youtu.be/x1Vh3GlF1ng?si=Ec055zNIgZ_swVDz
    */
    private  var specie =""
    private var specieNumber = ""
    private fun addDataValidate() {
        specie = binding.SpecieCategory.text.toString().trim()

        if (specie.isEmpty()){
            Toast.makeText(this,"Enter a Specie", Toast.LENGTH_SHORT).show()
        }else{
            addSpecietoFirebase()
        }
    }

    private fun addSpecietoFirebase() {
        progressDialog.show()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val timestamp = System.currentTimeMillis()
        val hashMap  = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["specie"]= specie
        hashMap["specieNumber"] = specieNumber
        hashMap["uid"] = userID.toString()

        val reference = FirebaseDatabase.getInstance().getReference("Species")
        reference.child("$timestamp").setValue(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this,"Specie Added", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { n->
            progressDialog.dismiss()
            Toast.makeText(this,n.message.toString(),Toast.LENGTH_SHORT).show()
        }

    }
    private fun loadCategories(){
        Log.d(TAG,"loadSpecies : Loading Bird Species")
        progressDialog.setMessage("Loading Categories")
        progressDialog.show()
        var userID = FirebaseAuth.getInstance().currentUser?.uid
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
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)

    }
}