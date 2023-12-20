package bangkit.project.fed.ui.captureegg.imagedisplay

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bangkit.project.fed.R
import bangkit.project.fed.data.api.ApiConfig
import bangkit.project.fed.databinding.ActivityImageDisplayBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        binding.uploadButton.setOnClickListener {
            uploadImage()
        }
        getImage()
    }

    private fun uploadImage() {
            val imageName = binding.photonameEd.text.toString().trim()

            if (imageName.isEmpty()) {
                showToast(getString(R.string.empty_image_warning))
                return
            }
            showLoading(true)
            lifecycleScope.launch {
                val labelRequestBody = imageName.toRequestBody("text/plain".toMediaType())

                val file = convertBitmapToFile(originalBitmap).reduceFileImage()
                val requestFile = file.asRequestBody("multipart/form-data".toMediaType())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                Log.i("infoo", file.length().toString())

                val apiService = ApiConfig.getApiService()

                try {
                    val response = apiService.uploadImage(filePart, labelRequestBody)
                    showToast("Image uploaded successfully. Response: ${response.message}")
                } catch (e: Exception) {
                    showToast("Error uploading image: ${e.message}")
                    Log.e("UploadImage", "Error uploading image", e)
                } finally {
                    showLoading(false) // Menyembunyikan ProgressBar setelah upload selesai
                }
            }
        }

    private fun convertBitmapToFile(bitmap: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timeStamp}.png"
        val file = File(cacheDir, fileName)

        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("ConvertBitmapToFile", "Error converting bitmap to file", e)
        }

        return file
    }

    private fun File.reduceFileImage(): File {
        val file = this
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMAL_SIZE)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
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
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object{
        private const val MAXIMAL_SIZE = 1000000
    }
}