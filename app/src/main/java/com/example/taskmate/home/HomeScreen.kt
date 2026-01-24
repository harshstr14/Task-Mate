package com.example.taskmate.home

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.taskmate.R
import com.example.taskmate.navigation.BottomNavRoute
import com.example.taskmate.notification.notifyOverdueTasks
import com.example.taskmate.profile.UserPrefs
import com.example.taskmate.profile.UserProfile
import com.example.taskmate.updatetask.formatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val fonts = FontFamily(
    Font(R.font.merriweathersans_bold, FontWeight.Bold),
    Font(R.font.merriweathersans_semibold, FontWeight.SemiBold),
    Font(R.font.merriweathersans_regular, FontWeight.Normal)
)

val taskGroupsIcons = listOf(
    R.drawable.briefcase,
    R.drawable.personal,
    R.drawable.daily_study,
    R.drawable.study
)
val taskGroupsIconsColors = listOf(
    Color(0xFFFFE4F2),
    Color(0xFFEDE4FF),
    Color(0xFFFFE6D4),
    Color(0xFFFFF6D4)
)

object TaskGroup {
    const val WORK = "Work"
    const val PERSONAL = "Personal"
    const val STUDY = "Study"
    const val DAILY_STUDY = "Daily Study"
}

val Context.taskDataStore by preferencesDataStore(
    name = "task_datastore"
)

object TaskKeys {
    val WORK = stringPreferencesKey("work_tasks")
    val PERSONAL = stringPreferencesKey("personal_tasks")
    val STUDY = stringPreferencesKey("study_tasks")
    val DAILY_STUDY = stringPreferencesKey("daily_study_tasks")
}

object TaskPrefs {

    private val gson = Gson()
    private val type = object : TypeToken<MutableList<Tasks>>() {}.type

    private fun parse(json: String?): MutableList<Tasks> =
        if (json.isNullOrEmpty()) mutableListOf()
        else gson.fromJson(json, type)

    private fun upsert(list: MutableList<Tasks>, task: Tasks) {
        val index = list.indexOfFirst { it.id == task.id }
        if (index != -1) list[index] = task else list.add(task)
    }

    suspend fun saveWorkTask(context: Context, task: Tasks) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.WORK])
            upsert(list, task)
            prefs[TaskKeys.WORK] = gson.toJson(list)
        }
    }

    suspend fun removeWorkTask(context: Context, taskId: String) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.WORK])
            list.removeAll { it.id == taskId }
            prefs[TaskKeys.WORK] = gson.toJson(list)
        }
    }

    fun loadWorkTasks(context: Context) =
        context.taskDataStore.data.map { prefs ->
            parse(prefs[TaskKeys.WORK])
        }

    suspend fun clearWorkTasks(context: Context) {
        context.taskDataStore.edit { it.remove(TaskKeys.WORK) }
    }

    suspend fun savePersonalTask(context: Context, task: Tasks) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.PERSONAL])
            upsert(list, task)
            prefs[TaskKeys.PERSONAL] = gson.toJson(list)
        }
    }

    suspend fun removePersonalTask(context: Context, taskId: String) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.PERSONAL])
            list.removeAll { it.id == taskId }
            prefs[TaskKeys.PERSONAL] = gson.toJson(list)
        }
    }

    fun loadPersonalTasks(context: Context) =
        context.taskDataStore.data.map {
            parse(it[TaskKeys.PERSONAL])
        }

    suspend fun clearPersonalTasks(context: Context) {
        context.taskDataStore.edit { it.remove(TaskKeys.PERSONAL) }
    }

    suspend fun saveStudyTask(context: Context, task: Tasks) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.STUDY])
            upsert(list, task)
            prefs[TaskKeys.STUDY] = gson.toJson(list)
        }
    }

    suspend fun removeStudyTask(context: Context, taskId: String) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.STUDY])
            list.removeAll { it.id == taskId }
            prefs[TaskKeys.STUDY] = gson.toJson(list)
        }
    }

    fun loadStudyTasks(context: Context) =
        context.taskDataStore.data.map {
            parse(it[TaskKeys.STUDY])
        }

    suspend fun clearStudyTasks(context: Context) {
        context.taskDataStore.edit { it.remove(TaskKeys.STUDY) }
    }

    suspend fun saveDailyStudyTask(context: Context, task: Tasks) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.DAILY_STUDY])
            upsert(list, task)
            prefs[TaskKeys.DAILY_STUDY] = gson.toJson(list)
        }
    }

    suspend fun removeDailyStudyTask(context: Context, taskId: String) {
        context.taskDataStore.edit { prefs ->
            val list = parse(prefs[TaskKeys.DAILY_STUDY])
            list.removeAll { it.id == taskId }
            prefs[TaskKeys.DAILY_STUDY] = gson.toJson(list)
        }
    }

    fun loadDailyStudyTasks(context: Context) =
        context.taskDataStore.data.map {
            parse(it[TaskKeys.DAILY_STUDY])
        }

    suspend fun clearDailyStudyTasks(context: Context) {
        context.taskDataStore.edit { it.remove(TaskKeys.DAILY_STUDY) }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var inProgressTasks by remember { mutableStateOf("0") }
    var savedUser by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(Unit) {
        savedUser = UserPrefs.getUser(context)
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (profileAvatar,greetingText,userNameText,todayProgressCard,inProgressTitle,
            inProgressCountBadge,inProgressList,taskGroupList,taskGroupTitle,taskGroupCountBadge,
            emptyInProgressCard) = createRefs()

        ProfileView(modifier = Modifier.constrainAs(profileAvatar) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start, margin = 22.dp)
        }.size(52.dp).clip(CircleShape),
            modifier2 = Modifier.constrainAs(greetingText) {
                start.linkTo(profileAvatar.end, margin = 15.dp)
                top.linkTo(profileAvatar.top, margin = 4.dp)
            }, modifier3 = Modifier.constrainAs(userNameText) {
                start.linkTo(profileAvatar.end, margin = 15.dp)
                top.linkTo(greetingText.bottom)
            }, navController, savedUser
        )

        TodayTaskProgress(
            modifier = Modifier.constrainAs(todayProgressCard) {
                top.linkTo(profileAvatar.bottom, margin = 22.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.padding(horizontal = 25.dp).fillMaxWidth().height(156.dp), navController
        )

        Text("In Progress", modifier = Modifier.constrainAs(inProgressTitle) {
            top.linkTo(todayProgressCard.bottom, margin = 22.dp)
            start.linkTo(parent.start, margin = 25.dp)
        }, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            fontSize = 19.sp, color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(inProgressCountBadge) {
            top.linkTo(inProgressTitle.top)
            start.linkTo(inProgressTitle.end, margin = 5.dp)
            bottom.linkTo(inProgressTitle.bottom)
        }.shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(19.dp),
            ambientColor = Color(0xFFEEE9FF).copy(alpha = 0.2f),
            spotColor = Color(0xFFEEE9FF).copy(alpha = 0.4f)
        ), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(18.dp)) {
                drawCircle(
                    color = Color(0xFFEEE9FF),
                    radius = size.minDimension / 2)
            }

            Text(inProgressTasks, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                fontSize = 11.sp, color = Color(0xFF5F33E1)
            )
        }

        InProgressTasks(modifier = Modifier.constrainAs(inProgressList) {
            top.linkTo(inProgressTitle.bottom, margin = 15.dp)
            start.linkTo(parent.start)
        }, modifier1 = Modifier.constrainAs(emptyInProgressCard) {
            top.linkTo(inProgressTitle.bottom, margin = 15.dp)
            start.linkTo(parent.start, margin = 25.dp)
        }, navController) { count ->
            inProgressTasks = count
        }
        
        Text("Task Groups", modifier = Modifier.constrainAs(taskGroupTitle) {
            top.linkTo(inProgressTitle.bottom, margin = 145.dp)
            start.linkTo(parent.start, margin = 25.dp)
        }, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            fontSize = 19.sp, color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(taskGroupCountBadge) {
            top.linkTo(taskGroupTitle.top)
            start.linkTo(taskGroupTitle.end, margin = 5.dp)
            bottom.linkTo(taskGroupTitle.bottom)
        }.shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(19.dp),
            ambientColor = Color(0xFFEEE9FF).copy(alpha = 0.2f),
            spotColor = Color(0xFFEEE9FF).copy(alpha = 0.4f)
        ), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(18.dp)) {
                drawCircle(
                    color = Color(0xFFEEE9FF),
                    radius = size.minDimension / 2)
            }

            Text("4", fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                fontSize = 11.sp, color = Color(0xFF5F33E1))
        }
        
        TaskGroups(modifier = Modifier.constrainAs(taskGroupList) {
            top.linkTo(taskGroupTitle.bottom, margin = 12.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom, margin = (-15).dp)
            height = Dimension.fillToConstraints
        }, navController)
    }
}

@Composable
private fun ProfileView(modifier: Modifier,modifier2: Modifier,modifier3: Modifier,
    navController: NavController,savedUser: UserProfile?
) {
    AsyncImage(
        model = savedUser?.profileImageUri?.ifEmpty { R.drawable.default_profile },
        contentDescription = "Profile Image",
        contentScale = ContentScale.Crop,
        modifier = modifier.clickable { navController.navigate(BottomNavRoute.Profile.route) },
        placeholder = painterResource(R.drawable.default_profile),
        error = painterResource(R.drawable.default_profile)
    )

    Text("Hello!", modifier = modifier2, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
        fontSize = 16.sp, lineHeight = 19.sp, color = Color(0xFF24252C)
    )

    Text(text = savedUser?.name ?: "Your Name", modifier = modifier3, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
        fontSize = 18.sp, lineHeight = 21.sp, color = Color(0xFF24252C)
    )
}

@Composable
private fun TodayTaskProgress(modifier: Modifier, navController: NavController) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val today = LocalDate.now()

    val work by TaskPrefs.loadWorkTasks(context).collectAsState(emptyList())
    val personal by TaskPrefs.loadPersonalTasks(context).collectAsState(emptyList())
    val study by TaskPrefs.loadStudyTasks(context).collectAsState(emptyList())
    val daily by TaskPrefs.loadDailyStudyTasks(context).collectAsState(emptyList())

    val allTasks = remember(work, personal, study, daily) {
        work + personal + study + daily
    }

    LaunchedEffect(Unit){
        notifyOverdueTasks(context, allTasks)
    }

    // Filter today tasks
    val todayTasks = remember(allTasks) {
        allTasks.filter { task ->
            val startDate = LocalDate.parse(task.startDate, formatter)
            val endDate = LocalDate.parse(task.endDate, formatter)
            today in startDate..endDate
        }
    }

    var progressLevel by remember { mutableFloatStateOf(0f) }

    val todayMessage = remember(progressLevel) {
        getTodayTaskMessage(progressLevel)
    }

    ElevatedCard(elevation = CardDefaults.cardElevation(
        defaultElevation = 6.dp
    ), colors = CardDefaults.cardColors(
        containerColor = Color(0xFF5F33E1)
    ), modifier = modifier, shape = RoundedCornerShape(24.dp)
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (todayMessageText,viewTaskButton,progressIndicator) = createRefs()

            Text(todayMessage, modifier = Modifier.constrainAs(todayMessageText) {
                top.linkTo(parent.top, margin = 18.dp)
                start.linkTo(parent.start, margin = 25.dp)
            }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                fontSize = 14.sp, color = Color(0xFFFFFFFF))

            Button(modifier = Modifier.constrainAs(viewTaskButton) {
                start.linkTo(parent.start, margin = 25.dp)
                bottom.linkTo(parent.bottom, margin = 22.dp)
            }.size(120.dp,42.dp),
                onClick = { navController.navigate(BottomNavRoute.Calendar.route)}
                , colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEEE9FF),
                    contentColor = Color(0xFF5F33E1)
                ) , shape = RoundedCornerShape(10.dp)) {

                Text("View Task", fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.constrainAs(progressIndicator) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 40.dp)
                }.size(80.dp)
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progressLevel / 100f,
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing
                    ),
                    label = "progressAnimation"
                )
                val animatedText = progressLevel.toInt()

                Canvas(modifier = Modifier.size(80.dp)) {

                    drawArc(
                        color = Color(0xFFEEE9FF).copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = Color(0xFFEEE9FF),
                        startAngle = -90f,
                        sweepAngle = -360 * animatedProgress,
                        useCenter = false,
                        style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "$animatedText%",
                    color = Color(0xFFFFFFFF),
                    fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, lineHeight = 17.sp
                )
            }
        }
    }

    LaunchedEffect(todayTasks) {
        progressLevel = calculateGroupProgress(todayTasks, formatter)
    }
}

private fun getTodayTaskMessage(progress: Float): String {
    return when {
        progress == 0f ->
            "Start your today’s task\nand build momentum 🚀"

        progress in 1f..30f ->
            "Good start so far,\nkeep moving forward 💪"

        progress in 31f..60f ->
            "You're making steady \nprogress, stay focused 👍"

        progress in 61f..90f ->
            "Almost there now,\nkeep pushing strong 🔥"

        progress >= 100f ->
            "All today’s tasks \ncompleted, great job 🎉"

        else ->
            "Your today’s task\nalmost done!"
    }
}

@Composable
private fun InProgressTasks(
    modifier: Modifier, modifier1: Modifier,
    navController: NavController,
    inProgressTask: (String) -> Unit
) {
    val context = LocalContext.current

    val workTasksList by TaskPrefs
        .loadWorkTasks(context)
        .collectAsState(initial = emptyList())

    val personalTasksList by TaskPrefs
        .loadPersonalTasks(context)
        .collectAsState(initial = emptyList())

    val studyTasksList by TaskPrefs
        .loadStudyTasks(context)
        .collectAsState(initial = emptyList())

    val dailyStudyTasksList by TaskPrefs
        .loadDailyStudyTasks(context)
        .collectAsState(initial = emptyList())

    val filteredWorkTasks = filterTasksAboveTenPercent(workTasksList)
    val filteredPersonalTasks = filterTasksAboveTenPercent(personalTasksList)
    val filteredStudyTasks = filterTasksAboveTenPercent(studyTasksList)
    val filteredDailyStudyTasks = filterTasksAboveTenPercent(dailyStudyTasksList)

    val allFilteredTasks = remember(
        filteredWorkTasks,
        filteredPersonalTasks,
        filteredStudyTasks,
        filteredDailyStudyTasks
    ) {
        listOf(
            filteredWorkTasks,
            filteredPersonalTasks,
            filteredStudyTasks,
            filteredDailyStudyTasks
        ).flatten()
    }

    LaunchedEffect(allFilteredTasks.size) {
        inProgressTask(allFilteredTasks.size.toString())
    }

    if (allFilteredTasks.isEmpty()) {
        ElevatedCard(elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ), colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE7F3FF)
        ), modifier = modifier1.size(202.dp,116.dp).shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(19.dp),
            ambientColor = Color(0xFFE7F3FF).copy(alpha = 0.2f),
            spotColor = Color(0xFFE7F3FF).copy(alpha = 0.4f)
        ), onClick = { }, shape = RoundedCornerShape(19.dp))
        {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (emptyStateTitle,emptyStateSubtitle,emptyProgressBar) = createRefs()

                Text("No tasks in progress", modifier = Modifier.constrainAs(emptyStateTitle) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start)
                }.fillMaxWidth().padding(start = 16.dp, end = 42.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1
                )

                Text("Add a new task to start tracking", modifier = Modifier.constrainAs(emptyStateSubtitle) {
                    top.linkTo(emptyStateTitle.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                }.fillMaxWidth().padding(start = 16.dp, end = 25.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF000000), maxLines = 2
                )

                val animatedProgress by animateFloatAsState(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing
                    ),
                    label = "progressAnim"
                )

                Box(
                    modifier = Modifier.constrainAs(emptyProgressBar) {
                        top.linkTo(emptyStateSubtitle.bottom)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }.padding(horizontal = 17.dp).height(7.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFFFFFF)) // track color
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF0087FF)) // progress color
                    )
                }
            }
        }
    } else {
        LazyRow(modifier = modifier, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(allFilteredTasks){ task ->

                ElevatedCard(elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                ), colors = CardDefaults.cardColors(
                    containerColor = when(task.icon) {
                        R.drawable.briefcase -> Color(0xFFE7F3FF)
                        R.drawable.personal -> Color(0xFFFFF3E0)
                        R.drawable.study -> Color(0xFFE8F5E9)
                        R.drawable.daily_study -> Color(0xFFFFEBEE)
                        else -> Color(0xFFE7F3FF)
                    }
                ), modifier = Modifier.size(202.dp,116.dp).shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(19.dp),
                    ambientColor = when(task.icon) {
                        R.drawable.briefcase -> Color(0xFFE7F3FF)
                        R.drawable.personal -> Color(0xFFFFF3E0)
                        R.drawable.study -> Color(0xFFE8F5E9)
                        R.drawable.daily_study -> Color(0xFFFFEBEE)
                        else -> Color(0xFFE7F3FF)
                    }.copy(alpha = 0.2f),
                    spotColor = when(task.icon) {
                        R.drawable.briefcase -> Color(0xFFE7F3FF)
                        R.drawable.personal -> Color(0xFFFFF3E0)
                        R.drawable.study -> Color(0xFFE8F5E9)
                        R.drawable.daily_study -> Color(0xFFFFEBEE)
                        else -> Color(0xFFE7F3FF)
                    }.copy(alpha = 0.4f)
                ), onClick = { navController.navigate("update_task/${task.id}/${task.taskGroup}") }
                    ,shape = RoundedCornerShape(19.dp))
                {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (taskGroupNameText,taskNameText,taskProgressBar,taskIconContainer) = createRefs()

                        Text(task.taskGroupName, modifier = Modifier.constrainAs(taskGroupNameText) {
                            top.linkTo(parent.top, margin = 16.dp)
                            start.linkTo(parent.start)
                        }.fillMaxWidth().padding(start = 16.dp, end = 42.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )

                        Text(task.taskName, modifier = Modifier.constrainAs(taskNameText) {
                            top.linkTo(taskGroupNameText.bottom, margin = 10.dp)
                            start.linkTo(parent.start)
                        }.fillMaxWidth().padding(start = 16.dp, end = 25.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF000000), maxLines = 2, overflow = TextOverflow.Ellipsis
                        )

                        val animatedProgress by animateFloatAsState(
                            targetValue = task.progress / 100f,
                            animationSpec = tween(
                                durationMillis = 1200,
                                easing = FastOutSlowInEasing
                            ),
                            label = "progressAnim"
                        )

                        Box(
                            modifier = Modifier.constrainAs(taskProgressBar) {
                                top.linkTo(taskNameText.bottom)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }.padding(horizontal = 17.dp).height(7.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFFFFFFF)) // track color
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .clip(RoundedCornerShape(50))
                                    .background(when(task.icon) {
                                        R.drawable.briefcase -> Color(0xFF0087FF)
                                        R.drawable.personal -> Color(0xFFFF9800)
                                        R.drawable.study -> Color(0xFF4CAF50)
                                        R.drawable.daily_study -> Color(0xFFF44336)
                                        else -> Color(0xFF0087FF)
                                    }) // progress color
                            )
                        }

                        Box(modifier = Modifier.constrainAs(taskIconContainer) {
                            top.linkTo(parent.top, margin = 14.dp)
                            end.linkTo(parent.end, margin = 14.dp)
                        }.size(24.dp).background(Color(task.iconBg.toULong()),
                            shape = RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Image(painter = painterResource(task.icon), contentDescription = "Icon")
                        }
                    }
                }
            }
        }
    }
}

private fun filterTasksAboveTenPercent(tasks: List<Tasks>): List<Tasks> {
    return tasks.filter { it.progress in 1..99 }
}

@Composable
private fun TaskGroups(modifier: Modifier, navController: NavController) {
    val progressList = remember { mutableStateListOf(0f, 0f, 0f, 0f) }

    val context = LocalContext.current

    val workTasksList by TaskPrefs
        .loadWorkTasks(context)
        .collectAsState(initial = emptyList())

    val personalTasksList by TaskPrefs
        .loadPersonalTasks(context)
        .collectAsState(initial = emptyList())

    val studyTasksList by TaskPrefs
        .loadStudyTasks(context)
        .collectAsState(initial = emptyList())

    val dailyStudyTasksList by TaskPrefs
        .loadDailyStudyTasks(context)
        .collectAsState(initial = emptyList())

    val taskGroups = listOf(
        TaskGroup.WORK,
        TaskGroup.PERSONAL,
        TaskGroup.STUDY,
        TaskGroup.DAILY_STUDY
    )
    val totalTasks = listOf(
        workTasksList.size,
        personalTasksList.size,
        studyTasksList.size,
        dailyStudyTasksList.size
    )
    val progressBarColors2 = listOf(
        Color(0xFFF478B8),
        Color(0xFF9260F4),
        Color(0xFFFF9142),
        Color(0xFFFFD12E)
    )

    LazyColumn(modifier = modifier,contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp))
    {
        items(4) { index ->

            val progressColor = progressBarColors2[index % progressBarColors2.size]
            val taskGroup = taskGroups[index % taskGroups.size]
            val taskGroupIcon = taskGroupsIcons[index % taskGroupsIcons.size]
            val taskGroupIconColor = taskGroupsIconsColors[index % taskGroupsIconsColors.size]
            val totalTask = totalTasks[index % totalTasks.size]

            ElevatedCard(elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ), colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ), modifier = Modifier.padding(horizontal = 20.dp).height(68.dp).fillMaxWidth().shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(15.dp),
                ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
            ), onClick = { navController.navigate("tasks_screen/${taskGroup}")},
                shape = RoundedCornerShape(15.dp)) {
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                    val (taskGroupIconContainer,taskGroupNameText,taskCountText,taskGroupProgress) = createRefs()

                    Box(modifier = Modifier.constrainAs(taskGroupIconContainer) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start, margin = 15.dp)
                        bottom.linkTo(parent.bottom)
                    }.size(34.dp).background(taskGroupIconColor,
                        shape = RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    )  {
                        Image(modifier = Modifier.size(20.dp), painter = painterResource(taskGroupIcon), contentDescription = "briefcase")
                    }

                    Text(taskGroup, modifier = Modifier.constrainAs(taskGroupNameText) {
                        top.linkTo(parent.top, margin = 16.dp)
                        start.linkTo(taskGroupIconContainer.end, margin = 12.dp)
                    }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                    )

                    Text("$totalTask Tasks", modifier = Modifier.constrainAs(taskCountText) {
                        start.linkTo(taskGroupNameText.start)
                        top.linkTo(taskGroupNameText.bottom, margin = 5.dp)
                    }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.constrainAs(taskGroupProgress) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, margin = 25.dp)
                        }.size(42.dp)
                    ) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = progressList[index] / 100f,
                            animationSpec = tween(
                                durationMillis = 1200,
                                easing = FastOutSlowInEasing
                            ),
                            label = "progressAnimation"
                        )
                        val animatedText = progressList[index].toInt()

                        Canvas(modifier = Modifier.size(42.dp)) {

                            drawArc(
                                color = Color(0xFFEEE9FF).copy(alpha = 0.2f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                            )

                            drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = -360 * animatedProgress,
                                useCenter = false,
                                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Text(
                            text = "$animatedText%",
                            color = Color(0xFF24252C),
                            fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(workTasksList, personalTasksList, studyTasksList, dailyStudyTasksList
    ) {
        progressList[0] = calculateGroupProgress(workTasksList, formatter)
        progressList[1] = calculateGroupProgress(personalTasksList, formatter)
        progressList[2] = calculateGroupProgress(studyTasksList, formatter)
        progressList[3] = calculateGroupProgress(dailyStudyTasksList, formatter)
    }
}

private fun calculateGroupProgress(tasks: List<Tasks>, formatter: DateTimeFormatter): Float {
    if (tasks.isEmpty()) return 0f

    var totalDays = 0
    var completedDays = 0

    tasks.forEach { task ->
        val start = runCatching {
            LocalDate.parse(task.startDate, formatter)
        }.getOrNull()

        val end = runCatching {
            LocalDate.parse(task.endDate, formatter)
        }.getOrNull()

        if (start != null && end != null && !end.isBefore(start)) {
            val days =
                ChronoUnit.DAYS.between(start, end).toInt() + 1

            totalDays += days

            completedDays += task.completedDates.count {
                runCatching {
                    val date = LocalDate.parse(it, formatter)
                    !date.isBefore(start) && !date.isAfter(end)
                }.getOrDefault(false)
            }
        }
    }

    if (totalDays == 0) return 0f

    return (completedDays.toFloat() / totalDays) * 100f
}

@Preview(showSystemUi = true)
@Composable
private fun ShowHomeScreen() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}