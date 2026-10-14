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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.local.entities.FixedCostEntity
import com.example.data.local.entities.MemberEntity
import com.example.ui.util.AppStrings
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedCostScreen(
    month: String,
    fixedCosts: List<FixedCostEntity>,
    activeMembers: List<MemberEntity>,
    isBangla: Boolean,
    onBack: () -> Unit,
    onAddFixedCost: (title: String, category: String, amount: Double, splitType: String, customSharesJson: String) -> Unit,
    onDeleteFixedCost: (FixedCostEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var costToDelete by remember { mutableStateOf<FixedCostEntity?>(null) }

    val totalFixed = fixedCosts.sumOf { it.amount }
    val activeMemberCount = activeMembers.size
    val perMemberShare = if (activeMemberCount > 0) totalFixed / activeMemberCount.toDouble() else 0.0

    val categories = listOf(
        "maid" to AppStrings.t("cat_maid", isBangla),
        "gas" to AppStrings.t("cat_gas", isBangla),
        "electricity" to AppStrings.t("cat_electricity", isBangla),
        "wifi" to AppStrings.t("cat_wifi", isBangla),
        "rent" to AppStrings.t("cat_rent", isBangla),
        "water" to AppStrings.t("cat_water", isBangla),
        "others" to AppStrings.t("cat_others", isBangla)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_fixed_cost")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Fixed Cost")
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
            // Top Bar with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = AppStrings.t("fixed_cost_title", isBangla),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Fixed & Per Member Share Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                            text = "${if (isBangla) "মোট স্থায়ী খরচ" else "Total Shared Cost"} ($month)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = AppStrings.formatTaka(totalFixed),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${AppStrings.t("per_member_share", isBangla)} ($activeMemberCount জন)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = AppStrings.formatTaka(perMemberShare),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fixed Costs List
            if (fixedCosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "এই মাসে কোনো স্থায়ী খরচ যোগ করা হয়নি" else "No fixed costs added for this month yet",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fixedCosts) { cost ->
                        val catLabel = categories.find { it.first == cost.category }?.second ?: cost.category
                        val isCustom = cost.splitType == "CUSTOM"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fixed_cost_card_${cost.id}"),
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
                                            text = cost.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isCustom) MaterialTheme.colorScheme.tertiaryContainer
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isCustom) {
                                                    if (isBangla) "কাস্টম বণ্টন" else "Custom Split"
                                                } else {
                                                    if (isBangla) "সমান বণ্টন" else "Equal Split"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCustom) MaterialTheme.colorScheme.onTertiaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = catLabel,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = AppStrings.formatTaka(cost.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { costToDelete = cost },
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

    // Add Fixed Cost Dialog with EQUAL vs CUSTOM Split Options
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("maid") }
        var amountText by remember { mutableStateOf("") }
        var splitType by remember { mutableStateOf("EQUAL") }
        var categoryExpanded by remember { mutableStateOf(false) }

        // Per-member custom amounts
        val memberAmounts = remember {
            mutableStateMapOf<String, String>().apply {
                activeMembers.forEach { put(it.id, "") }
            }
        }

        // When custom amounts change, auto-calculate total
        val customTotal = memberAmounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(AppStrings.t("add_fixed_cost", isBangla), fontWeight = FontWeight.Bold) },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        val catLabel = categories.find { it.first == category }?.second ?: category
                        OutlinedTextField(
                            value = catLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(AppStrings.t("category_label", isBangla)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { (catKey, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        category = catKey
                                        if (title.isBlank()) title = label
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(AppStrings.t("title_label", isBangla)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Split Method Selection
                    Text(
                        text = if (isBangla) "বণ্টন পদ্ধতি নির্বাচন করুন:" else "Select Split Method:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = splitType == "EQUAL",
                            onClick = { splitType = "EQUAL" },
                            label = { Text(if (isBangla) "সমান বণ্টন (EQUAL)" else "Equal Split") }
                        )
                        FilterChip(
                            selected = splitType == "CUSTOM",
                            onClick = { splitType = "CUSTOM" },
                            label = { Text(if (isBangla) "কাস্টম বণ্টন (CUSTOM)" else "Custom Split") }
                        )
                    }

                    if (splitType == "EQUAL") {
                        // Total Amount for Equal Split
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text(AppStrings.t("amount_label", isBangla)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fixed_cost_amount_input")
                        )
                        if (amountText.toDoubleOrNull() != null && activeMemberCount > 0) {
                            val perPerson = (amountText.toDoubleOrNull() ?: 0.0) / activeMemberCount
                            Text(
                                text = "${if (isBangla) "জনপ্রতি" else "Per person"}: ${AppStrings.formatTaka(perPerson)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // CUSTOM Per-Member Inputs
                        Text(
                            text = if (isBangla) "প্রতিটি সদস্যের জন্য নির্দিষ্ট টাকা লিখুন:" else "Enter custom amount for each member:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        activeMembers.forEach { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = member.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1.5f)
                                )
                                OutlinedTextField(
                                    value = memberAmounts[member.id] ?: "",
                                    onValueChange = { memberAmounts[member.id] = it },
                                    label = { Text("৳") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("custom_amount_${member.id}")
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBangla) "মোট কাস্টম খরচ:" else "Total Custom Cost:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = AppStrings.formatTaka(customTotal),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (splitType == "EQUAL") {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0 && title.isNotBlank()) {
                                onAddFixedCost(title, category, amt, "EQUAL", "{}")
                                showAddDialog = false
                            }
                        } else {
                            if (customTotal > 0.0 && title.isNotBlank()) {
                                val json = JSONObject()
                                memberAmounts.forEach { (memId, amtStr) ->
                                    val amt = amtStr.toDoubleOrNull() ?: 0.0
                                    json.put(memId, amt)
                                }
                                onAddFixedCost(title, category, customTotal, "CUSTOM", json.toString())
                                showAddDialog = false
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_fixed_cost_button")
                ) {
                    Text(if (isBangla) "যোগ করুন" else "Add")
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
    if (costToDelete != null) {
        val cost = costToDelete!!
        AlertDialog(
            onDismissRequest = { costToDelete = null },
            title = { Text(if (isBangla) "স্থায়ী খরচ ডিলিট" else "Delete Fixed Cost") },
            text = { Text(if (isBangla) "আপনি কি '${cost.title}' ডিলিট করতে চান?" else "Are you sure you want to delete '${cost.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFixedCost(cost)
                        costToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "ডিলিট" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { costToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
