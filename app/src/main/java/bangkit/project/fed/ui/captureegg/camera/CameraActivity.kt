package bangkit.project.fed.ui.captureegg.camera

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import bangkit.project.fed.databinding.ActivityCameraBinding
import com.google.android.material.tabs.TabLayoutMediator
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.task.vision.classifier.Classifications

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewPagerAdapter: CameraPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPagerAdapter = CameraPagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Camera"
                1 -> tab.text = "Realtime"
            }
        }.attach()

        if (binding.viewPager.currentItem == 1) {
            checkDeviceSupport()
        }

    }

    private fun checkDeviceSupport() {
        if (CompatibilityList().isDelegateSupportedOnThisDevice) {
            val imageClassifierHelper = ImageClassifierHelper(
                context = this,
                currentDelegate = ImageClassifierHelper.DELEGATE_GPU,
                imageClassifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(error: String) {
                        // Handle the error and show the alert dialog
                        showAlertDialog("Error", error)

                        // If GPU is not supported, switch back to the Camera fragment (position 0)
                        binding.viewPager.currentItem = 0
                    }

                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        // Handle classification results if needed
                    }
                }
            )

            // Now, you can use the imageClassifierHelper for GPU-supported operations
        } else {
            // GPU is not supported, handle accordingly (show alert dialog, switch fragments, etc.)
            showAlertDialog("Error", "GPU is not supported on this device")
            binding.viewPager.currentItem = 0
        }
    }

    private fun showAlertDialog(message: String, error: String) {
        val builder = AlertDialog.Builder(this@CameraActivity)
        builder.setTitle(message).setMessage(error)
            .setPositiveButton("OK") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

}

