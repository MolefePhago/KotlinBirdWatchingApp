package com.example.birdview

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.birdview.databinding.ActivitySpecieCatgeoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SpecieCatgeory : AppCompatActivity() {
    private lateinit var binding: ActivitySpecieCatgeoryBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var SpecieCategoryAdp:SpecieCategoryAdapter
    private lateinit var birdsList : ArrayList<SpecieViewModel>
    private lateinit var SpecieCategoryList : ArrayList<BirdSpecieCategoryModel>
    private lateinit var progressDialog: ProgressDialog
    var specie =""
    var categoryId =""
    var TAG ="Load Species"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecieCatgeoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Species Loading...")
        progressDialog.setCanceledOnTouchOutside(false)

       showCategories()
        binding.addSpecie.setOnClickListener {
            startActivity(Intent(this, BirdCategoryEntry::class.java))
        }
    }
    /*
    Book App Firebase | 11 Show Books User | Android Studio | Kotlin
    This code was sourced from Youtube
    Atif Pervaiz
    https://www.youtube.com/@AtifSayings
    https://youtu.be/gttjc_t0PDU?si=RHdr5oAy3FZxsR2U
    */
    private fun showCategories() {
        SpecieCategoryList = ArrayList()
        birdsList = ArrayList()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val referenceTime = FirebaseDatabase.getInstance().getReference("Birds")
        referenceTime.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val models = i.getValue(SpecieViewModel::class.java)
                    birdsList.add(models!!)
                }
                val reference = FirebaseDatabase.getInstance().getReference("Species")
                reference.orderByChild("uid").equalTo(userID).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children){
                            val model = ds.getValue(BirdSpecieCategoryModel::class.java)
                            SpecieCategoryList.add(model!!)
                        }

                        SpecieCategoryAdp = SpecieCategoryAdapter(this@SpecieCatgeory,SpecieCategoryList)
                        binding.CategoryRec.adapter = SpecieCategoryAdp

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //This code is for the side bar
        toggle = ActionBarDrawerToggle(this@SpecieCatgeory, binding.drawerLayouts, 0, 0)
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
                R.id.Homepage ->{
                    startActivity(Intent(this, HomePage::class.java))
                }
                R.id.Category ->{
                    val category = Intent(this, SpecieCatgeory::class.java)
                    startActivity(category)
                }
                R.id.logout -> Toast.makeText(applicationContext, "cghj", Toast.LENGTH_SHORT).show()
            }
            true
            // The code ends here
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)

    }
}