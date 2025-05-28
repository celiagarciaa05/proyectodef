package com.example.proyectodef.ui.screen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyectodef.data.repository.MetaRepositoryImpl
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.ChatViewModel
import com.example.proyectodef.viewmodel.MetaViewModel
import com.example.proyectodef.viewmodel.ProgresoViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel
) {
    val userState by authViewModel.user.collectAsState()
    val isLoggedIn = userState != null

    val metaViewModel = remember {
        Log.d("garcia", "MetaViewModel creado en AppNavGraph")
        MetaViewModel(MetaRepositoryImpl())
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            Log.d("garcia", "Ruta: splash")
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("login") {
            Log.d("garcia", "Ruta: login")
            LoginScreen(authViewModel = authViewModel, navController = navController, metaViewModel = metaViewModel)
        }

        composable("register") {
            Log.d("garcia", "Ruta: register")
            RegisterScreen(authViewModel = authViewModel, navController = navController)
        }

        composable("home") {
            Log.d("garcia", "Ruta: home")

            val progresoViewModel: ProgresoViewModel = viewModel()

            HomeScreen(
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                metaViewModel = metaViewModel,
                progresoViewModel = progresoViewModel, // ✅ AÑADIDO
                navController = navController
            )
        }


        composable("miCuenta") {
            Log.d("garcia", "Ruta: miCuenta")
            MiCuentaScreen(
                authViewModel = authViewModel,
                navController = navController,
                transactionViewModel = transactionViewModel
            )
        }

        composable("transacciones/{tipo}") { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "Gasto"
            Log.d("garcia", "Ruta: transacciones/$tipo")
            TransaccionesScreen(
                tipo = tipo,
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                navController = navController
            )
        }

        composable("metas") {
            Log.d("garcia", "Ruta: metas")
            MetasScreen(
                authViewModel = authViewModel,
                metaViewModel = metaViewModel,
                transactionViewModel = transactionViewModel,
                navController = navController
            )
        }

        composable("gestionMetas") {
            Log.d("garcia", "Ruta: gestionMetas")
            GestionMetasScreen(
                authViewModel = authViewModel,
                metaViewModel = metaViewModel,
                navController = navController
            )
        }

        composable("chatbot") {
            Log.d("garcia", "Ruta: chatbot")
            val chatViewModel = remember {
                Log.d("garcia", "ChatViewModel creado")
                ChatViewModel()
            }

            ChatScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                metaViewModel = metaViewModel,
                navController = navController
            )
        }
    }
}
