package com.example.taskmate.home

data class Tasks(
    val id: String,
    val time: Long,
    val taskGroup: String,
    val category: String,
    val taskGroupName: String,
    val taskName: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val icon: Int,
    val iconBg: Long,
    val progress: Float,
    val progressStatus: String
)
