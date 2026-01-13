package com.example.compose.jetchat.core.navigation

import androidx.annotation.StringRes
import com.example.compose.jetchat.R

sealed class DrawerDestination(
    val key: String,
    @StringRes val labelRes: Int,
    val navId: Int? = null   // ✅ ADD THIS
) {
    data object Composers : DrawerDestination("composers", R.string.menu_composers)
    data object TestByKeshav : DrawerDestination("TestbyKeshav", R.string.menu_testbykeshav)
    data object Droidcon : DrawerDestination("droidcon-nyc", R.string.menu_droidcon)
    data object ChatWsV1 : DrawerDestination("ChatWsV1", R.string.menu_ChatWsV1)
    data object ChatWsV2 : DrawerDestination("ChatWsV2", R.string.menu_ChatWsV2)

    // 🔥 Add this block
    companion object {
        fun fromKey(key: String): DrawerDestination = when (key) {
            Composers.key -> Composers
            TestByKeshav.key -> TestByKeshav
            Droidcon.key -> Droidcon
            ChatWsV1.key -> ChatWsV1
            ChatWsV2.key -> ChatWsV2

            else -> Composers
        }
    }
}
