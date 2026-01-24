package com.example.taskmate.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.android.identity.util.UUID
import com.example.taskmate.R
import com.example.taskmate.home.Tasks
import com.example.taskmate.home.fonts
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

val Context.notificationDataStore by preferencesDataStore(
    name = "notification_store"
)

object NotificationStore {
    private val LIST = stringPreferencesKey("notification_list")
    private val gson = Gson()
    private val type =
        object : TypeToken<List<StoredNotification>>() {}.type

    private fun parse(json: String?): List<StoredNotification> =
        if (json.isNullOrEmpty()) emptyList()
        else gson.fromJson(json, type)

    fun getAll(context: Context): Flow<List<StoredNotification>> =
        context.notificationDataStore.data.map { prefs ->
            parse(prefs[LIST])
        }

    suspend fun add(context: Context, notification: StoredNotification) {
        context.notificationDataStore.edit { prefs ->
            val current = parse(prefs[LIST]).toMutableList()
            current.add(0, notification) // newest first
            prefs[LIST] = gson.toJson(current)
        }
    }

    suspend fun clear(context: Context) {
        context.notificationDataStore.edit {
            it.remove(LIST)
        }
    }

    suspend fun removeNotification(context: Context, taskId: String) {
        context.notificationDataStore.edit { prefs ->
            val list = parse(prefs[LIST]).toMutableList()
            list.removeAll { it.id == taskId }
            prefs[LIST] = gson.toJson(list)
        }
    }
}

class TaskDeadlineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {

        val taskId= inputData.getString("taskId") ?: return Result.failure()
        val taskName = inputData.getString("taskName") ?: return Result.failure()
        val progressStatus = inputData.getString("progressStatus") ?: "Pending"
        val taskIcon = inputData.getInt("taskIcon",0)
        val taskIconBG = inputData.getLong("taskIconBG",0L)

        Log.d("TaskWorker", "Worker executed for task: $taskName")

        if (progressStatus == "Completed") {
            return Result.success()
        }

        val endMillis = inputData.getLong("endMillis", -1L)

        if (endMillis <= 0L) return Result.failure()

        val message = getTaskNotificationMessage(endMillis)

        NotificationHelper.show(
            applicationContext,
            taskName,
            message
        )

        val notification = StoredNotification(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            title = "Task Ending Soon ⏳",
            message = "$taskName • $message",
            timestamp = System.currentTimeMillis(),
            icon  = taskIcon,
            iconBg = taskIconBG
        )

        NotificationStore.add(applicationContext, notification)

        return Result.success()
    }
}

object DateConverter {
    private val formatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun startDateToMillis(date: String): Long {
        val localDate = LocalDate.parse(date, formatter)
        return localDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun endDateWithCreationTime(endDate: String, createdAtMillis: Long): Long {
        val endLocalDate = LocalDate.parse(endDate, formatter)

        val creationTime = Instant.ofEpochMilli(createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        return endLocalDate
            .atTime(creationTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}

object NotificationHelper {
    private const val CHANNEL_ID = "task_channel"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun show(context: Context, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(UUID.randomUUID().hashCode(), notification)
    }
}

fun getTaskNotificationMessage(endMillis: Long): String {

    val nowMillis = System.currentTimeMillis()

    val now = Instant.ofEpochMilli(nowMillis)
        .atZone(ZoneId.systemDefault())

    val end = Instant.ofEpochMilli(endMillis)
        .atZone(ZoneId.systemDefault())

    val duration = Duration.between(now, end)
    val hoursLeft = duration.toHours()

    return when {

        // 🔴 Already overdue
        endMillis < nowMillis ->
            "Ended on ${formatDate(endMillis)} – Please complete it"

        // ⏰ Ending in next 4 hours
        hoursLeft in 0..4 ->
            "Ending in $hoursLeft hour${if (hoursLeft != 1L) "s" else ""}"

        // 📅 Ends today
        end.toLocalDate() == now.toLocalDate() ->
            "Ends today at ${formatTime(endMillis)}"

        // 📅 Ends tomorrow
        end.toLocalDate() == now.toLocalDate().plusDays(1) ->
            "Ends tomorrow at ${formatTime(endMillis)}"

        duration.toMinutes() < 60 ->
            "Ending in ${duration.toMinutes()} minutes"

        // 📆 Ends later
        else ->
            "Ends on ${formatDate(endMillis)}"
    }
}

fun formatTime(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("h:mm a"))
}

fun formatDate(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd MMM"))
}

fun scheduleTaskEndDateNotification(context: Context, task: Tasks) {
    val endMillis = DateConverter.endDateWithCreationTime(task.endDate, task.time)

    val now = System.currentTimeMillis()
    val notifyTime = endMillis - TimeUnit.HOURS.toMillis(4)

    val delay = when {
        notifyTime > now -> notifyTime - now          // normal case
        endMillis > now -> TimeUnit.SECONDS.toMillis(5)
        else -> return                                 // task already ended
    }

    val data = workDataOf(
        "taskId" to task.id,
        "taskName" to task.taskName,
        "progressStatus" to task.progressStatus,
        "endMillis" to endMillis,
        "taskIcon" to task.icon,
        "taskIconBG" to task.iconBg
    )

    val work = OneTimeWorkRequestBuilder<TaskDeadlineWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag(task.id)
        .build()



    WorkManager.getInstance(context).enqueueUniqueWork(
        task.id,
        androidx.work.ExistingWorkPolicy.REPLACE,
        work
    )

    Log.e("TaskSchedule", "WorkManager enqueue called")
}

fun cancelTaskNotifications(context: Context, taskId: String) {
    WorkManager.getInstance(context)
        .cancelAllWorkByTag(taskId)
}

suspend fun notifyOverdueTasks(context: Context, tasks: List<Tasks>) {

    val prefs = context.getSharedPreferences(
        "overdue_prefs",
        Context.MODE_PRIVATE
    )

    val todayKey = LocalDate.now().toString()
    val lastNotifiedDay = prefs.getString("last_notified_day", "")

    if (lastNotifiedDay == todayKey) return

    val now = System.currentTimeMillis()

    tasks.forEach { task ->

        val endMillis =
            DateConverter.endDateWithCreationTime(
                task.endDate,
                task.time
            )

        if (task.progressStatus != "Completed" && now > endMillis) {

            NotificationHelper.show(
                context,
                "Task Overdue 🚨",
                task.taskName
            )

            val notification = StoredNotification(
                id = UUID.randomUUID().toString(),
                taskId = task.id,
                title = "Task Overdue 🚨",
                message = task.taskName,
                timestamp = System.currentTimeMillis(),
                icon = task.icon,
                iconBg = task.iconBg
            )

            NotificationStore.add(context, notification)
        }
    }

    prefs.edit { putString("last_notified_day", todayKey) }
}

@Composable
fun NotificationScreen(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingDelete by remember { mutableStateOf<StoredNotification?>(null) }
    val swipeStates = remember { mutableMapOf<String, SwipeToDismissBoxState>() }

    val notifications by NotificationStore
        .getAll(context)
        .collectAsState(initial = emptyList())

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (titleText, todayText, clearAllButton, emptyIcon, emptyMessage, notificationsList) = createRefs()

        Text("Notification", modifier = Modifier.constrainAs(titleText) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Text("Today", modifier = Modifier.constrainAs(todayText) {
            top.linkTo(titleText.bottom, margin = 15.dp)
            start.linkTo(parent.start, margin = 20.dp)
        }, fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(clearAllButton) {
            top.linkTo(titleText.bottom, margin = 15.dp)
            end.linkTo(parent.end, margin = 20.dp)
        }.size(72.dp,20.dp).clip(RoundedCornerShape(6.dp))
            .clickable {
                if (notifications.isEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "No Notification to clear",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    scope.launch {
                        NotificationStore.clear(context)
                        swipeStates.clear()

                        snackbarHostState.showSnackbar(
                            message = "Notifications cleared",
                            duration = SnackbarDuration.Short
                        )
                    }
                } }, contentAlignment = Alignment.Center) {
            Text("Clear All", fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = Color(0xFF5F33E1)
            )
        }

        if (notifications.isEmpty()) {
            Icon(painter = painterResource(R.drawable.empty_notification), contentDescription = "empty_notification",
                tint = Color(0xFF5F33E1), modifier = Modifier.constrainAs(emptyIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.size(92.dp)
            )

            Text("No Notifications", modifier = Modifier.constrainAs(emptyMessage) {
                top.linkTo(emptyIcon.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = Color(0xFF6E6A7C)
            )
        }

        LazyColumn(modifier = Modifier.constrainAs(notificationsList) {
            top.linkTo(todayText.bottom, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom, margin = (-15).dp)
            height = Dimension.fillToConstraints
        },contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp))
        {
            items(items = notifications, key = { it.id }) { task ->
                val dismissState = swipeStates.getOrPut(task.id) {
                    rememberSwipeToDismissBoxState(SwipeToDismissBoxValue.Settled)
                }

                LaunchedEffect(dismissState) {
                    snapshotFlow { dismissState.currentValue }
                        .collect { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                // Trigger deletion
                                pendingDelete = task
                            }
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
                        ), modifier = Modifier.padding(horizontal = 20.dp).height(68.dp).fillMaxWidth().shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(15.dp),
                            ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                            spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                        ),shape = RoundedCornerShape(15.dp)) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                                val (iconBox, taskNameText, deadlineText, timeText) = createRefs()

                                Box(modifier = Modifier.constrainAs(iconBox) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start, margin = 15.dp)
                                    bottom.linkTo(parent.bottom)
                                }.size(34.dp).background(Color(task.iconBg.toULong()),
                                    shape = RoundedCornerShape(9.dp)),
                                    contentAlignment = Alignment.Center
                                )  {
                                    Image(modifier = Modifier.size(20.dp), painter = painterResource(task.icon), contentDescription = "briefcase")
                                }

                                Text(task.title, modifier = Modifier.constrainAs(taskNameText) {
                                    top.linkTo(parent.top, margin = 16.dp)
                                    start.linkTo(iconBox.end, margin = 12.dp)
                                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C), maxLines = 1
                                )

                                Text(task.message, modifier = Modifier.constrainAs(deadlineText) {
                                    start.linkTo(taskNameText.start)
                                    top.linkTo(taskNameText.bottom, margin = 5.dp)
                                    bottom.linkTo(parent.bottom, margin = 14.dp)
                                    width = Dimension.fillToConstraints
                                    end.linkTo(parent.end, margin = 15.dp)
                                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 2
                                )

                                Text(formatNotificationTime(task.timestamp), modifier = Modifier.constrainAs(timeText) {
                                    end.linkTo(parent.end, margin = 15.dp)
                                    top.linkTo(taskNameText.top)
                                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C)
                                )
                            }
                        }
                    }
                )
            }
        }

        LaunchedEffect(pendingDelete) {
            pendingDelete?.let { task ->
                NotificationStore.removeNotification(context, task.id)

                snackbarHostState.showSnackbar(
                    message = "Notification deleted",
                    duration = SnackbarDuration.Short
                )

                swipeStates[task.id]?.reset()
                pendingDelete = null
            }
        }
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    val time = Instant.ofEpochMilli(timestamp)

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val date = time.atZone(zone).toLocalDate()

    return when (date) {
        today ->
            time.atZone(zone)
                .format(DateTimeFormatter.ofPattern("h:mm a"))
        today.minusDays(1) ->
            "Yesterday"
        else -> time.atZone(zone)
            .format(DateTimeFormatter.ofPattern("dd MMM"))
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ShowNotificationScreen() {
    val snackbarHostState = SnackbarHostState()
    NotificationScreen(snackbarHostState)
}