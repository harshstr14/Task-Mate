package com.example.taskmate.addtask

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.taskmate.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddTaskScreen() {
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

    var startDateText by remember {
        mutableStateOf("Select date")
    }
    var endDateText by remember {
        mutableStateOf("Select date")
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(text1,box1,box2,box3,box4,box5,addButton) = createRefs()

        Text("Add Task", modifier = Modifier.constrainAs(text1) {
            top.linkTo(parent.top, margin = 25.dp)
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
                }.size(34.dp).background(Color(0xFFFFE4F2),
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Image(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.briefcase), contentDescription = "briefcase")
                }

                Text("Task Group", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 9.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, color = Color(0xFF6E6A7C)
                )

                Text("Work", modifier = Modifier.constrainAs(text2) {
                    top.linkTo(text1.bottom)
                    bottom.linkTo(boxShape.bottom)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, color = Color(0xFF24252C)
                )

                Box(modifier = Modifier.constrainAs(arrowDown) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 15.dp)
                }.size(32.dp).clip(RoundedCornerShape(10.dp)).clickable {

                }, contentAlignment = Alignment.Center) {
                    Icon(modifier = Modifier.size(12.dp), painter = painterResource(R.drawable.arrow),
                        contentDescription = "arrowLeft", tint = Color(0xFF24252C))
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
                val(text1,text2) = createRefs()

                Text("Task Name", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 9.dp)
                    start.linkTo(parent.start, margin = 15.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, color = Color(0xFF6E6A7C)
                )

                Text("Grocery Shopping App", modifier = Modifier.constrainAs(text2) {
                    bottom.linkTo(parent.bottom, margin = 9.dp)
                    start.linkTo(parent.start, margin = 15.dp)
                }.fillMaxWidth().padding(end = 28.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, color = Color(0xFF24252C), maxLines = 1
                )
            }
        }

        Box(modifier = Modifier.constrainAs(box3) {
            top.linkTo(box2.bottom, margin = 25.dp)
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
                val(text1,text2) = createRefs()

                Text("Description", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 9.dp)
                    start.linkTo(parent.start, margin = 15.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, color = Color(0xFF6E6A7C)
                )

                Text("This application is designed for super shops. By using this application they can enlist all their products in one place and can deliver. Customers will get a one-stop solution for their daily shopping.", modifier = Modifier.constrainAs(text2) {
                    top.linkTo(text1.bottom)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }.fillMaxWidth().fillMaxHeight().padding(horizontal = 15.dp, vertical = 17.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 11.sp, color = Color(0xFF24252C)
                )
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
                    top.linkTo(parent.top, margin = 9.dp)
                    start.linkTo(calendar.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, color = Color(0xFF6E6A7C)
                )

                Text(startDateText, modifier = Modifier.constrainAs(text2) {
                    top.linkTo(text1.bottom)
                    bottom.linkTo(calendar.bottom)
                    start.linkTo(calendar.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 14.sp, color = Color(0xFF24252C)
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
                        top.linkTo(parent.top, margin = 9.dp)
                        start.linkTo(calendar.end, margin = 10.dp)
                    },
                    fontFamily = fonts,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    fontSize = 9.sp,
                    color = Color(0xFF6E6A7C)
                )

                Text(text = if (dateError) "End date is before start date" else endDateText,
                    color = if (dateError)
                        MaterialTheme.colorScheme.error
                    else
                        Color(0xFF24252C),
                    modifier = Modifier.constrainAs(text2) {
                        top.linkTo(text1.bottom)
                        bottom.linkTo(calendar.bottom)
                        start.linkTo(calendar.end, margin = 10.dp)
                    },
                    fontFamily = fonts,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    fontSize = 14.sp,
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

        Button(modifier = Modifier.constrainAs(addButton) {
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom, margin = 20.dp)
        }.fillMaxWidth().padding(horizontal = 25.dp).height(52.dp),
            onClick = {

            }, colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5F33E1),
                contentColor = Color(0xFFFFFFFF)
            ) , shape = RoundedCornerShape(10.dp)) {

            Text("Add Task", fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                fontSize = 18.sp)
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
                            val selectedDate = LocalDate.ofEpochDay(
                                selectedMillis / (24 * 60 * 60 * 1000)
                            )

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

@Preview(showSystemUi = true)
@Composable
private fun ShowAddTask() {
    AddTaskScreen()
}