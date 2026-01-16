package com.example.taskmate.updatetask

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.taskmate.R
import com.example.taskmate.addtask.DateType
import com.example.taskmate.home.TaskGroup
import com.example.taskmate.home.TaskPrefs
import com.example.taskmate.home.Tasks
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
@Composable
fun UpdateTaskScreen(snackbarHostState: SnackbarHostState, taskId: String?, taskGroup: String?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val taskList = remember(taskGroup) {
        when(taskGroup) {
            TaskGroup.WORK -> TaskPrefs.loadWorkTasks(context)
            TaskGroup.PERSONAL -> TaskPrefs.loadPersonalTasks(context)
            TaskGroup.STUDY -> TaskPrefs.loadStudyTasks(context)
            TaskGroup.DAILY_STUDY  -> TaskPrefs.loadDailyStudyTasks(context)
            else -> TaskPrefs.loadWorkTasks(context)
        }
    }

    val task = taskList.firstOrNull { it.id == taskId }
    Log.d("Tasks","$task")

    val fonts = FontFamily(
        Font(R.font.merriweathersans_bold, FontWeight.Bold),
        Font(R.font.merriweathersans_semibold, FontWeight.SemiBold),
        Font(R.font.merriweathersans_regular, FontWeight.Normal)
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var activeDateType by remember { mutableStateOf(DateType.START) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    var startDate by remember {  mutableStateOf(task?.startDate?.let {
        LocalDate.parse(it, formatter)
    }) }
    var endDate by remember {  mutableStateOf(task?.endDate?.let {
        LocalDate.parse(it, formatter)
    }) }
    var dateError by remember { mutableStateOf(false) }

    var startDateText by remember { mutableStateOf(task?.startDate ?: "Select date") }
    var endDateText by remember { mutableStateOf(task?.endDate ?: "Select date") }

    var expanded by remember { mutableStateOf(false) }
    var expandedDates by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf(task?.taskGroup ?: "Work") }
    var selectedGroupBG by remember { mutableStateOf(Color(0xFFFFE4F2)) }
    var selectedGroupIcon by remember { mutableIntStateOf(task?.icon ?: R.drawable.briefcase) }

    val rotationArrow by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )
    val rotationArrow2 by animateFloatAsState(
        targetValue = if (expandedDates) 180f else 0f,
        label = "arrowRotation"
    )

    var completedDates by remember {
        mutableStateOf(task?.completedDates ?: emptyList())
    }

    var taskGroupName by remember { mutableStateOf(task?.taskGroupName ?: "") }
    var taskName by remember { mutableStateOf(task?.taskName ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(column,text1,box1,box2,addButton,taskGroup,taskDates) = createRefs()

        Text("Task", modifier = Modifier.constrainAs(text1) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(box1) {
            top.linkTo(text1.bottom, margin = 25.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(15.dp),
            ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
            spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
        ).background(Color(0xFFFFFFFF),
            shape = RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val(boxShape,text1,text2,arrowDown) = createRefs()

                Box(modifier = Modifier.constrainAs(boxShape) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 15.dp)
                }.size(34.dp).background(selectedGroupBG,
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Image(modifier = Modifier.size(20.dp), painter = painterResource(selectedGroupIcon), contentDescription = "briefcase")
                }

                Text("Task Group", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 13.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                Text(selectedGroup, modifier = Modifier.constrainAs(text2) {
                    top.linkTo(text1.bottom, margin = 7.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                )

                Box(modifier = Modifier.constrainAs(arrowDown) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 15.dp)
                }.size(32.dp).clip(RoundedCornerShape(10.dp)).clickable {
                    expanded = !expanded
                }, contentAlignment = Alignment.Center) {
                    Icon(modifier = Modifier.size(12.dp).rotate(rotationArrow), painter = painterResource(R.drawable.arrow),
                        contentDescription = "arrowLeft", tint = Color(0xFF24252C)
                    )
                }
            }
        }

        Box(modifier = Modifier.constrainAs(box2) {
            top.linkTo(box1.bottom, margin = 25.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(15.dp),
            ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
            spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
        ).background(Color(0xFFFFFFFF),
            shape = RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val(boxShape,text1,text2,arrowDown) = createRefs()

                Box(modifier = Modifier.constrainAs(boxShape) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 15.dp)
                }.size(34.dp).background(Color(0xFFEDE8FF),
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.task_icon),
                        contentDescription = "briefcase", tint = Color(0xFF5F33E1)
                    )
                }

                Text("Task Progress", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 13.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                Text("Mark Completed Days", modifier = Modifier.constrainAs(text2) {
                    top.linkTo(text1.bottom, margin = 7.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                )

                Box(modifier = Modifier.constrainAs(arrowDown) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 15.dp)
                }.size(32.dp).clip(RoundedCornerShape(10.dp)).clickable {
                    expandedDates = !expandedDates
                }, contentAlignment = Alignment.Center) {
                    Icon(modifier = Modifier.size(12.dp).rotate(rotationArrow2), painter = painterResource(R.drawable.arrow),
                        contentDescription = "arrowLeft", tint = Color(0xFF24252C)
                    )
                }
            }
        }

        val taskGroups = listOf(
            TaskGroup.WORK,
            TaskGroup.PERSONAL,
            TaskGroup.STUDY,
            TaskGroup.DAILY_STUDY
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

        AnimatedVisibility( modifier = Modifier.constrainAs(taskGroup) {
            top.linkTo(box1.bottom)
            start.linkTo(box1.start)
            end.linkTo(box1.end)
        }.zIndex(10f),
            visible = expanded,
            enter = fadeIn() + slideInVertically { -it / 4 },
            exit = fadeOut() + slideOutVertically { -it / 4 }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth().shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(15.dp),
                        ambientColor = Color(0xFFF5F5F5).copy(alpha = 0.2f),
                        spotColor = Color(0xFFF5F5F5).copy(alpha = 0.4f)
                    ).background(Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp))
                {
                    items(4) { index ->
                        val taskGroupIcon = taskGroupsIcons[index % taskGroupsIcons.size]
                        val taskGroupIconColor = taskGroupsIconsColors[index % taskGroupsIconsColors.size]
                        val taskGroup = taskGroups[index % taskGroups.size]
                        val isSelected = taskGroup == selectedGroup

                        ElevatedCard(
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF5F33E1) else Color(0xFFFFFFFF)
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp)
                                .fillMaxWidth().shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(15.dp),
                                    ambientColor = if (isSelected) Color(0xFF5F33E1) else Color(0xFFFFFFFF).copy(alpha = 0.2f),
                                    spotColor = if (isSelected) Color(0xFF5F33E1) else Color(0xFFFFFFFF).copy(alpha = 0.4f)
                                ).clickable {
                                    selectedGroup = taskGroup
                                    selectedGroupBG = taskGroupIconColor
                                    selectedGroupIcon = taskGroupIcon
                                    expanded = false }, shape = RoundedCornerShape(15.dp)
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                                val (boxShape, text1) = createRefs()

                                Box(
                                    modifier = Modifier.constrainAs(boxShape) {
                                        top.linkTo(parent.top, margin = 8.dp)
                                        start.linkTo(parent.start, margin = 10.dp)
                                        bottom.linkTo(parent.bottom, margin = 8.dp)
                                    }.size(34.dp).background(
                                        taskGroupIconColor,
                                        shape = RoundedCornerShape(9.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(taskGroupIcon),
                                        contentDescription = "briefcase"
                                    )
                                }

                                Text(taskGroup, modifier = Modifier.constrainAs(text1) {
                                    top.linkTo(boxShape.top)
                                    bottom.linkTo(boxShape.bottom)
                                    start.linkTo(boxShape.end, margin = 10.dp)
                                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF24252C)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility( modifier = Modifier.constrainAs(taskDates) {
            top.linkTo(box2.bottom)
            start.linkTo(box2.start)
            end.linkTo(box2.end)
        }.zIndex(10f),
            visible = expandedDates,
            enter = fadeIn() + slideInVertically { -it / 4 },
            exit = fadeOut() + slideOutVertically { -it / 4 }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth().shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(15.dp),
                        ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                        spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                    ).background(Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                val dates = remember(startDate, endDate) {
                    if (startDate != null && endDate != null) {
                        getDatesBetween(
                            startDate!!, endDate!!
                        )
                    } else emptyList()
                }

                val completedDatesSet = remember(completedDates) {
                    completedDates.mapNotNull {
                        runCatching { LocalDate.parse(it, formatter) }.getOrNull()
                    }.toSet()
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dates) { date ->
                        val isCompleted = completedDatesSet.contains(date)

                        val background = when {
                            isCompleted -> Color(0xFF5F33E1)
                            else -> Color(0xFFFFFFFF)
                        }

                        val textColor = when {
                            isCompleted -> Color(0xFFFFFFFF)
                            else -> Color(0xFF24252C)
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = background.copy(alpha = 0.2f),
                                    spotColor = background.copy(alpha = 0.4f)
                                )
                                .background(background, RoundedCornerShape(10.dp))
                                .clickable { task?.let {
                                    completedDates = toggleCompletedDate(
                                        completedDates = completedDates,
                                        date = date
                                    )
                                }}
                            ,
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.constrainAs(column) {
            top.linkTo(box2.bottom, margin = (-15).dp)
            bottom.linkTo(addButton.top, margin = (-15).dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            height = Dimension.fillToConstraints
        }.verticalScroll(rememberScrollState())) {
            ConstraintLayout {
                val(box2,box3,box4,box5,box6) = createRefs()

                Box(modifier = Modifier.constrainAs(box2) {
                    top.linkTo(parent.top, margin = 35.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(text1,text2,text3) = createRefs()

                        Text("Task Group Name", modifier = Modifier.constrainAs(text1) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (taskGroupName.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(text3) {
                                top.linkTo(text1.bottom, margin = 8.dp)
                                start.linkTo(parent.start, margin = 15.dp) },
                                text = "Enter Task Group Name",
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 14.sp, lineHeight = 17.sp,
                                color = Color(0xFF24252C)
                            )
                        }

                        val selectionColors = TextSelectionColors(
                            handleColor = Color(0xFFAB94FF),
                            backgroundColor = Color(0xFFAB94FF).copy(alpha = 0.3f)
                        )

                        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                            BasicTextField(
                                value = taskGroupName,
                                onValueChange = { taskGroupName = it },
                                modifier = Modifier
                                    .constrainAs(text2) {
                                        top.linkTo(text1.bottom, margin = 8.dp)
                                        start.linkTo(parent.start, margin = 15.dp)
                                    }
                                    .fillMaxWidth()
                                    .padding(end = 28.dp),
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp,
                                    color = Color(0xFF24252C)
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF6A5AE0))
                            )
                        }
                    }
                }

                Box(modifier = Modifier.constrainAs(box6) {
                    top.linkTo(box2.bottom, margin = 25.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(text1,text2,text3) = createRefs()

                        Text("Task Name", modifier = Modifier.constrainAs(text1) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (taskName.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(text3) {
                                top.linkTo(text1.bottom, margin = 8.dp)
                                start.linkTo(parent.start, margin = 15.dp) },
                                text = "Enter Task Name",
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 14.sp, lineHeight = 17.sp,
                                color = Color(0xFF24252C)
                            )
                        }

                        val selectionColors = TextSelectionColors(
                            handleColor = Color(0xFFAB94FF),
                            backgroundColor = Color(0xFFAB94FF).copy(alpha = 0.3f)
                        )

                        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                            BasicTextField(
                                value = taskName,
                                onValueChange = { taskName = it },
                                modifier = Modifier
                                    .constrainAs(text2) {
                                        top.linkTo(text1.bottom, margin = 8.dp)
                                        start.linkTo(parent.start, margin = 15.dp)
                                    }
                                    .fillMaxWidth()
                                    .padding(end = 28.dp),
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp,
                                    color = Color(0xFF24252C)
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF6A5AE0))
                            )
                        }
                    }
                }

                Box(modifier = Modifier.constrainAs(box3) {
                    top.linkTo(box6.bottom, margin = 25.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(142.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(text1,text2,text3) = createRefs()

                        Text("Description", modifier = Modifier.constrainAs(text1) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (description.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(text3) {
                                top.linkTo(text1.bottom, margin = 8.dp)
                                start.linkTo(parent.start, margin = 15.dp) },
                                text = "Enter Task Description",
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 11.sp,
                                color = Color(0xFF24252C)
                            )
                        }

                        val selectionColors = TextSelectionColors(
                            handleColor = Color(0xFFAB94FF),
                            backgroundColor = Color(0xFFAB94FF).copy(alpha = 0.3f)
                        )

                        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier
                                    .constrainAs(text2) {
                                        top.linkTo(text1.bottom)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                    }
                                    .fillMaxWidth().fillMaxHeight()
                                    .padding(horizontal = 15.dp, vertical = 17.dp),
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 11.sp,
                                    color = Color(0xFF24252C)
                                ),
                                singleLine = false,
                                cursorBrush = SolidColor(Color(0xFF6A5AE0))
                            )
                        }
                    }
                }

                Box(modifier = Modifier.constrainAs(box4) {
                    top.linkTo(box3.bottom, margin = 25.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(calendar,text1,text2,arrowDown) = createRefs()

                        Icon(painterResource(R.drawable.calendar), contentDescription = "calendar Icon",
                            modifier = Modifier.constrainAs(calendar) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                            }, tint = Color(0xFF5F33E1)
                        )

                        Text("Start Date", modifier = Modifier.constrainAs(text1) {
                            top.linkTo(parent.top, margin = 13.dp)
                            start.linkTo(calendar.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        Text(startDateText, modifier = Modifier.constrainAs(text2) {
                            top.linkTo(text1.bottom, margin = 7.dp)
                            start.linkTo(calendar.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                        )

                        Box(modifier = Modifier.constrainAs(arrowDown) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, margin = 15.dp)
                        }.size(32.dp).clip(RoundedCornerShape(10.dp)).clickable {
                            activeDateType = DateType.START
                            showDatePicker = true
                        }, contentAlignment = Alignment.Center) {
                            Icon(modifier = Modifier.size(12.dp), painter = painterResource(R.drawable.arrow),
                                contentDescription = "arrowLeft", tint = Color(0xFF24252C))
                        }
                    }
                }

                Box(modifier = Modifier.constrainAs(box5) {
                    top.linkTo(box4.bottom, margin = 25.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 25.dp)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (calendar, text1, text2, arrowDown) = createRefs()

                        Icon(
                            painterResource(R.drawable.calendar), contentDescription = "calendar Icon",
                            modifier = Modifier.constrainAs(calendar) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                            }, tint = Color(0xFF5F33E1)
                        )

                        Text("End Date",
                            modifier = Modifier.constrainAs(text1) {
                                top.linkTo(parent.top, margin = 13.dp)
                                start.linkTo(calendar.end, margin = 10.dp)
                            },
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp,
                            color = Color(0xFF6E6A7C)
                        )

                        Text(text = if (dateError) "End date is before start date" else endDateText,
                            color = if (dateError)
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF24252C),
                            modifier = Modifier.constrainAs(text2) {
                                top.linkTo(text1.bottom, margin = 7.dp)
                                start.linkTo(calendar.end, margin = 10.dp)
                            },
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp
                        )

                        Box(modifier = Modifier.constrainAs(arrowDown) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, margin = 15.dp)
                        }.size(32.dp).clip(RoundedCornerShape(10.dp)).clickable {
                            activeDateType = DateType.END
                            showDatePicker = true
                        }, contentAlignment = Alignment.Center) {
                            Icon(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(R.drawable.arrow),
                                contentDescription = "arrowLeft",
                                tint = Color(0xFF24252C)
                            )
                        }
                    }
                }
            }
        }

        Button(modifier = Modifier.constrainAs(addButton) {
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom, margin = 15.dp)
        }.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
            onClick = {
                if (taskName.isBlank() || taskGroupName.isBlank() ||
                    description.isBlank() || startDateText == "Select date" || endDateText == "Select date" ||
                    startDate == null || endDate == null || dateError) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Please fill all fields",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@Button
                }

                val updatedTask = Tasks(
                    id = task?.id ?: return@Button,
                    time = System.currentTimeMillis(),
                    taskGroup = selectedGroup,
                    taskGroupName = taskGroupName,
                    taskName = taskName,
                    description = description,
                    startDate = startDateText,
                    endDate = endDateText,
                    completedDates = completedDates,
                    icon = selectedGroupIcon,
                    iconBg = selectedGroupBG.value.toLong(),
                    progress = task.progress,
                    progressStatus = task.progressStatus
                )

                val oldGroup = task.taskGroup

                if (oldGroup != selectedGroup) {
                    // group changed
                    removeTaskFromOldGroup(context, oldGroup, updatedTask.id)
                    addTaskToNewGroup(context, selectedGroup, updatedTask)
                } else {
                    when (selectedGroup) {
                        TaskGroup.WORK ->  TaskPrefs.saveWorkTasks(context, updatedTask)
                        TaskGroup.PERSONAL ->  TaskPrefs.savePersonalTasks(context, updatedTask)
                        TaskGroup.STUDY -> TaskPrefs.saveStudyTasks(context, updatedTask)
                        TaskGroup.DAILY_STUDY ->  TaskPrefs.saveDailyStudyTasks(context, updatedTask)
                    }
                }

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Task updated successfully",
                        duration = SnackbarDuration.Short
                    )
                }
            }, colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5F33E1),
                contentColor = Color(0xFFFFFFFF)
            ) , shape = RoundedCornerShape(10.dp)) {

            Text("Update Task", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal, fontSize = 18.sp
            )
        }
    }

    val customDatePickerColors = DatePickerDefaults.colors(
        containerColor = Color(0xFFEEE9FF),

        titleContentColor = Color(0xFF24252C),
        headlineContentColor = Color(0xFF5F33E1),
        navigationContentColor = Color(0xFFAB94FF),

        weekdayContentColor = Color(0xFF6E6A7C),

        dayContentColor = Color(0xFF24252C),
        disabledDayContentColor = Color(0xFF6E6A7C),

        selectedDayContainerColor = Color(0xFF5F33E1),
        selectedDayContentColor = Color(0xFFFFFFFF),

        todayContentColor = Color(0xFF5F33E1),
        todayDateBorderColor = Color(0xFFAB94FF),

        yearContentColor = Color(0xFF24252C),
        selectedYearContainerColor = Color(0xFF5F33E1),
        selectedYearContentColor = Color(0xFFFFFFFF)
    )
    val datePickerTypography = Typography(
        headlineSmall = TextStyle(
            fontSize = 20.sp,
            fontFamily = fonts,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        headlineLarge = TextStyle(
            fontSize = 26.sp,
            fontFamily = fonts,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        titleLarge = TextStyle(
            fontFamily = fonts,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fonts,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fonts,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fonts,
            fontWeight = FontWeight.Medium
        )
    )

    if (showDatePicker) {
        MaterialTheme(typography = datePickerTypography) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedDate = Instant
                                .ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            when (activeDateType) {
                                DateType.START -> {
                                    startDate = selectedDate
                                    startDateText = selectedDate.format(formatter)
                                    completedDates = filterCompletedDates(
                                        completedDates,
                                        startDate,
                                        endDate
                                    )

                                    // Optional: clear end date if invalid
                                    if (endDate != null && selectedDate.isAfter(endDate)) {
                                        endDate = null
                                        endDateText = "End date"
                                    }
                                    dateError = false
                                }

                                DateType.END -> {
                                    if (startDate != null && selectedDate.isBefore(startDate)) {
                                        dateError = true
                                    } else {
                                        // ✅ VALID
                                        endDate = selectedDate
                                        endDateText = selectedDate.format(formatter)
                                        completedDates = filterCompletedDates(
                                            completedDates,
                                            startDate,
                                            endDate
                                        )
                                        dateError = false
                                    }
                                }
                            }
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal, color = Color(0xFFAB94FF)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {   showDatePicker = false }) {
                        Text("Cancel", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal, color = Color(0xFFAB94FF)
                        )
                    }
                }, colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFFEEE9FF)
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = customDatePickerColors
                )
            }
        }
    }

    LaunchedEffect(task) {
        task?.iconBg?.let { bg ->
            selectedGroupBG = Color(bg.toULong())
        }
    }
}

private fun filterCompletedDates(completedDates: List<String>, start: LocalDate?, end: LocalDate?): List<String> {
    if (start == null || end == null) return emptyList()

    return completedDates.filter {
        val date = LocalDate.parse(it, formatter)
        !date.isBefore(start) && !date.isAfter(end)
    }
}

private fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {

    val dates = mutableListOf<LocalDate>()
    var current = startDate

    while (!current.isAfter(endDate)) {
        dates.add(current)
        current = current.plusDays(1)
    }
    return dates
}

private fun toggleCompletedDate(completedDates: List<String>, date: LocalDate): List<String> {

    val dateStr = date.format(formatter)
    val updated = completedDates.toMutableSet()

    if (updated.contains(dateStr)) {
        updated.remove(dateStr)
    } else {
        updated.add(dateStr)
    }

    return updated.toList()
}

private fun addTaskToNewGroup(context: Context, group: String, task: Tasks) {
    when (group) {
        TaskGroup.WORK -> TaskPrefs.saveWorkTasks(context, task)
        TaskGroup.PERSONAL -> TaskPrefs.savePersonalTasks(context, task)
        TaskGroup.STUDY -> TaskPrefs.saveStudyTasks(context, task)
        TaskGroup.DAILY_STUDY -> TaskPrefs.saveDailyStudyTasks(context, task)
    }
}

private fun removeTaskFromOldGroup(context: Context, oldGroup: String, taskId: String) {
    when (oldGroup) {
        TaskGroup.WORK -> TaskPrefs.removeWorkTask(context, taskId)
        TaskGroup.PERSONAL -> TaskPrefs.removePersonalTasks(context, taskId)
        TaskGroup.STUDY -> TaskPrefs.removeStudyTasks(context, taskId)
        TaskGroup.DAILY_STUDY -> TaskPrefs.removeDailyStudyTasks(context, taskId)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ShowAddTask() {
    val snackbarHostState = SnackbarHostState()
    UpdateTaskScreen(snackbarHostState, "10002",TaskGroup.WORK)
}