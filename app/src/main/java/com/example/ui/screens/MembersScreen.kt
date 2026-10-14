package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.MessEntity
import com.example.ui.util.AppStrings

@Composable
fun MembersScreen(
    currentMess: MessEntity?,
    activeMembers: List<MemberEntity>,
    pendingMembers: List<MemberEntity>,
    isAdmin: Boolean,
    isBangla: Boolean,
    onBack: () -> Unit,
    onApprove: (memberId: String) -> Unit,
    onReject: (memberId: String) -> Unit,
    onAddManagedMember: (name: String, phone: String?, openingBalance: Double) -> Unit,
    onResetPin: (phone: String) -> Unit,
    onUpdateRole: (memberId: String, newRole: String) -> Unit,
    onRemoveMember: (memberId: String) -> Unit,
    onRegenerateCode: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onResetPinByAdmin: (MemberEntity, (String?) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddManagedDialog by remember { mutableStateOf(false) }
    var memberToResetPin by remember { mutableStateOf<MemberEntity?>(null) }
    var memberToRemove by remember { mutableStateOf<MemberEntity?>(null) }
    var generatedTempPin by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = AppStrings.t("members_title", isBangla),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mess Invite Code Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = AppStrings.t("invite_code_banner", isBangla),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentMess?.inviteCode ?: "------",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Copy Button
                        IconButton(
                            onClick = {
                                val code = currentMess?.inviteCode ?: return@IconButton
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Mess Invite Code", code))
                                onShowSnackbar(if (isBangla) "ইনভাইট কোড কপি হয়েছে: $code" else "Invite code copied: $code")
                            },
                            modifier = Modifier.testTag("copy_invite_code_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = MaterialTheme.colorScheme.primary)
                        }

                        // Share Button (WhatsApp / SMS / Intent)
                        IconButton(
                            onClick = {
                                val code = currentMess?.inviteCode ?: return@IconButton
                                val shareText = if (isBangla) {
                                    "আমাদের '${currentMess.name}' মেসে যুক্ত হতে ইনভাইট কোড ব্যবহার করুন: $code\nমেস ম্যানেজার অ্যাপ ডাউনলোড করে যুক্ত হোন!"
                                } else {
                                    "Join our mess '${currentMess.name}' with invite code: $code\nDownload Meal Manager app to join!"
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Invite Code"))
                            },
                            modifier = Modifier.testTag("share_invite_code_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Code", tint = MaterialTheme.colorScheme.primary)
                        }

                        // Regenerate Code (Admin only)
                        if (isAdmin) {
                            IconButton(onClick = onRegenerateCode) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate Code", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Managed Member Button
        if (isAdmin) {
            OutlinedButton(
                onClick = { showAddManagedDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_add_managed_member"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.t("add_managed_member", isBangla),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Pending Requests List
        if (pendingMembers.isNotEmpty()) {
            Text(
                text = "${AppStrings.t("pending_requests", isBangla)} (${pendingMembers.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(6.dp))

            pendingMembers.forEach { pending ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("pending_request_card_${pending.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = pending.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = pending.phone ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        if (isAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onApprove(pending.id) },
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("approve_btn_${pending.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.t("btn_approve", isBangla), fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { onReject(pending.id) },
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("reject_btn_${pending.id}"),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Members List Header
        Text(
            text = "${if (isBangla) "সক্রিয় সদস্য" else "Active Members"} (${activeMembers.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activeMembers) { member ->
                var menuExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_member_card_${member.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (member.role) {
                                            "ADMIN" -> MaterialTheme.colorScheme.primaryContainer
                                            "MANAGER" -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = when (member.role) {
                                        "ADMIN" -> MaterialTheme.colorScheme.primary
                                        "MANAGER" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    RoleBadge(role = member.role, type = member.type, isBangla = isBangla)
                                }
                                Text(
                                    text = member.phone ?: (if (isBangla) "কোনো ফোন নম্বর নেই" else "No phone"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Admin Context Menu (Reset PIN, Promote, Remove)
                        if (isAdmin && member.role != "ADMIN") {
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    if (member.userId != null) {
                                        DropdownMenuItem(
                                            text = { Text(AppStrings.t("reset_pin_title", isBangla)) },
                                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                memberToResetPin = member
                                            }
                                        )
                                    }

                                    if (member.role == "MEMBER") {
                                        DropdownMenuItem(
                                            text = { Text(AppStrings.t("promote_manager", isBangla)) },
                                            onClick = {
                                                menuExpanded = false
                                                onUpdateRole(member.id, "MANAGER")
                                            }
                                        )
                                    } else if (member.role == "MANAGER") {
                                        DropdownMenuItem(
                                            text = { Text(AppStrings.t("demote_member", isBangla)) },
                                            onClick = {
                                                menuExpanded = false
                                                onUpdateRole(member.id, "MEMBER")
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text(AppStrings.t("remove_member", isBangla), color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            menuExpanded = false
                                            memberToRemove = member
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Managed Member Dialog
    if (showAddManagedDialog) {
        var mName by remember { mutableStateOf("") }
        var mPhone by remember { mutableStateOf("") }
        var mBalanceText by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddManagedDialog = false },
            title = { Text(AppStrings.t("add_managed_member", isBangla), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppStrings.t("managed_member_desc", isBangla),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = mName,
                        onValueChange = { mName = it },
                        label = { Text(AppStrings.t("name_label", isBangla)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("managed_name_input")
                    )
                    OutlinedTextField(
                        value = mPhone,
                        onValueChange = { mPhone = it },
                        label = { Text("${AppStrings.t("phone_label", isBangla)} (${if (isBangla) "ঐচ্ছিক" else "Optional"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mBalanceText,
                        onValueChange = { mBalanceText = it },
                        label = { Text(if (isBangla) "পূর্ববর্তী ব্যালেন্স (ঐচ্ছিক)" else "Opening Balance (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mName.isNotBlank()) {
                            val bal = mBalanceText.toDoubleOrNull() ?: 0.0
                            onAddManagedMember(mName, mPhone.ifBlank { null }, bal)
                            showAddManagedDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_managed_button")
                ) {
                    Text(if (isBangla) "যোগ করুন" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddManagedDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Firebase Password Reset & Admin-Assisted PIN Reset Dialog
    if (memberToResetPin != null) {
        val targetMember = memberToResetPin!!

        AlertDialog(
            onDismissRequest = { memberToResetPin = null },
            title = { Text(if (isBangla) "পিন / পাসওয়ার্ড রিসেট - ${targetMember.name}" else "PIN / Password Reset - ${targetMember.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBangla) 
                            "সদস্য: ${targetMember.name} (${targetMember.phone ?: "ফোন নেই"})\n\nআপনি অ্যাডমিন হিসেবে এই সদস্যের জন্য ক্লাউড ফাংশন দিয়ে নতুন এককালীন ৬-ডিজিট টেম্পোরারি পিন তৈরি করতে পারেন, অথবা পাসওয়ার্ড রিসেট ইমেইল পাঠাতে পারেন।"
                        else 
                            "Member: ${targetMember.name} (${targetMember.phone ?: "No phone"})\n\nAs Admin, you can generate a one-time 6-digit temp PIN via Cloud Function (calls Admin SDK), or send a password reset link to their verified email."
                    )

                    Button(
                        onClick = {
                            val target = targetMember
                            memberToResetPin = null
                            onResetPinByAdmin(target) { pin ->
                                if (pin != null) {
                                    generatedTempPin = Pair(target.name, pin)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_admin_generate_temp_pin"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isBangla) "এককালীন টেম্প পিন তৈরি করুন (Admin SDK)" else "Generate One-Time Temp PIN (Admin SDK)")
                    }

                    OutlinedButton(
                        onClick = {
                            val ph = targetMember.phone
                            if (!ph.isNullOrBlank()) {
                                onResetPin(ph)
                            } else {
                                onShowSnackbar(if (isBangla) "কোনো ফোন নম্বর নেই" else "No phone number available")
                            }
                            memberToResetPin = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_reset_pin_button")
                    ) {
                        Text(if (isBangla) "রিসেট ইমেইল লিংক পাঠান" else "Send Reset Email Link")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { memberToResetPin = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Display Generated One-Time Temp PIN Dialog
    if (generatedTempPin != null) {
        val (memName, pin) = generatedTempPin!!
        AlertDialog(
            onDismissRequest = { generatedTempPin = null },
            title = { Text(if (isBangla) "নতুন টেম্প পিন তৈরি হয়েছে!" else "Temporary PIN Generated!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla)
                            "সদস্য '$memName'-এর জন্য নতুন ওয়ান-টাইম পিন:"
                        else
                            "New one-time temporary PIN for '$memName':"
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = pin,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                                .testTag("generated_temp_pin_text")
                        )
                    }
                    Text(
                        text = if (isBangla)
                            "এই ৬ ডিজিটের পিনটি সদস্যকে দিন। সদস্য এই পিন দিয়ে এখনই লগইন করতে পারবেন।"
                        else
                            "Share this 6-digit PIN with the member. They can log in with this PIN immediately.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Temp PIN", pin)
                        clipboard.setPrimaryClip(clip)
                        onShowSnackbar(if (isBangla) "পিন ক্লিপবোর্ডে কপি হয়েছে: $pin" else "PIN copied to clipboard: $pin")
                        generatedTempPin = null
                    },
                    modifier = Modifier.testTag("btn_copy_temp_pin")
                ) {
                    Text(if (isBangla) "কপি করুন" else "Copy PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { generatedTempPin = null }) {
                    Text(if (isBangla) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // Remove Member Dialog
    if (memberToRemove != null) {
        val target = memberToRemove!!
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text(AppStrings.t("remove_member", isBangla)) },
            text = { Text(if (isBangla) "আপনি কি নিশ্চিত '${target.name}'-কে মেস থেকে সরাতে চান?" else "Are you sure you want to remove '${target.name}' from this mess?") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveMember(target.id)
                        memberToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "সরিয়ে দিন" else "Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun RoleBadge(role: String, type: String, isBangla: Boolean) {
    val (label, bg, text) = when {
        type == "MANAGED" -> Triple(AppStrings.t("managed_badge", isBangla), MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        role == "ADMIN" -> Triple(AppStrings.t("admin_badge", isBangla), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        role == "MANAGER" -> Triple(AppStrings.t("manager_badge", isBangla), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        else -> Triple(AppStrings.t("member_badge", isBangla), MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = text)
    }
}
