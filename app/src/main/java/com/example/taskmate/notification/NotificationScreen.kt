package com.example.taskmate.notification

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.taskmate.R

@Composable
fun NotificationScreen() {
    val fonts = FontFamily(
        Font(R.font.merriweathersans_bold, FontWeight.Bold),
        Font(R.font.merriweathersans_semibold, FontWeight.SemiBold),
        Font(R.font.merriweathersans_regular, FontWeight.Normal)
    )

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
            .clickable { }, contentAlignment = Alignment.Center) {
            Text("Clear All", fontSize = 14.sp, lineHeight = 17.sp, fontFamily = com.example.taskmate.home.fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = Color(0xFF5F33E1)
            )
        }

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

        val taskGroups = listOf(
            "Market Research",
            "Prepare Presentation",
            "Submit Resume",
            "Complete Kotlin Practice"
        )
        val deadlineStatus  = listOf(
            "Ended on 8 Jan – Please complete it",
            "Ends today at 9:00 am",
            "Ends tomorrow at 10:00 am",
            "Ends on 12 Jan"
        )
        val taskGroupsIcons = listOf(
            R.drawable.briefcase,
            R.drawable.briefcase,
            R.drawable.personal,
            R.drawable.study
        )
        val taskGroupsIconsColors = listOf(
            Color(0xFFFFE4F2),
            Color(0xFFFFE4F2),
            Color(0xFFEDE4FF),
            Color(0xFFFFF6D4)
        )
        val time = listOf(
            "8:00 am","7:00 am","10:00 am","6:00 pm"
        )

        LazyColumn(modifier = Modifier.constrainAs(notificationsList) {
            top.linkTo(todayText.bottom, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            height = Dimension.fillToConstraints
        },contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp))
        {
            items(0) { index ->
                val taskGroupIcon = taskGroupsIcons[index % taskGroupsIcons.size]
                val taskGroupIconColor = taskGroupsIconsColors[index % taskGroupsIconsColors.size]
                val taskGroup = taskGroups[index % taskGroups.size]
                val totalTask = deadlineStatus[index % deadlineStatus.size]
                val time = time[index % time.size]

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
                        }.size(34.dp).background(taskGroupIconColor,
                            shape = RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Image(modifier = Modifier.size(20.dp), painter = painterResource(taskGroupIcon), contentDescription = "briefcase")
                        }

                        Text(taskGroup, modifier = Modifier.constrainAs(taskNameText) {
                            top.linkTo(parent.top, margin = 16.dp)
                            start.linkTo(iconBox.end, margin = 12.dp)
                        }, fontFamily = com.example.taskmate.home.fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C), maxLines = 1
                        )

                        Text(totalTask, modifier = Modifier.constrainAs(deadlineText) {
                            start.linkTo(taskNameText.start)
                            top.linkTo(taskNameText.bottom, margin = 5.dp)
                        }, fontFamily = com.example.taskmate.home.fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1
                        )

                        Text(time, modifier = Modifier.constrainAs(timeText) {
                            end.linkTo(parent.end, margin = 15.dp)
                            top.linkTo(taskNameText.top)
                        }, fontFamily = com.example.taskmate.home.fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ShowNotificationScreen() {
    NotificationScreen()
}