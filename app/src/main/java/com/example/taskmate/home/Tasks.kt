package com.example.taskmate.home

data class Tasks(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val endAt: Long,
    val taskGroup: String,
    val taskGroupName: String,
    val taskName: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val completedDates: List<String>,
    val icon: Int,
    val iconBg: Long,
    val progress: Int,
    val progressStatus: String
)
