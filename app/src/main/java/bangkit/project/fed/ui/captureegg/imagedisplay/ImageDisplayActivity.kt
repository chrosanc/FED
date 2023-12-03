package bangkit.project.fed.ui.captureegg.imagedisplay

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import bangkit.project.fed.databinding.ActivityImageDisplayBinding
import com.bumptech.glide.Glide

class ImageDisplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDisplayBinding
    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        getImage()
    }

    private fun getImage() {
        val imageUri = intent.getParcelableExtra<Uri>("imageUri")
        val capturedImage = intent.getParcelableExtra<Uri>("capturedImage")
        when {
            imageUri != null -> showImage(imageUri)
            capturedImage != null -> showCapturedImage(capturedImage)
            else -> {
                Toast.makeText(this@ImageDisplayActivity, "No Image Available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCapturedImage(capturedImage: Uri) {
        this.contentResolver.notifyChange(capturedImage, null)
        originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, capturedImage)
        binding.imageView.setImageBitmap(originalBitmap)

        binding.rotateButton.setOnClickListener {
            rotateBitmap(-90f)
        }

    }

    private fun showImage(uri: Uri) {

        originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        Glide.with(this)
            .load(originalBitmap)
            .into(binding.imageView)

        binding.rotateButton.setOnClickListener {
            rotateBitmap(-90f)
        }
    }

    private fun rotateBitmap(degrees: Float) {
        val matrix = Matrix()
        matrix.postRotate(degrees)

        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )

        binding.imageView.setImageBitmap(rotatedBitmap)
        originalBitmap = rotatedBitmap

    }

}