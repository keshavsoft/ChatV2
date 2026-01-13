package com.example.compose.jetchat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.compose.jetchat.components.JetchatDrawer
import com.example.compose.jetchat.core.navigation.DrawerDestination
import com.example.compose.jetchat.databinding.ContentMainBinding
import com.example.compose.jetchat.feature.chatws.v1.ChatWsV1Screen
import com.example.compose.jetchat.feature.chatws.v2.ChatWsV2Screen


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Main activity for the app.
 */
class NavActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                setContent {
                    val drawerState = rememberDrawerState(initialValue = Closed)

                    val drawerOpen by viewModel.drawerShouldBeOpened.collectAsStateWithLifecycle()
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(drawerOpen) {
                        if (drawerOpen) {
                            drawerState.open()
                            viewModel.resetOpenDrawerAction()
                        }
                    }

                    // Which drawer item is currently selected
                    var selectedDestination by remember {
                        mutableStateOf<DrawerDestination>(DrawerDestination.Composers)
                    }

                    JetchatDrawer(
                        drawerState = drawerState,
                        selectedMenu = selectedDestination.key,
                        onChatClicked = { key ->
                            val destination = DrawerDestination.fromKey(key)

                            // ✅ prevent crash on re-select
                            if (destination == selectedDestination) {
                                scope.launch { drawerState.close() }
                                return@JetchatDrawer
                            }

                            scope.launch { drawerState.close() }

                            handleDrawerDestinationClick(
                                destination = destination,
                                scope = scope,
                                navControllerProvider = { findNavController() },
                                onDestinationSelected = { selectedDestination = it }
                            )
                        },
                        onProfileClicked = { userId ->
                            val bundle = bundleOf("userId" to userId)
                            findNavController()?.navigate(R.id.nav_profile, bundle)
                            scope.launch { drawerState.close() }
                        }
                    ) {
                        DrawerDestinationContent(
                            selectedDestination = selectedDestination,
                            drawerState = drawerState,
                            scope = scope,
                            onBackToHome = { selectedDestination = DrawerDestination.Composers }
                        )
                    }
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController()?.navigateUp() == true || super.onSupportNavigateUp()
    }


    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController? {
        val fragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)

        return (fragment as? NavHostFragment)?.navController
    }

}

/**
 * Central place to decide what to do when a drawer destination is clicked:
 * - Some items go to Fragment navigation (home, gps, newchat)
 * - Some are pure Compose screens (SMS, VoiceToText)
 */
@OptIn(ExperimentalMaterial3Api::class)
private fun handleDrawerDestinationClick(
    destination: DrawerDestination,
    scope: CoroutineScope,
    navControllerProvider: () -> NavController?,   // ✅ FIX HERE
    onDestinationSelected: (DrawerDestination) -> Unit
) {
    onDestinationSelected(destination)

    val navController = navControllerProvider() ?: return

    val chatWsDestinations = listOf(
        DrawerDestination.ChatWsV1
    )

    when (destination) {
        in chatWsDestinations -> {
            // Handled entirely in Compose; do not touch fragment nav
        }

        else -> {
            // Only fragment-based destinations should reach here
            destination.navId?.let { navId ->
                navController.popBackStack(
                    navController.graph.startDestinationId,
                    false
                )
                navController.navigate(navId)
            }
        }
    }
}

/**
 * Decides which screen content to show for the currently selected drawer destination.
 */
@Composable
private fun DrawerDestinationContent(
    selectedDestination: DrawerDestination,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onBackToHome: () -> Unit
) {
    when (selectedDestination) {
        // Voice to Text variants – simple single-page screens
        DrawerDestination.ChatWsV1 -> {
            ChatWsSection(
                destination = selectedDestination,
                drawerState = drawerState,
                scope = scope,
                onBackToHome = onBackToHome
            )
        }
        DrawerDestination.ChatWsV2 -> {
            ChatWsSection(
                destination = selectedDestination,
                drawerState = drawerState,
                scope = scope,
                onBackToHome = onBackToHome
            )
        }
        else -> {
            // Default: show the original NavHost fragment content
            AndroidViewBinding(ContentMainBinding::inflate)
        }
    }
}


/**
 * Handles VoiceToText screens for all implemented V1–V6.
 * (V7 still falls back to the default content, same as your original code.)
 */
@Composable
private fun ChatWsSection(
    destination: DrawerDestination,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onBackToHome: () -> Unit
) {
    when (destination) {
        DrawerDestination.ChatWsV1 -> {
            ChatWsV1Screen(
                onNavIconPressed = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }
        DrawerDestination.ChatWsV2 -> {
            ChatWsV2Screen(
                onNavIconPressed = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }
        else -> Unit
    }
}
