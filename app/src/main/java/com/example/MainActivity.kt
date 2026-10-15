package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BazarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.FixedCostScreen
import com.example.ui.screens.MealScreen
import com.example.ui.screens.MembersScreen
import com.example.ui.screens.MessSelectScreen
import com.example.ui.screens.MonthlySummaryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MealManagerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isBangla by viewModel.isBangla.collectAsState()

            val currentUser by viewModel.currentUser.collectAsState()
            val currentMess by viewModel.currentMess.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val currentMemberProfile by viewModel.currentMemberProfile.collectAsState()

            val calculationSummary by viewModel.calculationSummary.collectAsState()
            val activeMembers by viewModel.activeMembers.collectAsState()
            val pendingRequests by viewModel.pendingRequests.collectAsState()
            val pendingCount by viewModel.pendingCount.collectAsState()
            val userMesses by viewModel.userMesses.collectAsState()

            val selectedMealDate by viewModel.selectedMealDate.collectAsState()
            val mealsForDate by viewModel.mealsForSelectedDate.collectAsState()
            val expenses by viewModel.expensesForMonth.collectAsState()
            val deposits by viewModel.depositsForMonth.collectAsState()
            val fixedCosts by viewModel.fixedCostsForMonth.collectAsState()
            val selectedMonth by viewModel.selectedMonth.collectAsState()
            val isMonthLocked by viewModel.isMonthLocked.collectAsState()

            val snackbarMsg by viewModel.snackbarMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            val isAdmin = currentMemberProfile?.role == "ADMIN" || currentMess?.adminId == currentUser?.id
            val isManager = currentMemberProfile?.role == "MANAGER"
            val canCloseMonth = isAdmin || isManager

            LaunchedEffect(snackbarMsg) {
                snackbarMsg?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearSnackbar()
                }
            }

            // BackHandler handling
            BackHandler(enabled = currentScreen != Screen.Auth) {
                when (currentScreen) {
                    Screen.MessSelect -> {
                        // Stay on MessSelect
                    }
                    Screen.Dashboard -> {
                        if (userMesses.isNotEmpty()) {
                            viewModel.navigateTo(Screen.MessSelect)
                        }
                    }
                    Screen.Meals, Screen.Bazar, Screen.Deposits, Screen.Summary -> {
                        viewModel.navigateTo(Screen.Dashboard)
                    }
                    Screen.Members, Screen.FixedCosts, Screen.Reports, Screen.Settings -> {
                        viewModel.navigateTo(Screen.Dashboard)
                    }
                    Screen.Auth -> {}
                }
            }

            MealManagerTheme(darkTheme = isDarkTheme) {
                val showBottomNav = currentMess != null && (
                    currentScreen == Screen.Dashboard ||
                    currentScreen == Screen.Meals ||
                    currentScreen == Screen.Bazar ||
                    currentScreen == Screen.Deposits ||
                    currentScreen == Screen.Summary
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (currentScreen != Screen.Auth) {
                            AppTopBar(
                                currentScreen = currentScreen,
                                currentMess = currentMess,
                                isBangla = isBangla,
                                pendingCount = pendingCount,
                                onNavigate = { viewModel.navigateTo(it) },
                                onToggleLanguage = { viewModel.setLanguage(!isBangla) },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomNav) {
                            AppBottomNavBar(
                                currentScreen = currentScreen,
                                isBangla = isBangla,
                                onTabSelected = { viewModel.navigateTo(it) }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    when (currentScreen) {
                        Screen.Auth -> {
                            AuthScreen(
                                isBangla = isBangla,
                                onLogin = { phone, pin -> viewModel.login(phone, pin) },
                                onRegister = { name, phone, pin -> viewModel.register(name, phone, pin) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.MessSelect -> {
                            MessSelectScreen(
                                user = currentUser,
                                messes = userMesses,
                                isBangla = isBangla,
                                onSelectMess = { viewModel.selectMess(it) },
                                onCreateMess = { viewModel.createMess(it) },
                                onJoinMess = { viewModel.joinMess(it) },
                                onLogout = { viewModel.logout() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Dashboard -> {
                            DashboardScreen(
                                currentMess = currentMess,
                                summary = calculationSummary,
                                pendingCount = pendingCount,
                                isBangla = isBangla,
                                onNavigate = { viewModel.navigateTo(it) },
                                currentMember = currentMemberProfile,
                                onRequestRejoin = { viewModel.requestRejoin() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Meals -> {
                            MealScreen(
                                selectedDate = selectedMealDate,
                                members = activeMembers,
                                meals = mealsForDate,
                                isBangla = isBangla,
                                onDateSelected = { viewModel.setSelectedMealDate(it) },
                                onSaveMeal = { mId, d, b, l, din, g, note ->
                                    if (isMonthLocked) {
                                        viewModel.showSnackbar(if (isBangla) "এই মাসটি লকড! পরিবর্তন করতে হলে মাস আনলক করুন।" else "This month is locked! Unlock the month to edit.")
                                    } else {
                                        viewModel.saveMeal(mId, d, b, l, din, g, note)
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Bazar -> {
                            BazarScreen(
                                month = selectedMonth,
                                expenses = expenses,
                                members = activeMembers,
                                isBangla = isBangla,
                                onAddExpense = { buyerId, d, amt, cat, note ->
                                    if (isMonthLocked) {
                                        viewModel.showSnackbar(if (isBangla) "এই মাসটি লকড! নতুন বাজার যোগ করা যাবে না।" else "This month is locked! Cannot add expenses.")
                                    } else {
                                        viewModel.addBazarExpense(buyerId, d, amt, cat, note)
                                    }
                                },
                                onDeleteExpense = {
                                    if (isMonthLocked) {
                                        viewModel.showSnackbar(if (isBangla) "এই মাসটি লকড!" else "This month is locked!")
                                    } else {
                                        viewModel.deleteBazarExpense(it)
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Deposits -> {
                            DepositScreen(
                                month = selectedMonth,
                                deposits = deposits,
                                members = activeMembers,
                                isBangla = isBangla,
                                onAddDeposit = { mId, d, amt, method, note ->
                                    if (isMonthLocked) {
                                        viewModel.showSnackbar(if (isBangla) "এই মাসটি লকড! নতুন জমা দেওয়া যাবে না।" else "This month is locked! Cannot add deposits.")
                                    } else {
                                        viewModel.addDeposit(mId, d, amt, method, note)
                                    }
                                },
                                onDeleteDeposit = {
                                    if (isMonthLocked) {
                                        viewModel.showSnackbar(if (isBangla) "এই মাসটি লকড!" else "This month is locked!")
                                    } else {
                                        viewModel.deleteDeposit(it)
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Summary -> {
                            MonthlySummaryScreen(
                                currentMess = currentMess,
                                summary = calculationSummary,
                                isAdmin = isAdmin,
                                canCloseMonth = canCloseMonth,
                                isBangla = isBangla,
                                onCloseMonth = { viewModel.closeMonth() },
                                onReopenMonth = { viewModel.reopenMonth() },
                                onNavigateToReports = { viewModel.navigateTo(Screen.Reports) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Members -> {
                            MembersScreen(
                                currentMess = currentMess,
                                activeMembers = activeMembers,
                                pendingMembers = pendingRequests,
                                isAdmin = isAdmin,
                                isBangla = isBangla,
                                onBack = { viewModel.navigateTo(Screen.Dashboard) },
                                onApprove = { viewModel.approveMember(it) },
                                onReject = { viewModel.rejectMember(it) },
                                onAddManagedMember = { name, phone, bal ->
                                    viewModel.addManagedMember(name, phone, bal)
                                },
                                onUpdateRole = { mId, role -> viewModel.updateMemberRole(mId, role) },
                                onRemoveMember = { viewModel.removeMember(it) },
                                onRegenerateCode = { viewModel.regenerateInviteCode() },
                                onShowSnackbar = { viewModel.showSnackbar(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.FixedCosts -> {
                            FixedCostScreen(
                                month = selectedMonth,
                                fixedCosts = fixedCosts,
                                activeMembers = activeMembers,
                                isBangla = isBangla,
                                onBack = { viewModel.navigateTo(Screen.Dashboard) },
                                onAddFixedCost = { title, cat, amt, splitType, customSharesJson ->
                                    if (isMonthLocked) {
                                        viewModel.showSnackbar(if (isBangla) "এই মাসটি লকড!" else "This month is locked!")
                                    } else {
                                        viewModel.addFixedCost(title, cat, amt, splitType, customSharesJson)
                                    }
                                },
                                onDeleteFixedCost = {
                                    if (isMonthLocked) {
                                        viewModel.showSnackbar(if (isBangla) "এই মাসটি লকড!" else "This month is locked!")
                                    } else {
                                        viewModel.deleteFixedCost(it)
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Reports -> {
                            ReportsScreen(
                                currentMess = currentMess,
                                summary = calculationSummary,
                                expenses = expenses,
                                isBangla = isBangla,
                                onBack = { viewModel.navigateTo(Screen.Dashboard) },
                                onShowSnackbar = { viewModel.showSnackbar(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.Settings -> {
                            SettingsScreen(
                                currentMess = currentMess,
                                currentUser = currentUser,
                                currentMember = currentMemberProfile,
                                isBangla = isBangla,
                                isDarkTheme = isDarkTheme,
                                onBack = { viewModel.navigateTo(Screen.Dashboard) },
                                onToggleLanguage = { viewModel.setLanguage(it) },
                                onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                                onSwitchMess = { viewModel.navigateTo(Screen.MessSelect) },
                                onLoadDemo = { viewModel.loadDemoData() },
                                onLogout = { viewModel.logout() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
