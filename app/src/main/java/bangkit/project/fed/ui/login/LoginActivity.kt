package bangkit.project.fed.ui.login

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.lifecycle.Observer
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
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        getUserName()

        binding.buttonRegister.setOnClickListener {
            showRegisterDialog()
        }

        binding.buttonLogin.setOnClickListener {
            showLoginDialog()
        }
    }

    private fun getUserName() {
        val pref = PreferencesDataStore.getInstance(application.dataStore)
        val settingViewModel = ViewModelProvider(this, ViewModelFactory(pref))[SettingViewModel::class.java]
        settingViewModel.userName.observe(this, Observer { userName ->
            val username = userName

            if (auth.currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this,
                    getString(R.string.welcome_back, username), Toast.LENGTH_SHORT).show()
                finish()
            }

        })



    }

    private fun showLoginDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loginBinding = LogindialogBinding.inflate(layoutInflater)
        dialog.setContentView(loginBinding.root)


        val textForgot = dialog.findViewById<TextView>(R.id.forgetText)
        textForgot.setOnClickListener{
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

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                progressBar.visibility = View.INVISIBLE
                                loginLayout.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Failed, " + task.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                }


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

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                val user = hashMapOf(
                                    "displayName" to name,
                                    "email" to email
                                )
                                firestore.collection("users")
                                    .document(auth.currentUser!!.uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Regristation Success, please login",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                    }

                            } else {
                                progressBar.visibility = View.INVISIBLE
                                registerLayout.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Regristasi Failed, " + task.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
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
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }
}