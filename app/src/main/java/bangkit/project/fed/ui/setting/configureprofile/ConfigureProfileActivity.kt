package bangkit.project.fed.ui.setting.configureprofile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import bangkit.project.fed.R
import bangkit.project.fed.databinding.ActivityConfigureProfileBinding

class ConfigureProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigureProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigureProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}