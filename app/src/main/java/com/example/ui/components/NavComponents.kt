package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MessEntity
import com.example.ui.Screen
import com.example.ui.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: Screen,
    currentMess: MessEntity?,
    isBangla: Boolean,
    pendingCount: Int,
    onNavigate: (Screen) -> Unit,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.testTag("app_top_bar"),
        title = {
            Text(
                text = currentMess?.name ?: AppStrings.t("app_name", isBangla),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1
            )
        },
        actions = {
            // Language Toggle Button (বাং / EN)
            TextButton(
                onClick = onToggleLanguage,
                modifier = Modifier.testTag("language_toggle_button")
            ) {
                Text(
                    text = if (isBangla) "বাং" else "EN",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (currentMess != null) {
                // Pending Requests Badge Icon
                IconButton(
                    onClick = { onNavigate(Screen.Members) },
                    modifier = Modifier.testTag("pending_requests_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (pendingCount > 0) {
                                Badge { Text("$pendingCount") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Members & Requests",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Overflow Menu
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("overflow_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("nav_fixed_costs", isBangla)) },
                        leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onNavigate(Screen.FixedCosts)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("nav_reports", isBangla)) },
                        leadingIcon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onNavigate(Screen.Reports)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("my_messes", isBangla)) },
                        leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onNavigate(Screen.MessSelect)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("nav_settings", isBangla)) },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onNavigate(Screen.Settings)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("logout", isBangla)) },
                        onClick = {
                            showMenu = false
                            onLogout()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun AppBottomNavBar(
    currentScreen: Screen,
    isBangla: Boolean,
    onTabSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(Screen.Dashboard, AppStrings.t("nav_dashboard", isBangla), Icons.Default.Dashboard),
        Triple(Screen.Meals, AppStrings.t("nav_meals", isBangla), Icons.Default.Restaurant),
        Triple(Screen.Bazar, AppStrings.t("nav_bazar", isBangla), Icons.Default.ReceiptLong),
        Triple(Screen.Deposits, AppStrings.t("nav_deposits", isBangla), Icons.Default.AccountBalanceWallet),
        Triple(Screen.Summary, AppStrings.t("nav_summary", isBangla), Icons.Default.Assessment)
    )

    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp(6f)
    ) {
        items.forEach { (screen, label, icon) ->
            val isSelected = currentScreen.route == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(screen) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}
