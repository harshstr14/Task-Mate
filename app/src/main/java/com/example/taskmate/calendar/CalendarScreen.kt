package com.example.taskmate.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowMetricsCalculator
import com.example.taskmate.R
import com.example.taskmate.navigation.BottomNavRoute
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.text.toInt
import kotlin.toString

val fonts = FontFamily(
    Font(R.font.merriweathersans_bold, FontWeight.Bold),
    Font(R.font.merriweathersans_semibold, FontWeight.SemiBold),
    Font(R.font.merriweathersans_regular, FontWeight.Normal)
)
@Composable
fun CalendarScreen(navController: NavController) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var currentMonth by remember {
        mutableStateOf(YearMonth.now())
    }

    val today = LocalDate.now()

    val dates = remember(currentMonth) {
        (1..currentMonth.lengthOfMonth()).map { day ->
            currentMonth.atDay(day)
        }
    }

    var selectedIndex by remember {
        mutableIntStateOf(
            dates.indexOfFirst { it == today }.coerceAtLeast(0)
        )
    }

    val context = LocalContext.current
    val density = LocalDensity.current

    val windowMetrics = remember {
        WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(context)
    }

    val screenWidthPx = with(density) {
        windowMetrics.bounds.width().toDp().toPx()
    }

    val itemWidthPx = with(density) {
        (64.dp + 16.dp).toPx()
    }
    val centerOffset = (screenWidthPx / 2f) - (itemWidthPx / 2f) - (32/2f)

    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(text1,arrowLeft,arrowRight,addButton,dateButton,datesRow,categories,taskListsColumn) = createRefs()

        Text(currentMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", ${currentMonth.year}", modifier = Modifier.constrainAs(text1) {
            top.linkTo(parent.top, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.clickable {
            val now = YearMonth.now()
            currentMonth = now

            val todayIndex = dates.indexOfFirst { it == LocalDate.now() }
                .coerceAtLeast(0)

            selectedIndex = todayIndex

            coroutineScope.launch {
                listState.animateScrollToItem(
                    index = todayIndex,
                    scrollOffset = -centerOffset.toInt()
                )
            }
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Box(modifier = Modifier.constrainAs(arrowLeft) {
            top.linkTo(text1.top)
            bottom.linkTo(text1.bottom)
            end.linkTo(text1.start, margin = 2.dp)
        }.size(32.dp).clickable {
            currentMonth = currentMonth.minusMonths(1)
        }, contentAlignment = Alignment.Center) {
            Icon(modifier = Modifier.size(16.dp).rotate(90f), painter = painterResource(R.drawable.arrow),
                contentDescription = "arrowLeft", tint = Color(0xFF24252C))
        }

        Box(modifier = Modifier.constrainAs(arrowRight) {
            top.linkTo(text1.top)
            bottom.linkTo(text1.bottom)
            start.linkTo(text1.end, margin = 1.dp)
        }.size(32.dp).clickable {
            currentMonth = currentMonth.plusMonths(1)
        }, contentAlignment = Alignment.Center) {
            Icon(modifier = Modifier.size(16.dp).rotate(-90f), painter = painterResource(R.drawable.arrow),
                contentDescription = "arrowLeft", tint = Color(0xFF24252C))
        }

        Box(modifier = Modifier.constrainAs(addButton) {
                    top.linkTo(text1.top)
                    bottom.linkTo(text1.bottom)
                    end.linkTo(parent.end, margin = 20.dp)
                }.size(28.dp).clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF5F33E1))
                .clickable { navController.navigate(BottomNavRoute.AddTask.route) }
        ) {
            Icon(
                painter = painterResource(R.drawable.add_icon),
                contentDescription = "add Icon",
                tint = Color(0xFFEEE9FF),
                modifier = Modifier.align(Alignment.Center).size(22.dp)
            )
        }

        Box(modifier = Modifier.constrainAs(dateButton) {
            top.linkTo(text1.top)
            bottom.linkTo(text1.bottom)
            start.linkTo(parent.start, margin = 20.dp)
        }.size(28.dp).clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF5F33E1))
            .clickable { showDatePicker = true }
        ) {
            Icon(
                painter = painterResource(R.drawable.calendar),
                contentDescription = "add Icon",
                tint = Color(0xFFEEE9FF),
                modifier = Modifier.align(Alignment.Center).size(18.dp)
            )
        }

        LazyRow(state = listState, modifier = Modifier.constrainAs(datesRow) {
            top.linkTo(text1.bottom, margin = 25.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp))
        {
            itemsIndexed(dates) { index, date ->
                val isSelected = index == selectedIndex

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF5F33E1) else Color(0xFFFFFFFF),
                    animationSpec = tween(durationMillis = 150),
                    label = "bg"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF24252C),
                    animationSpec = tween(durationMillis = 150),
                    label = "text"
                )

                val elevation by animateDpAsState(
                    targetValue = if (isSelected) 12.dp else 0.dp,
                    animationSpec = tween(durationMillis = 150),
                    label = "elevation"
                )

                ElevatedCard(elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                ), colors = CardDefaults.cardColors(
                    containerColor = bgColor
                ), modifier = Modifier.size(64.dp,84.dp).shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFF5F33E1).copy(alpha = 0.2f),
                    spotColor = Color(0xFF5F33E1).copy(alpha = 0.4f)
                ).clickable{selectedIndex = index
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = index,
                            scrollOffset = -centerOffset.toInt()
                        )
                    } },shape = RoundedCornerShape(15.dp))
                {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (month, day, week) = createRefs()

                        Text(
                            text = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                            fontSize = 11.sp, lineHeight = 14.sp,
                            fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            color = textColor,
                            modifier = Modifier.constrainAs(month) {
                                top.linkTo(parent.top)
                                bottom.linkTo(day.top)
                                centerHorizontallyTo(parent)
                            }
                        )

                        Text(
                            text = date.dayOfMonth.toString(),
                            fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 19.sp, lineHeight = 22.sp,
                            color = textColor,
                            modifier = Modifier.constrainAs(day) {
                                centerTo(parent)
                            }
                        )

                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                            fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp,
                            color = textColor,
                            modifier = Modifier.constrainAs(week) {
                                bottom.linkTo(parent.bottom)
                                top.linkTo(day.bottom)
                                centerHorizontallyTo(parent)
                            }
                        )
                    }
                }
            }
        }

        val categoriesList = listOf(
            "All", "To Do", "In Progress", "Complete"
        )

        var selectedCategoryIndex by remember { mutableIntStateOf(0) }

        LazyRow(modifier = Modifier.constrainAs(categories) {
            top.linkTo(datesRow.bottom, margin = 28.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp))
        {
            itemsIndexed(categoriesList) { index, category ->
                val isSelected = index == selectedCategoryIndex

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF5F33E1) else Color(0xFFEDE8FF),
                    animationSpec = tween(durationMillis = 350),
                    label = "bg"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF5F33E1),
                    animationSpec = tween(durationMillis = 350),
                    label = "text"
                )

                val elevation by animateDpAsState(
                    targetValue = if (isSelected) 12.dp else 0.dp,
                    animationSpec = tween(durationMillis = 350),
                    label = "elevation"
                )

                ElevatedCard(elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                ), colors = CardDefaults.cardColors(
                    containerColor = bgColor
                ), modifier = Modifier.height(34.dp).shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(9.dp),
                    ambientColor = Color(0xFF5F33E1).copy(alpha = 0.2f),
                    spotColor = Color(0xFF5F33E1).copy(alpha = 0.4f)
                ).clickable{selectedCategoryIndex = index},shape = RoundedCornerShape(9.dp)) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(text1) = createRefs()

                        Text(category, modifier = Modifier.constrainAs(text1) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 20.dp)
                            end.linkTo(parent.end, margin = 20.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 13.sp, color = textColor
                        )
                    }
                }
            }
        }

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
        val taskLevels = listOf(
            "Done","In Progress","To Do","To Do"
        )
        val taskLevelsBG = listOf(
            Color(0xFFEDE8FF),
            Color(0xFFFFE9E1),
            Color(0xFFE3F2FF),
            Color(0xFFE3F2FF)
        )
        val taskLevelsColors = listOf(
            Color(0xFF5F33E1),
            Color(0xFFFF7D53),
            Color(0xFF0087FF),
            Color(0xFF0087FF)
        )
        val taskLists = listOf(
            "Grocery shopping app design",
            "Grocery shopping app design",
            "Uber Eats redesign challange",
            "About design sprint"
        )
        val taskListTopics = listOf(
            "Market Research",
            "Competitive Analysis",
            "Create Low-fidelity Wireframe",
            "How to pitch a Design Sprint"
        )

        LazyColumn(modifier = Modifier.constrainAs(taskListsColumn) {
            top.linkTo(categories.bottom, margin = 25.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom, margin = (-15).dp)
            height = Dimension.fillToConstraints
        },contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp))
        {
            items(4) { index ->
                val taskGroupIcon = taskGroupsIcons[index % taskGroupsIcons.size]
                val taskGroupIconColor = taskGroupsIconsColors[index % taskGroupsIconsColors.size]
                val taskLevel = taskLevels[index % taskLevels.size]
                val taskLevelColor = taskLevelsColors[index % taskLevelsColors.size]
                val taskLevelBG = taskLevelsBG[index % taskLevelsBG.size]
                val taskList = taskLists[index % taskLists.size]
                val taskListTopic = taskListTopics[index % taskListTopics.size]

                ElevatedCard(elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                ), colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFFFF)
                ), modifier = Modifier.padding(horizontal = 20.dp).height(94.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ),shape = RoundedCornerShape(15.dp)) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(text1,text2,clock,timeText,boxShape,boxShape2) = createRefs()

                        Text(taskList, modifier = Modifier.constrainAs(text1) {
                            top.linkTo(parent.top, margin = 5.dp)
                            bottom.linkTo(text2.top)
                            start.linkTo(parent.start)
                        }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1
                        )

                        Text(taskListTopic, modifier = Modifier.constrainAs(text2) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C), maxLines = 1
                        )

                        Image(modifier = Modifier.constrainAs(clock) {
                            bottom.linkTo(parent.bottom, margin = 14.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }.size(14.dp), painter = painterResource(R.drawable.clock), contentDescription = "clock Icon")

                        Text("12:00", modifier = Modifier.constrainAs(timeText) {
                            top.linkTo(clock.top)
                            start.linkTo(clock.end, margin = 2.dp)
                            bottom.linkTo(clock.bottom)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFFAB94FF), maxLines = 1
                        )

                        Box(modifier = Modifier.constrainAs(boxShape) {
                            top.linkTo(parent.top, margin = 15.dp)
                            end.linkTo(parent.end, margin = 15.dp)
                        }.size(34.dp).background(taskGroupIconColor,
                            shape = RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Image(modifier = Modifier.size(20.dp), painter = painterResource(taskGroupIcon), contentDescription = "briefcase")
                        }

                        ElevatedCard(elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ), colors = CardDefaults.cardColors(
                            containerColor = taskLevelBG
                        ), modifier = Modifier.constrainAs(boxShape2) {
                            bottom.linkTo(parent.bottom, margin = 15.dp)
                            end.linkTo(parent.end, margin = 15.dp)
                        }.shadow( elevation = 12.dp,
                            shape = RoundedCornerShape(7.dp),
                            ambientColor = taskLevelBG.copy(alpha = 0.2f),
                            spotColor = taskLevelBG.copy(alpha = 0.4f)
                        ),shape = RoundedCornerShape(7.dp)) {
                            Box(modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = taskLevel,
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp,
                                    color = taskLevelColor
                                )
                            }
                        }
                    }
                }
            }
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
        headlineSmall = androidx.compose.ui.text.TextStyle(
            fontSize = 20.sp,
            fontFamily = fonts,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        headlineLarge = androidx.compose.ui.text.TextStyle(
            fontSize = 26.sp,
            fontFamily = fonts,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        titleLarge = androidx.compose.ui.text.TextStyle(
            fontFamily = fonts,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),
        titleMedium = androidx.compose.ui.text.TextStyle(
            fontFamily = fonts,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),
        labelLarge = androidx.compose.ui.text.TextStyle(
            fontFamily = fonts,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        ),
        bodyLarge = androidx.compose.ui.text.TextStyle(
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

                            // 🔹 Update month
                            currentMonth = YearMonth.from(selectedDate)

                            // 🔹 Update selected index
                            selectedIndex = selectedDate.dayOfMonth - 1

                            coroutineScope.launch {
                                listState.animateScrollToItem(
                                    index = selectedIndex,
                                    scrollOffset = -centerOffset.toInt()
                                )
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

    LaunchedEffect(dates) {
        val todayIndex = dates.indexOfFirst { it == today }
        if (todayIndex != -1) {
            listState.animateScrollToItem(
                index = todayIndex,
                scrollOffset = -centerOffset.toInt()
            )
            selectedIndex = todayIndex
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ShowCalendarScreen() {
    val navController = rememberNavController()
    CalendarScreen(navController = navController)
}