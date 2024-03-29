package com.example.birdview

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.birdview.databinding.ActivityAllBirdsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllBirds : AppCompatActivity() {

    private lateinit var binding: ActivityAllBirdsBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var birdlist: ArrayList<SpecieViewModel>
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var birdAdapter: SpecieViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllBirdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setCanceledOnTouchOutside(false)
        showAllBirds()
                /*
                    Navigation Drawer and Drawer Layout in Kotlin
                    This code was sourced from Medium
                    Reyhaneh
                    https://androidgeek.co/
                    https://androidgeek.co/navigation-drawer-and-drawer-layout-in-kotlin-in-depth-guide-103ce411416d
                */
        toggle = ActionBarDrawerToggle(this@AllBirds, binding.drawerLayouts, 0, 0)
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

        // Set up the button click listener
        binding.btnShare.setOnClickListener {
           shareButtonClick()
        }

    }

    private fun showAllBirds() {
        progressDialog.show()
        birdlist = ArrayList()

        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val reference = FirebaseDatabase.getInstance().getReference("Bird")
        reference.orderByChild("uid").equalTo(userID).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                for (i in snapshot.children) {
                    val model = i.getValue(SpecieViewModel::class.java)
                    birdlist.add(model!!)
                }

                birdAdapter = SpecieViewAdapter(this@AllBirds, birdlist)
                binding.SpecieList.adapter = birdAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

   private fun shareButtonClick() {
        // Get the selected bird species observation from the RecyclerView
        val selectedSpecies = birdAdapter.getSelectedSpecies()
        if (selectedSpecies == null) {
            Toast.makeText(this, "Please select a bird species", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a share intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        // Add the captured information to the share body
        val shareBody =
            "Check out my bird observation captured from BirdView!\n" +
                    "Name: ${selectedSpecies.Name}\n" +
                    "Description: ${selectedSpecies.Description}\n" +
                    "Category ID: ${selectedSpecies.categoryId}\n" +
                    "Date: ${selectedSpecies.Date}\n" +
                    "Location: ${selectedSpecies.Location}\n" +
                    "Time Image: ${selectedSpecies.BirdImage}"

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)

        // Create a chooser dialog to let the user choose the sharing app that's on their device
        val chooserIntent = Intent.createChooser(shareIntent, "Share Bird Observation")

        // Check if any app can handle the intent
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(chooserIntent)
        } else {
            Toast.makeText(
                this,
                "No app installed that can handle this action",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
