package bangkit.project.fed.ui.setting

import android.app.LocaleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.R
import bangkit.project.fed.data.SharedViewModel
import bangkit.project.fed.data.ViewModelFactory
import bangkit.project.fed.data.datastore.PreferencesDataStore
import bangkit.project.fed.data.datastore.dataStore
import bangkit.project.fed.databinding.FragmentSettingBinding
import bangkit.project.fed.ui.login.LoginActivity
import bangkit.project.fed.ui.setting.configureAccount.ConfigureAccountActivity
import bangkit.project.fed.ui.setting.configureprofile.ConfigureProfileActivity

class SettingFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingViewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.selectedImageUri.observe(viewLifecycleOwner) { imageUri ->
            if (imageUri != null) {
                binding.profilePicture.setImageURI(imageUri)
            } else {
                binding.profilePicture.setImageResource(R.drawable.potokucing)
            }
        }

        val pref = PreferencesDataStore.getInstance(requireContext().applicationContext.dataStore)
        val viewModelFactory = ViewModelFactory(pref)
        settingViewModel = ViewModelProvider(this, viewModelFactory)[SettingViewModel::class.java]
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        val language: Array<String> = resources.getStringArray(R.array.language_array)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, language)
        settingViewModel.getLocale().observe(viewLifecycleOwner) {
            when (it) {
                "in" -> {
                    binding.spLanguage.setSelection(arrayAdapter.getPosition(language[1]))
                }
                else -> {
                    binding.spLanguage.setSelection(arrayAdapter.getPosition(language[0]))
                }
            }
        }

        binding.spLanguage.apply {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if(parent?.getItemAtPosition(position).toString() == language[1]) {
                        setLocale("in", settingViewModel)
                    } else {
                        setLocale("en", settingViewModel)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            adapter = arrayAdapter
        }


        settingViewModel.getThemeSetting().observe(viewLifecycleOwner) {isDarkModeActive: Boolean ->
            if(isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.profilePicture.alpha = 0f
                binding.profilePicture.animate().alpha(1f).start()
                binding.toggleDarkMode.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.profilePicture.alpha = 0f
                binding.profilePicture.animate().alpha(1f).start()
                binding.toggleDarkMode.isChecked = false
            }
        }

        binding.toggleDarkMode.setOnCheckedChangeListener{ _: CompoundButton?, isChecked: Boolean ->
            settingViewModel.saveThemeSetting(isChecked)
        }


        binding.buttonLogout.setOnClickListener {
            settingViewModel.logout()
            navigatetoLogin()
        }

        binding.buttonDisplayProfile.setOnClickListener {
            val intent = Intent(activity, ConfigureProfileActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_CODE_CONFIGURE_PROFILE)
        }

        binding.buttonAccount.setOnClickListener{
            val intent = Intent(activity, ConfigureAccountActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_CODE_CONFIGURE_ACCOUNT)
        }

        settingViewModel.userName.observe(viewLifecycleOwner) { displayName ->
            binding.nameText.text = displayName
        }

        settingViewModel.userEmail.observe(viewLifecycleOwner) { userEmail ->
            binding.emailText.text = userEmail
        }

        return binding.root

    }


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CONFIGURE_PROFILE && resultCode == AppCompatActivity.RESULT_OK) {
            updateUserData(data)
        }
        if (requestCode ==  REQUEST_CODE_CONFIGURE_ACCOUNT && resultCode == AppCompatActivity.RESULT_OK) {
            updateUserData(data)
        }
    }

    private fun updateUserData(data: Intent?) {
        val imageUri: Uri? = data?.getParcelableExtra("imageUri")

        sharedViewModel.setSelectedImageUri(imageUri)

        if (imageUri != null) {
            binding.profilePicture.setImageURI(imageUri)
        } else {
            binding.profilePicture.setImageResource(R.drawable.potokucing)
        }

        settingViewModel.loadUserData()
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

    private fun setLocale(localeCode: String, viewModel: SettingViewModel) {
        viewModel.saveLocale(localeCode)
        val context = requireContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(AppCompatActivity.LOCALE_SERVICE).let { localeManager ->
                if (localeManager is LocaleManager) {
                    localeManager.applicationLocales = LocaleList.forLanguageTags(localeCode)
                }
            }
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_CONFIGURE_PROFILE = 123
        private const val REQUEST_CODE_CONFIGURE_ACCOUNT = 111
    }
}