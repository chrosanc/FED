package bangkit.project.fed.ui.setting.configureprofile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ConfigureProfileViewModel(): ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val userName = MutableLiveData<String>()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        auth.currentUser?.uid.let { userId ->
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener {documentSnapshot ->
                        if(documentSnapshot.exists()) {
                            userName.value = documentSnapshot.getString("displayName")
                        }
                    }
            }
        }
    }

    fun updateUserName(newUserName: String) {
        userName.value = newUserName
        auth.currentUser?.uid.let { userId ->
            if (userId != null) {
                val userRef = firestore.collection("users").document(userId)
                userRef
                    .update("displayName", newUserName)
                    .addOnSuccessListener {
                        Log.i("Berhasil", userId)
                    }
                    .addOnFailureListener { _ ->
                        Log.i("Gagal", userId)
                    }
            }
        }
    }


}