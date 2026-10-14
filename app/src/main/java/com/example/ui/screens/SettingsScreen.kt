package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.MessEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.util.AppStrings

@Composable
fun SettingsScreen(
    currentMess: MessEntity?,
    currentUser: UserEntity?,
    currentMember: MemberEntity?,
    isBangla: Boolean,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onToggleLanguage: (Boolean) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSwitchMess: () -> Unit,
    onLoadDemo: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = AppStrings.t("settings_title", isBangla),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBangla) "অ্যাপ পছন্দসমূহ" else "App Preferences",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Language Switcher Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = AppStrings.t("language_toggle", isBangla), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = if (isBangla) "বর্তমান: বাংলা" else "Current: English", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            text = "বাং",
                            isSelected = isBangla,
                            onClick = { onToggleLanguage(true) }
                        )
                        FilterChip(
                            text = "EN",
                            isSelected = !isBangla,
                            onClick = { onToggleLanguage(false) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Dark Theme Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = AppStrings.t("dark_mode", isBangla), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onToggleDarkTheme,
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mess Info Card
        if (currentMess != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.t("mess_info", isBangla),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${AppStrings.t("mess_name_label", isBangla)}: ${currentMess.name}", fontSize = 13.sp)
                    Text(text = "${AppStrings.t("invite_code_banner", isBangla)}: ${currentMess.inviteCode}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${AppStrings.t("cutoff_setting", isBangla)}: ${currentMess.cutoffHour}:00 PM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onSwitchMess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isBangla) "মেস পরিবর্তন করুন" else "Switch Mess", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Current User Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = AppStrings.t("current_user_info", isBangla),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${AppStrings.t("name_label", isBangla)}: ${currentUser?.name ?: ""}", fontSize = 13.sp)
                Text(text = "${AppStrings.t("phone_label", isBangla)}: ${currentUser?.phone ?: ""}", fontSize = 13.sp)
                if (currentMember != null) {
                    Text(text = "ভূমিকা: ${currentMember.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onLoadDemo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = AppStrings.t("load_demo", isBangla), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("settings_logout_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = AppStrings.t("logout", isBangla), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
