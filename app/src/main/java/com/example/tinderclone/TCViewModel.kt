package com.example.tinderclone

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tinderclone.data.COLLECTION_CHAT
import com.example.tinderclone.data.COLLECTION_USER
import com.example.tinderclone.data.ChatData
import com.example.tinderclone.data.ChatUser
import com.example.tinderclone.data.Event
import com.example.tinderclone.data.UserData
import com.example.tinderclone.ui.Gender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject


sealed class DestinationScreen(val route: String) {
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object Swipe : DestinationScreen("swipe")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}") {
        fun createRoute(id: String) = "singleChat/$id"
    }
}

@HiltViewModel
class TCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val inProgress = mutableStateOf(value = false)
    var popupNotification = mutableStateOf<Event<String>?>(null)
    val signedIn = mutableStateOf(value = false)
    val userData = mutableStateOf<UserData?>(value = null)

    val matchProfiles = mutableStateOf<List<UserData>>(listOf())
    val inProgressProfiles = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTION_USER).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Cannot retrieve user data")
            }
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                populateCards()
            }
        }
    }

    fun updateProfileData(
        name: String,
        userName: String,
        bio: String,
        gender: Gender,
        genderPreference: Gender
    ) {
        createOrUpdateProfile(
            name = name,
            userName = userName,
            bio = bio,
            gender = gender,
            genderPreference = genderPreference
        )
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
        }.addOnFailureListener {
            handleException(it)
            inProgress.value = false
        }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")

    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("TinderClone", "Tinder exception", exception)
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMessage else "$customMessage: $errorMessage"
        popupNotification.value = Event(message)
        inProgress.value = false
    }

    fun onSignup(userName: String, email: String, password: String) {
        if (userName.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection(COLLECTION_USER).whereEqualTo("username", userName).get()
            .addOnSuccessListener {
                if (it.isEmpty)
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Create user in DB
                                signedIn.value = true
                                createOrUpdateProfile(userName = userName)
                            } else
                                handleException(task.exception, "Signup failed")
                        }
                else
                    handleException(customMessage = "User name already exists!")
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun onLogin(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill all fields")
            return
        }

        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                signedIn.value = true
                inProgress.value = false
                auth.currentUser?.uid?.let {
                    getUserData(it)
                }
            } else {
                handleException(task.exception, "Login failed")
            }
        }.addOnFailureListener {
            handleException(it, "Login failed")
        }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        userName: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
        gender: Gender? = null,
        genderPreference: Gender? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            userName = userName ?: userData.value?.userName,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            bio = bio ?: userData.value?.bio,
            gender = gender?.toString() ?: userData.value?.gender,
            genderPreference = genderPreference?.toString() ?: userData.value?.genderPreference,
        )

        uid?.let { uid ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(uid)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                                populateCards()
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                            }
                    } else {
                        db.collection(COLLECTION_USER).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid)
                    }
                }.addOnFailureListener {
                    handleException(it, "Cannot update user")
                }
        }
    }

    private fun populateCards() {
        inProgressProfiles.value = true

        val g =
            if (userData.value?.gender.isNullOrEmpty()) "ANY" else userData.value!!.gender!!.uppercase()

        val gPref =
            if (userData.value?.genderPreference.isNullOrEmpty()) "ANY" else userData.value!!.genderPreference!!.uppercase()

        val cardsQuery = when (Gender.valueOf(gPref)) {
            Gender.MALE -> db.collection(COLLECTION_USER).whereEqualTo("gender", Gender.MALE)
            Gender.FEMALE -> db.collection(COLLECTION_USER).whereEqualTo("gender", Gender.FEMALE)
            Gender.ANY -> db.collection(COLLECTION_USER)
        }
        val userGender = Gender.valueOf(g)

        cardsQuery.where(
            Filter.and(
                Filter.notEqualTo("userId", userData.value?.userId),
                Filter.or(
                    Filter.equalTo("genderPreference", userGender),
                    Filter.equalTo("genderPreference", Gender.ANY)
                )
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                inProgressProfiles.value = false
                handleException(error)
            }
            if (value != null) {
                val potentials = mutableListOf<UserData>()
                value.documents.forEach {
                    it.toObject<UserData>()?.let { potential ->
                        var showUser = true
                        if (userData.value?.swipesLeft?.contains(potential.userId) == true ||
                            userData.value?.swiperRight?.contains(potential.userId) == true ||
                            userData.value?.matches?.contains(potential.userId) == true
                        )
                            showUser = false
                        if (showUser)
                            potentials.add(potential)
                    }
                }
                matchProfiles.value = potentials
                inProgressProfiles.value = false
            }
        }
    }

    fun onDislike(selectedUser: UserData) {
        db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
            .update("swipresLeft", FieldValue.arrayUnion(selectedUser.userId))
    }

    fun onLike(selectedUser: UserData) {
        val reciprocalMatch = selectedUser.swiperRight.contains(userData.value?.userId)
        if (!reciprocalMatch) {
            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("swipesRight", FieldValue.arrayUnion(selectedUser.userId))
        } else {
            popupNotification.value = Event("Match!")

            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("swipesRight", FieldValue.arrayRemove(userData.value?.userId))

            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("matches", FieldValue.arrayUnion(userData.value?.userId))

            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("matches", FieldValue.arrayUnion(selectedUser.userId))

            val chatKey = db.collection(COLLECTION_CHAT).document().id
            val chatData = ChatData(
                chatKey,
                ChatUser(
                    userData.value?.userId,
                    if (userData.value?.name.isNullOrEmpty()) userData.value?.userName else userData.value?.name,
                    userData.value?.imageUrl
                ),
                ChatUser(
                    selectedUser.userId,
                    if (selectedUser.name.isNullOrEmpty()) selectedUser.userName else selectedUser.name,
                    selectedUser.imageUrl
                )
            )
            db.collection(COLLECTION_CHAT).document(chatKey).set(chatData)
        }
    }
}