package bangkit.project.fed.ui.setting.configureAccount

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ConfigureAccountViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val userEmail = MutableLiveData<String>()

    init {
        loadUserData()
    }

    fun changeEmail(currentEmail: String, newEmail: String, oldPassword: String): MutableLiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val credential = EmailAuthProvider.getCredential(currentEmail, oldPassword)

            currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    currentUser.updateEmail(newEmail).addOnCompleteListener { updateEmailTask ->
                        if (updateEmailTask.isSuccessful) {
                            userEmail.value = newEmail
                            updateEmailInFirestore(newEmail)
                            result.value = Pair(true, "bsia bisa")
                        } else {
                            result.value = Pair(false, updateEmailTask.exception?.message ?: "Gagal mengubah email")
                        }
                    }
                } else {
                    result.value = Pair(false, reauthTask.exception?.message ?: "Gagal me-reauthenticate")
                }
            }
        } else {
            result.value = Pair(false, "Pengguna tidak ditemukan")
        }

        return result
    }

    private fun updateEmailInFirestore(newEmail: String) {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .update("email", newEmail)
                .addOnSuccessListener {
                    // Berhasil memperbarui email di Firestore
                }
                .addOnFailureListener {
                    // Gagal memperbarui email di Firestore
                }
        }
    }

    private fun loadUserData() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userEmail.value = auth.currentUser?.email
                    }
                }
        }
    }
}