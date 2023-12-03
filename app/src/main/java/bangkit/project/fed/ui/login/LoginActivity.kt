package bangkit.project.fed.ui.login

import android.app.Dialog
import android.app.LocaleManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.MainActivity
import bangkit.project.fed.R
import bangkit.project.fed.data.ViewModelFactory
import bangkit.project.fed.data.datastore.PreferencesDataStore
import bangkit.project.fed.data.datastore.dataStore
import bangkit.project.fed.databinding.ActivityLoginBinding
import bangkit.project.fed.databinding.LogindialogBinding
import bangkit.project.fed.databinding.RegisterdialogBinding
import bangkit.project.fed.ui.setting.SettingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var registerBinding: RegisterdialogBinding
    private lateinit var loginBinding: LogindialogBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: LoginViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var settingViewModel: SettingViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]


        val pref = PreferencesDataStore.getInstance(this.applicationContext.dataStore)
        settingViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[SettingViewModel::class.java]

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        viewModel.userName.observe(this) { userName ->
            if (auth.currentUser != null) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.welcome_back, userName),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        binding.buttonRegister.setOnClickListener {
            showRegisterDialog()
        }

        binding.buttonLogin.setOnClickListener {
            showLoginDialog()
        }

        settingViewModel.getLocale().observe(this) {
            setLocale(it)
        }
    }


    private fun showLoginDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loginBinding = LogindialogBinding.inflate(layoutInflater)
        dialog.setContentView(loginBinding.root)


        val textForgot = dialog.findViewById<TextView>(R.id.forgetText)
        textForgot.setOnClickListener {
            showForgotPWDialog()
        }

        //Login Dialog Feature Implementation
        loginBinding.apply {
            buttonRegister.setOnClickListener {
                showRegisterDialog()
            }

            buttonLogin.setOnClickListener {
                val emailEd = loginBinding.emailEd
                val passwordEd = loginBinding.passwordEd
                val email = emailEd.text.toString().trim()
                val password = passwordEd.text.toString().trim()

                if (email.isEmpty()) {
                    emailEd.error = "Email Should Not be Empty"
                } else if (password.isEmpty()) {
                    passwordEd.error = "Password Should Not be Empty"
                } else {
                    progressBar.visibility = View.VISIBLE
                    loginLayout.visibility = View.INVISIBLE

                    viewModel.loginUser(email, password)
                }
            }
        }

        viewModel.loginResult.observe(this) { success ->
            if (success) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_success),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                loginBinding.progressBar.visibility = View.INVISIBLE
                loginBinding.loginLayout.visibility = View.VISIBLE
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(
                this@LoginActivity,
                getString(R.string.loginfailed, errorMessage),
                Toast.LENGTH_SHORT
            ).show()

        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    private fun showRegisterDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        registerBinding = RegisterdialogBinding.inflate(layoutInflater)
        dialog.setContentView(registerBinding.root)

        //Register Dialog Feature Implementation
        registerBinding.apply {

            buttonLogin.setOnClickListener {
                showLoginDialog()
            }

            buttonRegister.setOnClickListener {
                val name = registerBinding.nameEd.text.toString().trim()
                val emailEd = registerBinding.emailEd
                val passwordEd = registerBinding.passwordEd
                val email = emailEd.text.toString().trim()
                val password = passwordEd.text.toString().trim()

                if (email.isEmpty()) {
                    emailEd.error = "Email Should Not be Empty"
                } else if (password.isEmpty()) {
                    passwordEd.error = "Password Should Not be Empty"
                } else {
                    progressBar.visibility = View.VISIBLE
                    registerLayout.visibility = View.INVISIBLE

                    viewModel.registerUser(name, email, password)
                }
            }
        }

        viewModel.registrationResult.observe(this) { success ->
            if (success) {
                Toast.makeText(
                    this@LoginActivity,
                    "Regristation Success, please check your email address for verification.",
                    Toast.LENGTH_LONG
                ).show()
                dialog.dismiss()
            } else {
                registerBinding.progressBar.visibility = View.INVISIBLE
                registerBinding.registerLayout.visibility = View.VISIBLE
                Toast.makeText(this@LoginActivity, "Regristation Failed", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    private fun showForgotPWDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.forgotdialog)



        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    private fun setLocale( localeCode: String) {
        val context = this@LoginActivity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LOCALE_SERVICE).let { localeManager ->
                if (localeManager is LocaleManager) {
                    localeManager.applicationLocales = LocaleList.forLanguageTags(localeCode)
                }
            }
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
        }
    }
}