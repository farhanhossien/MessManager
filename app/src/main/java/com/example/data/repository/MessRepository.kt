package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entities.BazarExpenseEntity
import com.example.data.local.entities.DepositEntity
import com.example.data.local.entities.FixedCostEntity
import com.example.data.local.entities.MealEntryEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.MessEntity
import com.example.data.local.entities.MonthlyClosingEntity
import com.example.data.local.entities.UserEntity
import com.example.domain.model.MemberCalculation
import com.example.domain.model.MessCalculationSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MessRepository(
    private val database: AppDatabase,
    val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val userDao = database.userDao()
    private val messDao = database.messDao()
    private val memberDao = database.memberDao()
    private val mealDao = database.mealDao()
    private val bazarDao = database.bazarDao()
    private val depositDao = database.depositDao()
    private val fixedCostDao = database.fixedCostDao()
    private val monthlyClosingDao = database.monthlyClosingDao()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Flow for real-time Firestore sync & listener error messages
    private val _syncErrorEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val syncErrorEvents: SharedFlow<String> = _syncErrorEvents.asSharedFlow()

    // Active Firestore snapshot listeners
    private var messListener: ListenerRegistration? = null
    private var membersListener: ListenerRegistration? = null
    private var mealsListener: ListenerRegistration? = null
    private var expensesListener: ListenerRegistration? = null
    private var depositsListener: ListenerRegistration? = null
    private var fixedCostsListener: ListenerRegistration? = null
    private var closingsListener: ListenerRegistration? = null

    // Single doc listener for PENDING membership status
    private var pendingMemberListener: ListenerRegistration? = null

    // --- Authentication with Firebase Email/Password ---
    fun phoneToEmail(phone: String): String {
        val clean = phone.trim().replace("[^0-9]".toRegex(), "")
        return "$clean@mealmanager.app"
    }

    fun cleanPhoneNumber(phone: String): String {
        return phone.trim().replace("[^0-9]".toRegex(), "")
    }

    suspend fun registerUser(name: String, phone: String, pin: String): Result<UserEntity> {
        val cleanPhone = cleanPhoneNumber(phone)
        val cleanName = name.trim()

        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be blank."))
        }
        if (cleanPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number cannot be blank."))
        }
        if (pin.length != 6) {
            return Result.failure(IllegalArgumentException("PIN must be exactly 6 digits."))
        }

        val email = phoneToEmail(cleanPhone)

        val authResult = try {
            auth.createUserWithEmailAndPassword(email, pin.trim()).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val authUser = authResult.user ?: return Result.failure(IllegalStateException("Firebase Auth UID is null"))
        val uid = authUser.uid

        val user = UserEntity(
            id = uid,
            phone = cleanPhone,
            name = cleanName,
            email = email,
            createdAt = System.currentTimeMillis()
        )

        userDao.insertUser(user)

        // Save to Firestore users/{uid}
        try {
            firestore.collection("users").document(uid).set(user).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(user)
    }

    suspend fun loginUser(phone: String, pin: String): Result<UserEntity> {
        val cleanPhone = cleanPhoneNumber(phone)
        if (cleanPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number cannot be blank."))
        }
        if (pin.length != 6) {
            return Result.failure(IllegalArgumentException("PIN must be exactly 6 digits."))
        }

        val email = phoneToEmail(cleanPhone)

        val authResult = try {
            auth.signInWithEmailAndPassword(email, pin.trim()).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val uid = authResult.user?.uid ?: return Result.failure(IllegalStateException("Firebase Auth UID is null"))

        var user: UserEntity? = null
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            user = doc.toObject(UserEntity::class.java)
        } catch (e: Exception) {
            Log.w("MessRepository", "Firestore user fetch error: ${e.message}")
        }

        if (user == null) {
            user = userDao.getUserById(uid)
        }

        if (user == null) {
            user = UserEntity(
                id = uid,
                phone = cleanPhone,
                name = "User",
                email = email,
                createdAt = System.currentTimeMillis()
            )
        }

        userDao.insertUser(user)

        // Fetch memberships across devices on successful login
        restoreUserMembershipsAndMesses(uid)

        return Result.success(user)
    }

    suspend fun restoreUserMembershipsAndMesses(userId: String): Result<List<MessEntity>> {
        return try {
            val membersSnap = firestore.collectionGroup("members")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val restoredMesses = mutableListOf<MessEntity>()
            for (doc in membersSnap.documents) {
                val member = doc.toObject(MemberEntity::class.java)
                if (member != null) {
                    memberDao.insertMember(member)
                    val messId = member.messId.ifEmpty { doc.reference.parent.parent?.id ?: "" }
                    if (messId.isNotEmpty()) {
                        try {
                            val messDoc = firestore.collection("messes").document(messId).get().await()
                            messDoc.toObject(MessEntity::class.java)?.let { mess ->
                                messDao.insertMess(mess)
                                restoredMesses.add(mess)
                            }
                        } catch (e: Exception) {
                            Log.w("MessRepository", "Failed to fetch mess $messId: ${e.message}")
                        }
                    }
                }
            }
            Result.success(restoredMesses)
        } catch (e: Exception) {
            Log.w("MessRepository", "restoreUserMembershipsAndMesses error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): UserEntity? {
        var user = userDao.getUserById(userId)
        if (user == null) {
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                user = doc.toObject(UserEntity::class.java)
                if (user != null) userDao.insertUser(user)
            } catch (e: Exception) {
                Log.w("MessRepository", "Failed to fetch user by ID: ${e.message}")
            }
        }
        return user
    }

    suspend fun sendPasswordReset(phoneOrEmail: String): Result<Unit> {
        return try {
            val target = phoneOrEmail.trim()
            val emailToSend = if (target.contains("@")) {
                target
            } else {
                val clean = cleanPhoneNumber(target)
                phoneToEmail(clean)
            }
            auth.sendPasswordResetEmail(emailToSend).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Mess Operations ---
    fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun getCurrentMonthString(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getNextMonthString(currentMonth: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(currentMonth) ?: Date()
            cal.add(Calendar.MONTH, 1)
            sdf.format(cal.time)
        } catch (e: Exception) {
            getCurrentMonthString()
        }
    }

    suspend fun createMess(userId: String, userName: String, userPhone: String, messName: String): MessEntity {
        val messId = UUID.randomUUID().toString()
        val code = generateInviteCode()
        val currentMonth = getCurrentMonthString()

        val mess = MessEntity(
            id = messId,
            name = messName.trim(),
            inviteCode = code,
            adminId = userId, // adminId is the Firebase Auth UID
            currentMonth = currentMonth
        )
        messDao.insertMess(mess)

        // Account member doc id in Firestore is the user's Firebase UID
        val adminMember = MemberEntity(
            id = userId,
            messId = messId,
            userId = userId,
            name = userName,
            phone = userPhone,
            role = "ADMIN",
            type = "ACCOUNT",
            status = "ACTIVE",
            joinDate = getTodayString(),
            createdBy = null
        )
        memberDao.insertMember(adminMember)

        // 1. Cloud sync to Firestore messes collection
        firestore.collection("messes").document(messId).set(mess).await()
        firestore.collection("messes").document(messId)
            .collection("members").document(adminMember.id).set(adminMember).await()

        // 2. Separate inviteCodes collection mapping code to messId
        firestore.collection("inviteCodes").document(code).set(
            hashMapOf(
                "code" to code,
                "messId" to messId,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()

        return mess
    }

    suspend fun regenerateInviteCode(messId: String): String {
        val mess = messDao.getMessById(messId) ?: throw IllegalStateException("Mess not found")
        val oldCode = mess.inviteCode
        val newCode = generateInviteCode()
        val updated = mess.copy(inviteCode = newCode)
        messDao.updateMess(updated)

        // Create new inviteCodes document
        firestore.collection("inviteCodes").document(newCode).set(
            hashMapOf(
                "code" to newCode,
                "messId" to messId,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()

        // Delete old invite code document if it existed
        if (oldCode.isNotBlank()) {
            try {
                firestore.collection("inviteCodes").document(oldCode).delete().await()
            } catch (e: Exception) {
                Log.w("MessRepository", "Old invite code delete error: ${e.message}")
            }
        }

        firestore.collection("messes").document(messId).update("inviteCode", newCode).await()
        return newCode
    }

    suspend fun joinMessByInviteCode(userId: String, userName: String, userPhone: String, code: String): Result<MessEntity> {
        val cleanCode = code.trim().uppercase()

        // 1. Look up messId from separate inviteCodes collection
        val codeDoc = try {
            firestore.collection("inviteCodes").document(cleanCode).get().await()
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to verify invite code: ${e.localizedMessage}"))
        }

        if (!codeDoc.exists()) {
            return Result.failure(Exception("Invalid invite code. No mess found with code $cleanCode."))
        }

        val targetMessId = codeDoc.getString("messId")
        if (targetMessId.isNullOrBlank()) {
            return Result.failure(Exception("Invalid invite code mapping."))
        }

        // 2. Check local mess & member status
        var mess = messDao.getMessById(targetMessId)

        if (mess?.adminId == userId) {
            return Result.failure(Exception("You are already the admin of this mess. You cannot join your own mess."))
        }

        val localMember = memberDao.getMemberByMessAndUser(targetMessId, userId)
        if (localMember != null) {
            if (localMember.status == "ACTIVE") {
                return Result.failure(Exception("You are already an active member of this mess."))
            } else if (localMember.status == "PENDING") {
                return Result.failure(Exception("Your join request is already pending admin approval."))
            }
        }

        // 3. Check remote Firestore membership in case local Room was not in sync
        try {
            val remoteMemberDoc = firestore.collection("messes").document(targetMessId)
                .collection("members").document(userId).get().await()
            if (remoteMemberDoc.exists()) {
                val remoteMember = remoteMemberDoc.toObject(MemberEntity::class.java)
                if (remoteMember != null) {
                    memberDao.insertMember(remoteMember)
                    if (remoteMember.status == "ACTIVE") {
                        return Result.failure(Exception("You are already an active member of this mess."))
                    } else if (remoteMember.status == "PENDING") {
                        return Result.failure(Exception("Your join request is already pending admin approval."))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("MessRepository", "Remote member check: ${e.message}")
        }

        // 4. Create new PENDING request in Firestore with doc ID = userId
        val member = MemberEntity(
            id = userId, // Account member uses Firebase UID as member doc id!
            messId = targetMessId,
            userId = userId,
            name = userName,
            phone = userPhone,
            role = "MEMBER",
            type = "ACCOUNT",
            status = "PENDING",
            joinDate = getTodayString(),
            createdBy = null
        )
        firestore.collection("messes").document(targetMessId)
            .collection("members").document(member.id).set(member).await()
        memberDao.insertMember(member)

        // 5. Read messes/{targetMessId} now that member doc exists
        try {
            val remoteMessDoc = firestore.collection("messes").document(targetMessId).get().await()
            val remoteMess = remoteMessDoc.toObject(MessEntity::class.java)
            if (remoteMess != null) {
                mess = remoteMess
                messDao.insertMess(remoteMess)
            }
        } catch (e: Exception) {
            Log.w("MessRepository", "Failed to fetch mess doc after joining: ${e.message}")
        }

        if (mess == null) {
            mess = MessEntity(
                id = targetMessId,
                name = "Mess ($cleanCode)",
                inviteCode = cleanCode,
                adminId = "",
                currentMonth = getCurrentMonthString()
            )
            messDao.insertMess(mess)
        }

        return Result.success(mess)
    }

    suspend fun approveMember(memberId: String) {
        val member = memberDao.getMemberById(memberId) ?: throw IllegalStateException("Member not found")
        val updated = member.copy(status = "ACTIVE")
        memberDao.updateMember(updated)
        firestore.collection("messes").document(member.messId)
            .collection("members").document(memberId).update("status", "ACTIVE").await()
    }

    suspend fun rejectMember(memberId: String) {
        val member = memberDao.getMemberById(memberId) ?: throw IllegalStateException("Member not found")
        val updated = member.copy(status = "REJECTED")
        memberDao.updateMember(updated)
        firestore.collection("messes").document(member.messId)
            .collection("members").document(memberId).update("status", "REJECTED").await()
    }

    suspend fun addManagedMember(messId: String, adminUid: String, name: String, phone: String?, openingBalance: Double = 0.0): MemberEntity {
        val member = MemberEntity(
            id = UUID.randomUUID().toString(), // Managed members get a random id
            messId = messId,
            userId = null,
            name = name.trim(),
            phone = phone?.trim()?.ifEmpty { null },
            role = "MEMBER",
            type = "MANAGED",
            status = "ACTIVE",
            joinDate = getTodayString(),
            openingBalance = openingBalance,
            createdBy = adminUid // createdBy field set to admin UID
        )
        memberDao.insertMember(member)
        firestore.collection("messes").document(messId)
            .collection("members").document(member.id).set(member).await()
        return member
    }

    suspend fun updateMemberRole(memberId: String, newRole: String) {
        val member = memberDao.getMemberById(memberId) ?: throw IllegalStateException("Member not found")
        val updated = member.copy(role = newRole)
        memberDao.updateMember(updated)
        firestore.collection("messes").document(member.messId)
            .collection("members").document(memberId).update("role", newRole).await()
    }

    suspend fun removeMember(memberId: String) {
        val member = memberDao.getMemberById(memberId) ?: throw IllegalStateException("Member not found")
        val updated = member.copy(status = "REMOVED", leaveDate = getTodayString())
        memberDao.updateMember(updated)
        firestore.collection("messes").document(member.messId)
            .collection("members").document(memberId).update(
                mapOf("status" to "REMOVED", "leaveDate" to getTodayString())
            ).await()
    }

    // --- PENDING Single-Doc Listener ---
    fun listenToPendingMembership(messId: String, userId: String, onStatusChanged: (MemberEntity) -> Unit) {
        stopPendingMemberListener()
        pendingMemberListener = firestore.collection("messes").document(messId)
            .collection("members").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val msg = "Pending membership listener error: ${error.localizedMessage ?: "Sync error"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshot?.toObject(MemberEntity::class.java)?.let { member ->
                    repositoryScope.launch {
                        memberDao.insertMember(member)
                    }
                    onStatusChanged(member)
                }
            }
    }

    fun stopPendingMemberListener() {
        pendingMemberListener?.remove()
        pendingMemberListener = null
    }

    // --- Start Realtime Firestore Sync for Mess ---
    fun startRealtimeSync(messId: String, month: String) {
        stopRealtimeSync()

        // 1. Listen for Mess Metadata
        messListener = firestore.collection("messes").document(messId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val msg = "Mess sync error: ${error.localizedMessage ?: "Permission or sync failure"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshot?.toObject(MessEntity::class.java)?.let { mess ->
                    repositoryScope.launch { messDao.insertMess(mess) }
                }
            }

        // 2. Listen for Members
        membersListener = firestore.collection("messes").document(messId)
            .collection("members")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    val msg = "Members sync error: ${error.localizedMessage ?: "Permission or sync failure"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshots?.documents?.forEach { doc ->
                    doc.toObject(MemberEntity::class.java)?.let { member ->
                        repositoryScope.launch { memberDao.insertMember(member) }
                    }
                }
            }

        // 3. Listen for Meals
        mealsListener = firestore.collection("messes").document(messId)
            .collection("meals")
            .whereGreaterThanOrEqualTo("date", "$month-01")
            .whereLessThanOrEqualTo("date", "$month-31")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    val msg = "Meals sync error: ${error.localizedMessage ?: "Permission or sync failure"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshots?.documents?.forEach { doc ->
                    doc.toObject(MealEntryEntity::class.java)?.let { meal ->
                        repositoryScope.launch { mealDao.insertOrUpdateMeal(meal) }
                    }
                }
            }

        // 4. Listen for Bazar Expenses
        expensesListener = firestore.collection("messes").document(messId)
            .collection("expenses")
            .whereGreaterThanOrEqualTo("date", "$month-01")
            .whereLessThanOrEqualTo("date", "$month-31")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    val msg = "Bazar sync error: ${error.localizedMessage ?: "Permission or sync failure"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshots?.documents?.forEach { doc ->
                    doc.toObject(BazarExpenseEntity::class.java)?.let { expense ->
                        repositoryScope.launch { bazarDao.insertExpense(expense) }
                    }
                }
            }

        // 5. Listen for Deposits
        depositsListener = firestore.collection("messes").document(messId)
            .collection("deposits")
            .whereGreaterThanOrEqualTo("date", "$month-01")
            .whereLessThanOrEqualTo("date", "$month-31")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    val msg = "Deposits sync error: ${error.localizedMessage ?: "Permission or sync failure"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshots?.documents?.forEach { doc ->
                    doc.toObject(DepositEntity::class.java)?.let { deposit ->
                        repositoryScope.launch { depositDao.insertDeposit(deposit) }
                    }
                }
            }

        // 6. Listen for Fixed Costs
        fixedCostsListener = firestore.collection("messes").document(messId)
            .collection("fixedCosts")
            .whereEqualTo("month", month)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    val msg = "Fixed costs sync error: ${error.localizedMessage ?: "Permission or sync failure"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshots?.documents?.forEach { doc ->
                    doc.toObject(FixedCostEntity::class.java)?.let { cost ->
                        repositoryScope.launch { fixedCostDao.insertFixedCost(cost) }
                    }
                }
            }

        // 7. Listen for Monthly Closings
        closingsListener = firestore.collection("messes").document(messId)
            .collection("closings")
            .whereEqualTo("month", month)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    val msg = "Closings sync error: ${error.localizedMessage ?: "Permission or sync failure"}"
                    Log.w("MessRepository", msg)
                    _syncErrorEvents.tryEmit(msg)
                    return@addSnapshotListener
                }
                snapshots?.documents?.forEach { doc ->
                    doc.toObject(MonthlyClosingEntity::class.java)?.let { closing ->
                        repositoryScope.launch { monthlyClosingDao.insertClosing(closing) }
                    }
                }
            }
    }

    // --- Admin-Assisted PIN Reset via Cloud Function ---
    suspend fun resetMemberPinByAdmin(targetUserId: String, targetPhone: String?, messId: String): Result<String> {
        return try {
            val functions = FirebaseFunctions.getInstance()
            val data = hashMapOf(
                "uid" to targetUserId,
                "phone" to (targetPhone ?: ""),
                "messId" to messId
            )
            val result = functions.getHttpsCallable("resetMemberPin").call(data).await()
            val resultData = result.data as? Map<*, *>
            val serverPin = resultData?.get("tempPin")?.toString()
                ?: throw IllegalStateException("Cloud function resetMemberPin did not return a tempPin")
            Result.success(serverPin)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Cloud function error"
            Log.e("MessRepository", "Cloud function resetMemberPin failed: $msg", e)
            Result.failure(e)
        }
    }

    fun stopRealtimeSync() {
        stopPendingMemberListener()
        messListener?.remove()
        membersListener?.remove()
        mealsListener?.remove()
        expensesListener?.remove()
        depositsListener?.remove()
        fixedCostsListener?.remove()
        closingsListener?.remove()
    }

    // --- Observable Flows ---
    fun observeUserMesses(userId: String): Flow<List<MessEntity>> = messDao.observeUserMesses(userId)
    fun observeMess(messId: String): Flow<MessEntity?> = messDao.observeMessById(messId)
    fun observeActiveMembers(messId: String): Flow<List<MemberEntity>> = memberDao.observeActiveMembers(messId)
    fun observePendingMembers(messId: String): Flow<List<MemberEntity>> = memberDao.observePendingMembers(messId)
    fun observePendingCount(messId: String): Flow<Int> = memberDao.observePendingCount(messId)
    fun observeAllMembers(messId: String): Flow<List<MemberEntity>> = memberDao.observeAllMembers(messId)
    fun observeMealsForDate(messId: String, date: String): Flow<List<MealEntryEntity>> = mealDao.observeMealsByDate(messId, date)
    fun observeMealsByDate(messId: String, date: String): Flow<List<MealEntryEntity>> = mealDao.observeMealsByDate(messId, date)
    fun observeMealsForMonth(messId: String, month: String): Flow<List<MealEntryEntity>> = mealDao.observeMealsForMonth(messId, month)
    fun observeExpensesForMonth(messId: String, month: String): Flow<List<BazarExpenseEntity>> = bazarDao.observeExpensesForMonth(messId, month)
    fun observeDepositsForMonth(messId: String, month: String): Flow<List<DepositEntity>> = depositDao.observeDepositsForMonth(messId, month)
    fun observeFixedCostsForMonth(messId: String, month: String): Flow<List<FixedCostEntity>> = fixedCostDao.observeFixedCostsForMonth(messId, month)
    fun observeClosing(messId: String, month: String): Flow<MonthlyClosingEntity?> = monthlyClosingDao.observeClosing(messId, month)
    fun observeMemberProfile(messId: String, userId: String): Flow<MemberEntity?> = memberDao.observeMemberByMessAndUser(messId, userId)
    fun observeMemberForUser(messId: String, userId: String): Flow<MemberEntity?> = memberDao.observeMemberByMessAndUser(messId, userId)

    fun observeMessCalculationSummary(messId: String, month: String): Flow<MessCalculationSummary> {
        return kotlinx.coroutines.flow.combine(
            memberDao.observeMembersForCalculation(messId),
            mealDao.observeMealsForMonth(messId, month),
            bazarDao.observeExpensesForMonth(messId, month),
            depositDao.observeDepositsForMonth(messId, month),
            fixedCostDao.observeFixedCostsForMonth(messId, month)
        ) { members, meals, expenses, deposits, fixedCosts ->
            calculateSummary(month, members, meals, expenses, deposits, fixedCosts)
        }
    }

    // --- Check if Month is Closed before write operations ---
    suspend fun isMonthClosed(messId: String, month: String): Boolean {
        // 1. Check local monthly_closings
        val localClosing = monthlyClosingDao.getClosing(messId, month)
        if (localClosing != null) return true

        // 2. Check local mess isClosed & currentMonth
        val localMess = messDao.getMessById(messId)
        if (localMess != null && localMess.currentMonth == month && localMess.isClosed) return true

        // 3. Fallback check Firestore if needed
        return try {
            val closingSnap = firestore.collection("messes").document(messId)
                .collection("closings").document(month).get().await()
            if (closingSnap.exists()) true
            else {
                val querySnap = firestore.collection("messes").document(messId)
                    .collection("closings").whereEqualTo("month", month).limit(1).get().await()
                !querySnap.isEmpty
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun verifyMonthNotClosed(messId: String, dateOrMonth: String) {
        val targetMonth = if (dateOrMonth.length >= 7) dateOrMonth.take(7) else dateOrMonth
        if (isMonthClosed(messId, targetMonth)) {
            throw IllegalStateException("Month $targetMonth is closed and locked for edits.")
        }
    }

    // --- Meal Operations ---
    suspend fun saveMealEntry(meal: MealEntryEntity) {
        verifyMonthNotClosed(meal.messId, meal.date)
        mealDao.insertOrUpdateMeal(meal)
        firestore.collection("messes").document(meal.messId)
            .collection("meals").document(meal.id).set(meal).await()
    }

    suspend fun saveMealEntry(
        messId: String,
        memberId: String,
        date: String,
        breakfast: Float,
        lunch: Float,
        dinner: Float,
        guestMeals: Float = 0f,
        note: String = ""
    ) {
        val id = "${messId}_${memberId}_$date"
        val entry = MealEntryEntity(
            id = id,
            messId = messId,
            memberId = memberId,
            date = date,
            breakfast = breakfast,
            lunch = lunch,
            dinner = dinner,
            guestMeals = guestMeals,
            note = note,
            updatedAt = System.currentTimeMillis()
        )
        saveMealEntry(entry)
    }

    suspend fun toggleTomorrowMeal(messId: String, memberId: String, enable: Boolean) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val tomorrowDate = sdf.format(cal.time)

        verifyMonthNotClosed(messId, tomorrowDate)

        val existing = mealDao.getMealEntry(messId, memberId, tomorrowDate)
        val targetCount = if (enable) 1f else 0f
        val updated = existing?.copy(
            lunch = targetCount,
            dinner = targetCount,
            updatedAt = System.currentTimeMillis()
        ) ?: MealEntryEntity(
            id = "${messId}_${memberId}_$tomorrowDate",
            messId = messId,
            memberId = memberId,
            date = tomorrowDate,
            breakfast = 0f,
            lunch = targetCount,
            dinner = targetCount,
            guestMeals = 0f,
            updatedAt = System.currentTimeMillis()
        )
        mealDao.insertOrUpdateMeal(updated)
        firestore.collection("messes").document(messId)
            .collection("meals").document(updated.id).set(updated).await()
    }

    // --- Bazar Operations ---
    suspend fun addBazarExpense(
        messId: String,
        buyerMemberId: String,
        date: String,
        amount: Double,
        category: String,
        note: String
    ): BazarExpenseEntity {
        verifyMonthNotClosed(messId, date)
        val expense = BazarExpenseEntity(
            id = UUID.randomUUID().toString(),
            messId = messId,
            buyerMemberId = buyerMemberId,
            date = date,
            amount = amount,
            category = category,
            note = note.trim(),
            receiptUri = null
        )
        bazarDao.insertExpense(expense)
        firestore.collection("messes").document(messId)
            .collection("expenses").document(expense.id).set(expense).await()
        return expense
    }

    suspend fun deleteBazarExpense(expense: BazarExpenseEntity) {
        verifyMonthNotClosed(expense.messId, expense.date)
        bazarDao.deleteExpense(expense)
        firestore.collection("messes").document(expense.messId)
            .collection("expenses").document(expense.id).delete().await()
    }

    // --- Deposit Operations ---
    suspend fun addDeposit(
        messId: String,
        memberId: String,
        date: String,
        amount: Double,
        paymentMethod: String,
        note: String
    ): DepositEntity {
        verifyMonthNotClosed(messId, date)
        val deposit = DepositEntity(
            id = UUID.randomUUID().toString(),
            messId = messId,
            memberId = memberId,
            date = date,
            amount = amount,
            paymentMethod = paymentMethod,
            note = note.trim()
        )
        depositDao.insertDeposit(deposit)
        firestore.collection("messes").document(messId)
            .collection("deposits").document(deposit.id).set(deposit).await()
        return deposit
    }

    suspend fun deleteDeposit(deposit: DepositEntity) {
        verifyMonthNotClosed(deposit.messId, deposit.date)
        depositDao.deleteDeposit(deposit)
        firestore.collection("messes").document(deposit.messId)
            .collection("deposits").document(deposit.id).delete().await()
    }

    // --- Fixed Cost Operations with CUSTOM Split Support ---
    suspend fun addFixedCost(
        messId: String,
        title: String,
        category: String,
        amount: Double,
        month: String,
        splitType: String = "EQUAL",
        customSharesJson: String = "{}"
    ): FixedCostEntity {
        verifyMonthNotClosed(messId, month)
        val cost = FixedCostEntity(
            id = UUID.randomUUID().toString(),
            messId = messId,
            title = title.trim(),
            category = category,
            amount = amount,
            month = month,
            splitType = splitType,
            customSharesJson = customSharesJson
        )
        fixedCostDao.insertFixedCost(cost)
        firestore.collection("messes").document(messId)
            .collection("fixedCosts").document(cost.id).set(cost).await()
        return cost
    }

    suspend fun deleteFixedCost(cost: FixedCostEntity) {
        verifyMonthNotClosed(cost.messId, cost.month)
        fixedCostDao.deleteFixedCost(cost)
        firestore.collection("messes").document(cost.messId)
            .collection("fixedCosts").document(cost.id).delete().await()
    }

    // --- Calculation Engine with Pro-Rata & Custom Fixed Cost Split ---
    fun getDaysInMonth(month: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(month) ?: Date()
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            30
        }
    }

    fun calculateActiveDaysInMonth(member: MemberEntity, month: String, totalDaysInMonth: Int): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val monthStart = "$month-01"
        val monthEnd = String.format(Locale.US, "%s-%02d", month, totalDaysInMonth)

        val joinDate = if (member.joinDate.isNotBlank()) member.joinDate else monthStart
        val leaveDate = member.leaveDate

        val effectiveStart = if (joinDate > monthStart) joinDate else monthStart
        val effectiveEnd = if (!leaveDate.isNullOrBlank() && leaveDate < monthEnd) leaveDate else monthEnd

        return try {
            val d1 = sdf.parse(effectiveStart)
            val d2 = sdf.parse(effectiveEnd)
            if (d1 != null && d2 != null && !d2.before(d1)) {
                val diff = ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt() + 1
                diff.coerceIn(1, totalDaysInMonth)
            } else {
                1
            }
        } catch (e: Exception) {
            totalDaysInMonth
        }
    }

    fun calculateSummary(
        month: String,
        members: List<MemberEntity>,
        meals: List<MealEntryEntity>,
        expenses: List<BazarExpenseEntity>,
        deposits: List<DepositEntity>,
        fixedCosts: List<FixedCostEntity>
    ): MessCalculationSummary {
        val totalBazar = expenses.sumOf { it.amount }
        val totalFixed = fixedCosts.sumOf { it.amount }
        val totalDep = deposits.sumOf { it.amount }

        val mealsByMember = meals.groupBy { it.memberId }
        val depositsByMember = deposits.groupBy { it.memberId }

        var totalMealsCount = 0f
        meals.forEach {
            totalMealsCount += (it.breakfast + it.lunch + it.dinner + it.guestMeals)
        }

        val mealRate = if (totalMealsCount > 0f) totalBazar / totalMealsCount.toDouble() else 0.0

        val daysInMonth = getDaysInMonth(month)

        // Pro-Rata Days for Each Member
        val activeDaysMap = members.associate { member ->
            member.id to calculateActiveDaysInMonth(member, month, daysInMonth)
        }
        val totalMemberDays = activeDaysMap.values.sum()

        val todayStr = getTodayString()
        var todayB = 0f
        var todayL = 0f
        var todayD = 0f
        meals.filter { it.date == todayStr }.forEach {
            todayB += it.breakfast
            todayL += it.lunch
            todayD += it.dinner
        }

        val memberCalculations = members.map { member ->
            val memberMealsList = mealsByMember[member.id] ?: emptyList()
            val mB = memberMealsList.sumOf { it.breakfast.toDouble() }.toFloat()
            val mL = memberMealsList.sumOf { it.lunch.toDouble() }.toFloat()
            val mD = memberMealsList.sumOf { it.dinner.toDouble() }.toFloat()
            val mG = memberMealsList.sumOf { it.guestMeals.toDouble() }.toFloat()
            val mTotal = mB + mL + mD + mG

            val mealCost = mTotal * mealRate

            // Calculate Fixed Cost Share:
            // EQUAL split costs: distributed pro-rata by active days in month
            // CUSTOM split costs: per-member custom amounts from customSharesJson
            val memberDays = activeDaysMap[member.id] ?: 0
            val equalFixedCostsTotal = fixedCosts.filter { it.splitType != "CUSTOM" }.sumOf { it.amount }
            val equalShare = if (totalMemberDays > 0) {
                equalFixedCostsTotal * (memberDays.toDouble() / totalMemberDays.toDouble())
            } else 0.0

            val customShare = fixedCosts.filter { it.splitType == "CUSTOM" }.sumOf { fc ->
                try {
                    val json = JSONObject(fc.customSharesJson)
                    json.optDouble(member.id, 0.0)
                } catch (e: Exception) {
                    0.0
                }
            }

            val fixedCostShare = equalShare + customShare
            val totalCost = mealCost + fixedCostShare
            val mDeposits = depositsByMember[member.id]?.sumOf { it.amount } ?: 0.0
            val opening = member.openingBalance
            val netBalance = (mDeposits + opening) - totalCost

            MemberCalculation(
                memberId = member.id,
                name = member.name,
                phone = member.phone,
                role = member.role,
                type = member.type,
                totalBreakfast = mB,
                totalLunch = mL,
                totalDinner = mD,
                totalGuestMeals = mG,
                totalMeals = mTotal,
                mealCost = mealCost,
                fixedCostShare = fixedCostShare,
                openingBalance = opening,
                totalDeposits = mDeposits,
                totalCost = totalCost,
                netBalance = netBalance
            )
        }

        return MessCalculationSummary(
            month = month,
            totalBazarCost = totalBazar,
            totalMeals = totalMealsCount,
            mealRate = mealRate,
            totalFixedCosts = totalFixed,
            totalDeposits = totalDep,
            todayBreakfastCount = todayB,
            todayLunchCount = todayL,
            todayDinnerCount = todayD,
            todayTotalMeals = todayB + todayL + todayD,
            memberCalculations = memberCalculations
        )
    }

    // --- Monthly Closing, Carry Forward & Reopening with Restoring Balances ---
    suspend fun closeMonth(messId: String, currentMonth: String): Result<String> {
        val members = memberDao.getMembersForCalculationSync(messId)
        val meals = mealDao.getMealsForMonthSync(messId, currentMonth)
        val expenses = bazarDao.getExpensesForMonthSync(messId, currentMonth)
        val deposits = depositDao.getDepositsForMonthSync(messId, currentMonth)
        val fixedCosts = fixedCostDao.getFixedCostsForMonthSync(messId, currentMonth)

        val summary = calculateSummary(currentMonth, members, meals, expenses, deposits, fixedCosts)

        // 1. Record snapshot and previous opening balances in JSON
        val previousBalancesObj = JSONObject()
        members.forEach { m ->
            previousBalancesObj.put(m.id, m.openingBalance)
        }

        val closing = MonthlyClosingEntity(
            id = UUID.randomUUID().toString(),
            messId = messId,
            month = currentMonth,
            totalBazar = summary.totalBazarCost,
            totalMeals = summary.totalMeals,
            mealRate = summary.mealRate,
            totalFixedCosts = summary.totalFixedCosts,
            previousBalancesJson = previousBalancesObj.toString(),
            closedAt = System.currentTimeMillis()
        )
        monthlyClosingDao.insertClosing(closing)
        firestore.collection("messes").document(messId)
            .collection("closings").document(closing.id).set(closing).await()

        // 2. Advance to next month and carry forward balances once
        val nextMonth = getNextMonthString(currentMonth)

        summary.memberCalculations.forEach { calc ->
            val member = memberDao.getMemberById(calc.memberId)
            if (member != null) {
                val updated = member.copy(openingBalance = calc.netBalance)
                memberDao.updateMember(updated)
                firestore.collection("messes").document(messId)
                    .collection("members").document(member.id).update("openingBalance", calc.netBalance).await()
            }
        }

        // 3. Update Mess to the new month
        val mess = messDao.getMessById(messId)
        if (mess != null) {
            val updatedMess = mess.copy(currentMonth = nextMonth, isClosed = false)
            messDao.updateMess(updatedMess)
            firestore.collection("messes").document(messId).update(
                mapOf("currentMonth" to nextMonth, "isClosed" to false)
            ).await()
        }

        return Result.success(nextMonth)
    }

    suspend fun reopenMonth(messId: String, month: String) {
        var closing = monthlyClosingDao.getClosing(messId, month)
        if (closing == null) {
            try {
                val snap = firestore.collection("messes").document(messId).collection("closings")
                    .whereEqualTo("month", month).limit(1).get().await()
                if (!snap.isEmpty) {
                    closing = snap.documents.first().toObject(MonthlyClosingEntity::class.java)
                }
            } catch (e: Exception) {
                Log.w("MessRepository", "Error fetching closing: ${e.message}")
            }
        }

        // Restore each member's previous opening balance from stored JSON snapshot
        if (closing != null && closing.previousBalancesJson.isNotBlank()) {
            try {
                val json = JSONObject(closing.previousBalancesJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val memberId = keys.next()
                    val restoredBalance = json.optDouble(memberId, 0.0)
                    val member = memberDao.getMemberById(memberId)
                    if (member != null) {
                        val restoredMember = member.copy(openingBalance = restoredBalance)
                        memberDao.updateMember(restoredMember)
                        firestore.collection("messes").document(messId)
                            .collection("members").document(memberId)
                            .update("openingBalance", restoredBalance).await()
                    }
                }
            } catch (e: Exception) {
                Log.w("MessRepository", "Failed to restore previous balances on reopen: ${e.message}")
            }
        }

        // Delete closing entry
        monthlyClosingDao.deleteClosing(messId, month)
        if (closing != null) {
            try {
                firestore.collection("messes").document(messId)
                    .collection("closings").document(closing.id).delete().await()
            } catch (e: Exception) {
                Log.w("MessRepository", "Failed to delete closing in Firestore: ${e.message}")
            }
        }

        // Restore mess currentMonth back to reopened month
        val mess = messDao.getMessById(messId)
        if (mess != null) {
            val restoredMess = mess.copy(currentMonth = month, isClosed = false)
            messDao.updateMess(restoredMess)
            firestore.collection("messes").document(messId).update(
                mapOf("currentMonth" to month, "isClosed" to false)
            ).await()
        }
    }

    // --- Seed Demo Data ---
    suspend fun seedDemoData(userId: String, userName: String, userPhone: String): MessEntity {
        val mess = createMess(userId, userName, userPhone, "পদ্মা ভিলা (ডেমো)")
        val messId = mess.id
        val currentMonth = getCurrentMonthString()

        // Add managed members with createdBy = userId
        val m1 = addManagedMember(messId, userId, "তানভীর হাসান", "01811223344", 2000.0)
        val m2 = addManagedMember(messId, userId, "রাকিবুল ইসলাম", "01911223344", 1500.0)
        val m3 = addManagedMember(messId, userId, "সাকিব আহমেদ", "01611223344", 0.0)

        // Bazar Expenses
        addBazarExpense(messId, userId, "$currentMonth-02", 1250.0, "rice", "মিনিকেট চাল ২৫ কেজি")
        addBazarExpense(messId, m1.id, "$currentMonth-04", 850.0, "meat", "মুরগি ২ কেজি ও মসলা")
        addBazarExpense(messId, m2.id, "$currentMonth-06", 620.0, "fish", "রুই মাছ ও তেল")
        addBazarExpense(messId, userId, "$currentMonth-08", 450.0, "vegetables", "আলু, পেঁয়াজ, কাঁচামরিচ")

        // Deposits
        addDeposit(messId, userId, "$currentMonth-01", 3000.0, "BKASH", "বিকাশে জমা")
        addDeposit(messId, m1.id, "$currentMonth-01", 3000.0, "NAGAD", "নগদে জমা")
        addDeposit(messId, m2.id, "$currentMonth-01", 3000.0, "CASH", "ক্যাশ জমা")
        addDeposit(messId, m3.id, "$currentMonth-02", 2000.0, "BKASH", "বিকাশ")

        // Fixed Costs: 1 Equal split (WiFi) and 1 Custom split (Maid)
        addFixedCost(messId, "ওয়াইফাই বিল", "wifi", 800.0, currentMonth, "EQUAL", "{}")

        val customMaidJson = JSONObject().apply {
            put(userId, 400.0)
            put(m1.id, 400.0)
            put(m2.id, 400.0)
            put(m3.id, 400.0)
        }.toString()
        addFixedCost(messId, "খালা বিল", "maid", 1600.0, currentMonth, "CUSTOM", customMaidJson)

        // Meal entries
        val memberIds = listOf(userId, m1.id, m2.id, m3.id)
        for (day in 1..8) {
            val date = String.format(Locale.US, "%s-%02d", currentMonth, day)
            memberIds.forEach { memId ->
                saveMealEntry(
                    MealEntryEntity(
                        id = "${messId}_${memId}_$date",
                        messId = messId,
                        memberId = memId,
                        date = date,
                        breakfast = 1f,
                        lunch = 1f,
                        dinner = 1f,
                        guestMeals = if (day == 5 && memId == userId) 2f else 0f
                    )
                )
            }
        }

        return mess
    }
}
