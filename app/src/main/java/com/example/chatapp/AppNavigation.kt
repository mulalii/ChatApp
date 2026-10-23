package com.example.chatapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.auth.LoginScreen
import com.example.chatapp.auth.SignUpScreen
import com.example.chatapp.contact.ContactListScreen
import com.example.chatapp.conversation.ConvoScreen
import com.google.firebase.auth.FirebaseAuth

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val CONTACT_LIST = "contact_list"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Routes.CONTACT_LIST
        } else {
            Routes.LOGIN
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGN_UP)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.CONTACT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUpSuccess = {
                    navController.navigate(Routes.CONTACT_LIST) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CONTACT_LIST) {
            ContactListScreen(
                onLogoutClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onContactClick = { user ->
                    val name = user.username.ifBlank { "User" }
                    navController.navigate("chat/${user.uid}/$name")
                }
            )
        }
        composable("chat/{recipientId}/{recipientName}") { backStackEntry ->
            val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
            val recipientName = backStackEntry.arguments?.getString("recipientName") ?: ""
            ConvoScreen(
                recipientId = recipientId,
                recipientName = recipientName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
