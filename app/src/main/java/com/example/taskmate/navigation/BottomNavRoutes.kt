package com.example.taskmate.navigation

sealed class BottomNavRoute(val route: String) {
    object Home : BottomNavRoute("home")
    object Calendar : BottomNavRoute("calendar")
    object Notification : BottomNavRoute("notification")
    object Search : BottomNavRoute("search")
    object AddTask : BottomNavRoute("add_task")
}
