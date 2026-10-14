package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.BazarExpenseEntity
import com.example.data.local.entities.DepositEntity
import com.example.data.local.entities.FixedCostEntity
import com.example.data.local.entities.MealEntryEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.MessEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.MessRepository
import com.example.domain.model.MessCalculationSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object MessSelect : Screen("mess_select")
    object Dashboard : Screen("dashboard")
    object Meals : Screen("meals")
    object Bazar : Screen("bazar")
    object Deposits : Screen("deposits")
    object Summary : Screen("summary")
    object Members : Screen("members")
    object FixedCosts : Screen("fixed_costs")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = MessRepository(db)
    private val prefs = application.getSharedPreferences("meal_manager_prefs", Context.MODE_PRIVATE)

    // Language & Theme
    private val _isBangla = MutableStateFlow(prefs.getBoolean("is_bangla", true))
    val isBangla: StateFlow<Boolean> = _isBangla.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Auth State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Current Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Auth)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Current Mess
    private val _currentMess = MutableStateFlow<MessEntity?>(null)
    val currentMess: StateFlow<MessEntity?> = _currentMess.asStateFlow()

    // Selected Month & Meal Date
    private val _selectedMonth = MutableStateFlow(repository.getCurrentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedMealDate = MutableStateFlow(repository.getTodayString())
    val selectedMealDate: StateFlow<String> = _selectedMealDate.asStateFlow()

    // Snackbar / Feedback
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Flow for Mess Calculation Summary
    val calculationSummary: StateFlow<MessCalculationSummary> = _currentMess.flatMapLatest { mess ->
        if (mess != null) {
            _selectedMonth.flatMapLatest { month ->
                repository.observeMessCalculationSummary(mess.id, month)
            }
        } else {
            flowOf(MessCalculationSummary())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MessCalculationSummary())

    // Active Members
    val activeMembers: StateFlow<List<MemberEntity>> = _currentMess.flatMapLatest { mess ->
        if (mess != null) repository.observeActiveMembers(mess.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending Requests
    val pendingRequests: StateFlow<List<MemberEntity>> = _currentMess.flatMapLatest { mess ->
        if (mess != null) repository.observePendingMembers(mess.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending Count
    val pendingCount: StateFlow<Int> = _currentMess.flatMapLatest { mess ->
        if (mess != null) repository.observePendingCount(mess.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current User's Member Profile in Current Mess
    val currentMemberProfile: StateFlow<MemberEntity?> = _currentMess.flatMapLatest { mess ->
        val user = _currentUser.value
        if (mess != null && user != null) {
            repository.observeMemberForUser(mess.id, user.id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User's Messes
    val userMesses: StateFlow<List<MessEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.observeUserMesses(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Meals for Selected Date
    val mealsForSelectedDate: StateFlow<List<MealEntryEntity>> = _currentMess.flatMapLatest { mess ->
        if (mess != null) {
            _selectedMealDate.flatMapLatest { date ->
                repository.observeMealsByDate(mess.id, date)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses for Selected Month
    val expensesForMonth: StateFlow<List<BazarExpenseEntity>> = _currentMess.flatMapLatest { mess ->
        if (mess != null) {
            _selectedMonth.flatMapLatest { month ->
                repository.observeExpensesForMonth(mess.id, month)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Deposits for Selected Month
    val depositsForMonth: StateFlow<List<DepositEntity>> = _currentMess.flatMapLatest { mess ->
        if (mess != null) {
            _selectedMonth.flatMapLatest { month ->
                repository.observeDepositsForMonth(mess.id, month)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fixed Costs for Selected Month
    val fixedCostsForMonth: StateFlow<List<FixedCostEntity>> = _currentMess.flatMapLatest { mess ->
        if (mess != null) {
            _selectedMonth.flatMapLatest { month ->
                repository.observeFixedCostsForMonth(mess.id, month)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month Lock Status
    val isMonthLocked: StateFlow<Boolean> = _currentMess.flatMapLatest { mess ->
        if (mess != null) {
            _selectedMonth.flatMapLatest { month ->
                db.monthlyClosingDao().observeClosings(mess.id).map { closings ->
                    closings.any { it.month == month }
                }
            }
        } else {
            flowOf(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Surface every Firestore snapshot listener error with a snackbar
        viewModelScope.launch {
            repository.syncErrorEvents.collect { errorMsg ->
                showSnackbar(errorMsg)
            }
        }

        // Restore session from Firebase Auth currentUser or local prefs
        val authUser = repository.auth.currentUser
        val savedUserId = authUser?.uid ?: prefs.getString("user_id", null)
        if (savedUserId != null) {
            viewModelScope.launch {
                val user = repository.getUserById(savedUserId)
                if (user != null) {
                    _currentUser.value = user
                    // Restore memberships and messes across devices from Firestore
                    repository.restoreUserMembershipsAndMesses(user.id)

                    val savedMessId = prefs.getString("mess_id", null)
                    if (savedMessId != null) {
                        db.messDao().getMessById(savedMessId)?.let {
                            selectMess(it)
                            return@launch
                        }
                    }
                    _currentScreen.value = Screen.MessSelect
                }
            }
        }
    }

    fun setLanguage(bangla: Boolean) {
        _isBangla.value = bangla
        prefs.edit().putBoolean("is_bangla", bangla).apply()
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
        prefs.edit().putBoolean("is_dark_theme", dark).apply()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setSelectedMealDate(date: String) {
        _selectedMealDate.value = date
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    // Auth Actions
    fun register(name: String, phone: String, pin: String) {
        viewModelScope.launch {
            val res = repository.registerUser(name, phone, pin)
            res.onSuccess { user ->
                _currentUser.value = user
                prefs.edit().putString("user_id", user.id).apply()
                // Fetch memberships from Firestore so reinstall / existing data is synced
                repository.restoreUserMembershipsAndMesses(user.id)
                _currentScreen.value = Screen.MessSelect
                showSnackbar(if (_isBangla.value) "স্বাগতম ${user.name}! সফলভাবে রেজিস্টার হয়েছে।" else "Welcome ${user.name}! Registered successfully.")
            }.onFailure { err ->
                val msg = err.localizedMessage ?: ""
                if (msg.contains("already in use", ignoreCase = true) || msg.contains("collision", ignoreCase = true)) {
                    login(phone, pin)
                } else {
                    showSnackbar(err.localizedMessage ?: "Registration failed")
                }
            }
        }
    }

    fun login(phone: String, pin: String) {
        viewModelScope.launch {
            val res = repository.loginUser(phone, pin)
            res.onSuccess { user ->
                _currentUser.value = user
                prefs.edit().putString("user_id", user.id).apply()
                // Fetch memberships from Firestore so mess lists survive reinstall and new devices
                repository.restoreUserMembershipsAndMesses(user.id)
                _currentScreen.value = Screen.MessSelect
                showSnackbar(if (_isBangla.value) "স্বাগতম, ${user.name}!" else "Welcome back, ${user.name}!")
            }.onFailure { err ->
                showSnackbar(err.localizedMessage ?: "Login failed")
            }
        }
    }

    fun logout() {
        repository.stopRealtimeSync()
        repository.auth.signOut()
        _currentUser.value = null
        _currentMess.value = null
        prefs.edit().remove("user_id").remove("mess_id").apply()
        _currentScreen.value = Screen.Auth
    }

    fun sendPasswordReset(phoneOrEmail: String) {
        viewModelScope.launch {
            val res = repository.sendPasswordReset(phoneOrEmail)
            res.onSuccess {
                showSnackbar(
                    if (_isBangla.value) "পাসওয়ার্ড রিসেট ইমেইল সফলভাবে পাঠানো হয়েছে।"
                    else "Password reset email sent successfully."
                )
            }.onFailure { err ->
                showSnackbar(err.localizedMessage ?: "Failed to send reset link")
            }
        }
    }

    // Mess Selection & Creation
    fun selectMess(mess: MessEntity) {
        _currentMess.value = mess
        val targetMonth = mess.currentMonth.ifBlank { repository.getCurrentMonthString() }
        _selectedMonth.value = targetMonth
        prefs.edit().putString("mess_id", mess.id).apply()

        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                val member = db.memberDao().getMemberByMessAndUser(mess.id, user.id)
                val isAdmin = mess.adminId == user.id || member?.role == "ADMIN"
                val isPending = member?.status == "PENDING"

                if (isPending && !isAdmin) {
                    // Single doc listener for PENDING so joiner sees ACTIVE/REJECTED instantly
                    // Do NOT attach the full members collection listener until status is ACTIVE
                    repository.stopRealtimeSync()
                    repository.listenToPendingMembership(mess.id, user.id) { updatedMember ->
                        if (updatedMember.status == "ACTIVE") {
                            repository.stopPendingMemberListener()
                            repository.startRealtimeSync(mess.id, targetMonth)
                            showSnackbar(
                                if (_isBangla.value) "'${mess.name}'-এ আপনার অনুরোধ অনুমোদিত হয়েছে!"
                                else "Your request to join '${mess.name}' was approved!"
                            )
                        } else if (updatedMember.status == "REJECTED") {
                            repository.stopPendingMemberListener()
                            showSnackbar(
                                if (_isBangla.value) "'${mess.name}'-এ আপনার অনুরোধ বাতিল করা হয়েছে।"
                                else "Your request to join '${mess.name}' was rejected."
                            )
                        }
                    }
                } else {
                    repository.stopPendingMemberListener()
                    repository.startRealtimeSync(mess.id, targetMonth)
                }
            }
        } else {
            repository.startRealtimeSync(mess.id, targetMonth)
        }
        _currentScreen.value = Screen.Dashboard
    }

    fun createMess(name: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val mess = repository.createMess(user.id, user.name, user.phone, name)
                selectMess(mess)
                showSnackbar(if (_isBangla.value) "'${mess.name}' মেস তৈরি হয়েছে! কোড: ${mess.inviteCode}" else "'${mess.name}' created! Code: ${mess.inviteCode}")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "মেস তৈরি ব্যর্থ: ${e.localizedMessage}" else "Failed to create mess: ${e.localizedMessage}")
            }
        }
    }

    fun joinMess(code: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.joinMessByInviteCode(user.id, user.name, user.phone, code)
            res.onSuccess { mess ->
                selectMess(mess)
                showSnackbar(if (_isBangla.value) "'${mess.name}'-এ যোগদানের অনুরোধ পাঠানো হয়েছে। অ্যাডমিনের অনুমোদনের অপেক্ষা করুন।" else "Join request sent for '${mess.name}'. Waiting for admin approval.")
            }.onFailure { err ->
                showSnackbar(err.localizedMessage ?: "Failed to join mess")
            }
        }
    }

    fun regenerateInviteCode() {
        val mess = _currentMess.value ?: return
        viewModelScope.launch {
            try {
                val newCode = repository.regenerateInviteCode(mess.id)
                _currentMess.value = mess.copy(inviteCode = newCode)
                showSnackbar(if (_isBangla.value) "নতুন ইনভাইট কোড: $newCode" else "New invite code: $newCode")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "কোড তৈরিতে ব্যর্থ: ${e.localizedMessage}" else "Failed to regenerate code: ${e.localizedMessage}")
            }
        }
    }

    // Member Management
    fun approveMember(memberId: String) {
        viewModelScope.launch {
            try {
                repository.approveMember(memberId)
                showSnackbar(if (_isBangla.value) "সদস্য অনুমোদন করা হয়েছে" else "Member approved")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "অনুমোদন ব্যর্থ: ${e.localizedMessage}" else "Failed to approve: ${e.localizedMessage}")
            }
        }
    }

    fun rejectMember(memberId: String) {
        viewModelScope.launch {
            try {
                repository.rejectMember(memberId)
                showSnackbar(if (_isBangla.value) "অনুরোধ বাতিল করা হয়েছে" else "Request rejected")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "বাতিল ব্যর্থ: ${e.localizedMessage}" else "Failed to reject: ${e.localizedMessage}")
            }
        }
    }

    fun addManagedMember(name: String, phone: String?, openingBalance: Double) {
        val mess = _currentMess.value ?: return
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                repository.addManagedMember(mess.id, user.id, name, phone, openingBalance)
                showSnackbar(if (_isBangla.value) "ম্যানেজড সদস্য '$name' যোগ করা হয়েছে" else "Managed member '$name' added")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "সদস্য যোগ করা যায়নি: ${e.localizedMessage}" else "Failed to add member: ${e.localizedMessage}")
            }
        }
    }



    fun updateMemberRole(memberId: String, newRole: String) {
        viewModelScope.launch {
            try {
                repository.updateMemberRole(memberId, newRole)
                showSnackbar(if (_isBangla.value) "ভূমিকা পরিবর্তন করা হয়েছে" else "Role updated")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "ভূমিকা পরিবর্তন ব্যর্থ: ${e.localizedMessage}" else "Failed to update role: ${e.localizedMessage}")
            }
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch {
            try {
                repository.removeMember(memberId)
                showSnackbar(if (_isBangla.value) "সদস্য সরানো হয়েছে" else "Member removed")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "সদস্য সরানো ব্যর্থ: ${e.localizedMessage}" else "Failed to remove member: ${e.localizedMessage}")
            }
        }
    }

    fun resetMemberPinByAdmin(member: MemberEntity, onResult: (String?) -> Unit) {
        val mess = _currentMess.value ?: return
        val targetUid = member.userId ?: member.id
        viewModelScope.launch {
            val res = repository.resetMemberPinByAdmin(targetUid, member.phone, mess.id)
            res.onSuccess { tempPin ->
                showSnackbar(
                    if (_isBangla.value) "'${member.name}'-এর নতুন পিন: $tempPin"
                    else "Temp PIN for '${member.name}': $tempPin"
                )
                onResult(tempPin)
            }.onFailure { err ->
                showSnackbar(err.localizedMessage ?: "Failed to reset PIN")
                onResult(null)
            }
        }
    }

    // Meal Operations
    fun saveMeal(
        memberId: String,
        date: String,
        breakfast: Float,
        lunch: Float,
        dinner: Float,
        guestMeals: Float = 0f,
        note: String = ""
    ) {
        val mess = _currentMess.value ?: return
        viewModelScope.launch {
            try {
                repository.saveMealEntry(mess.id, memberId, date, breakfast, lunch, dinner, guestMeals, note)
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "মিল এন্ট্রি ব্যর্থ: ${e.localizedMessage}" else "Failed to save meal: ${e.localizedMessage}")
            }
        }
    }

    // Bazar / Expense Operations
    fun addBazarExpense(buyerMemberId: String, date: String, amount: Double, category: String, note: String) {
        val mess = _currentMess.value ?: return
        viewModelScope.launch {
            try {
                repository.addBazarExpense(mess.id, buyerMemberId, date, amount, category, note)
                showSnackbar(if (_isBangla.value) "বাজার খরচ যুক্ত হয়েছে (৳$amount)" else "Bazar expense added (৳$amount)")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "বাজার খরচ যুক্ত করা যায়নি: ${e.localizedMessage}" else "Failed to add expense: ${e.localizedMessage}")
            }
        }
    }

    fun deleteBazarExpense(expense: BazarExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.deleteBazarExpense(expense)
                showSnackbar(if (_isBangla.value) "বাজার খরচ ডিলিট করা হয়েছে" else "Expense deleted")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "ডিলিট ব্যর্থ: ${e.localizedMessage}" else "Failed to delete: ${e.localizedMessage}")
            }
        }
    }

    // Deposits
    fun addDeposit(memberId: String, date: String, amount: Double, method: String, note: String) {
        val mess = _currentMess.value ?: return
        viewModelScope.launch {
            try {
                repository.addDeposit(mess.id, memberId, date, amount, method, note)
                showSnackbar(if (_isBangla.value) "জমা যুক্ত হয়েছে (৳$amount)" else "Deposit added (৳$amount)")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "জমা যুক্ত করা যায়নি: ${e.localizedMessage}" else "Failed to add deposit: ${e.localizedMessage}")
            }
        }
    }

    fun deleteDeposit(deposit: DepositEntity) {
        viewModelScope.launch {
            try {
                repository.deleteDeposit(deposit)
                showSnackbar(if (_isBangla.value) "জমা এন্ট্রি ডিলিট করা হয়েছে" else "Deposit deleted")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "ডিলিট ব্যর্থ: ${e.localizedMessage}" else "Failed to delete: ${e.localizedMessage}")
            }
        }
    }

    // Fixed Costs
    fun addFixedCost(title: String, category: String, amount: Double, splitType: String = "EQUAL", customSharesJson: String = "{}") {
        val mess = _currentMess.value ?: return
        viewModelScope.launch {
            try {
                repository.addFixedCost(mess.id, title, category, amount, _selectedMonth.value, splitType, customSharesJson)
                showSnackbar(if (_isBangla.value) "স্থায়ী খরচ যোগ হয়েছে (৳$amount)" else "Fixed cost added (৳$amount)")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "স্থায়ী খরচ যোগ করা যায়নি: ${e.localizedMessage}" else "Failed to add cost: ${e.localizedMessage}")
            }
        }
    }

    fun deleteFixedCost(cost: FixedCostEntity) {
        viewModelScope.launch {
            try {
                repository.deleteFixedCost(cost)
                showSnackbar(if (_isBangla.value) "স্থায়ী খরচ ডিলিট করা হয়েছে" else "Fixed cost deleted")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "ডিলিট ব্যর্থ: ${e.localizedMessage}" else "Failed to delete: ${e.localizedMessage}")
            }
        }
    }

    // Close Month
    fun closeMonth() {
        val mess = _currentMess.value ?: return
        viewModelScope.launch {
            val res = repository.closeMonth(mess.id, _selectedMonth.value)
            res.onSuccess { nextMonth ->
                _selectedMonth.value = nextMonth
                repository.startRealtimeSync(mess.id, nextMonth)
                showSnackbar(
                    if (_isBangla.value)
                        "চলতি মাস ক্লোজ করা হয়েছে! নতুন মাস ($nextMonth) শুরু হয়েছে এবং ব্যালেন্স ক্যারি-ফরওয়ার্ড হয়েছে।"
                    else
                        "Month closed! Advanced to $nextMonth with balances carried forward."
                )
            }.onFailure { err ->
                showSnackbar(err.localizedMessage ?: "Failed to close month")
            }
        }
    }

    fun reopenMonth() {
        val mess = _currentMess.value ?: return
        viewModelScope.launch {
            try {
                repository.reopenMonth(mess.id, _selectedMonth.value)
                showSnackbar(if (_isBangla.value) "মাসের হিসাব আনলক করা হয়েছে ও ব্যালেন্স রিস্টোর হয়েছে" else "Month unlocked and previous balances restored")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "আনলক ব্যর্থ: ${e.localizedMessage}" else "Failed to unlock: ${e.localizedMessage}")
            }
        }
    }

    // Seed Demo
    fun loadDemoData() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val mess = repository.seedDemoData(user.id, user.name, user.phone)
                selectMess(mess)
                showSnackbar(if (_isBangla.value) "ডেমো মেস 'পদ্মা ভিলা' এবং সদস্য, মিল, বাজার খরচ লোড হয়েছে!" else "Demo mess 'Padma Villa' with members, meals, and expenses loaded!")
            } catch (e: Exception) {
                showSnackbar(if (_isBangla.value) "ডেমো লোড ব্যর্থ: ${e.localizedMessage}" else "Failed to load demo: ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopRealtimeSync()
    }
}
