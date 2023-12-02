package bangkit.project.fed.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.R
import bangkit.project.fed.data.ViewModelFactory
import bangkit.project.fed.data.datastore.PreferencesDataStore
import bangkit.project.fed.data.datastore.dataStore
import bangkit.project.fed.databinding.FragmentSettingBinding
import bangkit.project.fed.ui.login.LoginActivity

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val pref = PreferencesDataStore.getInstance(requireContext().applicationContext.dataStore)
        val viewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[SettingViewModel::class.java]
        _binding = FragmentSettingBinding.inflate(inflater, container, false)


        viewModel.getThemeSetting().observe(viewLifecycleOwner) {isDarkModeActive: Boolean ->
            if(isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.profilePicture.setImageResource(R.drawable.profiletest)
                binding.profilePicture.alpha = 0f
                binding.profilePicture.animate().alpha(1f).start()
                binding.toggleDarkMode.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.profilePicture.setImageResource(R.drawable.whiteman)
                binding.profilePicture.alpha = 0f
                binding.profilePicture.animate().alpha(1f).start()
                binding.toggleDarkMode.isChecked = false
            }
        }

        binding.toggleDarkMode.setOnCheckedChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            viewModel.saveThemeSetting(isChecked)
        }


        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
            navigatetoLogin()
        }

        viewModel.userName.observe(viewLifecycleOwner, Observer { userName ->
            binding.nameText.text = userName
        })

        viewModel.userEmail.observe(viewLifecycleOwner, Observer {userEmail ->
            binding.emailText.text = userEmail
        })

        return binding.root
    }

    private fun navigatetoLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}