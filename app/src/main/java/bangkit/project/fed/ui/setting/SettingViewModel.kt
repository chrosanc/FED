package bangkit.project.fed.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import bangkit.project.fed.data.datastore.PreferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(private val pref : PreferencesDataStore) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val userName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()

    init {
        loadUserData()
    }

    fun getThemeSetting(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    fun getLocale(): LiveData<String> {
        return pref.getLocaleSetting().asLiveData(Dispatchers.IO)
    }

    fun saveLocale(localeName: String) {
        viewModelScope.launch {
            pref.saveLocaleSetting(localeName)
        }
    }

    fun loadUserData() {
        auth.currentUser?.uid.let { userId ->
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener {documentSnapshot ->
                        if(documentSnapshot.exists()) {
                            userName.value = documentSnapshot.getString("displayName")
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