package com.jouse.mytravelalbum.mainFragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.MODE_PRIVATE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jouse.mytravelalbum.R
import com.jouse.mytravelalbum.adapter.AddImageAdapter
import com.jouse.mytravelalbum.databinding.FragmentAddTravelBinding
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION", "SameParameterValue")
class AddTravelFragment(val add: Boolean, val name: String?, val location: String?) : Fragment() {
    private lateinit var binding: FragmentAddTravelBinding
    lateinit var travelDatabase: SQLiteDatabase
    private val imageArray = ArrayList<Bitmap>()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var addImageAdapter: AddImageAdapter
    private lateinit var permissionType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        travelDatabase = requireActivity().openOrCreateDatabase("Travels", MODE_PRIVATE, null)
        permissionType = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddTravelBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerLaunchers()

        binding.addImageRecyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        addImageAdapter = AddImageAdapter(imageArray,this)
        binding.addImageRecyclerView.adapter = addImageAdapter

        if (add) {
            binding.travelSaveButton.setOnClickListener {
                if (binding.nameInput.text.toString().isNotEmpty() && binding.locationInput.text.toString().isNotEmpty() && imageArray.isNotEmpty()) {
                    saveTravel()
                }
                else {
                    Toast.makeText(requireContext(),getString(R.string.please_fill_out_all_fields),Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            val cursor = travelDatabase.rawQuery("SELECT * FROM travels WHERE name = ?", arrayOf(name))
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                val imageByteArray = cursor.getBlob(imageIx)
                val image = BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray.size)
                imageArray.add(image)
            }
            cursor.close()

            addImageAdapter.notifyDataSetChanged()
            binding.nameInput.apply {
                setText(name)
                isFocusable = false
            }
            binding.locationInput.apply {
                setText(location)
                isFocusable = false
            }
            binding.travelSaveButton.visibility = View.INVISIBLE
        }
    }

    fun addImage(){
        if (ContextCompat.checkSelfPermission(requireContext(),permissionType) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),permissionType)) {
                Snackbar.make(binding.root,getString(R.string.permission_needed),Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.give_permission)) {
                    permissionLauncher.launch(permissionType)
                }.show()
            }
            else {
                permissionLauncher.launch(permissionType)
            }
        }
        else {
            activityResultLauncher.launch(Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }
    }

    private fun registerLaunchers() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.let {resultIntent ->
                    resultIntent.data?.let {imageUri ->
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,imageUri)
                            val imageBitmap = ImageDecoder.decodeBitmap(source)
                            imageArray.add(makeSmallerBitmap(imageBitmap))
                        }
                        else {
                            val imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,imageUri)
                            imageArray.add(makeSmallerBitmap(imageBitmap))
                        }
                        addImageAdapter.notifyItemChanged(imageArray.lastIndex)
                        addImageAdapter.notifyItemInserted(imageArray.size + 1)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {permission ->
            if (permission) {
                activityResultLauncher.launch(Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
            }
        }
    }

    private fun makeSmallerBitmap(image: Bitmap): Bitmap {
        var width = image.width
        var height = image.height
        println(width)
        println(height)

        val bitmapRadio = width.toDouble() / height.toDouble()

        if (bitmapRadio > 1) {
            //Landscape
            width = 300
            height = (width / bitmapRadio).toInt()
        }
        else {
            //portrait
            height = 300
            width = (height * bitmapRadio).toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    private fun saveTravel() {
        println("Save")
        val name = binding.nameInput.text.toString()
        val location = binding.locationInput.text.toString()
        val imageByteArrays = ArrayList<ByteArray>()

        for (bitmap in imageArray) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val imageByteArray = outputStream.toByteArray()
            imageByteArrays.add(imageByteArray)
        }

        try {
            travelDatabase.execSQL("CREATE TABLE IF NOT EXISTS travels(id INTEGER PRIMARY KEY, name VARCHAR, location VARCHAR, image BLOB)")
            val statement = travelDatabase.compileStatement("INSERT INTO travels(name, location, image) VALUES(?, ?, ?)")
            for (imageByteArray in imageByteArrays) {
                statement.apply {
                    bindString(1,name)
                    bindString(2,location)
                    bindBlob(3,imageByteArray)
                    execute()
                }
            }
            requireActivity().supportFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}