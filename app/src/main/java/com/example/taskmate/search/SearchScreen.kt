package com.example.taskmate.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.constraintlayout.compose.Dimension
import com.example.taskmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val fonts = FontFamily(
        Font(R.font.merriweathersans_bold, FontWeight.Bold),
        Font(R.font.merriweathersans_semibold, FontWeight.SemiBold),
        Font(R.font.merriweathersans_regular, FontWeight.Normal)
    )

    var searchText by remember { mutableStateOf("") }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(text1,search,searchResult) = createRefs()

        Text("Search", modifier = Modifier.constrainAs(text1) {
            top.linkTo(parent.top, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        SearchBar(fonts,
            modifier = Modifier.constrainAs(search) {
                top.linkTo(text1.bottom, margin = 25.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)

            }.fillMaxWidth().padding(horizontal = 20.dp),
            query = searchText,
            onQueryChange = { searchText = it }
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
        val taskLevels = listOf(
            "Done","In Progress","In Progress","To Do"
        )
        val taskLevelsBG = listOf(
            Color(0xFFEDE8FF),
            Color(0xFFFFE9E1),
            Color(0xFFFFE9E1),
            Color(0xFFE3F2FF)
        )
        val taskLevelsColors = listOf(
            Color(0xFF5F33E1),
            Color(0xFFFF7D53),
            Color(0xFFFF7D53),
            Color(0xFF0087FF)
        )
        val taskLists = listOf(
            "Grocery shopping app design",
            "Brand Growth Strategy",
            "Product launch preparation",
            "Startup promotion"
        )
        val taskListTopics = listOf(
            "Market Research",
            "Marketing Plan",
            "Market Analysis",
            "Digital Marketing Campaign"
        )

        LazyColumn(modifier = Modifier.constrainAs(searchResult) {
            top.linkTo(search.bottom, margin = 25.dp)
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
                        }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = com.example.taskmate.calendar.fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1
                        )

                        Text(taskListTopic, modifier = Modifier.constrainAs(text2) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = com.example.taskmate.calendar.fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
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
                        }, fontFamily = com.example.taskmate.calendar.fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
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
                                    fontFamily = com.example.taskmate.calendar.fonts,
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
}
@Composable
private  fun SearchBar(fontFamily: FontFamily,
    modifier: Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search", fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
            fontSize = 16.sp, lineHeight = 17.sp, color = Color(0xFF6E6A7C)) },
        singleLine = true,
        leadingIcon = {
            Box(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.search_icon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color(0xFF5F33E1)
                )
            }
        },
        modifier = modifier.shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(15.dp),
            ambientColor = Color(0xFF5F33E1).copy(alpha = 0.2f),
            spotColor = Color(0xFF5F33E1).copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(15.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEDE8FF),
            unfocusedContainerColor = Color(0xFFEDE8FF),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF6A5AE0)
        ),textStyle = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            fontSize = 16.sp, lineHeight = 17.sp,
            color = Color(0xFF24252C)
        )
    )
}

@Preview(showSystemUi = true)
@Composable
private fun ShowSearchScreen() {
    SearchScreen()
}