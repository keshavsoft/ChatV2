package com.example.compose.jetchat.conversation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.compose.jetchat.MainViewModel
import com.example.compose.jetchat.R
import com.example.compose.jetchat.data.keshavSoftHrMessages
import com.example.compose.jetchat.theme.JetchatTheme
class NewchatFragment : Fragment() {
    private val activityViewModel: MainViewModel by activityViewModels()

    // Use the existing ConversationUiState class with new message data
    private val newChatUiState = ConversationUiState(
        channelName = "TestbyKeshav",
        channelMembers = 100,
        initialMessages = keshavSoftHrMessages
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(inflater.context).apply {
        setContent {
            JetchatTheme {
                ConversationContent(
                    uiState = newChatUiState,
                    navigateToProfile = { user ->
                        val bundle = Bundle().apply {
                            putString("userId", user)
                        }
                        findNavController().navigate(R.id.nav_profile, bundle)
                    },
                    onNavIconPressed = {
                        activityViewModel.openDrawer()
                    }
                )
            }
        }
    }
}