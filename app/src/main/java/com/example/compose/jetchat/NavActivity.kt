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
import com.example.compose.jetchat.feature.sms.SmsDetailScreen
import com.example.compose.jetchat.feature.sms.SmsDetailScreenV2
import com.example.compose.jetchat.feature.sms.SmsDetailScreenV5
import com.example.compose.jetchat.feature.sms.SmsListScreen
import com.example.compose.jetchat.feature.sms.SmsListScreenV3
import com.example.compose.jetchat.feature.sms.SmsListScreenV4
import com.example.compose.jetchat.feature.sms.SmsListScreenV5
import com.example.compose.jetchat.feature.voicetotextV1.VoiceToTextScreenV1
import com.example.compose.jetchat.feature.voicetotext.VoiceToTextScreenV2
import com.example.compose.jetchat.feature.voicetotext.VoiceToTextScreenV3
import com.example.compose.jetchat.feature.voicetotext.VoiceToTextScreenV4
import com.example.compose.jetchat.feature.voicetotext.VoiceToTextScreenV5
import com.example.compose.jetchat.feature.voicetotext.VoiceToTextScreenV6
import com.example.compose.jetchat.feature.voicetotext.VoiceToTextScreenV7
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

                    // Which SMS thread is open in detail (null = show list)
                    var selectedSmsAddress by remember { mutableStateOf<String?>(null) }

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
                                onDestinationSelected = { selectedDestination = it },
                                onSmsDetailCleared = { selectedSmsAddress = null }
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
                            selectedSmsAddress = selectedSmsAddress,
                            drawerState = drawerState,
                            scope = scope,
                            onBackToHome = { selectedDestination = DrawerDestination.Composers },
                            onSmsAddressSelected = { selectedSmsAddress = it },
                            onBackFromSmsDetail = { selectedSmsAddress = null }
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
    onDestinationSelected: (DrawerDestination) -> Unit,
    onSmsDetailCleared: () -> Unit
) {
    onDestinationSelected(destination)

    val navController = navControllerProvider() ?: return


    val smsDestinations = listOf(
        DrawerDestination.Sms,
        DrawerDestination.SmsV2,
        DrawerDestination.SmsV3,
        DrawerDestination.SmsV4,
        DrawerDestination.SmsV5
    )

    val voiceDestinations = listOf(
        DrawerDestination.VoiceToTextV1,
        DrawerDestination.VoiceToTextV2,
        DrawerDestination.VoiceToTextV3,
        DrawerDestination.VoiceToTextV4,
        DrawerDestination.VoiceToTextV5,
        DrawerDestination.VoiceToTextV6,
        DrawerDestination.VoiceToTextV7
    )

    val chatWsDestinations = listOf(
        DrawerDestination.ChatWsV1
    )

    when (destination) {
        DrawerDestination.TestByKeshav -> {
            navController.popBackStack(R.id.nav_newchat, false)
            navController.navigate(R.id.nav_newchat)
        }

        DrawerDestination.Gps -> {
            navController.popBackStack(R.id.nav_gps, false)
            navController.navigate(R.id.nav_gps)
        }

        in smsDestinations -> {
            // Handled entirely in Compose; do not touch fragment nav
        }

        in voiceDestinations -> {
            // Handled entirely in Compose; do not touch fragment nav
        }

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

    // If we left SMS section, clear SMS detail selection
    if (destination !in smsDestinations) {
        onSmsDetailCleared()
    }
}

/**
 * Decides which screen content to show for the currently selected drawer destination.
 */
@Composable
private fun DrawerDestinationContent(
    selectedDestination: DrawerDestination,
    selectedSmsAddress: String?,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onBackToHome: () -> Unit,
    onSmsAddressSelected: (String) -> Unit,
    onBackFromSmsDetail: () -> Unit
) {
    when (selectedDestination) {

        // All SMS variants share the same structure (list + detail)
        DrawerDestination.Sms,
        DrawerDestination.SmsV2,
        DrawerDestination.SmsV3,
        DrawerDestination.SmsV4,
        DrawerDestination.SmsV5 -> {
            SmsSection(
                destination = selectedDestination,
                selectedSmsAddress = selectedSmsAddress,
                onBackToHome = onBackToHome,
                onSmsAddressSelected = onSmsAddressSelected,
                onBackFromSmsDetail = onBackFromSmsDetail
            )
        }

        // Voice to Text variants – simple single-page screens
        DrawerDestination.VoiceToTextV1,
        DrawerDestination.VoiceToTextV2,
        DrawerDestination.VoiceToTextV3,
        DrawerDestination.VoiceToTextV4,
        DrawerDestination.VoiceToTextV5,
        DrawerDestination.VoiceToTextV6,
        DrawerDestination.VoiceToTextV7-> {
            VoiceToTextSection(
                destination = selectedDestination,
                onBackToHome = onBackToHome
            )
        }

        // Voice to Text variants – simple single-page screens
        DrawerDestination.ChatWsV1 -> {
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
 * Handles SMS list + detail flow for all SMS drawer variants.
 */
@Composable
private fun SmsSection(
    destination: DrawerDestination,
    selectedSmsAddress: String?,
    onBackToHome: () -> Unit,
    onSmsAddressSelected: (String) -> Unit,
    onBackFromSmsDetail: () -> Unit
) {
    val isListScreen = selectedSmsAddress == null

    if (isListScreen) {
        when (destination) {
            DrawerDestination.Sms -> {
                SmsListScreen(
                    onBack = onBackToHome,
                    onSmsClick = { group -> onSmsAddressSelected(group.mobile) }
                )
            }

            DrawerDestination.SmsV2 -> {
                SmsListScreen(
                    onBack = onBackToHome,
                    onSmsClick = { group -> onSmsAddressSelected(group.mobile) }
                )
            }

            DrawerDestination.SmsV3 -> {
                SmsListScreenV3(
                    onBack = onBackToHome,
                    onSmsClick = { group -> onSmsAddressSelected(group.mobile) }
                )
            }

            DrawerDestination.SmsV4 -> {
                SmsListScreenV4(
                    onBack = onBackToHome,
                    onSmsClick = { group -> onSmsAddressSelected(group.mobile) }
                )
            }

            DrawerDestination.SmsV5 -> {
                SmsListScreenV5(
                    onBack = onBackToHome,
                    onSmsClick = { group -> onSmsAddressSelected(group.mobile) }
                )
            }

            else -> Unit
        }
    } else {
        // Detail screens
        val mobile = selectedSmsAddress!!
        when (destination) {
            DrawerDestination.Sms -> {
                SmsDetailScreen(
                    mobile = mobile,
                    onBack = onBackFromSmsDetail
                )
            }

            DrawerDestination.SmsV2 -> {
                SmsDetailScreenV2(
                    mobile = mobile,
                    onBack = onBackFromSmsDetail
                )
            }

            DrawerDestination.SmsV3,
            DrawerDestination.SmsV4 -> {
                // These variants currently reuse SmsDetailScreen in your original code
                SmsDetailScreen(
                    mobile = mobile,
                    onBack = onBackFromSmsDetail
                )
            }

            DrawerDestination.SmsV5 -> {
                SmsDetailScreenV5(
                    mobile = mobile,
                    onBack = onBackFromSmsDetail
                )
            }

            else -> Unit
        }
    }
}

/**
 * Handles VoiceToText screens for all implemented V1–V6.
 * (V7 still falls back to the default content, same as your original code.)
 */
@Composable
private fun VoiceToTextSection(
    destination: DrawerDestination,
    onBackToHome: () -> Unit
) {
    when (destination) {
        DrawerDestination.VoiceToTextV1 -> {
            VoiceToTextScreenV1(onBack = onBackToHome)
        }

        DrawerDestination.VoiceToTextV2 -> {
            VoiceToTextScreenV2(onBack = onBackToHome)
        }

        DrawerDestination.VoiceToTextV3 -> {
            VoiceToTextScreenV3(onBack = onBackToHome)
        }

        DrawerDestination.VoiceToTextV4 -> {
            VoiceToTextScreenV4(onBack = onBackToHome)
        }

        DrawerDestination.VoiceToTextV5 -> {
            VoiceToTextScreenV5(onBack = onBackToHome)
        }

        DrawerDestination.VoiceToTextV6 -> {
            VoiceToTextScreenV6(onBack = onBackToHome)
        }

        DrawerDestination.VoiceToTextV7 -> {
            VoiceToTextScreenV7(onBack = onBackToHome)
        }

        else -> Unit
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
        else -> Unit
    }
}
