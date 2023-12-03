package bangkit.project.fed.ui.setting

import android.app.LocaleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.R
import bangkit.project.fed.data.ViewModelFactory
import bangkit.project.fed.data.datastore.PreferencesDataStore
import bangkit.project.fed.data.datastore.dataStore
import bangkit.project.fed.databinding.FragmentSettingBinding
import bangkit.project.fed.ui.login.LoginActivity
import java.text.FieldPosition

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

        val language: Array<String> = resources.getStringArray(R.array.language_array)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, language)
        viewModel.getLocale().observe(viewLifecycleOwner) {
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
                        setLocale("in", viewModel)
                    } else {
                        setLocale("en", viewModel)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            adapter = arrayAdapter
        }


        viewModel.getThemeSetting().observe(viewLifecycleOwner) {isDarkModeActive: Boolean ->
            if(isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.profilePicture.setImageResource(R.drawable.profiletest)
                binding.profilePicture.alpha = 0f
                binding.profilePicture.animate().alpha(1f).start()
                binding.toggleDarkMode.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.profilePicture.setImageResource(R.drawable.potokucing)
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

        viewModel.userName.observe(viewLifecycleOwner, Observer { displayName ->
            binding.nameText.text = displayName
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

}