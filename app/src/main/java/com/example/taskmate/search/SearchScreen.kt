package com.example.taskmate.search

import android.content.Context
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.taskmate.R
import com.example.taskmate.home.TaskPrefs
import com.example.taskmate.home.fonts
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.recentSearchDataStore by preferencesDataStore(
    name = "task_prefs"
)

object RecentSearchPrefs {

    private const val MAX_SIZE = 5
    private val KEY_RECENT_SEARCHES = stringPreferencesKey("recent_searches")

    private val gson = Gson()

    suspend fun saveSearch(context: Context, query: String) {
        if (query.isBlank()) return

        context.recentSearchDataStore.edit { prefs ->
            val currentList = getListFromPrefs(prefs)

            currentList.remove(query)
            currentList.add(0, query)

            if (currentList.size > MAX_SIZE) {
                currentList.removeAt(currentList.lastIndex)
            }

            prefs[KEY_RECENT_SEARCHES] = gson.toJson(currentList)
        }
    }

    fun recentSearchesFlow(context: Context) =
        context.recentSearchDataStore.data.map { prefs ->
            getListFromPrefs(prefs)
        }

    suspend fun removeSearch(context: Context, query: String) {
        context.recentSearchDataStore.edit { prefs ->
            val currentList = getListFromPrefs(prefs)
            currentList.remove(query)
            prefs[KEY_RECENT_SEARCHES] = gson.toJson(currentList)
        }
    }

    suspend fun clearRecentSearches(context: Context) {
        context.recentSearchDataStore.edit { prefs ->
            prefs.remove(KEY_RECENT_SEARCHES)
        }
    }

    private fun getListFromPrefs(prefs: Preferences): MutableList<String> {
        val json = prefs[KEY_RECENT_SEARCHES] ?: return mutableListOf()
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(json, type)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    val work by TaskPrefs.loadWorkTasks(context).collectAsState(emptyList())
    val personal by TaskPrefs.loadPersonalTasks(context).collectAsState(emptyList())
    val study by TaskPrefs.loadStudyTasks(context).collectAsState(emptyList())
    val daily by TaskPrefs.loadDailyStudyTasks(context).collectAsState(emptyList())

    val allTasks = remember(work, personal, study, daily) {
        work + personal + study + daily
    }

    val filteredTasks = remember(searchText) {
        if (searchText.text.isBlank()) {
            emptyList()
        } else {
            allTasks.filter {
                it.taskName.contains(searchText.text, ignoreCase = true)
            }
        }
    }

    val recentSearches by RecentSearchPrefs
        .recentSearchesFlow(context)
        .collectAsState(initial = emptyList())

    val showRecentSearches =
        searchText.text.isBlank() && recentSearches.isNotEmpty()

    val showSearchResults =
        searchText.text.isNotBlank() && filteredTasks.isNotEmpty()

    val showEmptyState =
        searchText.text.isNotBlank() && filteredTasks.isEmpty()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var fromRecentClick by remember { mutableStateOf(false) }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(titleText, emptyStateText, recentSearchContainer, emptyStateIcon, searchBar, searchResultsList) = createRefs()

        Text("Search", modifier = Modifier.constrainAs(titleText) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        SearchBar(fonts,
            modifier = Modifier.constrainAs(searchBar) {
                top.linkTo(titleText.bottom, margin = 20.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.fillMaxWidth().padding(horizontal = 20.dp).focusRequester(focusRequester),
            query = searchText,
            onQueryChange = { newValue ->
                searchText = newValue
            }
        )

        if (!showSearchResults && !showEmptyState) {
            Box(modifier = Modifier.constrainAs(recentSearchContainer) {
                top.linkTo(searchBar.bottom, margin = 20.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.padding(horizontal = 20.dp).fillMaxWidth().shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(15.dp),
                ambientColor = Color(0xFFF5F5F5).copy(alpha = 0.2f),
                spotColor = Color(0xFFF5F5F5).copy(alpha = 0.4f)
            ).background(Color(0xFFF5F5F5),
                shape = RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                    val(recentSearchTitle, clearRecentButton, recentSearchList) = createRefs()

                    Text("Recent Search", modifier = Modifier.constrainAs(recentSearchTitle) {
                        top.linkTo(parent.top, margin = 15.dp)
                        start.linkTo(parent.start, margin = 15.dp) }.padding(bottom = if (showRecentSearches) 0.dp else 15.dp)
                        , fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts,
                        fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = Color(0xFF24252C)
                    )

                    Box(modifier = Modifier.constrainAs(clearRecentButton) {
                        top.linkTo(searchBar.bottom, margin = 15.dp)
                        end.linkTo(parent.end, margin = 15.dp)
                    }.size(72.dp,20.dp).clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (recentSearches.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "No recent search to clear",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                scope.launch {
                                    RecentSearchPrefs.clearRecentSearches(context)
                                    snackbarHostState.showSnackbar(
                                        message = "Search history cleared",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }, contentAlignment = Alignment.Center) {
                        Text("Clear All", fontSize = 14.sp, lineHeight = 17.sp,
                            fontFamily = fonts, fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal, color = Color(0xFF5F33E1)
                        )
                    }

                    if (showRecentSearches) {
                        LazyColumn(modifier = Modifier.constrainAs(recentSearchList) {
                            top.linkTo(recentSearchTitle.bottom, margin = 15.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },contentPadding = PaddingValues(bottom = 15.dp), verticalArrangement = Arrangement.spacedBy(16.dp))
                        {
                            items(recentSearches) { query ->
                                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                                    val(recentQueryText, removeQueryIcon) = createRefs()

                                    Text(query, modifier = Modifier.constrainAs(recentQueryText) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start, margin = 15.dp) }
                                        .clickable {
                                            fromRecentClick = true
                                            searchText = TextFieldValue(
                                                text = query,
                                                selection = TextRange(query.length)
                                            )
                                            focusRequester.requestFocus()
                                            keyboardController?.show()
                                        }, fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts,
                                        fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                        color = Color(0xFF6E6A7C), maxLines = 1
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.remove_icon),
                                        contentDescription = "clear_icon",
                                        tint = Color(0xFF5F33E1),
                                        modifier = Modifier.constrainAs(removeQueryIcon) {
                                            top.linkTo(parent.top)
                                            bottom.linkTo(parent.bottom)
                                            end.linkTo(parent.end, margin = 15.dp)
                                        }.size(20.dp).clickable {
                                            scope.launch {
                                                RecentSearchPrefs.removeSearch(context, query)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEmptyState) {
            Icon(
                painter = painterResource(R.drawable.search_empty),
                contentDescription = "empty_notification",
                tint = Color(0xFF5F33E1),
                modifier = Modifier.constrainAs(emptyStateIcon) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }.size(92.dp)
            )

            Text(
                "No results found",
                modifier = Modifier.constrainAs(emptyStateText) {
                    top.linkTo(emptyStateIcon.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                fontSize = 14.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6E6A7C)
            )
        }

        if (showSearchResults) {
            LazyColumn(modifier = Modifier.constrainAs(searchResultsList) {
                top.linkTo(searchBar.bottom, margin = 25.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom, margin = (-15).dp)
                height = Dimension.fillToConstraints
            },contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp))
            {
                items(filteredTasks) { task ->
                    ElevatedCard(elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    ), colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFFFF)
                    ), modifier = Modifier.padding(horizontal = 20.dp).height(94.dp).fillMaxWidth().shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(15.dp),
                        ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                        spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                    ), onClick = { navController.navigate("update_task/${task.id}/${task.taskGroup}") },
                        shape = RoundedCornerShape(15.dp)) {
                        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                            val(taskGroupNameText,taskNameText,taskTimeIcon,taskTimeText,
                                taskIconBox,taskStatusCard) = createRefs()

                            Text(task.taskGroupName, modifier = Modifier.constrainAs(taskGroupNameText) {
                                top.linkTo(parent.top, margin = 5.dp)
                                bottom.linkTo(taskNameText.top)
                                start.linkTo(parent.start)
                            }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFF6E6A7C), maxLines = 1
                            )

                            Text(task.taskName, modifier = Modifier.constrainAs(taskNameText) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                            }.fillMaxWidth().padding(start = 14.dp, end = 65.dp), fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C), maxLines = 1
                            )

                            Image(modifier = Modifier.constrainAs(taskTimeIcon) {
                                bottom.linkTo(parent.bottom, margin = 14.dp)
                                start.linkTo(parent.start, margin = 15.dp)
                            }.size(14.dp), painter = painterResource(R.drawable.clock), contentDescription = "clock Icon")

                            Text("12:00", modifier = Modifier.constrainAs(taskTimeText) {
                                top.linkTo(taskTimeIcon.top)
                                start.linkTo(taskTimeIcon.end, margin = 2.dp)
                                bottom.linkTo(taskTimeIcon.bottom)
                            }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                fontSize = 11.sp, lineHeight = 14.sp, color = Color(0xFFAB94FF), maxLines = 1
                            )

                            Box(modifier = Modifier.constrainAs(taskIconBox) {
                                top.linkTo(parent.top, margin = 15.dp)
                                end.linkTo(parent.end, margin = 15.dp)
                            }.size(34.dp).background(Color(task.iconBg.toULong()),
                                shape = RoundedCornerShape(7.dp)),
                                contentAlignment = Alignment.Center
                            )  {
                                Image(modifier = Modifier.size(20.dp), painter = painterResource(task.icon), contentDescription = "briefcase")
                            }

                            ElevatedCard(elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            ), colors = CardDefaults.cardColors(
                                containerColor = when(task.progressStatus) {
                                    "Done" -> Color(0xFFEDE8FF)
                                    "In Progress" -> Color(0xFFFFE9E1)
                                    "To Do" -> Color(0xFFE3F2FF)
                                    else -> Color(0xFFEDE8FF)
                                }
                            ), modifier = Modifier.constrainAs(taskStatusCard) {
                                bottom.linkTo(parent.bottom, margin = 15.dp)
                                end.linkTo(parent.end, margin = 15.dp)
                            }.shadow( elevation = 12.dp,
                                shape = RoundedCornerShape(7.dp),
                                ambientColor = when(task.progressStatus) {
                                    "Done" -> Color(0xFFEDE8FF)
                                    "In Progress" -> Color(0xFFFFE9E1)
                                    "To Do" -> Color(0xFFE3F2FF)
                                    else -> Color(0xFFEDE8FF)
                                },
                                spotColor = when(task.progressStatus) {
                                    "Done" -> Color(0xFFEDE8FF)
                                    "In Progress" -> Color(0xFFFFE9E1)
                                    "To Do" -> Color(0xFFE3F2FF)
                                    else -> Color(0xFFEDE8FF)
                                }
                            ),shape = RoundedCornerShape(7.dp)) {
                                Box(modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = task.progressStatus,
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.sp,
                                        lineHeight = 12.sp,
                                        color = when(task.progressStatus) {
                                            "Done" -> Color(0xFF5F33E1)
                                            "In Progress" -> Color(0xFFFF7D53)
                                            "To Do" -> Color(0xFF0087FF)
                                            else -> Color(0xFF5F33E1)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(searchText.text) {
        if (!fromRecentClick && searchText.text.trim().length >= 2) {
            delay(800)
            RecentSearchPrefs.saveSearch(context, searchText.text)
        }
        fromRecentClick = false
    }
}
@Composable
private  fun SearchBar(fontFamily: FontFamily,
    modifier: Modifier,
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit
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
            ambientColor = Color(0xFFF5F5F5).copy(alpha = 0.2f),
            spotColor = Color(0xFFF5F5F5).copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(15.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
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
    val navController = rememberNavController()
    val snackbarHostState = SnackbarHostState()
    SearchScreen(navController,snackbarHostState)
}