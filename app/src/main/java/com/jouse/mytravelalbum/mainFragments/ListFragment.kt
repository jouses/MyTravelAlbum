package com.jouse.mytravelalbum.mainFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.app.Activity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.jouse.mytravelalbum.R
import com.jouse.mytravelalbum.adapter.AlbumListAdapter
import com.jouse.mytravelalbum.databinding.FragmentListBinding
import com.jouse.mytravelalbum.model.TravelTitle

class ListFragment : Fragment() {
    lateinit var binding: FragmentListBinding
    private val travelTitles = ArrayList<TravelTitle>()
    lateinit var albumListAdapter: AlbumListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.AlbumListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        albumListAdapter = AlbumListAdapter(travelTitles,this)
        binding.AlbumListRecyclerView.adapter = albumListAdapter

        binding.addAlbumButton.setOnClickListener{
            replaceFragment(AddTravelFragment(true,null,null),"addTravelFragment")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        travelTitles.clear()
        val travelDatabase = requireActivity().openOrCreateDatabase("Travels", MODE_PRIVATE, null)

        try {
            val cursor = travelDatabase.rawQuery("SELECT * FROM travels GROUP BY name ORDER BY id DESC",null)
            val idIx = cursor.getColumnIndex("id")
            val nameIx = cursor.getColumnIndex("name")
            val locationIx = cursor.getColumnIndex("location")

            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIx)
                val name = cursor.getString(nameIx)
                val location = cursor.getString(locationIx)
                travelTitles.add(TravelTitle(id,name,location))
            }
            cursor.close()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        albumListAdapter.notifyDataSetChanged()
    }

    fun replaceFragment(fragment: Fragment, tag: String){
        if(tag != requireActivity().supportFragmentManager.fragments.last().tag){
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.main_frame_layout,fragment).addToBackStack(tag).commit()
        }
    }
}