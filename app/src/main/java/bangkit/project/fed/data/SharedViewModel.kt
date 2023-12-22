package bangkit.project.fed.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Di SharedViewModel.kt
class SharedViewModel : ViewModel() {
    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }
}
