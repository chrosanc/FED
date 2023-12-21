package bangkit.project.fed.data.api

import bangkit.project.fed.data.EggData
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val eggDetectedCollection = firestore.collection("Egg-detected")

    fun getDataEggByUserId(userId:String, onComplete: (List<EggData>) -> Unit){
        eggDetectedCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val eggDataList = mutableListOf<EggData>()
                for (document in documents) {
                    val eggData = document.toObject(EggData::class.java)
                    eggDataList.add(eggData)
                }
                onComplete(eggDataList)
            }.addOnFailureListener {exception ->
                onComplete(emptyList())
            }
    }

}
