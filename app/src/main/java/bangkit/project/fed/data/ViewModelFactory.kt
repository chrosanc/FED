package bangkit.project.fed.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.data.datastore.PreferencesDataStore
import bangkit.project.fed.ui.setting.SettingViewModel

class ViewModelFactory(private val pref:PreferencesDataStore) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)){
            return SettingViewModel(pref) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class: " + modelClass.name)
    }

}