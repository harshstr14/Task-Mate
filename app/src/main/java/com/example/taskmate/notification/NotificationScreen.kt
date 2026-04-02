package com.example.taskmate.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.android.identity.util.UUID
import com.example.taskmate.R
import com.example.taskmate.home.Tasks
import com.example.taskmate.home.fonts
import com.example.taskmate.pressScale
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

    fun show(context: Context, title: String, message: String, id: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(id, notification)
    }}

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

        duration.toMinutes() < 60 ->
            "Ending in ${duration.toMinutes()} minutes"

        hoursLeft in 0..4 ->
            "Ending in $hoursLeft hour${if (hoursLeft != 1L) "s" else ""}"

        // 📅 Ends today
        end.toLocalDate() == now.toLocalDate() ->
            "Ends today at ${formatTime(endMillis)}"

        // 📅 Ends tomorrow
        end.toLocalDate() == now.toLocalDate().plusDays(1) ->
            "Ends tomorrow at ${formatTime(endMillis)}"

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
    val now = System.currentTimeMillis()

    val times = listOf(
        task.endAt - TimeUnit.HOURS.toMillis(4),
        task.endAt - TimeUnit.HOURS.toMillis(1),
        task.endAt - TimeUnit.MINUTES.toMillis(30),
        task.endAt
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    times.forEachIndexed { index, time ->
        if (time <= now) return@forEachIndexed

        val intent = Intent(context, TaskDeadlineReceiver::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.taskName)
            putExtra("endMillis", task.endAt)
            putExtra("taskIcon", task.icon)
            putExtra("taskIconBG", task.iconBg)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + index).hashCode(), // unique per alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }
}

fun cancelTaskNotifications(context: Context, taskId: String) {

    val intent = Intent(context, TaskDeadlineReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
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

        val endMillis = task.endAt

        if (task.progressStatus != "Completed" && now > endMillis) {

            NotificationHelper.show(
                context,
                task.taskName,
                "Task Overdue 🚨",
                task.id.hashCode()
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
fun NotificationScreen(snackBarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val (clearInteraction, clearScale) = pressScale()

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

        Box(
            modifier = Modifier.constrainAs(clearAllButton) {
                    top.linkTo(titleText.bottom, margin = 15.dp)
                    end.linkTo(parent.end, margin = 20.dp)
            }.size(72.dp,20.dp)
             .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = clearInteraction,
                indication = null
            ) {
                if (notifications.isEmpty()) {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "No Notification to clear",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    scope.launch {
                        NotificationStore.clear(context)
                        swipeStates.clear()

                        snackBarHostState.showSnackbar(
                            message = "Notifications cleared",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }, contentAlignment = Alignment.Center)
        {
            Text(modifier = Modifier.graphicsLayer {
                scaleX = clearScale
                scaleY = clearScale
            },  text = "Clear All", fontSize = 14.sp, lineHeight = 17.sp,
                fontFamily = fonts, fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal, color = Color(0xFF5F33E1)
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
                                modifier = Modifier.size(44.dp).padding(end = 24.dp)
                            )
                        }
                    },
                    content = {
                        ElevatedCard(elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ), colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFFFF)
                        ), modifier = Modifier.padding(horizontal = 20.dp).height(72.dp).fillMaxWidth().shadow(
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
                                    Image(modifier = Modifier.size(20.dp),
                                        painter = painterResource(if (task.icon != 0) task.icon else R.drawable.notification_icon),
                                        contentDescription = "briefcase")
                                }

                                Text(task.title, modifier = Modifier.constrainAs(taskNameText) {
                                    top.linkTo(parent.top, margin = 12.dp)
                                    start.linkTo(iconBox.end, margin = 12.dp)
                                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C), maxLines = 1
                                )

                                Text(task.message, modifier = Modifier.constrainAs(deadlineText) {
                                    start.linkTo(taskNameText.start)
                                    top.linkTo(taskNameText.bottom, margin = 8.dp)
                                    bottom.linkTo(parent.bottom, margin = 8.dp)
                                    width = Dimension.fillToConstraints
                                    end.linkTo(parent.end, margin = 25.dp)
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

                snackBarHostState.showSnackbar(
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
    val snackBarHostState = SnackbarHostState()
    NotificationScreen(snackBarHostState)
}