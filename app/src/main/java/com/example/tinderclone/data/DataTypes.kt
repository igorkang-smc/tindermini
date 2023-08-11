package com.example.tinderclone.data

data class UserData(
    var userId: String? = "",
    var name: String? = "",
    var userName: String? = "",
    var imageUrl: String? = "",
    var bio: String? = "",
    var gender: String? = "",
    var genderPreference: String? = "",
    var swipesLeft: List<String> = listOf(),
    var swiperRight: List<String> = listOf(),
    var matches: List<String> = listOf()
) {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "userName" to userName,
        "imageUrl" to imageUrl,
        "bio" to bio,
        "gender" to gender,
        "genderPreference" to genderPreference,
        "swipesLeft" to swipesLeft,
        "swipesRight" to swiperRight,
        "matches" to matches
    )
}

data class ChatData(
    val chatId: String? = "",
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()
)
data class ChatUser(
    val userId: String? = "",
    val name: String? = "",
    val imageUrl: String? = ""
)