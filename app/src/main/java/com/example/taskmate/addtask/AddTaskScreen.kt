package com.example.taskmate.addtask

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.taskmate.home.TaskGroup
import com.example.taskmate.home.TaskPrefs
import com.example.taskmate.home.Tasks
import com.example.taskmate.notification.scheduleTaskEndDateNotification
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddTaskScreen(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var dateError by remember { mutableStateOf(false) }

    var startDateText by remember { mutableStateOf("Select date") }
    var endDateText by remember { mutableStateOf("Select date") }

    var expanded by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf("Work") }
    var selectedGroupBG by remember { mutableStateOf(Color(0xFFFFE4F2)) }
    var selectedGroupIcon by remember { mutableIntStateOf(R.drawable.briefcase) }

    val rotationArrow by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    var taskGroupName by remember { mutableStateOf("") }
    var taskName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (taskColumn, headerText, taskGroupBox, addButtonRef, taskGroupsDropdown) = createRefs()

        Text("Add Task", modifier = Modifier.constrainAs(headerText) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(taskGroupBox) {
            top.linkTo(headerText.bottom, margin = 25.dp)
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
                val (iconBox, labelText, groupNameText, arrowIcon) = createRefs()

                Box(modifier = Modifier.constrainAs(iconBox) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 15.dp)
                }.size(34.dp).background(selectedGroupBG,
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Image(modifier = Modifier.size(20.dp), painter = painterResource(selectedGroupIcon), contentDescription = "briefcase")
                }

                Text("Task Group", modifier = Modifier.constrainAs(labelText) {
                    top.linkTo(parent.top, margin = 13.dp)
                    start.linkTo(iconBox.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                Text(selectedGroup, modifier = Modifier.constrainAs(groupNameText) {
                    top.linkTo(labelText.bottom, margin = 7.dp)
                    start.linkTo(iconBox.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                )

                Box(modifier = Modifier.constrainAs(arrowIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 15.dp)
                }.size(32.dp).clip(RoundedCornerShape(10.dp)).clickable {
                    expanded = !expanded
                }, contentAlignment = Alignment.Center) {
                    Icon(modifier = Modifier.size(12.dp).rotate(rotationArrow), painter = painterResource(R.drawable.arrow),
                        contentDescription = "arrowLeft", tint = Color(0xFF24252C))
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

        AnimatedVisibility( modifier = Modifier.constrainAs(taskGroupsDropdown) {
            top.linkTo(taskGroupBox.bottom)
            start.linkTo(taskGroupBox.start)
            end.linkTo(taskGroupBox.end)
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
                                .fillMaxWidth().clickable {
                                    selectedGroup = taskGroup
                                    selectedGroupBG = taskGroupIconColor
                                    selectedGroupIcon = taskGroupIcon
                                    expanded = false }, shape = RoundedCornerShape(15.dp)
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val (iconBox, groupNameText) = createRefs()

                                Box(
                                    modifier = Modifier.constrainAs(iconBox) {
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

                                Text(taskGroup, modifier = Modifier.constrainAs(groupNameText) {
                                    top.linkTo(iconBox.top)
                                    bottom.linkTo(iconBox.bottom)
                                    start.linkTo(iconBox.end, margin = 10.dp)
                                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF24252C)
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.constrainAs(taskColumn) {
            top.linkTo(taskGroupBox.bottom, margin = 12.dp)
            bottom.linkTo(addButtonRef.top, margin = (-15).dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            height = Dimension.fillToConstraints
        }.verticalScroll(rememberScrollState())) {
            ConstraintLayout {
                val(taskGroupNameBox, taskNameBox, descriptionBox, startDateBox, endDateBox) = createRefs()

                Box(modifier = Modifier.constrainAs(taskGroupNameBox) {
                    top.linkTo(parent.top, margin = 12.dp)
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
                        val(labelText, inputText, placeholderText) = createRefs()

                        Text("Task Group Name", modifier = Modifier.constrainAs(labelText) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (taskGroupName.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(placeholderText) {
                                        top.linkTo(labelText.bottom, margin = 8.dp)
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
                                    .constrainAs(inputText) {
                                        top.linkTo(labelText.bottom, margin = 8.dp)
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

                Box(modifier = Modifier.constrainAs(taskNameBox) {
                    top.linkTo(taskGroupNameBox.bottom, margin = 25.dp)
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
                        val(labelText, inputText, placeholderText) = createRefs()

                        Text("Task Name", modifier = Modifier.constrainAs(labelText) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (taskName.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(placeholderText) {
                                top.linkTo(labelText.bottom, margin = 8.dp)
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
                                    .constrainAs(inputText) {
                                        top.linkTo(labelText.bottom, margin = 8.dp)
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

                Box(modifier = Modifier.constrainAs(descriptionBox) {
                    top.linkTo(taskNameBox.bottom, margin = 25.dp)
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
                        val(labelText, inputText, placeholderText) = createRefs()

                        Text("Description", modifier = Modifier.constrainAs(labelText) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (description.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(placeholderText) {
                                top.linkTo(labelText.bottom, margin = 8.dp)
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
                                    .constrainAs(inputText) {
                                        top.linkTo(labelText.bottom)
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

                Box(modifier = Modifier.constrainAs(startDateBox) {
                    top.linkTo(descriptionBox.bottom, margin = 25.dp)
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
                        val(labelText,inputText,calendarIcon,dropdownIcon) = createRefs()

                        Icon(painterResource(R.drawable.calendar), contentDescription = "calendar Icon",
                            modifier = Modifier.constrainAs(calendarIcon) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                            }, tint = Color(0xFF5F33E1)
                        )

                        Text("Start Date", modifier = Modifier.constrainAs(labelText) {
                            top.linkTo(parent.top, margin = 13.dp)
                            start.linkTo(calendarIcon.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        Text(startDateText, modifier = Modifier.constrainAs(inputText) {
                            top.linkTo(labelText.bottom, margin = 7.dp)
                            start.linkTo(calendarIcon.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                        )

                        Box(modifier = Modifier.constrainAs(dropdownIcon) {
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

                Box(modifier = Modifier.constrainAs(endDateBox) {
                    top.linkTo(startDateBox.bottom, margin = 25.dp)
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
                        val(labelText,inputText,calendarIcon,dropdownIcon) = createRefs()

                        Icon(
                            painterResource(R.drawable.calendar), contentDescription = "calendar Icon",
                            modifier = Modifier.constrainAs(calendarIcon) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                            }, tint = Color(0xFF5F33E1)
                        )

                        Text("End Date",
                            modifier = Modifier.constrainAs(labelText) {
                                top.linkTo(parent.top, margin = 13.dp)
                                start.linkTo(calendarIcon.end, margin = 10.dp)
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
                            modifier = Modifier.constrainAs(inputText) {
                                top.linkTo(labelText.bottom, margin = 7.dp)
                                start.linkTo(calendarIcon.end, margin = 10.dp)
                            },
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp
                        )

                        Box(modifier = Modifier.constrainAs(dropdownIcon) {
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

        Button(modifier = Modifier.constrainAs(addButtonRef) {
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom, margin = 15.dp)
        }.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp).shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(10.dp),
            ambientColor = Color(0xFF5F33E1).copy(alpha = 0.2f),
            spotColor = Color(0xFF5F33E1).copy(alpha = 0.4f)
        ),onClick = {
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

            val now = System.currentTimeMillis()
            val endAtMillis = endDate!!
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val task = Tasks(
                    id = System.currentTimeMillis().toString(),
                    createdAt = now,
                    updatedAt = now,
                    endAt = endAtMillis,
                    taskGroup = selectedGroup,
                    taskGroupName = taskGroupName,
                    taskName = taskName,
                    description = description,
                    startDate = startDateText,
                    endDate = endDateText,
                    completedDates = emptyList(),
                    icon = selectedGroupIcon,
                    iconBg = selectedGroupBG.value.toLong(),
                    progress = 0,
                    progressStatus = "To Do"
                )

                scope.launch {
                    when(selectedGroup) {
                        TaskGroup.WORK  ->  TaskPrefs.saveWorkTask(context,task)
                        TaskGroup.PERSONAL ->  TaskPrefs.savePersonalTask(context,task)
                        TaskGroup.STUDY -> TaskPrefs.saveStudyTask(context,task)
                        TaskGroup.DAILY_STUDY ->  TaskPrefs.saveDailyStudyTask(context,task)
                    }

                    scheduleTaskEndDateNotification(context, task)
                }

                clearFields(
                    setTaskGroupName = { taskGroupName = it },
                    setTaskName = { taskName = it },
                    setDescription = { description = it },
                    setStartDate = { startDate = it },
                    setEndDate = { endDate = it },
                    setStartDateText = { startDateText = it },
                    setEndDateText = { endDateText = it },
                    setDateError = { dateError = it }
                )

                scope.launch { 
                    snackbarHostState.showSnackbar(
                        message = "Task added successfully",
                        duration = SnackbarDuration.Short
                    )
                }
            }, colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5F33E1),
                contentColor = Color(0xFFFFFFFF)
            ) , shape = RoundedCornerShape(10.dp)) {

            Text("Add Task", fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                fontSize = 18.sp
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
                                    startDateText = selectedDate.format(
                                        DateTimeFormatter.ofPattern("dd MMM yyyy")
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
                                        endDateText = selectedDate.format(
                                            DateTimeFormatter.ofPattern("dd MMM yyyy")
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
                    TextButton(onClick = { showDatePicker = false }) {
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
}
private fun clearFields(
    setTaskGroupName: (String) -> Unit,
    setTaskName: (String) -> Unit,
    setDescription: (String) -> Unit,
    setStartDate: (LocalDate?) -> Unit,
    setEndDate: (LocalDate?) -> Unit,
    setStartDateText: (String) -> Unit,
    setEndDateText: (String) -> Unit,
    setDateError: (Boolean) -> Unit
) {
    setTaskGroupName("")
    setTaskName("")
    setDescription("")

    setStartDate(null)
    setEndDate(null)
    setDateError(false)

    setStartDateText("Select date")
    setEndDateText("Select date")
}

@Preview(showSystemUi = true)
@Composable
private fun ShowAddTask() {
    val snackbarHostState = SnackbarHostState()
    AddTaskScreen(snackbarHostState)
}