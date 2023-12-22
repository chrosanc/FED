package bangkit.project.fed

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import bangkit.project.fed.data.ViewModelFactory
import bangkit.project.fed.data.datastore.PreferencesDataStore
import bangkit.project.fed.data.datastore.dataStore
import bangkit.project.fed.databinding.ActivityMainBinding
import bangkit.project.fed.ui.captureegg.camera.CameraActivity
import bangkit.project.fed.ui.captureegg.imagedisplay.ImageDisplayActivity
import bangkit.project.fed.ui.setting.SettingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mImageUri: Uri
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private val IMAGEPICKERREQUEST = 1
    private val CAMERACAPTUREREQUEST = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance())

        val appCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckProviderFactory)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        imageCapture = ImageCapture.Builder().build()

        setUpTheme()

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_capture, R.id.navigation_setting
            )
        )
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_capture -> {
                    showImageSourceDialog()
                    true
                }

                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }

        }


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

    private fun startCameraCapture() {
        val intent = Intent(this@MainActivity, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun startGalleryPicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, IMAGEPICKERREQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGEPICKERREQUEST -> {
                    val selectedImage: Uri? = data?.data
                    val intent = Intent(this, ImageDisplayActivity::class.java)
                    intent.putExtra("imageUri", selectedImage)
                    startActivity(intent)
                }

                CAMERACAPTUREREQUEST -> {
                    val capturedImage: Uri = mImageUri
                    capturedImage.let {
                        val intent = Intent(this, ImageDisplayActivity::class.java)
                        intent.putExtra("capturedImage", capturedImage)
                        startActivity(intent)
                    }
                }
            }
        }
    }


    private fun setUpTheme() {
        val pref = PreferencesDataStore.getInstance(application.dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getThemeSetting().observe(this) { isDarkModeActive ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}