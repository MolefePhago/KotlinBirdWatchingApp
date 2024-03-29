package com.example.birdview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.birdview.databinding.ActivitySettingsBinding
import java.lang.Exception

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var metrics :String = ""
        binding.metricSystem.setOnCheckedChangeListener { buttonView, isChecked ->
            metrics = if (isChecked){
                "Miles"
            }else{
                "Km"
            }
        }
        binding.btnDistance.setOnClickListener {
            try{
                val value = binding.etDistance.text.toString().toInt()
                if(value >50){
                    Toast.makeText(this,"Distance can not be over 50Km/miles",Toast.LENGTH_SHORT).show()
                }else{
                    val intent = Intent(this,MapV2::class.java)
                    intent.putExtra("metrics",metrics)
                    intent.putExtra("DistanceValue",value)
                    startActivity(intent)
                }
            }catch (e:Exception){
                Toast.makeText(this,"Please enter a number",Toast.LENGTH_SHORT).show()
            }
        }

        toggle = ActionBarDrawerToggle(this@Settings, binding.drawerLayouts, 0, 0)
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)

    }
}