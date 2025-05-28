package com.example.proyectodef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.navigation.compose.rememberNavController
import com.example.proyectodef.data.repository.AuthRepositoryImpl
import com.example.proyectodef.data.repository.TransactionRepositoryImpl
import com.example.proyectodef.ui.screen.AppNavGraph
import com.example.proyectodef.ui.theme.ProyectodefTheme
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val authViewModel = AuthViewModel(AuthRepositoryImpl(this))
        val transactionViewModel = TransactionViewModel(TransactionRepositoryImpl())

        setContent {
            ProyectodefTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    transactionViewModel = transactionViewModel
                )
            }
        }
    }
}
