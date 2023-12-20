package bangkit.project.fed.ui.captureegg.camera

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import bangkit.project.fed.R
import bangkit.project.fed.databinding.FragmentRealtimeBinding
import bangkit.project.fed.ui.captureegg.imagedisplay.ImageDisplayActivity
import com.google.common.util.concurrent.ListenableFuture
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.text.NumberFormat
import java.util.concurrent.Executors

class RealtimeFragment : Fragment() {

    private var _binding : FragmentRealtimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageCapture: ImageCapture
    private lateinit var preview: Preview
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRealtimeBinding.inflate(inflater, container, false)


        binding.flashlight.setOnClickListener {
            toggleFlash()
        }

        binding.capture.setOnClickListener {
            takePicture()
        }

        startCameraCapture()

        return binding.root
    }

    private fun toggleFlash() {


    }

    private fun startCameraCapture() {

        imageClassifierHelper =
            ImageClassifierHelper(
                context = requireActivity(),
                imageClassifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(error: String) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        activity?.runOnUiThread {
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
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // CameraProvider is now ready
            val cameraProvider = cameraProviderFuture.get()

            // Set up the preview
            preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.preview.surfaceProvider)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(activity?.windowManager!!.defaultDisplay.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                        imageClassifierHelper.classify(image)
                    }
                }

            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(activity?.windowManager!!.defaultDisplay.rotation)
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
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePicture() {
        val photoFile = createTemporaryFile("picture", ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val mImageUri =
                        FileProvider.getUriForFile(requireContext(), "bangkit.project.fed.provider", photoFile)
                    val intent = Intent(requireContext(), ImageDisplayActivity::class.java)
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
        val tempDir = activity?.filesDir
        if (tempDir != null) {
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
        }
        return File.createTempFile(part, ext, tempDir)
    }

    override fun onResume() {
        super.onResume()
        startCameraCapture()
    }

}


