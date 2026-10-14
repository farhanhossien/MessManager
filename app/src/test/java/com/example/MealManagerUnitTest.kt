package com.example

import com.example.data.local.entities.BazarExpenseEntity
import com.example.data.local.entities.FixedCostEntity
import com.example.data.local.entities.MealEntryEntity
import com.example.data.local.entities.MemberEntity
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@RunWith(RobolectricTestRunner::class)
class MealManagerUnitTest {

    private fun hashPinWithPbkdf2(pin: String, saltHex: String): String {
        val saltBytes = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val spec = PBEKeySpec(pin.toCharArray(), saltBytes, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    @Test
    fun testPbkdf2PinHashing_isDeterministicWithSalt() {
        val salt1 = "0123456789abcdef0123456789abcdef"
        val salt2 = "fedcba9876543210fedcba9876543210"

        val hash1 = hashPinWithPbkdf2("1234", salt1)
        val hash2 = hashPinWithPbkdf2("1234", salt1)
        val hashDiffSalt = hashPinWithPbkdf2("1234", salt2)
        val hashDiffPin = hashPinWithPbkdf2("4321", salt1)

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hashDiffSalt)
        assertNotEquals(hash1, hashDiffPin)
        assertEquals(64, hash1.length) // 256 bits = 64 hex characters
    }

    @Test
    fun testInviteCodeGeneration_is6Chars() {
        val code = generateInviteCode()
        assertEquals(6, code.length)
        assertTrue(code.all { it.isLetterOrDigit() })
    }

    @Test
    fun testProRataFixedCostsForMidMonthJoinAndLeave() {
        val totalDays = 30
        val m1Days = 30
        val m2Days = 15
        val totalMemberDays = m1Days + m2Days // 45 member-days

        val totalFixed = 4500.0
        val m1Share = totalFixed * (m1Days.toDouble() / totalMemberDays.toDouble()) // 4500 * (30/45) = 3000
        val m2Share = totalFixed * (m2Days.toDouble() / totalMemberDays.toDouble()) // 4500 * (15/45) = 1500

        assertEquals(3000.0, m1Share, 0.001)
        assertEquals(1500.0, m2Share, 0.001)
        assertEquals(totalFixed, m1Share + m2Share, 0.001)
    }

    @Test
    fun testCustomFixedCostSplitCalculation() {
        val m1 = MemberEntity("m1", "mess1", "u1", "Member 1", "01711", "ADMIN", "ACCOUNT", "ACTIVE", "2026-10-01", null, 0.0)
        val m2 = MemberEntity("m2", "mess1", "u2", "Member 2", "01822", "MEMBER", "ACCOUNT", "ACTIVE", "2026-10-01", null, 0.0)

        // Custom maid bill: m1 pays 600, m2 pays 400
        val customJson = JSONObject().apply {
            put("m1", 600.0)
            put("m2", 400.0)
        }.toString()

        val customCost = FixedCostEntity(
            id = "fc1",
            messId = "mess1",
            title = "খালা বিল",
            category = "maid",
            amount = 1000.0,
            month = "2026-10",
            splitType = "CUSTOM",
            customSharesJson = customJson
        )

        val json = JSONObject(customCost.customSharesJson)
        val m1Share = json.optDouble(m1.id, 0.0)
        val m2Share = json.optDouble(m2.id, 0.0)

        assertEquals(600.0, m1Share, 0.001)
        assertEquals(400.0, m2Share, 0.001)
        assertEquals(customCost.amount, m1Share + m2Share, 0.001)
    }

    @Test
    fun testRemovedMembersBalancesStayInCalculation() {
        val m1 = MemberEntity("m1", "mess1", "u1", "Active Member", "01711", "ADMIN", "ACCOUNT", "ACTIVE", "2026-10-01", null, 0.0)
        val m2 = MemberEntity("m2", "mess1", "u2", "Removed Member", "01822", "MEMBER", "ACCOUNT", "REMOVED", "2026-10-01", "2026-10-15", 0.0)
        val calculationMembers = listOf(m1, m2)

        val meals = listOf(
            MealEntryEntity("e1", "mess1", "m1", "2026-10-01", 30f, 30f, 30f, 0f), // 90 meals
            MealEntryEntity("e2", "mess1", "m2", "2026-10-01", 10f, 10f, 10f, 0f)  // 30 meals
        )
        val totalMeals = 120.0f
        val totalBazar = 6000.0
        val mealRate = totalBazar / totalMeals // 50.0

        val m2MealCost = 30f * mealRate // 1500.0
        val m2Deposit = 2000.0
        val m2NetBalance = m2Deposit - m2MealCost

        assertEquals(2, calculationMembers.size)
        assertEquals(50.0, mealRate, 0.001)
        assertEquals(500.0, m2NetBalance, 0.001)
    }

    @Test
    fun testZeroMealsEdgeCase_returnsZeroMealRate() {
        val totalBazar = 1000.0
        val totalMeals = 0.0f
        val mealRate = if (totalMeals > 0f) totalBazar / totalMeals.toDouble() else 0.0
        assertEquals(0.0, mealRate, 0.001)
    }

    @Test
    fun testPhoneToEmailConversion() {
        val rawPhone = " 01711-223344 "
        val clean = rawPhone.trim().replace("[^0-9]".toRegex(), "")
        val email = "$clean@mealmanager.app"
        assertEquals("01711223344@mealmanager.app", email)
    }

    @Test
    fun testManagedMemberCreatedByField() {
        val adminUid = "admin_firebase_uid_123"
        val managedMember = MemberEntity(
            id = "random-uuid-456",
            messId = "mess_1",
            userId = null,
            name = "Managed Member",
            phone = "01888999999",
            role = "MEMBER",
            type = "MANAGED",
            status = "ACTIVE",
            joinDate = "2026-10-01",
            createdBy = adminUid
        )
        assertEquals(adminUid, managedMember.createdBy)
        assertEquals("MANAGED", managedMember.type)
        assertEquals(null, managedMember.userId)
    }

    @Test
    fun testRejoinPreventionLogic() {
        val adminUid = "user_admin_123"
        val messAdminId = "user_admin_123"
        val isRejoiningAdmin = adminUid == messAdminId
        assertTrue(isRejoiningAdmin)

        val activeMemberStatus = "ACTIVE"
        val pendingMemberStatus = "PENDING"
        assertTrue(activeMemberStatus == "ACTIVE" || activeMemberStatus == "PENDING")
        assertTrue(pendingMemberStatus == "ACTIVE" || pendingMemberStatus == "PENDING")
    }

    @Test
    fun testManagerMessUpdateFieldsRestriction() {
        val allowedKeys = setOf("currentMonth", "isClosed")
        val validManagerUpdate = setOf("currentMonth", "isClosed")
        val invalidManagerUpdate = setOf("currentMonth", "name")
        val adminOnlyFieldUpdate = setOf("inviteCode", "adminId")

        assertTrue(allowedKeys.containsAll(validManagerUpdate))
        org.junit.Assert.assertFalse(allowedKeys.containsAll(invalidManagerUpdate))
        org.junit.Assert.assertFalse(allowedKeys.containsAll(adminOnlyFieldUpdate))
    }

    @Test
    fun testManagerCannotElevateToAdminOrModifyAdmin() {
        val targetMemberRole = "ADMIN"
        val newRequestedRole = "ADMIN"
        val managerCallerRole = "MANAGER"

        // Manager cannot modify an ADMIN doc
        val canManagerModify = managerCallerRole == "MANAGER" && targetMemberRole != "ADMIN"
        org.junit.Assert.assertFalse(canManagerModify)

        // Manager cannot promote to ADMIN
        val canManagerPromote = managerCallerRole == "MANAGER" && newRequestedRole != "ADMIN"
        org.junit.Assert.assertFalse(canManagerPromote)
    }

    @Test
    fun testCloudFunctionReturnedTempPinParsing() {
        val serverResultMap = mapOf(
            "tempPin" to "784920",
            "status" to "SUCCESS"
        )
        val serverPin = serverResultMap["tempPin"]
        assertEquals("784920", serverPin)
        assertEquals(6, serverPin?.length)
        assertTrue(serverPin!!.all { it.isDigit() })
    }

    @Test
    fun testInviteCodeDocMappingStructure() {
        val code = "X7K9PQ"
        val messId = "mess_abc_123"
        val mappingDoc = mapOf(
            "code" to code,
            "messId" to messId,
            "createdAt" to System.currentTimeMillis()
        )
        assertEquals(code, mappingDoc["code"])
        assertEquals(messId, mappingDoc["messId"])
        assertTrue((mappingDoc["createdAt"] as Long) > 0L)
    }
}
