package com.example.taskmate.profile

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val birthDate: String = "",
    val bio: String = "",
    val profileImageUri: String = ""
)
