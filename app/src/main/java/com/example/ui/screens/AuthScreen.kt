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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.AppStrings

@Composable
fun AuthScreen(
    isBangla: Boolean,
    onLogin: (phone: String, pin: String) -> Unit,
    onRegister: (name: String, phone: String, pin: String) -> Unit,
    onSendPasswordReset: (phoneOrEmail: String) -> Unit,
    onQuickDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Register
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Hero Header & Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = AppStrings.t("app_name", isBangla),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = AppStrings.t("tagline", isBangla),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card with Login / Register
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                        },
                        text = { Text(AppStrings.t("login_title", isBangla), fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tab_login")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                        },
                        text = { Text(AppStrings.t("register_title", isBangla), fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tab_register")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Name field (if Register)
                if (selectedTab == 1) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(AppStrings.t("name_label", isBangla)) },
                        placeholder = { Text(AppStrings.t("name_hint", isBangla)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(AppStrings.t("phone_label", isBangla)) },
                    placeholder = { Text(AppStrings.t("phone_hint", isBangla)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_phone_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PIN field
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text(AppStrings.t("pin_label", isBangla)) },
                    placeholder = { Text("৬ ডিজিটের পিন (••••••)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle PIN visibility"
                            )
                        }
                    },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_pin_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (phone.isBlank()) {
                            errorMessage = if (isBangla) "মোবাইল নম্বর লিখুন" else "Please enter your phone number"
                            return@Button
                        }
                        if (pin.length != 6) {
                            errorMessage = if (isBangla) "পিন অবশ্যই ৬ ডিজিটের হতে হবে" else "PIN must be exactly 6 digits"
                            return@Button
                        }
                        if (selectedTab == 1 && name.isBlank()) {
                            errorMessage = if (isBangla) "আপনার নাম লিখুন" else "Please enter your name"
                            return@Button
                        }

                        errorMessage = null
                        if (selectedTab == 0) {
                            onLogin(phone, pin)
                        } else {
                            onRegister(name, phone, pin)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (selectedTab == 0) AppStrings.t("btn_login", isBangla) else AppStrings.t("btn_register", isBangla),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Forgot PIN / Reset Link Button & Notice
                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = {
                            resetInput = phone
                            showResetDialog = true
                        }) {
                            Text(
                                text = if (isBangla) "পিন ভুলে গেছেন? রিসেট করুন" else "Forgot PIN? Reset",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = AppStrings.t("forgot_pin_hint", isBangla),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Instant Test Button (Preloaded test credentials)
        OutlinedButton(
            onClick = onQuickDemo,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_demo_button"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = if (isBangla) "১-ক্লিকে টেস্ট ইউজার লগইন (দ্রুত পরীক্ষার জন্য)" else "1-Click Quick Demo Login",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Password Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (isBangla) "পিন / পাসওয়ার্ড রিসেট" else "PIN / Password Reset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla)
                            "আপনার অ্যাকাউন্টের ফোন নম্বর বা ইমেইল দিন। এছাড়া মেসের অ্যাডমিন মেম্বার স্ক্রিন থেকে ওয়ান-টাইম পিন দিয়ে সরাসরি রিসেট করতে পারেন।"
                        else
                            "Enter your phone number or email. You can also ask your mess Admin to reset your PIN via Cloud Function from the Members screen."
                    )
                    OutlinedTextField(
                        value = resetInput,
                        onValueChange = { resetInput = it },
                        label = { Text(if (isBangla) "ইমেইল বা ফোন" else "Email or Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetInput.isNotBlank()) {
                            onSendPasswordReset(resetInput)
                            showResetDialog = false
                        }
                    }
                ) {
                    Text(if (isBangla) "লিংক পাঠান" else "Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
