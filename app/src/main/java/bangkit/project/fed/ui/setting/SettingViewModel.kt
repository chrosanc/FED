package bangkit.project.fed.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val userName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()

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
                            userName.value = documentSnapshot.getString("name")
                            userEmail.value = auth.currentUser?.email
                        }

                    }
            }

        }
    }

    fun logout() {
        auth.signOut()
    }


}