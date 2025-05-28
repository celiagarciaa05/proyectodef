package com.example.proyectodef.ui.screen

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectodef.R
import com.example.proyectodef.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    var alphaAnim by remember { mutableStateOf(0f) }
    val alpha by animateFloatAsState(
        targetValue = alphaAnim,
        animationSpec = tween(durationMillis = 1000)
    )

    val user by authViewModel.user.collectAsState()

    LaunchedEffect(user) {
        Log.d("Celia", "Celia dice: Estado inicial del usuario en splash: $user")
        alphaAnim = 1f

        val startTime = System.currentTimeMillis()
        while (user == null && System.currentTimeMillis() - startTime < 5000) {
            Log.d("Celia", "Celia dice: Esperando a que user no sea null...")
            delay(100)
        }

        delay(3000) // Para ver logs antes de navegación

        Log.d("Celia", "Celia dice: Navegando a: ${if (user != null) "home" else "login"}")
        navController.navigate(if (user != null) "home" else "login") {
            popUpTo("splash") { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF121212),
                        Color(0xFFAF1978),
                        Color(0xFFBB86FC),
                        Color(0xFF5FFFFF),
                        Color(0xFF121212)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1500f)
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(180.dp)
                    .alpha(alpha)
            )
        }
    }
}
