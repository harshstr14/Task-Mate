package com.example.taskmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskmate.addtask.AddTaskScreen
import com.example.taskmate.calendar.CalendarScreen
import com.example.taskmate.home.HomeScreen
import com.example.taskmate.navigation.BottomNavRoute
import com.example.taskmate.notification.NotificationScreen
import com.example.taskmate.search.SearchScreen
import com.example.taskmate.ui.theme.TaskMateTheme
import com.example.taskmate.updatetask.UpdateTaskScreen

class MainScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskMateTheme {
                Main_Screen()
            }
        }
    }
}

@Composable
fun Main_Screen() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavRoute.Home.route) {
                HomeScreen(navController)  // ⬅ current Home UI
            }
            composable(BottomNavRoute.Calendar.route) {
                CalendarScreen(navController)
            }
            composable(BottomNavRoute.Notification.route) {
                NotificationScreen()
            }
            composable(BottomNavRoute.Search.route) {
                SearchScreen()
            }
            composable(BottomNavRoute.AddTask.route) {
                AddTaskScreen()
            }
            composable(BottomNavRoute.UpdateTask.route) {
                UpdateTaskScreen()
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    ConstraintLayout(
        modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 8.dp).navigationBarsPadding().fillMaxWidth().height(65.dp)
        .background(Color(0xFFEEE9FF), shape = RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate(BottomNavRoute.Home.route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }}) {
                Icon(painterResource(R.drawable.home_icon), contentDescription = "homeIcon",
                    tint = if (currentRoute == BottomNavRoute.Home.route)
                        Color.Unspecified
                    else Color(0x9F5F33E1),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { navController.navigate(BottomNavRoute.Calendar.route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }}) {
                Icon(painterResource(R.drawable.calendar_icon), contentDescription = "calendarIcon",
                    tint = if (currentRoute == BottomNavRoute.Calendar.route)
                        Color.Unspecified
                    else Color(0x9F5F33E1),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { navController.navigate(BottomNavRoute.Notification.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
            }}) {
                Icon(painterResource(R.drawable.notification_icon), contentDescription = "notificationIcon",
                    tint = if (currentRoute == BottomNavRoute.Notification.route)
                        Color.Unspecified
                    else Color(0x9F5F33E1),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { navController.navigate(BottomNavRoute.Search.route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }}) {
                Icon(painterResource(R.drawable.search_icon), contentDescription = "searchIcon",
                    tint = if (currentRoute == BottomNavRoute.Search.route)
                        Color.Unspecified
                    else Color(0x9F5F33E1),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomeScreenView() {
    TaskMateTheme {
        Main_Screen()
    }
}