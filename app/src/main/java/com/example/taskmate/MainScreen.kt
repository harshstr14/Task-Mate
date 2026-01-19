package com.example.taskmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.taskmate.addtask.AddTaskScreen
import com.example.taskmate.calendar.CalendarScreen
import com.example.taskmate.home.HomeScreen
import com.example.taskmate.home.fonts
import com.example.taskmate.navigation.BottomNavRoute
import com.example.taskmate.notification.NotificationHelper
import com.example.taskmate.notification.NotificationScreen
import com.example.taskmate.profile.ProfileScreen
import com.example.taskmate.search.SearchScreen
import com.example.taskmate.tasksscreen.TasksScreen
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

        NotificationHelper.createChannel(this)
    }
}

@Composable
fun Main_Screen() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isError = data.visuals.message.contains("Please") ||
                        data.visuals.message.contains("No")

                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 15.dp).shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color(0xFFEDE8FF).copy(alpha = 0.2f),
                            spotColor = Color(0xFFEDE8FF).copy(alpha = 0.4f)
                        ),
                    containerColor = Color(0xFFEDE8FF),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes (
                                if (isError) R.raw.wrong else R.raw.done
                            )
                        )

                        LottieAnimation(
                            composition = composition,
                            iterations = 1,
                            modifier = Modifier.offset(x = if(isError) (-25).dp else (-15).dp).size(if (isError) 88.dp else 62.dp)
                        )

                        Text(
                            modifier = Modifier.offset(x = if(isError) (-42).dp else (-22).dp).weight(1f),
                            text = data.visuals.message,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 13.sp,
                            color = Color(0xFF24252C)
                        )

                        data.visuals.actionLabel?.let { actionLabel ->
                            Text(
                                text = actionLabel,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable { data.performAction() },
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 13.sp,
                                color = Color(0xFF5F33E1)
                            )
                        }
                    }
                }
            }
        },
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
            composable(BottomNavRoute.Profile.route) {
                ProfileScreen(snackbarHostState = snackbarHostState)
            }
            composable(BottomNavRoute.Calendar.route) {
                CalendarScreen(navController)
            }
            composable(BottomNavRoute.Notification.route) {
                NotificationScreen()
            }
            composable(BottomNavRoute.Search.route) {
                SearchScreen(navController, snackbarHostState = snackbarHostState)
            }
            composable(BottomNavRoute.AddTask.route) {
                AddTaskScreen(snackbarHostState)
            }
            composable(
                route = BottomNavRoute.UpdateTask.route,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.StringType
                    },
                    navArgument("taskGroup") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val taskId = backStackEntry.arguments?.getString("taskId")
                val taskGroup = backStackEntry.arguments?.getString("taskGroup")

                UpdateTaskScreen(
                    snackbarHostState = snackbarHostState,
                    taskId = taskId,
                    taskGroup = taskGroup
                )
            }
            composable(
                route = BottomNavRoute.Tasks.route,
                arguments = listOf(
                    navArgument("taskGroup") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val taskGroup = backStackEntry.arguments?.getString("taskGroup")

                TasksScreen(
                    navController,
                    snackbarHostState = snackbarHostState,
                    taskGroup = taskGroup
                )
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