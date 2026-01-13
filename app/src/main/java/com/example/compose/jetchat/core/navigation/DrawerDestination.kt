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
    data object Gps : DrawerDestination("Gps", R.string.menu_gps)
    data object Sms : DrawerDestination("SMS", R.string.menu_sms)
    data object SmsV1 : DrawerDestination("SMSV1", R.string.menu_smsV1)
    data object SmsV2 : DrawerDestination("SMSV2", R.string.menu_smsV2)
    data object SmsV3 : DrawerDestination("SMSV3", R.string.menu_smsV3)
    data object SmsV4 : DrawerDestination("SMSV4", R.string.menu_smsV4)
    data object SmsV5 : DrawerDestination("SMSV5", R.string.menu_smsV5)
    data object VoiceToTextV1 : DrawerDestination("VoiceToTextV1", R.string.menu_VoiceToTextV1)
    data object VoiceToTextV2 : DrawerDestination("VoiceToTextV2", R.string.menu_VoiceToTextV2)
    data object VoiceToTextV3 : DrawerDestination("VoiceToTextV3", R.string.menu_VoiceToTextV3)
    data object VoiceToTextV4 : DrawerDestination("VoiceToTextV4", R.string.menu_VoiceToTextV4)
    data object VoiceToTextV5 : DrawerDestination("VoiceToTextV5", R.string.menu_VoiceToTextV5)
    data object VoiceToTextV6 : DrawerDestination("VoiceToTextV6", R.string.menu_VoiceToTextV6)
    data object VoiceToTextV7 : DrawerDestination("VoiceToTextV7", R.string.menu_VoiceToTextV7)
    data object ChatWsV1 : DrawerDestination("ChatWsV1", R.string.menu_ChatWsV1)

    // 🔥 Add this block
    companion object {
        fun fromKey(key: String): DrawerDestination = when (key) {
            Composers.key -> Composers
            TestByKeshav.key -> TestByKeshav
            Droidcon.key -> Droidcon
            Gps.key -> Gps
            Sms.key -> Sms
            SmsV1.key -> SmsV1
            SmsV2.key -> SmsV2
            SmsV3.key -> SmsV3
            SmsV4.key -> SmsV4
            SmsV5.key -> SmsV5
            VoiceToTextV1.key -> VoiceToTextV1
            VoiceToTextV2.key -> VoiceToTextV2
            VoiceToTextV3.key -> VoiceToTextV3
            VoiceToTextV4.key -> VoiceToTextV4
            VoiceToTextV5.key -> VoiceToTextV5
            VoiceToTextV6.key -> VoiceToTextV6
            VoiceToTextV7.key -> VoiceToTextV7
            ChatWsV1.key -> ChatWsV1

            else -> Composers
        }
    }
}
