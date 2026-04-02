package com.example.taskmate.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.identity.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TaskDeadlineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val taskId = intent.getStringExtra("taskId") ?: return
        val taskName = intent.getStringExtra("taskName") ?: return
        val progressStatus = intent.getStringExtra("progressStatus") ?: "Pending"
        val taskIcon = intent.getIntExtra("taskIcon", 0)
        val taskIconBG = intent.getLongExtra("taskIconBG", 0L)
        val endMillis = intent.getLongExtra("endMillis", -1L)

        if (progressStatus == "Completed") return
        if (endMillis <= 0L) return

        val now = System.currentTimeMillis()

        val title = when {
            endMillis < now -> "Task Overdue 🚨"
            endMillis - now <= TimeUnit.HOURS.toMillis(1) -> "Ending Soon ⏳"
            else -> "Task Reminder 📌"
        }

        val message = getTaskNotificationMessage(endMillis)

        NotificationHelper.show(
            context,
            taskName,
            message,
            taskId.hashCode()
        )

        val notification = StoredNotification(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            title = title,
            message = "$taskName • $message",
            timestamp = System.currentTimeMillis(),
            icon = taskIcon,
            iconBg = taskIconBG
        )

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationStore.add(context, notification)
            } finally {
                pendingResult.finish()
            }
        }
    }
}