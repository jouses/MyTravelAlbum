package com.jouse.mytravelalbum.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jouse.mytravelalbum.databinding.RecyclerAlbumListBinding
import com.jouse.mytravelalbum.mainFragments.AddTravelFragment
import com.jouse.mytravelalbum.mainFragments.ListFragment
import com.jouse.mytravelalbum.model.TravelTitle

class AlbumListAdapter(private val travelTitles: ArrayList<TravelTitle>, val listFragment: ListFragment) : RecyclerView.Adapter<AlbumListAdapter.AlbumListHolder>() {
    class AlbumListHolder(val binding: RecyclerAlbumListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumListHolder {
        val recyclerAlbumListBinding = RecyclerAlbumListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AlbumListHolder(recyclerAlbumListBinding)
    }

    override fun getItemCount(): Int {
        return travelTitles.size
    }

    override fun onBindViewHolder(holder: AlbumListHolder, position: Int) {
        holder.binding.recyclerAlbumListName.text = travelTitles[position].name
        holder.binding.recyclerAlbumListLocation.text = travelTitles[position].location

        holder.binding.recyclerAlbumItemLayout.setOnClickListener {
            listFragment.replaceFragment(AddTravelFragment(false,travelTitles[position].name,travelTitles[position].location),"showTravelFragment")
        }
    }
}