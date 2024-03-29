package com.example.birdview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.birdview.databinding.ActivityBirdInfoBinding
import com.google.gson.JsonArray
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class BirdInfo : AppCompatActivity() {
    private lateinit var binding: ActivityBirdInfoBinding
    lateinit var toggle: ActionBarDrawerToggle
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirdInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitQuestion.setOnClickListener {
        val query :String = binding.Question.text.toString().trim()
            Toast.makeText(this,query,Toast.LENGTH_SHORT).show()
            getResponse(query){response->
                runOnUiThread{
                    binding.displayQuestion.text = response
                }

            }
        }
        toggle = ActionBarDrawerToggle(this@BirdInfo, binding.drawerLayouts, 0, 0)
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
    fun getResponse(query:String,Callback:(String)->Unit){
        val key ="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"
        val url = "https://api.openai.com/v1/completions"
        val requestBody ="""
            {
            "model": "gpt-3.5-turbo-instruct",
            "prompt": "$query",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $key")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
               Log.d(e.message,"It didnt work")
            }

            override fun onResponse(call: Call, response: Response) {
               val body = response.body?.string()
                if (body != null){
                    Log.d("data Body",body)
                }else{
                    Log.d("data","empty")
                }
                val jsonObject =JSONObject(body)
                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                val text = jsonArray.getJSONObject(0).getString("text")
                Callback(text)
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