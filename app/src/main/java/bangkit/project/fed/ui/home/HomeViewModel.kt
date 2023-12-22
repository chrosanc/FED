package bangkit.project.fed.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bangkit.project.fed.data.EggData
import bangkit.project.fed.data.api.FirestoreHelper

class HomeViewModel : ViewModel() {

    private val _eggDataList = MutableLiveData<List<EggData>>()
    val eggDataList : LiveData<List<EggData>> get() = _eggDataList

    private val firestoreHelper = FirestoreHelper()

    fun fetchEggDataByUserId(userId:String) {
        firestoreHelper.getDataEggByUserId(userId){eggDataList ->
            _eggDataList.value = eggDataList
        }
    }


}