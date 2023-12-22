package bangkit.project.fed.ui.setting.configureprofile

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.R
import bangkit.project.fed.databinding.ActivityConfigureProfileBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConfigureProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigureProfileBinding
    private lateinit var configureProfileViewModel: ConfigureProfileViewModel
    private val IMAGEPICKERREQUEST = 1
    private val CAMERACAPTUREREQUEST = 2
    private var file: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigureProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureProfileViewModel = ViewModelProvider(this)[ConfigureProfileViewModel::class.java]

        configureProfileViewModel.userName.observe(this) { displayName ->
            binding.edName.setText(displayName)
            loadProfilePicture()
        }

        binding.tvChangeProfile.setOnClickListener {
            showImageSourceDialog()
        }

        binding.profilePicture.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnSave.setOnClickListener {
            showConfirmationDialog()
        }

        binding.btnCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.konfirmasi))
        builder.setMessage(getString(R.string.konfirmasi_message))

        builder.setPositiveButton("Ya") { _: DialogInterface, _: Int ->
            configureProfileViewModel.updateUserName(binding.edName.text.toString())

            val profilePictureBitmap = getBitmapFromImageView(binding.profilePicture)
            saveProfilePicture(profilePictureBitmap)

            val intent = Intent()
            file?.let {
                intent.putExtra("imageUri", Uri.fromFile(it))
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        builder.setNegativeButton("Tidak") { _: DialogInterface, _: Int ->
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveProfilePicture(bitmap: Bitmap) {
        try {
            val directory = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ProfilePictures")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )
            val fileName = "profile_picture_$timeStamp.jpg"

            file = File(directory, fileName)

            val file = File(directory, fileName)
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val savedImagePath = file.absolutePath
            Log.i("Berhasil", "Gambar disimpan secara lokal: $savedImagePath")

            showToast("Gambar berhasil disimpan")
        } catch (e: Exception) {
            Log.e("Gagal", "Gagal menyimpan gambar secara lokal: $e")
            showToast("Gagal menyimpan gambar")
        }
    }

    private fun loadProfilePicture() {
        val directory = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ProfilePictures")
        val files = directory.listFiles()

        if (files != null && files.isNotEmpty()) {

            val latestFile = files.maxByOrNull { it.lastModified() }
            val profilePictureBitmap = BitmapFactory.decodeFile(latestFile?.absolutePath)
            binding.profilePicture.setImageBitmap(profilePictureBitmap)
        }
    }

    private fun getBitmapFromImageView(imageView: ImageView): Bitmap {
        val drawable: Drawable = imageView.drawable
        val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
        return bitmap
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_image_source))
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> requestCameraPermission()
                1 -> requestGalleryPermission()
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun requestGalleryPermission() {
        val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                this,
                galleryPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startGalleryPicker()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(galleryPermission),
                IMAGEPICKERREQUEST
            )
        }
    }

    private fun requestCameraPermission() {
        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(
                this,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraCapture()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(cameraPermission),
                CAMERACAPTUREREQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            IMAGEPICKERREQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryPicker()
                }
            }

            CAMERACAPTUREREQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraCapture()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGEPICKERREQUEST -> {
                    val selectedImageUri: Uri? = data?.data
                    binding.profilePicture.setImageURI(selectedImageUri)

                    val profilePictureBitmap = getBitmapFromImageView(binding.profilePicture)
                    saveProfilePicture(profilePictureBitmap)
                }

                CAMERACAPTUREREQUEST -> {
                    val photo: Bitmap? = data?.extras?.get("data") as Bitmap?
                    binding.profilePicture.setImageBitmap(photo)

                    val profilePictureBitmap = getBitmapFromImageView(binding.profilePicture)
                    saveProfilePicture(profilePictureBitmap)
                }
            }
        }
    }


    @Suppress("DEPRECATION")
    private fun startGalleryPicker() {

        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, IMAGEPICKERREQUEST)

    }

    @Suppress("DEPRECATION")
    private fun startCameraCapture() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERACAPTUREREQUEST)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}