package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.DragonViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileAndSettingsScreen
import com.example.ui.theme.DragonTheme
import com.example.ui.utils.LanguageUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configures edge-to-edge layouts as per Android accessibility guidelines
        enableEdgeToEdge()

        val viewModel: DragonViewModel by viewModels()

        setContent {
            val user by viewModel.currentUser.collectAsState()
            val settings by viewModel.settings.collectAsState()
            
            val isDark = settings?.isDark ?: true
            val langCode = settings?.language ?: "en"

            var loggedInLocalState by remember { mutableStateOf(false) }

            LaunchedEffect(user) {
                loggedInLocalState = user != null
            }

            DragonTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!loggedInLocalState) {
                        AuthScreen(
                            viewModel = viewModel,
                            langCode = langCode,
                            onAuthSuccess = {
                                loggedInLocalState = true
                            }
                        )
                    } else {
                        MainNavigationScaffold(
                            viewModel = viewModel,
                            langCode = langCode
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationScaffold(
    viewModel: DragonViewModel,
    langCode: String
) {
    var activeTab by remember { mutableStateOf("CREATE") } // "CREATE", "LIBRARY", "SETTINGS"

    Scaffold(
        bottomBar = {
            // Material 3 high-contrast localized navigation component
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Task 1: Create Space
                NavigationBarItem(
                    selected = activeTab == "CREATE",
                    onClick = { activeTab = "CREATE" },
                    icon = {
                        Icon(Icons.Default.MovieCreation, contentDescription = "Create Workspace Icon")
                    },
                    label = {
                        Text(
                            text = if (langCode == "es") "Estudio" else if (langCode == "zh") "设计室" else "Studio",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.testTag("tab_nav_create")
                )

                // Task 2: Gallery Library Space
                NavigationBarItem(
                    selected = activeTab == "LIBRARY",
                    onClick = { activeTab = "LIBRARY" },
                    icon = {
                        Icon(Icons.Default.VideoLibrary, contentDescription = "Creations Library Grid Icon")
                    },
                    label = {
                        Text(
                            text = LanguageUtils.translate("history", langCode).substringBefore(" "),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.testTag("tab_nav_library")
                )

                // Task 3: Profiles + Configurations Space
                NavigationBarItem(
                    selected = activeTab == "SETTINGS",
                    onClick = { activeTab = "SETTINGS" },
                    icon = {
                        Icon(Icons.Default.Person, contentDescription = "Creator Configuration Console Icon")
                    },
                    label = {
                        Text(
                            text = if (langCode == "es") "Perfil" else if (langCode == "zh") "账户" else "Console",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.testTag("tab_nav_settings")
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Tab router content switching with crossfades
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "navigation_tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    "CREATE" -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            langCode = langCode
                        )
                    }
                    "LIBRARY" -> {
                        HistoryScreen(
                            viewModel = viewModel,
                            langCode = langCode
                        )
                    }
                    "SETTINGS" -> {
                        ProfileAndSettingsScreen(
                            viewModel = viewModel,
                            langCode = langCode
                        )
                    }
                }
            }
        }
    }
}
