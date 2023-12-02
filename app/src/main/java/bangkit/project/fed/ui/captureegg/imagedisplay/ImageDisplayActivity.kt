package bangkit.project.fed.ui.captureegg.imagedisplay

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import bangkit.project.fed.databinding.ActivityImageDisplayBinding

class ImageDisplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDisplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        showImage()


    }

    private fun showImage() {
        val imageUri = intent.getParcelableExtra<Uri>("imageUri")
        val capturedImage = intent.getParcelableExtra<Bitmap>("capturedImage")

        if (imageUri != null) {
            binding.imageView.setImageURI(imageUri)
        } else if (capturedImage != null){
            binding.imageView.setImageBitmap(capturedImage)
        }
    }
}