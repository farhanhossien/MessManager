package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MessEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.util.AppStrings

@Composable
fun MessSelectScreen(
    user: UserEntity?,
    messes: List<MessEntity>,
    isBangla: Boolean,
    onSelectMess: (MessEntity) -> Unit,
    onCreateMess: (name: String) -> Unit,
    onJoinMess: (code: String) -> Unit,
    onLoadDemo: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var messNameInput by remember { mutableStateOf("") }
    var inviteCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Welcome Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isBangla) "স্বাগতম, ${user?.name ?: "ব্যবহারকারী"}!" else "Welcome, ${user?.name ?: "User"}!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = user?.phone ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                TextButton(onClick = onLogout) {
                    Text(AppStrings.t("logout", isBangla), color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row: Create & Join
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_create_mess_dialog"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "নতুন মেস" else "New Mess",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = { showJoinDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_join_mess_dialog"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "কোড দিয়ে যুক্ত" else "Join with Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Demo Mess Card (Instant Calculations Check)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("demo_load_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.t("load_demo", isBangla),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = if (isBangla) "৫ জন সদস্য, মিল, বাজার খরচ ও জমা সহ প্রি-লোডেড মেস" else "5 members, meals, bazar and deposit calculations preloaded",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
                Button(
                    onClick = onLoadDemo,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("load_demo_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = AppStrings.t("my_messes", isBangla),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (messes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.t("no_messes_yet", isBangla),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messes) { mess ->
                    Card(
                        onClick = { onSelectMess(mess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mess_card_${mess.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mess.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "${AppStrings.t("invite_code_banner", isBangla)}: ${mess.inviteCode}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "• ${mess.currentMonth}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Mess Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(AppStrings.t("start_mess_title", isBangla), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (isBangla) "আপনার মেস বা হোস্টেলের নাম লিখুন। আপনি স্বয়ংক্রিয়ভাবে এই মেসের অ্যাডমিন হবেন।" else "Enter mess/hostel name. You will be the admin of this mess.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = messNameInput,
                        onValueChange = { messNameInput = it },
                        label = { Text(AppStrings.t("mess_name_label", isBangla)) },
                        placeholder = { Text(AppStrings.t("mess_name_hint", isBangla)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_mess_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (messNameInput.isNotBlank()) {
                            onCreateMess(messNameInput)
                            messNameInput = ""
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_mess_button")
                ) {
                    Text(AppStrings.t("btn_create_mess", isBangla))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Join Mess Dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text(AppStrings.t("join_mess_title", isBangla), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (isBangla) "মেস অ্যাডমিনের কাছ থেকে পাওয়া ৬ অক্ষরের ইনভাইট কোড দিন (যেমন: MX4K7P)।" else "Enter the 6-character mess invite code (e.g. MX4K7P).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { if (it.length <= 6) inviteCodeInput = it.uppercase() },
                        label = { Text(AppStrings.t("invite_code_label", isBangla)) },
                        placeholder = { Text(AppStrings.t("invite_code_hint", isBangla)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("join_mess_code_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteCodeInput.isNotBlank()) {
                            onJoinMess(inviteCodeInput)
                            inviteCodeInput = ""
                            showJoinDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_join_mess_button")
                ) {
                    Text(AppStrings.t("btn_send_request", isBangla))
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
