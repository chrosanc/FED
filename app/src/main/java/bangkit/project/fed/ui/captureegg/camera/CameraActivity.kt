package bangkit.project.fed.ui.captureegg.camera

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import bangkit.project.fed.databinding.ActivityCameraBinding
import bangkit.project.fed.ui.captureegg.imagedisplay.ImageDisplayActivity
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.text.NumberFormat
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var preview: Preview
    private lateinit var gestureDetector: GestureDetector
    private lateinit var imageClassifierHelper: ImageClassifierHelper

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

        imageClassifierHelper =
            ImageClassifierHelper(
                context = this,
                imageClassifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(error: String) {
                        runOnUiThread {
                            Toast.makeText(this@CameraActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        runOnUiThread {
                            results?.let { it ->
                                if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                    println(it)
                                    val sortedCategories =
                                        it[0].categories.sortedByDescending { it?.score }
                                    val displayResult =
                                        sortedCategories.joinToString("\n") {
                                            "${it.label} " + NumberFormat.getPercentInstance().format(it.score).trim()
                                        }
                                    binding.detectionText.text = displayResult
                                }
                            }
                        }
                    }
                })


        // Get the CameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // CameraProvider is now ready
            val cameraProvider = cameraProviderFuture.get()

            // Set up the preview
            preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.preview.surfaceProvider)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(binding.preview.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) {image ->
                        imageClassifierHelper.classify(image)
                    }
                }

            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(windowManager.defaultDisplay.rotation)
                .build()

            try {
                cameraProvider.unbindAll()
                // Connect the preview use case to the Camera
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
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

        override fun onPause() {
        super.onPause()
        imageClassifierHelper.clearImageClassifier()
    }

    override fun onResume() {
        super.onResume()
        if (imageClassifierHelper.isClosed()) {
            imageClassifierHelper.setupImageClassifier()
        }
    }


}
