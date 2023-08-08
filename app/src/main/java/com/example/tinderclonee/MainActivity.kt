package com.example.tinderclonee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tinderclonee.ui.ChatListScreen
import com.example.tinderclonee.ui.DestinationScreen
import com.example.tinderclonee.ui.LoginScreen
import com.example.tinderclonee.ui.ProfileScreen
import com.example.tinderclonee.ui.SignupScreen
import com.example.tinderclonee.ui.SingleChatScreen
import com.example.tinderclonee.ui.SwipeCards
import com.example.tinderclonee.ui.theme.TinderCloneeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TinderCloneeTheme {
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
    NavHost(navController = navController, startDestination = DestinationScreen.Swipe.route) {
        composable(DestinationScreen.Signup.route) {
            SignupScreen()
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen()
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(DestinationScreen.Swipe.route) {
            SwipeCards(navController)
        }
        composable(DestinationScreen.ChatList.route) {
            ChatListScreen(navController)
        }
        composable(DestinationScreen.SingleChat.route) {
            SingleChatScreen("123")
        }
    }
}