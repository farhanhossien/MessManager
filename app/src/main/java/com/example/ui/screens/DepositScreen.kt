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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DepositEntity
import com.example.data.local.entities.MemberEntity
import com.example.ui.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    month: String,
    deposits: List<DepositEntity>,
    members: List<MemberEntity>,
    isBangla: Boolean,
    onAddDeposit: (memberId: String, date: String, amount: Double, method: String, note: String) -> Unit,
    onDeleteDeposit: (DepositEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var depositToDelete by remember { mutableStateOf<DepositEntity?>(null) }

    val membersById = members.associateBy { it.id }
    val totalDeposits = deposits.sumOf { it.amount }

    val paymentMethods = listOf(
        "BKASH" to AppStrings.t("method_bkash", isBangla),
        "NAGAD" to AppStrings.t("method_nagad", isBangla),
        "ROCKET" to AppStrings.t("method_rocket", isBangla),
        "CASH" to AppStrings.t("method_cash", isBangla),
        "BANK" to AppStrings.t("method_bank", isBangla)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("fab_add_deposit")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Deposit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Total Deposit Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${AppStrings.t("nav_deposits", isBangla)} ($month)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.formatTaka(totalDeposits),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppStrings.t("deposit_list", isBangla),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Deposits List
            if (deposits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.t("no_deposits", isBangla),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deposits) { deposit ->
                        val memberName = membersById[deposit.memberId]?.name ?: "সদস্য"
                        val methodLabel = paymentMethods.find { it.first == deposit.paymentMethod }?.second ?: deposit.paymentMethod

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_card_${deposit.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = memberName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = methodLabel,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = deposit.date,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    if (deposit.note.isNotBlank()) {
                                        Text(
                                            text = deposit.note,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = AppStrings.formatTaka(deposit.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    IconButton(
                                        onClick = { depositToDelete = deposit },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Deposit Dialog
    if (showAddDialog) {
        var memberId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
        var amountText by remember { mutableStateOf("") }
        var method by remember { mutableStateOf("BKASH") }
        var note by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }

        var memberExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(AppStrings.t("add_deposit_title", isBangla), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Member Dropdown
                    ExposedDropdownMenuBox(
                        expanded = memberExpanded,
                        onExpandedChange = { memberExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = membersById[memberId]?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(AppStrings.t("member_label", isBangla)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = memberExpanded,
                            onDismissRequest = { memberExpanded = false }
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        memberId = m.id
                                        memberExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(AppStrings.t("amount_label", isBangla)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input")
                    )

                    // Payment Method Options
                    Text(
                        text = AppStrings.t("payment_method", isBangla),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        paymentMethods.take(3).forEach { (mKey, mLabel) ->
                            FilterChip(
                                text = mLabel,
                                isSelected = method == mKey,
                                onClick = { method = mKey }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        paymentMethods.drop(3).forEach { (mKey, mLabel) ->
                            FilterChip(
                                text = mLabel,
                                isSelected = method == mKey,
                                onClick = { method = mKey }
                            )
                        }
                    }

                    // Date
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text(AppStrings.t("meal_date", isBangla)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Note
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(AppStrings.t("note_label", isBangla)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0 && memberId.isNotBlank()) {
                            onAddDeposit(memberId, date, amt, method, note)
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_deposit_button")
                ) {
                    Text(if (isBangla) "জমা দিন" else "Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Delete Confirmation
    if (depositToDelete != null) {
        val dep = depositToDelete!!
        AlertDialog(
            onDismissRequest = { depositToDelete = null },
            title = { Text(if (isBangla) "জমা এন্ট্রি ডিলিট" else "Delete Deposit") },
            text = { Text(if (isBangla) "আপনি কি এই জমা এন্ট্রিটি ডিলিট করতে চান?" else "Are you sure you want to delete this deposit?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDeposit(dep)
                        depositToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "ডিলিট" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { depositToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
