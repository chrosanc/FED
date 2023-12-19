package bangkit.project.fed.ui.captureegg.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import bangkit.project.fed.databinding.ActivityCameraBinding
import bangkit.project.fed.ui.captureegg.imagedisplay.ImageDisplayActivity
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var preview: Preview
    private lateinit var gestureDetector: GestureDetector
    private var flashEnabled = false
    private var cameraControl : CameraControl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gestureDetector = GestureDetector(this, GestureListener())

        binding.preview.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                binding.preview.performClick()
            }
            true
        }

        binding.flashlight.setOnClickListener {
            toggleFlash()
        }

        binding.capture.setOnClickListener {
            takePicture()
        }

        startCameraCapture()
    }

    private fun startCameraCapture() {
        // Get the CameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // CameraProvider is now ready
            val cameraProvider = cameraProviderFuture.get()

            // Set up the preview
            preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.preview.surfaceProvider)

            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(windowManager.defaultDisplay.rotation)
                .build()

            try {
                // Connect the preview use case to the Camera
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview
                )

                // Now, bind the ImageCapture use case to the Camera
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleFlash() {
        flashEnabled = !flashEnabled
        imageCapture.flashMode =
            if (flashEnabled) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }
    }

    private fun takePicture() {
        val photoFile = createTemporaryFile("picture", ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val mImageUri =
                        FileProvider.getUriForFile(this@CameraActivity, "bangkit.project.fed.provider", photoFile)
                    val intent = Intent(this@CameraActivity, ImageDisplayActivity::class.java)
                    intent.putExtra("capturedImage", mImageUri)
                    startActivity(intent)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun createTemporaryFile(part: String, ext: String): File {
        val tempDir = filesDir
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return File.createTempFile(part, ext, tempDir)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            e?.let {
                handleTapToFocus(e.x, e.y)
                return true
            }
            return false
        }
    }

    private fun handleTapToFocus(x: Float, y: Float) {
        val meteringPointFactory = binding.preview.meteringPointFactory
        val autoFocusPoint = meteringPointFactory.createPoint(x, y)

        try {
            val action = FocusMeteringAction.Builder(autoFocusPoint).build()
            cameraControl?.startFocusAndMetering(action)
        } catch (e: CameraInfoUnavailableException) {
            e.printStackTrace()
        }
    }

}
