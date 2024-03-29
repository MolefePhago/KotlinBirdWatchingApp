package com.example.birdview

import android.app.Application
import android.util.Log
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TheApplication: Application() {

    override fun onCreate(){
        super.onCreate()
    }
    companion object{
        fun loadCategory(categoryId: String, CategoryTV: TextView){
            val reference = FirebaseDatabase.getInstance().getReference("Species")
            reference.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val category = "${snapshot.child("specie").value}"
                    CategoryTV.text = category
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Database",error.message)
                }

            })
        }
    }
}