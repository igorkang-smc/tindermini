package com.example.tinderclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tinderclone.ui.ChatListScreen
import com.example.tinderclone.ui.ProfileScreen
import com.example.tinderclone.ui.SignupScreen
import com.example.tinderclone.ui.SingleChatScreen
import com.example.tinderclone.ui.SwipeScreen
import com.example.tinderclone.ui.theme.tindercloneTheme
import com.example.tinderclonee.ui.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            tindercloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwipeAppNavigation()
                }
            }
        }
    }
}

@Composable
fun SwipeAppNavigation() {
    val navController = rememberNavController()
    val vm = hiltViewModel<TCViewModel>()

    NotificationMessage(vm = vm)
    NavHost(navController = navController, startDestination = DestinationScreen.Signup.route) {
        composable(DestinationScreen.Signup.route) {
            SignupScreen(navController, vm)
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(navController, vm)
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController, vm)
        }
        composable(DestinationScreen.Swipe.route) {
            SwipeScreen(navController, vm)
        }
        composable(DestinationScreen.ChatList.route) {
            ChatListScreen(navController)
        }
        composable(DestinationScreen.SingleChat.route) {
            SingleChatScreen("123")
        }
    }
}