package com.xtremeiptv.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xtremeiptv.ui.account.AccountInfoScreen
import com.xtremeiptv.ui.player.PlayerActivity
import com.xtremeiptv.ui.profile.ProfileAddEditScreen
import com.xtremeiptv.ui.profile.ProfileSelectionScreen
import com.xtremeiptv.ui.search.SearchScreen
import com.xtremeiptv.ui.settings.SettingsScreen
import com.xtremeiptv.utils.Screen
import com.xtremeiptv.utils.XtremeIPTVTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XtremeIPTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.ProfileSelection.route
                    ) {
                        composable(Screen.ProfileSelection.route) {
                            ProfileSelectionScreen(
                                onProfileSelected = { profileId ->
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                                    }
                                },
                                onAddProfile = {
                                    navController.navigate(Screen.ProfileAddEdit.pass())
                                }
                            )
                        }
                        
                        composable(Screen.ProfileAddEdit.route) { backStackEntry ->
                            val profileId = backStackEntry.arguments?.getString("profileId")?.takeIf { it != "new" }
                            ProfileAddEditScreen(
                                profileId = profileId,
                                onSave = {
                                    navController.popBackStack()
                                },
                                onCancel = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(Screen.Main.route) {
                            MainScreen(
                                onNavigateToPlayer = { contentId, contentType, title, streamUrl ->
                                    startActivity(PlayerActivity.newIntent(this@MainActivity, contentId, contentType, title, streamUrl))
                                },
                                onNavigateToAccountInfo = {
                                    navController.navigate(Screen.AccountInfo.route)
                                },
                                onNavigateToSearch = {
                                    navController.navigate(Screen.Search.route)
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onProfileSwitched = {
                                    navController.popBackStack(Screen.ProfileSelection.route, false)
                                }
                            )
                        }
                        
                        composable(Screen.AccountInfo.route) {
                            AccountInfoScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable(Screen.Search.route) {
                            SearchScreen(
                                onBack = { navController.popBackStack() },
                                onPlay = { contentId, contentType, title, streamUrl ->
                                    startActivity(PlayerActivity.newIntent(this@MainActivity, contentId, contentType, title, streamUrl))
                                }
                            )
                        }
                        
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
