package com.example.taskmate.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
        val(text1,search) = createRefs()

        Text("Search", modifier = Modifier.constrainAs(text1) {
            top.linkTo(parent.top, margin = 25.dp)
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