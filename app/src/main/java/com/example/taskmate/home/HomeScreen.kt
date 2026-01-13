package com.example.taskmate.home

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.taskmate.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

object TaskPrefs {
    private const val PREF_NAME = "task_prefs"
    private const val KEY_WORK_TASKS = "work_tasks"
    private const val KEY_PERSONAL_TASKS = "personal_tasks"
    private const val KEY_STUDY_TASKS = "study_tasks"
    private const val KEY_DAILY_STUDY_TASKS = "daily_study_tasks"

    fun saveWorkTasks(context: Context, task: Tasks) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val existingJson = prefs.getString(KEY_WORK_TASKS, null)
        val type = object : TypeToken<MutableList<Tasks>>() {}.type

        val taskList: MutableList<Tasks> =
            if (existingJson != null)
                gson.fromJson(existingJson, type)
            else
                mutableListOf()

        val index = taskList.indexOfFirst { it.id == task.id }

        if (index != -1) {
            // ✅ UPDATE
            taskList[index] = task
        } else {
            // ➕ ADD
            taskList.add(task)
        }

        prefs.edit {
            putString(KEY_WORK_TASKS, gson.toJson(taskList))
        }
    }
    fun removeWorkTask(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val json = prefs.getString(KEY_WORK_TASKS, null) ?: return
        val type = object : TypeToken<MutableList<Tasks>>() {}.type

        val list: MutableList<Tasks> = gson.fromJson(json, type)
        list.removeAll { it.id == taskId }

        prefs.edit {
            putString(KEY_WORK_TASKS, gson.toJson(list))
        }
    }
    fun loadWorkTasks(context: Context): List<Tasks> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_WORK_TASKS, null) ?: return emptyList()

        val type = object : TypeToken<List<Tasks>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun savePersonalTasks(context: Context, task: Tasks) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val existingJson = prefs.getString(KEY_PERSONAL_TASKS, null)

        val type = object : TypeToken<MutableList<Tasks>>() {}.type
        val taskList: MutableList<Tasks> =
            if (existingJson != null)
                gson.fromJson(existingJson, type)
            else
                mutableListOf()

        val index = taskList.indexOfFirst { it.id == task.id }

        if (index != -1) {
            // ✅ UPDATE
            taskList[index] = task
        } else {
            // ➕ ADD
            taskList.add(task)
        }

        prefs.edit {
            putString(KEY_PERSONAL_TASKS, gson.toJson(taskList))
        }
    }
    fun removePersonalTasks(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val json = prefs.getString(KEY_PERSONAL_TASKS, null) ?: return
        val type = object : TypeToken<MutableList<Tasks>>() {}.type

        val list: MutableList<Tasks> = gson.fromJson(json, type)
        list.removeAll { it.id == taskId }

        prefs.edit {
            putString(KEY_PERSONAL_TASKS, gson.toJson(list))
        }
    }
    fun loadPersonalTasks(context: Context): List<Tasks> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PERSONAL_TASKS, null) ?: return emptyList()

        val type = object : TypeToken<List<Tasks>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveStudyTasks(context: Context, task: Tasks) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val existingJson = prefs.getString(KEY_STUDY_TASKS, null)

        val type = object : TypeToken<MutableList<Tasks>>() {}.type
        val taskList: MutableList<Tasks> =
            if (existingJson != null)
                gson.fromJson(existingJson, type)
            else
                mutableListOf()

        val index = taskList.indexOfFirst { it.id == task.id }

        if (index != -1) {
            // ✅ UPDATE
            taskList[index] = task
        } else {
            // ➕ ADD
            taskList.add(task)
        }

        prefs.edit {
            putString(KEY_STUDY_TASKS, gson.toJson(taskList))
        }
    }
    fun removeStudyTasks(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val json = prefs.getString(KEY_STUDY_TASKS, null) ?: return
        val type = object : TypeToken<MutableList<Tasks>>() {}.type

        val list: MutableList<Tasks> = gson.fromJson(json, type)
        list.removeAll { it.id == taskId }

        prefs.edit {
            putString(KEY_STUDY_TASKS, gson.toJson(list))
        }
    }
    fun loadStudyTasks(context: Context): List<Tasks> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_STUDY_TASKS, null) ?: return emptyList()

        val type = object : TypeToken<List<Tasks>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveDailyStudyTasks(context: Context, task: Tasks) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val existingJson = prefs.getString(KEY_DAILY_STUDY_TASKS, null)

        val type = object : TypeToken<MutableList<Tasks>>() {}.type
        val taskList: MutableList<Tasks> =
            if (existingJson != null)
                gson.fromJson(existingJson, type)
            else
                mutableListOf()

        val index = taskList.indexOfFirst { it.id == task.id }

        if (index != -1) {
            // ✅ UPDATE
            taskList[index] = task
        } else {
            // ➕ ADD
            taskList.add(task)
        }

        prefs.edit {
            putString(KEY_DAILY_STUDY_TASKS, gson.toJson(taskList))
        }
    }
    fun removeDailyStudyTasks(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        val json = prefs.getString(KEY_DAILY_STUDY_TASKS, null) ?: return
        val type = object : TypeToken<MutableList<Tasks>>() {}.type

        val list: MutableList<Tasks> = gson.fromJson(json, type)
        list.removeAll { it.id == taskId }

        prefs.edit {
            putString(KEY_DAILY_STUDY_TASKS, gson.toJson(list))
        }
    }
    fun loadDailyStudyTasks(context: Context): List<Tasks> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_DAILY_STUDY_TASKS, null) ?: return emptyList()

        val type = object : TypeToken<List<Tasks>>() {}.type
        return Gson().fromJson(json, type)
    }
}
@Composable
fun HomeScreen(navController: NavController) {
    var inProgressTasks by remember { mutableStateOf("0") }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(profileImage,text1,nameText,card,text2,circle1,lazyRow,lazyColumn,
            text3,circle2,defaultCard) = createRefs()

        ProfileView(modifier = Modifier.constrainAs(profileImage) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start, margin = 22.dp)
        }.size(52.dp).clip(CircleShape),
            modifier2 = Modifier.constrainAs(text1) {
                start.linkTo(profileImage.end, margin = 15.dp)
                top.linkTo(profileImage.top, margin = 4.dp)
            }, modifier3 = Modifier.constrainAs(nameText) {
                start.linkTo(profileImage.end, margin = 15.dp)
                top.linkTo(text1.bottom)
            }
        )

        TodayTaskProgress(modifier = Modifier.constrainAs(card) {
            top.linkTo(profileImage.bottom, margin = 22.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(horizontal = 25.dp).fillMaxWidth().height(156.dp))

        Text("In Progress", modifier = Modifier.constrainAs(text2) {
            top.linkTo(card.bottom, margin = 22.dp)
            start.linkTo(parent.start, margin = 25.dp)
        }, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            fontSize = 19.sp, color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(circle1) {
            top.linkTo(text2.top)
            start.linkTo(text2.end, margin = 5.dp)
            bottom.linkTo(text2.bottom)
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

        InProgressTasks(modifier = Modifier.constrainAs(lazyRow) {
            top.linkTo(text2.bottom, margin = 15.dp)
            start.linkTo(parent.start)
        }, modifier1 = Modifier.constrainAs(defaultCard) {
            top.linkTo(text2.bottom, margin = 15.dp)
            start.linkTo(parent.start, margin = 25.dp)
        }, navController) { count ->
            inProgressTasks = count
        }
        
        Text("Task Groups", modifier = Modifier.constrainAs(text3) {
            top.linkTo(text2.bottom, margin = 145.dp)
            start.linkTo(parent.start, margin = 25.dp)
        }, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            fontSize = 19.sp, color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(circle2) {
            top.linkTo(text3.top)
            start.linkTo(text3.end, margin = 5.dp)
            bottom.linkTo(text3.bottom)
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
        
        TaskGroups(modifier = Modifier.constrainAs(lazyColumn) {
            top.linkTo(text3.bottom, margin = 12.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom, margin = (-15).dp)
            height = Dimension.fillToConstraints
        })
    }
}

@Composable
private fun ProfileView(modifier: Modifier,modifier2: Modifier,modifier3: Modifier) {
    Image(painter = painterResource(R.drawable.picofme), contentDescription = "profile Image",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )

    Text("Hello!", modifier = modifier2, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
        fontSize = 16.sp, lineHeight = 19.sp, color = Color(0xFF24252C)
    )

    Text("Harsh Suthar", modifier = modifier3, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
        fontSize = 18.sp, lineHeight = 21.sp, color = Color(0xFF24252C)
    )
}

@Composable
private fun TodayTaskProgress(modifier: Modifier) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val today = LocalDate.now()

    val allTasks = remember {
        listOf(
            TaskPrefs.loadWorkTasks(context),
            TaskPrefs.loadPersonalTasks(context),
            TaskPrefs.loadStudyTasks(context),
            TaskPrefs.loadDailyStudyTasks(context)
        ).flatten()
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
            val (text1,button,progress) = createRefs()

            Text(todayMessage, modifier = Modifier.constrainAs(text1) {
                top.linkTo(parent.top, margin = 18.dp)
                start.linkTo(parent.start, margin = 25.dp)
            }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                fontSize = 14.sp, color = Color(0xFFFFFFFF))

            Button(modifier = Modifier.constrainAs(button) {
                start.linkTo(parent.start, margin = 25.dp)
                bottom.linkTo(parent.bottom, margin = 22.dp)
            }.size(120.dp,42.dp),
                onClick = {

                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEEE9FF),
                    contentColor = Color(0xFF5F33E1)
                ) , shape = RoundedCornerShape(10.dp)) {

                Text("View Task", fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.constrainAs(progress) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 45.dp)
                }.size(80.dp)
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progressLevel,              // final progress
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing
                    ),
                    label = "progressAnimation"
                )
                val animatedText = (animatedProgress * 100).toInt()

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
        progressLevel = calculateGroupProgress(todayTasks)
    }
}

private fun getTodayTaskMessage(progress: Float): String {
    return when {
        progress == 0f ->
            "Start your today’s task\nand build momentum 🚀"

        progress in 0.01f..0.3f ->
            "Good start so far,\nkeep moving forward 💪"

        progress in 0.31f..0.6f ->
            "You're making steady \nprogress,stay focused 👍"

        progress in 0.61f..0.9f ->
            "Almost there now,\nkeep pushing strong 🔥"

        progress >= 1f ->
            "All today’s tasks \ncompleted,great job 🎉"
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
    val workTasksList = TaskPrefs.loadWorkTasks(LocalContext.current)
    val personalTasksList = TaskPrefs.loadPersonalTasks(LocalContext.current)
    val studyTasksList = TaskPrefs.loadStudyTasks(LocalContext.current)
    val dailyStudyTasksList = TaskPrefs.loadDailyStudyTasks(LocalContext.current)

    val filteredWorkTasks = filterTasksAboveTenPercent(workTasksList)
    val filteredPersonalTasks = filterTasksAboveTenPercent(personalTasksList)
    val filteredStudyTasks = filterTasksAboveTenPercent(studyTasksList)
    val filteredDailyStudyTasks = filterTasksAboveTenPercent(dailyStudyTasksList)

    val allFilteredTasks = listOf(
        filteredWorkTasks,
        filteredPersonalTasks,
        filteredStudyTasks,
        filteredDailyStudyTasks
    ).flatten()

    Log.d("Tasks","$allFilteredTasks")

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
                val(text1,text2,progressBar) = createRefs()

                Text("No tasks in progress", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start)
                }.fillMaxWidth().padding(start = 16.dp, end = 42.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1
                )

                Text("Add a new task to start tracking", modifier = Modifier.constrainAs(text2) {
                    top.linkTo(text1.bottom, margin = 10.dp)
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
                    modifier = Modifier.constrainAs(progressBar) {
                        top.linkTo(text2.bottom)
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
                ), onClick = { navController.navigate("update_task/${task.id}/${task.taskGroup}")}
                    ,shape = RoundedCornerShape(19.dp))
                {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(text1,text2,progressBar,boxShape) = createRefs()

                        Text(task.taskGroupName, modifier = Modifier.constrainAs(text1) {
                            top.linkTo(parent.top, margin = 16.dp)
                            start.linkTo(parent.start)
                        }.fillMaxWidth().padding(start = 16.dp, end = 42.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1
                        )

                        Text(task.taskName, modifier = Modifier.constrainAs(text2) {
                            top.linkTo(text1.bottom, margin = 10.dp)
                            start.linkTo(parent.start)
                        }.fillMaxWidth().padding(start = 16.dp, end = 25.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF000000), maxLines = 2
                        )

                        val animatedProgress by animateFloatAsState(
                            targetValue = task.progress,
                            animationSpec = tween(
                                durationMillis = 1200,
                                easing = FastOutSlowInEasing
                            ),
                            label = "progressAnim"
                        )

                        Box(
                            modifier = Modifier.constrainAs(progressBar) {
                                top.linkTo(text2.bottom, margin = 12.dp)
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

                        Box(modifier = Modifier.constrainAs(boxShape) {
                            top.linkTo(parent.top, margin = 14.dp)
                            end.linkTo(parent.end, margin = 14.dp)
                        }.size(24.dp).background(Color(task.iconBg.toULong()),
                            shape = RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Image(painter = painterResource(task.icon), contentDescription = "briefcase")
                        }
                    }
                }
            }
        }
    }
}

private fun filterTasksAboveTenPercent(tasks: List<Tasks>): List<Tasks> {
    return tasks.filter { it.progress > 0.1f && it.progress < 1f}
}

@Composable
private fun TaskGroups(modifier: Modifier) {
    val progressList = remember { mutableStateListOf(0f, 0f, 0f, 0f) }

    val workTasksList = TaskPrefs.loadWorkTasks(LocalContext.current)
    val personalTasksList = TaskPrefs.loadPersonalTasks(LocalContext.current)
    val studyTasksList = TaskPrefs.loadStudyTasks(LocalContext.current)
    val dailyStudyTasksList = TaskPrefs.loadDailyStudyTasks(LocalContext.current)

    val taskGroups = listOf(
        "Work",
        "Personal",
        "Study",
        "Daily Study"
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
            ),shape = RoundedCornerShape(15.dp)) {
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                    val (boxShape,text1,text2,progress) = createRefs()

                    Box(modifier = Modifier.constrainAs(boxShape) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start, margin = 15.dp)
                        bottom.linkTo(parent.bottom)
                    }.size(34.dp).background(taskGroupIconColor,
                        shape = RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    )  {
                        Image(modifier = Modifier.size(20.dp), painter = painterResource(taskGroupIcon), contentDescription = "briefcase")
                    }

                    Text(taskGroup, modifier = Modifier.constrainAs(text1) {
                        top.linkTo(parent.top, margin = 16.dp)
                        start.linkTo(boxShape.end, margin = 12.dp)
                    }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                    )

                    Text("$totalTask Tasks", modifier = Modifier.constrainAs(text2) {
                        start.linkTo(text1.start)
                        top.linkTo(text1.bottom, margin = 5.dp)
                    }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.constrainAs(progress) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, margin = 25.dp)
                        }.size(42.dp)
                    ) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = progressList[index],
                            animationSpec = tween(
                                durationMillis = 1200,
                                easing = FastOutSlowInEasing
                            ),
                            label = "progressAnimation"
                        )
                        val animatedText = (animatedProgress * 100).toInt()

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

    LaunchedEffect(Unit) {
        progressList[0] = calculateGroupProgress(workTasksList)
        progressList[1] = calculateGroupProgress(personalTasksList)
        progressList[2] = calculateGroupProgress(studyTasksList)
        progressList[3] = calculateGroupProgress(dailyStudyTasksList)
    }
}

private fun calculateGroupProgress(tasks: List<Tasks>): Float {
    if (tasks.isEmpty()) return 0f
    return tasks.map { it.progress }.average().toFloat()
}

@Preview(showSystemUi = true)
@Composable
private fun ShowHomeScreen() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}