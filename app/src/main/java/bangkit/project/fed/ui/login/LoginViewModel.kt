package bangkit.project.fed.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _regristationResult = MutableLiveData<Boolean>()
    val registrationResult : LiveData<Boolean> get() = _regristationResult

    private val _userName = MutableLiveData<String>()
    val userName : LiveData<String> get() = _userName

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        checkCurrentUserAndFetchUsername()
    }

    private fun checkCurrentUserAndFetchUsername() {
        if (auth.currentUser != null) {
            val uid = auth.currentUser!!.uid
            fetchUsernamefromFirestore(uid)
        } else {
            return
        }
    }

    private fun fetchUsernamefromFirestore(uid: String) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {documentSnapshot ->
                val userName = documentSnapshot.getString("displayName")
                if(userName != null) {
                    _userName.value = userName!!
                } else {
                }
            }
    }

    fun loginUser(email: String, password:String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->
                _loginResult.value = task.isSuccessful
            }
    }

    fun registerUser(name: String, email: String, password:String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val user = hashMapOf(
                        "displayName" to name,
                        "email" to email
                    )
                    firestore.collection("users")
                        .document(auth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener {
                            _regristationResult.value = true
                        }
                } else {
                    _regristationResult.value = false
                }
            }
    }

}