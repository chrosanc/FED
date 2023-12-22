package bangkit.project.fed.ui.setting.configureAccount

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.R
import bangkit.project.fed.databinding.ActivityConfigureAccountBinding

class ConfigureAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigureAccountBinding
    private lateinit var configureAccountViewModel: ConfigureAccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigureAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureAccountViewModel = ViewModelProvider(this)[ConfigureAccountViewModel::class.java]

        configureAccountViewModel.userEmail.observe(this) { displayEmail ->
            binding.emailEd.setText(displayEmail)
        }

        binding.btnSave.setOnClickListener {
            showConfirmationDialog()
        }

        binding.btnCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showConfirmationDialog() {
        val email = binding.emailEd.text.toString().trim()
        val newEmail = binding.newemailEd.text.toString().trim()
        val oldPassword = binding.passwordEd.text.toString().trim()

        if (!isValidEmail(email)) {
            binding.emailEd.error = getString(R.string.email_tidak_valid)
            return
        }

        if (!isValidEmail(newEmail)) {
            binding.newemailEd.error = getString(R.string.email_tidak_valid)
            return
        }

        if (oldPassword.length < 6) {
            binding.oldPasswordTextInputLayout.error =
                getString(R.string.password_minimal_characters)
            binding.oldPasswordTextInputLayout.errorIconDrawable = null
            Handler(Looper.getMainLooper()).postDelayed({
                binding.oldPasswordTextInputLayout.error = null
            }, 2000)
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.konfirmasi))
        builder.setMessage(getString(R.string.konfirmasi_message))

        builder.setPositiveButton("Ya") { _: DialogInterface, _: Int ->
            // Panggil metode di ViewModel untuk mengubah alamat email
            configureAccountViewModel.changeEmail(email, newEmail, oldPassword)
                .observe(this, Observer { result ->
                    val (isSuccess, errorMessage) = result
                    if (isSuccess) {
                        finish()
                    } else {
                        showErrorDialog(errorMessage)
                    }
                })
        }

        builder.setNegativeButton("Tidak") { _: DialogInterface, _: Int ->
            // Tidak melakukan apa-apa jika pengguna membatalkan perubahan
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showErrorDialog(errorMessage: String) {
        val errorBuilder = AlertDialog.Builder(this)
        errorBuilder.setTitle(getString(R.string.error))
        errorBuilder.setMessage(errorMessage)
        errorBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val errorDialog = errorBuilder.create()
        errorDialog.show()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
}
