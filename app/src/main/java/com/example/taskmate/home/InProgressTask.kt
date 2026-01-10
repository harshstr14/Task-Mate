package com.example.taskmate.home

data class InProgressTask(
    val id: String,
    val group: String,
    val task: String,
    val bgColor: Long,
    val progressColor: Long,
    val icon: Int,
    val iconBg: Long,
    val progress: Float
)

