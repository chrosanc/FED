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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import bangkit.project.fed.MainActivity
import bangkit.project.fed.R
import bangkit.project.fed.databinding.ActivityLoginBinding
import bangkit.project.fed.databinding.LogindialogBinding
import bangkit.project.fed.databinding.RegisterdialogBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var registerBinding: RegisterdialogBinding
    private lateinit var loginBinding: LogindialogBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.buttonRegister.setOnClickListener {
            showRegisterDialog()
        }

        binding.buttonLogin.setOnClickListener {
            showLoginDialog()
        }


    }

    private fun showLoginDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loginBinding = LogindialogBinding.inflate(layoutInflater)
        dialog.setContentView(loginBinding.root)

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

                if(email.isEmpty()) {
                    emailEd.error = "Email Should Not be Empty"
                } else if (password.isEmpty()) {
                    passwordEd.error = "Password Should Not be Empty"
                } else {
                    progressBar.visibility = View.VISIBLE
                    loginLayout.visibility = View.INVISIBLE

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {task ->
                            if(task.isSuccessful) {
                                Toast.makeText(this@LoginActivity,"Login Success", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                progressBar.visibility = View.INVISIBLE
                                loginLayout.visibility = View.VISIBLE
                                Toast.makeText(this@LoginActivity, "Login Failed, " + task.exception?.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                }


            }


        }




        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
                val emailEd = dialog.findViewById<EditText>(R.id.emailEd)
                val passwordEd = dialog.findViewById<EditText>(R.id.passwordEd)
                val email = emailEd.text.toString().trim()
                val password = passwordEd.text.toString().trim()

                if(email.isEmpty()) {
                    emailEd.error = "Email Should Not be Empty"
                } else if (password.isEmpty()) {
                    passwordEd.error = "Password Should Not be Empty"
                } else {
                    progressBar.visibility = View.VISIBLE
                    registerLayout.visibility = View.INVISIBLE

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {task ->
                            if(task.isSuccessful) {
                                Toast.makeText(this@LoginActivity,"Regristation Success, please login", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                progressBar.visibility = View.INVISIBLE
                                registerLayout.visibility = View.VISIBLE
                                Toast.makeText(this@LoginActivity, "Regristasi Failed, " + task.exception?.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                }



            }


        }


        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }
}