package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AppViewModel
import com.example.ui.screens.*

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController, viewModel)
        }
        
        composable("onboarding") {
            OnboardingScreen(navController)
        }
        
        composable("login") {
            LoginScreen(navController, viewModel)
        }
        
        composable("register") {
            RegisterScreen(navController, viewModel)
        }
        
        composable("pending_approval") {
            PendingApprovalScreen(navController, viewModel)
        }
        
        composable("main_client") {
            MainClientScreen(navController, viewModel)
        }
        
        composable(
            route = "event_details/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
            EventDetailsScreen(matchId = matchId, navController = navController, viewModel = viewModel)
        }
        
        composable("admin_dashboard") {
            AdminDashboardScreen(navController, viewModel)
        }
    }
}
