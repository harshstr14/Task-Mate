package com.example.taskmate.tasksscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.taskmate.R
import com.example.taskmate.home.TaskGroup
import com.example.taskmate.home.TaskPrefs
import com.example.taskmate.home.Tasks
import com.example.taskmate.home.fonts
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TasksScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    taskGroup: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(text1,text2,text3,text4,taskListsColumn,icon) = createRefs()

        var tasksList by remember { mutableStateOf(mutableListOf<Tasks>()) }
        var pendingDelete by remember { mutableStateOf<Tasks?>(null) }
        val swipeStates = remember { mutableMapOf<String, SwipeToDismissBoxState>() }
        var showClearAllDialog by remember { mutableStateOf(false) }

        LaunchedEffect(taskGroup) {
            tasksList = when(taskGroup) {
                TaskGroup.WORK -> TaskPrefs.loadWorkTasks(context).toMutableList()
                TaskGroup.PERSONAL -> TaskPrefs.loadPersonalTasks(context).toMutableList()
                TaskGroup.STUDY -> TaskPrefs.loadStudyTasks(context).toMutableList()
                TaskGroup.DAILY_STUDY -> TaskPrefs.loadDailyStudyTasks(context).toMutableList()
                else -> mutableListOf()
            }
        }

        if (tasksList.isEmpty()) {
            Icon(painter = painterResource(R.drawable.empty_task), contentDescription = "empty_notification",
                tint = Color(0xFF5F33E1), modifier = Modifier.constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = (-3).dp)
                    end.linkTo(parent.end)
                }.size(92.dp)
            )

            Text("Empty Tasks ", modifier = Modifier.constrainAs(text4) {
                top.linkTo(icon.bottom, margin = (-8).dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = Color(0xFF6E6A7C)
            )
        }

        Text("Tasks", modifier = Modifier.constrainAs(text1) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Text("$taskGroup tasks", modifier = Modifier.constrainAs(text2) {
            top.linkTo(text1.bottom, margin = 15.dp)
            start.linkTo(parent.start, margin = 20.dp)
        }, fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(text3) {
            top.linkTo(text1.bottom, margin = 15.dp)
            end.linkTo(parent.end, margin = 20.dp)
        }.size(72.dp,20.dp).clip(RoundedCornerShape(6.dp))
            .clickable {
                if (tasksList.isEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "No tasks to clear",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    showClearAllDialog = true
                }
            }, contentAlignment = Alignment.Center) {
            Text("Clear All", fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = Color(0xFF5F33E1)
            )
        }

        LazyColumn(modifier = Modifier.constrainAs(taskListsColumn) {
            top.linkTo(text2.bottom, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom, margin = (-15).dp)
            height = Dimension.fillToConstraints
        },contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp))
        {
            items(items = tasksList, key = { it.id }) { task ->
                val dismissState = swipeStates.getOrPut(task.id) {
                    rememberSwipeToDismissBoxState(SwipeToDismissBoxValue.Settled)
                }

                var handled by remember(task.id) { mutableStateOf(false) }

                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart && !handled) {
                        handled = true
                        pendingDelete = task
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                                .background(
                                    color = Color(0xFFFF4F4F),
                                    shape = RoundedCornerShape(15.dp)
                                ),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.remove_icon),
                                contentDescription = "Delete",
                                tint = Color(0xFFEEE9FF),
                                modifier = Modifier.size(46.dp).padding(end = 24.dp)
                            )
                        }
                    },
                    content = {
                        ElevatedCard(elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ), colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFFFF)
                        ), modifier = Modifier.padding(horizontal = 20.dp).height(94.dp).fillMaxWidth().shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(15.dp),
                            ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                            spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                        ), onClick = { navController.navigate("update_task/${task.id}/${task.taskGroup}") }
                            ,shape = RoundedCornerShape(15.dp)) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val(text1,text2,clock,timeText,boxShape,boxShape2) = createRefs()

                                Text(task.taskGroupName, modifier = Modifier.constrainAs(text1) {
                                    top.linkTo(parent.top, margin = 5.dp)
                                    bottom.linkTo(text2.top)
                                    start.linkTo(parent.start)
                                }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1, overflow = TextOverflow.Ellipsis
                                )

                                Text(task.taskName, modifier = Modifier.constrainAs(text2) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C), maxLines = 1 , overflow = TextOverflow.Ellipsis
                                )

                                Image(modifier = Modifier.constrainAs(clock) {
                                    bottom.linkTo(parent.bottom, margin = 14.dp)
                                    start.linkTo(parent.start, margin = 15.dp)
                                }.size(14.dp), painter = painterResource(R.drawable.clock), contentDescription = "clock Icon")

                                Text(formatTime(task.time), modifier = Modifier.constrainAs(timeText) {
                                    top.linkTo(clock.top)
                                    start.linkTo(clock.end, margin = 2.dp)
                                    bottom.linkTo(clock.bottom)
                                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFFAB94FF), maxLines = 1
                                )

                                Box(modifier = Modifier.constrainAs(boxShape) {
                                    top.linkTo(parent.top, margin = 15.dp)
                                    end.linkTo(parent.end, margin = 15.dp)
                                }.size(34.dp).background(Color(task.iconBg.toULong()),
                                    shape = RoundedCornerShape(7.dp)),
                                    contentAlignment = Alignment.Center
                                )  {
                                    Image(modifier = Modifier.size(20.dp), painter = painterResource(task.icon), contentDescription = "briefcase")
                                }

                                ElevatedCard(elevation = CardDefaults.cardElevation(
                                    defaultElevation = 0.dp
                                ), colors = CardDefaults.cardColors(
                                    containerColor = when(task.progressStatus) {
                                        "Done" -> Color(0xFFEDE8FF)
                                        "In Progress" -> Color(0xFFFFE9E1)
                                        "To Do" -> Color(0xFFE3F2FF)
                                        else -> Color(0xFFEDE8FF)
                                    }
                                ), modifier = Modifier.constrainAs(boxShape2) {
                                    bottom.linkTo(parent.bottom, margin = 15.dp)
                                    end.linkTo(parent.end, margin = 15.dp)
                                }.shadow( elevation = 12.dp,
                                    shape = RoundedCornerShape(7.dp),
                                    ambientColor = when(task.progressStatus) {
                                        "Done" -> Color(0xFFEDE8FF)
                                        "In Progress" -> Color(0xFFFFE9E1)
                                        "To Do" -> Color(0xFFE3F2FF)
                                        else -> Color(0xFFEDE8FF)
                                    }.copy(alpha = 0.2f),
                                    spotColor = when(task.progressStatus) {
                                        "Done" -> Color(0xFFEDE8FF)
                                        "In Progress" -> Color(0xFFFFE9E1)
                                        "To Do" -> Color(0xFFE3F2FF)
                                        else -> Color(0xFFEDE8FF)
                                    }.copy(alpha = 0.4f)
                                ),shape = RoundedCornerShape(7.dp)) {
                                    Box(modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = task.progressStatus,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp,
                                            color = when(task.progressStatus) {
                                                "Done" -> Color(0xFF5F33E1)
                                                "In Progress" -> Color(0xFFFF7D53)
                                                "To Do" -> Color(0xFF0087FF)
                                                else -> Color(0xFF5F33E1)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                shape = RoundedCornerShape(12.dp),
                containerColor = Color(0xFFEEE9FF),
                tonalElevation = 6.dp,
                title = {
                    Text(
                        text = "Clear all tasks",
                        fontSize = 16.sp, lineHeight = 19.sp,
                        fontFamily = fonts, fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = Color(0xFF5F33E1)
                    )
                },
                text = {
                    Text(
                        text = "This will permanently delete all tasks in this group.",
                        fontSize = 14.sp, lineHeight = 17.sp,
                        fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                        color = Color(0xFF24252C)
                    )
                },
                confirmButton = {
                    Text(
                        text = "Clear",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                showClearAllDialog = false
                                scope.launch {
                                    when (taskGroup) {
                                        TaskGroup.WORK -> TaskPrefs.clearWorkTasks(context)
                                        TaskGroup.PERSONAL -> TaskPrefs.clearPersonalTasks(context)
                                        TaskGroup.STUDY -> TaskPrefs.clearStudyTasks(context)
                                        TaskGroup.DAILY_STUDY -> TaskPrefs.clearDailyStudyTasks(context)
                                    }
                                    tasksList = mutableListOf()

                                    snackbarHostState.showSnackbar(
                                        message = "All tasks cleared",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                        fontSize = 14.sp, lineHeight = 17.sp,
                        fontFamily = fonts, fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = Color(0xFF6E6A7C)
                    )
                },
                dismissButton = {
                    Text(
                        text = "Cancel",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                showClearAllDialog = false
                            },
                        fontSize = 14.sp, lineHeight = 17.sp,
                        fontFamily = fonts, fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = Color(0xFF6E6A7C)
                    )
                }
            )
        }

        LaunchedEffect(pendingDelete) {
            pendingDelete?.let { task ->

                tasksList = tasksList.filter { it.id != task.id }.toMutableList()

                when (task.taskGroup) {
                    TaskGroup.WORK ->
                        TaskPrefs.removeWorkTask(context, task.id)
                    TaskGroup.PERSONAL ->
                        TaskPrefs.removePersonalTasks(context, task.id)
                    TaskGroup.STUDY ->
                        TaskPrefs.removeStudyTasks(context, task.id)
                    TaskGroup.DAILY_STUDY ->
                        TaskPrefs.removeDailyStudyTasks(context, task.id)
                }

                snackbarHostState.showSnackbar(
                    message = "Task deleted",
                    duration = SnackbarDuration.Short
                )

                swipeStates[task.id]?.reset()

                pendingDelete = null
            }
        }
    }
}

private fun formatTime(time: Long): String {
    return Instant.ofEpochMilli(time)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("hh:mm a"))
}

@Preview(showSystemUi = true)
@Composable
private fun ShowTasksScreen() {
    val navController = rememberNavController()
    val snackbarHostState = SnackbarHostState()
    TasksScreen(navController, snackbarHostState, TaskGroup.WORK)
}