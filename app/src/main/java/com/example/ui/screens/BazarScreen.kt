package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BazarExpenseEntity
import com.example.data.local.entities.MemberEntity
import com.example.ui.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarScreen(
    month: String,
    expenses: List<BazarExpenseEntity>,
    members: List<MemberEntity>,
    isBangla: Boolean,
    onAddExpense: (buyerMemberId: String, date: String, amount: Double, category: String, note: String) -> Unit,
    onDeleteExpense: (BazarExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var expenseToDelete by remember { mutableStateOf<BazarExpenseEntity?>(null) }

    val membersById = members.associateBy { it.id }

    val categories = listOf(
        "rice" to AppStrings.t("cat_rice", isBangla),
        "meat" to AppStrings.t("cat_meat", isBangla),
        "fish" to AppStrings.t("cat_fish", isBangla),
        "vegetables" to AppStrings.t("cat_veg", isBangla),
        "dal" to AppStrings.t("cat_dal", isBangla),
        "oil" to AppStrings.t("cat_oil", isBangla),
        "spices" to AppStrings.t("cat_spices", isBangla),
        "others" to AppStrings.t("cat_others", isBangla)
    )

    val filteredExpenses = if (selectedCategoryFilter == null) {
        expenses
    } else {
        expenses.filter { it.category == selectedCategoryFilter }
    }

    val totalSpent = expenses.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_bazar")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bazar")
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
            // Month Total Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
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
                            text = "${AppStrings.t("total_bazar", isBangla)} ($month)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.formatTaka(totalSpent),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    text = if (isBangla) "সব" else "All",
                    isSelected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null }
                )
                categories.forEach { (catKey, catLabel) ->
                    FilterChip(
                        text = catLabel,
                        isSelected = selectedCategoryFilter == catKey,
                        onClick = { selectedCategoryFilter = catKey }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.t("no_expenses", isBangla),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExpenses) { expense ->
                        val buyerName = membersById[expense.buyerMemberId]?.name ?: "সদস্য"
                        val catLabel = categories.find { it.first == expense.category }?.second ?: expense.category

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_card_${expense.id}"),
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
                                            text = catLabel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${expense.date}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${AppStrings.t("buyer_label", isBangla)}: $buyerName",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (expense.note.isNotBlank()) {
                                        Text(
                                            text = expense.note,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = AppStrings.formatTaka(expense.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { expenseToDelete = expense },
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

    // Add Expense Dialog
    if (showAddDialog) {
        var buyerId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
        var amountText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("rice") }
        var note by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }

        var buyerExpanded by remember { mutableStateOf(false) }
        var categoryExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(AppStrings.t("add_expense_title", isBangla), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Buyer Dropdown
                    ExposedDropdownMenuBox(
                        expanded = buyerExpanded,
                        onExpandedChange = { buyerExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = membersById[buyerId]?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(AppStrings.t("buyer_label", isBangla)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buyerExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = buyerExpanded,
                            onDismissRequest = { buyerExpanded = false }
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        buyerId = m.id
                                        buyerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount Field
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(AppStrings.t("amount_label", isBangla)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )

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
                                        categoryExpanded = false
                                    }
                                )
                            }
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

                    // Note Field
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
                        if (amt > 0.0 && buyerId.isNotBlank()) {
                            onAddExpense(buyerId, date, amt, category, note)
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_bazar_button")
                ) {
                    Text(if (isBangla) "যুক্ত করুন" else "Add")
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
    if (expenseToDelete != null) {
        val exp = expenseToDelete!!
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text(if (isBangla) "খরচ ডিলিট" else "Delete Expense") },
            text = { Text(if (isBangla) "আপনি কি এই বাজার খরচটি ডিলিট করতে চান?" else "Are you sure you want to delete this expense?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(exp)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "ডিলিট" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("filter_chip_$text"),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = onClick,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            modifier = Modifier.height(24.dp)
        ) {
            Text(
                text = text,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
