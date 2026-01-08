package com.example.taskmate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.taskmate.ui.theme.TaskMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskMateTheme {
                SplashScreen()
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, MainScreen()::class.java)
                    startActivity(intent)
                    finish()
                },1200)
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9C2CF3),
                        Color(0xFF3A49F9),
                    )
                )
            )
    ) {
        Image(painter = painterResource(R.drawable.logo), contentDescription = "Logo",
            modifier = Modifier.align(Alignment.Center).size(82.dp))
    }
}

@Preview(showSystemUi = true)
@Composable
fun SplashScreenView() {
    TaskMateTheme {
       SplashScreen()
    }
}