package com.example.birdview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.birdview.databinding.BirdRowBinding

/*
    Book App Firebase | 11 Show Books User | Android Studio | Kotlin
    This code was sourced from Youtube
    Atif Pervaiz
    https://www.youtube.com/@AtifSayings
    https://youtu.be/gttjc_t0PDU?si=RHdr5oAy3FZxsR2U
    */

class SpecieViewAdapter(context: Context, speciesArraylist: ArrayList<SpecieViewModel>) :
    RecyclerView.Adapter<SpecieViewAdapter.HoldPDF>() {

    private var context: Context
    public var speciesArraylist: ArrayList<SpecieViewModel>
    private lateinit var binding: BirdRowBinding
    private var selectedPosition = RecyclerView.NO_POSITION

    init {
        this.context = context
        this.speciesArraylist = speciesArraylist
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoldPDF {
        binding = BirdRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return HoldPDF(binding.root)
    }

    override fun getItemCount(): Int {
        return speciesArraylist.size
    }

    override fun onBindViewHolder(holder: HoldPDF, position: Int) {
        val model = speciesArraylist[position]

        val pdfId = model.id
        val categoryId = model.categoryId
        val Name = model.Name
        val Description = model.Description
        val Date = model.Date
        val Location = model.Location
        val BirdImage = model.BirdImage

        holder.birdNameTV.text = Name
        holder.birdDescriptionTV.text = Description
        holder.birdDateTV.text = Date
        holder.Location.text = Location
        Glide.with(holder.itemView).load(BirdImage).transition(DrawableTransitionOptions.withCrossFade()).into(holder.birdImageTV)

        //https://youtu.be/zvMuqbTObiw?si=hFHmnXjb5svexDmo
        //Hassouna Academy
        //https://youtube.com/@HassounaAcademy?si=s2BBas_iXM2mvfuX
        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.selected_item_background)
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }

        TheApplication.loadCategory(categoryId, holder.birdCategoryTV)

        //Set a click listener to handle item selection
        holder.itemView.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
        }
    }

    //ViewHolder class representing an item view in the RecyclerView
    inner class HoldPDF(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val birdNameTV: TextView = binding.BirdName
        val birdDescriptionTV = binding.BirdDescription
        val birdCategoryTV = binding.BirdCategory
        val birdDateTV = binding.BirdDate
        val birdImageTV = binding.BirdImage
        val Location = binding.BirdLocation
    }

    //Return the SpecieViewModel of the currently selected item, or null if no item is selected
    fun getSelectedSpecies(): SpecieViewModel? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            speciesArraylist[selectedPosition]
        } else {
            null
        }
    }
}
