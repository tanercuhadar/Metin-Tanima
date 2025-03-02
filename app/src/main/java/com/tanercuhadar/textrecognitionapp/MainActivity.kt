

package com.tanercuhadar.textrecognitionapp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tanercuhadar.textrecognitionapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var detectImageUri: Uri? = null
    private var detectImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.textViewResult.movementMethod = ScrollingMovementMethod()
        binding.buttonCopy.bringToFront()
        copy()
        buttonClick()
    }

    private fun buttonClick() {
        binding.apply {
            buttonCamera.setOnClickListener {
                controlCameraPermisson()
            }
            buttonGallery.setOnClickListener {
                controlGalleryPermisson()
            }
            buttonDetect.setOnClickListener {
                detectImage?.let {
                    Toast.makeText(this@MainActivity, "Re Recogniton Image!", Toast.LENGTH_SHORT).show()
                    setRecognitonTextFromBitmap(detectImage!!)
                }
            }
            buttonShowImage.setOnClickListener {
                if (detectImage != null && detectImageUri != null) {
                    val intent = Intent(this@MainActivity, ResultActivity::class.java)
                    intent.putExtra("uriimage", detectImageUri.toString())
                    startActivity(intent)
                } else if (detectImage != null) {
                    val intent = Intent(this@MainActivity, ResultActivity::class.java)
                    intent.putExtra("bitmapimage", detectImage)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "No Image Have Been Selected ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun controlCameraPermisson() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), camera_PERMİSSON_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            detectImage = result.data?.extras?.get("data") as Bitmap
            setRecognitonTextFromBitmap(detectImage!!)
        }
    }

    private fun controlGalleryPermisson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), Gallery_PERMİSSON_CODE)
            } else {
                openGallery()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Gallery_PERMİSSON_CODE)
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri: Uri? = result.data?.data
        if (result.resultCode == RESULT_OK && uri != null) {
            detectImageUri = uri
            contentResolver.openInputStream(detectImageUri!!)?.use { inputStream ->
                detectImage = BitmapFactory.decodeStream(inputStream)
                setRecognitonTextFromBitmap(detectImage!!)
            }
        }
    }

    private fun copy() {
        if (!binding.textViewResult.text.isNullOrEmpty()) {
            binding.buttonCopy.setOnClickListener {
                val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("copied", binding.textViewResult.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setRecognitonTextFromBitmap(bitmap: Bitmap) {
        runBlocking {
            withContext(Dispatchers.Default) {
                viewModel.textRecognizer(this@MainActivity, binding.textViewResult, bitmap)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            camera_PERMİSSON_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera Permisson Denided!", Toast.LENGTH_SHORT).show()
                }
            }
            Gallery_PERMİSSON_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Gallery Permisson Denided", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val camera_PERMİSSON_CODE: Int = 0
        private const val Gallery_PERMİSSON_CODE: Int = 1
    }
}
