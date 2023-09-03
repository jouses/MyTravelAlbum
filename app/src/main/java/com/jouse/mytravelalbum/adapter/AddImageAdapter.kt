package com.jouse.mytravelalbum.adapter

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jouse.mytravelalbum.databinding.RecyclerAddImageBinding
import com.jouse.mytravelalbum.mainFragments.AddTravelFragment

class AddImageAdapter(private val imageArray: ArrayList<Bitmap>, private val addTravelFragment: AddTravelFragment) : RecyclerView.Adapter<AddImageAdapter.AddImageHolder>() {
    class AddImageHolder(val binding: RecyclerAddImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddImageHolder {
        val recyclerAddImageBinding = RecyclerAddImageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AddImageHolder(recyclerAddImageBinding)
    }

    override fun getItemCount(): Int {
        return if (addTravelFragment.add) {
            imageArray.size + 1
        } else {
            imageArray.size
        }
    }

    override fun onBindViewHolder(holder: AddImageHolder, position: Int) {
        if (position == imageArray.size && addTravelFragment.add){
            holder.itemView.setOnClickListener{
                addTravelFragment.addImage()
            }
        }
        else {
            holder.itemView.isClickable = false
            holder.binding.imageView2.setImageBitmap(imageArray[position])
        }
    }
}