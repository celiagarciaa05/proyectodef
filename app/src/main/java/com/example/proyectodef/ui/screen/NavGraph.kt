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
        MetaViewModel(MetaRepositoryImpl())
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("login") {
            LoginScreen(authViewModel = authViewModel, navController = navController, metaViewModel = metaViewModel)
        }

        composable("register") {
            RegisterScreen(authViewModel = authViewModel, navController = navController)
        }

        composable("home") {
            val progresoViewModel: ProgresoViewModel = viewModel()

            HomeScreen(
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                metaViewModel = metaViewModel,
                progresoViewModel = progresoViewModel,
                navController = navController
            )
        }


        composable("miCuenta") {
            MiCuentaScreen(
                authViewModel = authViewModel,
                navController = navController,
                transactionViewModel = transactionViewModel
            )
        }

        composable("transacciones/{tipo}") { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "Gasto"
            TransaccionesScreen(
                tipo = tipo,
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                navController = navController
            )
        }

        composable("metas") {
            MetasScreen(
                authViewModel = authViewModel,
                metaViewModel = metaViewModel,
                transactionViewModel = transactionViewModel,
                navController = navController
            )
        }

        composable("gestionMetas") {
            GestionMetasScreen(
                authViewModel = authViewModel,
                metaViewModel = metaViewModel,
                navController = navController
            )
        }

        composable("chatbot") {
            val chatViewModel = remember {
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
