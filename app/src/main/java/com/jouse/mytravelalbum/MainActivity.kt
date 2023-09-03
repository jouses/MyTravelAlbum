package com.jouse.mytravelalbum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jouse.mytravelalbum.databinding.ActivityMainBinding
import com.jouse.mytravelalbum.mainFragments.ListFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().replace(R.id.main_frame_layout, ListFragment()).commit()
    }
}