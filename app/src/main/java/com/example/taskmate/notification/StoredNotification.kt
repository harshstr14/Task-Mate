package com.example.taskmate.notification

data class StoredNotification(
    val id: String,
    val taskId: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val icon: Int,
    val iconBg: Long
)