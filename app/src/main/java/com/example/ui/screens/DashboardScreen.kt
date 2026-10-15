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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.MessEntity
import com.example.domain.model.MessCalculationSummary
import com.example.ui.Screen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.AppStrings

@Composable
fun DashboardScreen(
    currentMess: MessEntity?,
    summary: MessCalculationSummary,
    pendingCount: Int,
    isBangla: Boolean,
    onNavigate: (Screen) -> Unit,
    currentMember: MemberEntity? = null,
    onRequestRejoin: () -> Unit = {},
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
        // If current member's status is REJECTED or REMOVED, show clear screen with "Request to join again"
        if (currentMember != null && (currentMember.status == "REJECTED" || currentMember.status == "REMOVED")) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rejected_removed_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBangla) "আপনার সদস্যপদ বাতিল বা অপসারণ করা হয়েছে" else "Membership Rejected or Removed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) 
                            "'${currentMess?.name ?: "মেস"}'-এ আপনার সদস্যপদ বর্তমানে সক্রিয় নয়। আপনি চাইলে পুনরায় যোগদানের জন্য অ্যাডমিনের কাছে অনুরোধ পাঠাতে পারেন।"
                        else 
                            "Your membership in '${currentMess?.name ?: "this mess"}' was rejected or removed. You can submit a request to join again.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRequestRejoin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_request_rejoin"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = if (isBangla) "পুনরায় যোগদানের অনুরোধ পাঠান" else "Request to join again",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onNavigate(Screen.MessSelect) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBangla) "অন্য মেস নির্বাচন করুন" else "Switch Mess")
                    }
                }
            }
            return
        }

        // If current member's status is PENDING, show waiting banner
        if (currentMember != null && currentMember.status == "PENDING" && currentMess?.adminId != currentMember.userId) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pending_approval_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBangla) "যোগদানের অনুরোধ অপেক্ষমান" else "Join Request Pending Approval",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) 
                            "'${currentMess?.name ?: "মেস"}'-এর অ্যাডমিন অনুমোদন দিলে আপনি সমস্ত হিসাব ও তথ্য দেখতে পাবেন।"
                        else 
                            "Waiting for '${currentMess?.name ?: "mess"}' admin approval. Once approved, you will have access to all details.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onNavigate(Screen.MessSelect) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBangla) "অন্য মেস নির্বাচন করুন" else "Switch Mess")
                    }
                }
            }
            return
        }

        // Pending Requests Alert Banner
        if (pendingCount > 0) {
            Card(
                onClick = { onNavigate(Screen.Members) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pending_alert_banner"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "$pendingCount জন নতুন সদস্য যোগ হতে চাচ্ছেন!" else "$pendingCount pending join request(s)!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = if (isBangla) "অনুমোদন দিতে এখানে ট্যাপ করুন" else "Tap to review and approve",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Current Month Header & Locked status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${AppStrings.t("month_label", isBangla)}: ${summary.month}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = AppStrings.t("nav_dashboard", isBangla),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (currentMess?.isClosed == true) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBangla) "মাস লকড" else "Locked",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Key Stat Cards Grid: Total Bazar, Total Meals, Meal Rate, Fixed Costs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Bazar Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_total_bazar"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.t("total_bazar", isBangla), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.formatTaka(summary.totalBazarCost),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Total Meals Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_total_meals"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.t("total_meals", isBangla), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.formatMeal(summary.totalMeals),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Live Meal Rate Card (Highlighted)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_meal_rate"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = AppStrings.t("current_meal_rate", isBangla),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = AppStrings.formatTaka(summary.mealRate),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (isBangla) "বাজার ÷ মোট মিল" else "Bazar ÷ Meals",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }

            // Fixed Costs Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_fixed_costs"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.t("nav_fixed_costs", isBangla), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.formatTaka(summary.totalFixedCosts),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Today's Meals Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_today_meals"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.t("today_meals", isBangla),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = { onNavigate(Screen.Meals) }) {
                        Text(if (isBangla) "মিল এন্ট্রি >" else "Entry >", fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MealStatItem(label = AppStrings.t("breakfast", isBangla), count = summary.todayBreakfastCount)
                    MealStatItem(label = AppStrings.t("lunch", isBangla), count = summary.todayLunchCount)
                    MealStatItem(label = AppStrings.t("dinner", isBangla), count = summary.todayDinnerCount)
                    MealStatItem(label = if (isBangla) "মোট" else "Total", count = summary.todayTotalMeals, isHighlighted = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onNavigate(Screen.Bazar) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("quick_add_bazar"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "+ বাজার" else "+ Bazar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onNavigate(Screen.Deposits) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("quick_add_deposit"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "+ জমা" else "+ Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onNavigate(Screen.Members) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("quick_members"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "সদস্য" else "Members", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Member Balances Summary Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.t("member_balances", isBangla),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { onNavigate(Screen.Summary) }) {
                Text(if (isBangla) "পূর্ণাঙ্গ হিসাব >" else "Full Statement >", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Member Cards List
        if (summary.memberCalculations.isEmpty()) {
            Text(
                text = if (isBangla) "কোনো সদস্য নেই" else "No members in this mess",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            summary.memberCalculations.forEach { memberCalc ->
                MemberBalanceCard(memberCalc = memberCalc, isBangla = isBangla)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MealStatItem(label: String, count: Float, isHighlighted: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = AppStrings.formatMeal(count),
            fontSize = 16.sp,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MemberBalanceCard(
    memberCalc: com.example.domain.model.MemberCalculation,
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    val isPositive = memberCalc.netBalance >= 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("member_balance_card_${memberCalc.memberId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = memberCalc.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (memberCalc.type == "MANAGED") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = AppStrings.t("managed_badge", isBangla),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${if (isBangla) "মিল" else "Meals"}: ${AppStrings.formatMeal(memberCalc.totalMeals)} | ${if (isBangla) "জমা" else "Dep"}: ${AppStrings.formatTaka(memberCalc.totalDeposits)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            // Balance Pill (Green = পাবে / gets back, Red = দিবে / owes)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPositive) SuccessGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isPositive) AppStrings.t("will_receive", isBangla) else AppStrings.t("will_give", isBangla),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) SuccessGreen else DangerRed
                    )
                    Text(
                        text = AppStrings.formatTaka(kotlin.math.abs(memberCalc.netBalance)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPositive) SuccessGreen else DangerRed
                    )
                }
            }
        }
    }
}
