package com.example.birdview

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.databinding.CategoryRowBinding
import com.google.firebase.database.FirebaseDatabase

/*  Book App Firebase | 03 Add Book Category | Android Studio | Kotlin
    This code was sourced from Youtube
    Atif Pervaiz
    https://www.youtube.com/@AtifSayings
    https://youtu.be/x1Vh3GlF1ng?si=Ec055zNIgZ_swVDz
    */
class SpecieCategoryAdapter:RecyclerView.Adapter<SpecieCategoryAdapter.HolderCategory>{
    private val context: Context
    public  var specieArrayLists : ArrayList<BirdSpecieCategoryModel>
    private lateinit var binding: CategoryRowBinding

    constructor(context: Context, specieArrayLists: ArrayList<BirdSpecieCategoryModel>) {
        this.context = context
        this.specieArrayLists = specieArrayLists
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = CategoryRowBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderCategory(binding.root)
    }
    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val model = specieArrayLists[position]
        val id = model.id
        val specie = model.specie
        val amountofBirda = model.amountCategory
        val uid = model.uid
        holder.totalview.text = amountofBirda
        holder.categoryView.text = specie
        holder.deleteBtn.setOnClickListener{
            val builder = AlertDialog.Builder(context).setMessage("Delete specie").setPositiveButton("Delete"){ a, N->
                Toast.makeText(context,"specie Deleting..", Toast.LENGTH_SHORT).show()
                deleteCat(model,holder)
            }.setNegativeButton("Cancel"){a,N->
                a.dismiss()
            }.show()
        }
        holder.itemView.setOnClickListener {
            val intent  = Intent(context,BirdObservation::class.java)
            intent.putExtra("categoryId",id)
            intent.putExtra("category",specie)
            context.startActivity(intent)
        }

    }

    private fun deleteCat(model: BirdSpecieCategoryModel, holder: HolderCategory) {
        val  id = model.id

        val reference = FirebaseDatabase.getInstance().getReference("Species")
        reference.child(id).removeValue().addOnSuccessListener {
            Toast.makeText(context,"Category Deleting..", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener { N->
            Toast.makeText(context,N.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount():Int{
        return specieArrayLists.size
    }

    inner class  HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView){
        var categoryView : TextView = binding.categoryTV
        var totalview : TextView = binding.totalTV
        var deleteBtn : ImageButton = binding.deleteBTN
    }



}