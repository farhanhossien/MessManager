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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MessEntity
import com.example.domain.model.MemberCalculation
import com.example.domain.model.MessCalculationSummary
import com.example.ui.Screen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.AppStrings

@Composable
fun MonthlySummaryScreen(
    currentMess: MessEntity?,
    summary: MessCalculationSummary,
    isAdmin: Boolean,
    canCloseMonth: Boolean = isAdmin,
    isBangla: Boolean,
    onCloseMonth: () -> Unit,
    onReopenMonth: () -> Unit,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCloseConfirmDialog by remember { mutableStateOf(false) }

    val isClosed = currentMess?.isClosed == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${AppStrings.t("monthly_summary_title", isBangla)} (${summary.month})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${AppStrings.t("current_meal_rate", isBangla)}: ${AppStrings.formatTaka(summary.mealRate)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Reports / Analytics Shortcut
            IconButton(
                onClick = onNavigateToReports,
                modifier = Modifier.testTag("nav_to_reports_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Reports",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Month Locked Banner or Close Month Action
        if (isClosed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.t("close_month_confirm", isBangla).take(20) + " (লকড)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    if (canCloseMonth) {
                        TextButton(onClick = onReopenMonth) {
                            Text(AppStrings.t("reopen_month_btn", isBangla), fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Summary Table Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Table Columns Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = AppStrings.t("member_name", isBangla),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2.2f)
                    )
                    Text(
                        text = AppStrings.t("meals_col", isBangla),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = if (isBangla) "মোট খরচ" else "Cost",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        text = AppStrings.t("balance_col", isBangla),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2.2f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Table Rows
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(summary.memberCalculations) { memberCalc ->
                        val isPositive = memberCalc.netBalance >= 0.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2.2f)) {
                                Text(
                                    text = memberCalc.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${if (isBangla) "জমা" else "Dep"}: ${AppStrings.formatTaka(memberCalc.totalDeposits)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Text(
                                text = AppStrings.formatMeal(memberCalc.totalMeals),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Text(
                                text = AppStrings.formatTaka(memberCalc.totalCost),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )

                            Column(
                                modifier = Modifier.weight(2.2f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = if (isPositive) AppStrings.t("will_receive", isBangla) else AppStrings.t("will_give", isBangla),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) SuccessGreen else DangerRed
                                )
                                Text(
                                    text = AppStrings.formatTaka(kotlin.math.abs(memberCalc.netBalance)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) SuccessGreen else DangerRed
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }

                HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Overall Totals Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBangla) "সর্বমোট হিসাব" else "Total Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${AppStrings.t("total_bazar", isBangla)}: ${AppStrings.formatTaka(summary.totalBazarCost)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Close Month Button
        if (canCloseMonth && !isClosed) {
            Button(
                onClick = { showCloseConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_close_month"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.t("close_month_btn", isBangla),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Confirmation Dialog for Closing Month
    if (showCloseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCloseConfirmDialog = false },
            title = { Text(AppStrings.t("close_month_btn", isBangla), fontWeight = FontWeight.Bold) },
            text = { Text(AppStrings.t("close_month_confirm", isBangla)) },
            confirmButton = {
                Button(
                    onClick = {
                        onCloseMonth()
                        showCloseConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_close_month_button")
                ) {
                    Text(if (isBangla) "হ্যাঁ, মাস ক্লোজ করুন" else "Yes, Close Month")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirmDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
