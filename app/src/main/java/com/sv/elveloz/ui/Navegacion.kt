package com.sv.elveloz.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth // <-- Importación para cerrar sesión
import com.sv.elveloz.ui.login.PantallaLogin
import com.sv.elveloz.ui.login.ViewModelLogin
import com.sv.elveloz.ui.login.PantallaRegistro
import com.sv.elveloz.ui.login.ViewModelRegistro
import com.sv.elveloz.ui.catalog.CatalogScreen
import com.sv.elveloz.ui.catalog.CatalogViewModel
import com.sv.elveloz.domain.model.RolUsuario

@Composable
fun NavegacionApp(factory: AppViewModelFactory, startDestination: String = "login") { // <-- 1. Recibimos la ruta inicial
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) { // <-- 2. Se la pasamos al NavHost
        composable("login") {
            val loginViewModel: ViewModelLogin = viewModel(factory = factory)
            PantallaLogin(
                viewModel = loginViewModel,
                onLoginExitoso = { usuario ->
                    navController.navigate("catalog/${usuario.rol.name}") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegistrarseClick = {
                    navController.navigate("registro")
                }
            )
        }
        composable("registro") {
            val registroViewModel: ViewModelRegistro = viewModel(factory = factory)
            PantallaRegistro(
                viewModel = registroViewModel,
                onRegistroExitoso = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("catalog/{rol}") { backStackEntry ->
            val rolString = backStackEntry.arguments?.getString("rol") ?: RolUsuario.CLIENTE.name
            val rol = RolUsuario.valueOf(rolString)
            val catalogViewModel: CatalogViewModel = viewModel(factory = factory)
            catalogViewModel.rolActual = rol

            CatalogScreen(
                viewModel = catalogViewModel,
                onLogout = {
                    // 3. Limpiamos la sesión en Firebase al cerrar sesión
                    FirebaseAuth.getInstance().signOut()

                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}