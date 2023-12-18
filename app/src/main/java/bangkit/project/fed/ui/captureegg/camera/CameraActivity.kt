package bangkit.project.fed.ui.captureegg.camera

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import bangkit.project.fed.R
import bangkit.project.fed.databinding.ActivityCameraBinding
import bangkit.project.fed.ui.captureegg.imagedisplay.ImageDisplayActivity
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCameraCapture()

    }

    private fun startCameraCapture() {
        // Get the CameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // CameraProvider is now ready
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.preview.surfaceProvider)

            // Create a unique file to save the image
            val photoFile = createTemporaryFile("picture", ".jpg")

            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(windowManager.defaultDisplay.rotation)
                .build()

            binding.capture.setOnClickListener {
                try {
                    // Connect the use cases to the Camera
                    cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, imageCapture
                    )

                    // Set up the output file for the image
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    // Capture the image
                    imageCapture.takePicture(
                        outputOptions, ContextCompat.getMainExecutor(this),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                // Image saved successfully, proceed to the next step
                                val capturedImageUri = Uri.fromFile(photoFile)
                                val intent = Intent(this@CameraActivity, ImageDisplayActivity::class.java)
                                intent.putExtra("capturedImage", capturedImageUri)
                                startActivity(intent)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                // Handle the error
                                exception.printStackTrace()
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun createTemporaryFile(part: String, ext: String): File {

        val tempDir = filesDir
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return File.createTempFile(part, ext, tempDir)
    }


}