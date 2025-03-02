package com.tanercuhadar.textrecognitionapp

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tanercuhadar.textrecognitionapp.databinding.ActivityMainBinding
import com.tanercuhadar.textrecognitionapp.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val (bitmap,uri) = getImageFromIntent()
        setImage(bitmap,uri)
        binding.buttonBack.setOnClickListener {
            //geriye dönme fonksiyonu
            onBackPressed()
        }

    }
  // ekranın ayarlamak için kodlar
    private fun setImage(bitmap: Bitmap?, uri: String?) {
 binding.apply {
     // ekranı önce temizliyoruz
     imageViewResult.setImageDrawable(null)
     if (uri != null){
         imageViewResult.setImageURI(Uri.parse(uri))
 }else{
     imageViewResult.setImageBitmap(bitmap)
     }


 }
    }

    // 2 li değişkeni tutan bir fonksiyon olduğu için Pair denir ve Pair kullanılır
    // bu kod diğer Activiyden geleni görseli almak için
    private fun getImageFromIntent(): Pair<Bitmap? , String?> {
        val intent = getIntent()
        val bitmap = if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("bitmapimage", Bitmap::class.java)
        }
        else{
            intent.getParcelableExtra("bitmapimage")
        }
        val uri = intent.getStringExtra("uriimage")
        return Pair(bitmap,uri)

    }


}