package com.xtremeiptv.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xtremeiptv.R
import com.xtremeiptv.ui.live.LiveTabScreen
import com.xtremeiptv.ui.movies.MoviesTabScreen
import com.xtremeiptv.ui.series.SeriesTabScreen
import com.xtremeiptv.utils.Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPlayer: (String, String, String, String) -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onProfileSwitched: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val tabs = listOf(Tab.Live, Tab.Movies, Tab.Series)
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(tabs[0]) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onNavigateToAccountInfo) {
                        Icon(Icons.Default.Person, contentDescription = "Account")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                when (tab) {
                                    Tab.Live -> Icons.Default.LiveTv
                                    Tab.Movies -> Icons.Default.Movie
                                    Tab.Series -> Icons.Default.Tv
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Tab.Live.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Tab.Live.route) {
                LiveTabScreen(
                    onPlay = onNavigateToPlayer
                )
            }
            composable(Tab.Movies.route) {
                MoviesTabScreen(
                    onPlay = onNavigateToPlayer
                )
            }
            composable(Tab.Series.route) {
                SeriesTabScreen(
                    onPlay = onNavigateToPlayer
                )
            }
        }
    }
}
