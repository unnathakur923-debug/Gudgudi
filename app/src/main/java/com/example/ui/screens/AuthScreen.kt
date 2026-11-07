package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GameRepository
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    repository: GameRepository,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isOtpLogin by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }

    // Form fields
    var fullName by remember { mutableStateOf("Kunwar Vikram Singh") }
    var mobile by remember { mutableStateOf("+91 9876543210") }
    var email by remember { mutableStateOf("vikram.gudgudi@example.com") }
    var password by remember { mutableStateOf("Gudgudi@2026") }
    var confirmPassword by remember { mutableStateOf("Gudgudi@2026") }
    var dob by remember { mutableStateOf("15/08/1998") }
    var state by remember { mutableStateOf("Rajasthan") }
    var district by remember { mutableStateOf("Jaipur") }
    var selectedAvatar by remember { mutableIntStateOf(0) }
    var otpCode by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GudgudiMaroonDark, GudgudiMaroonSurface, GudgudiMaroonCard)
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Royal Emblem
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, GudgudiGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_gudgudi_emblem),
                    contentDescription = "Gudgudi Gaming Emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "GUDGUDI",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GudgudiGold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Online Dice Gaming Darbar",
                fontSize = 13.sp,
                color = GudgudiGoldLight,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Card container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldDark.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode Switcher Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GudgudiMaroonDark, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isRegisterMode) GudgudiCrimson else Color.Transparent)
                                .clickable { isRegisterMode = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LOGIN",
                                color = if (!isRegisterMode) GudgudiGold else GudgudiTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRegisterMode) GudgudiCrimson else Color.Transparent)
                                .clickable { isRegisterMode = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "REGISTER",
                                color = if (isRegisterMode) GudgudiGold else GudgudiTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (isRegisterMode) {
                        // Registration Fields
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GudgudiGold) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                            colors = royalTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GudgudiGold) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_mobile_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = royalTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GudgudiGold) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = royalTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State") },
                                modifier = Modifier.weight(1f),
                                colors = royalTextFieldColors(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = district,
                                onValueChange = { district = it },
                                label = { Text("District") },
                                modifier = Modifier.weight(1f),
                                colors = royalTextFieldColors(),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth (DD/MM/YYYY)") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GudgudiGold) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = royalTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GudgudiGold) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                            colors = royalTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar Picker
                        Text(
                            text = "Choose Gudgudi Avatar",
                            color = GudgudiGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val avatars = listOf("⚔️", "🛡️", "👑", "🦁", "🐎", "🏰")
                            avatars.forEachIndexed { idx, emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedAvatar == idx) GudgudiCrimson else GudgudiMaroonDark)
                                        .border(
                                            2.dp,
                                            if (selectedAvatar == idx) GudgudiGold else Color.Gray,
                                            CircleShape
                                        )
                                        .clickable { selectedAvatar = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (fullName.isBlank() || mobile.isBlank() || email.isBlank()) {
                                    errorMessage = "Please fill all required fields"
                                } else {
                                    repository.registerUser(
                                        fullName = fullName,
                                        mobile = mobile,
                                        email = email,
                                        dob = dob,
                                        state = state,
                                        district = district,
                                        avatarIndex = selectedAvatar
                                    )
                                    onAuthSuccess()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "COMPLETE REGISTRATION (+5,000 COINS)",
                                color = GudgudiMaroonDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                    } else {
                        // Login Mode
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            FilterChip(
                                selected = !isOtpLogin,
                                onClick = { isOtpLogin = false },
                                label = { Text("Password Login") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = isOtpLogin,
                                onClick = { isOtpLogin = true },
                                label = { Text("Mobile OTP Login") }
                            )
                        }

                        if (!isOtpLogin) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address or Mobile") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GudgudiGold) },
                                modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                                colors = royalTextFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GudgudiGold) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                                colors = royalTextFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showForgotPasswordDialog = true }) {
                                    Text("Forgot Password?", color = GudgudiGold, fontSize = 12.sp)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                label = { Text("Registered Mobile Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GudgudiGold) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = royalTextFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!otpSent) {
                                Button(
                                    onClick = { otpSent = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = GudgudiCrimson)
                                ) {
                                    Text("SEND OTP VERIFICATION", color = GudgudiGold)
                                }
                            } else {
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { otpCode = it },
                                    label = { Text("Enter 6-Digit OTP (e.g. 123456)") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = GudgudiGold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = royalTextFieldColors(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Demo OTP: 123456 (Auto-sent to $mobile)",
                                    fontSize = 11.sp,
                                    color = GudgudiEmeraldLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onAuthSuccess() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ENTER GUDGUDI DARBAR",
                                color = GudgudiMaroonDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instant Guest Play Button
                    OutlinedButton(
                        onClick = { onAuthSuccess() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("guest_play_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = GudgudiGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play as Guest (Instant 5,000 Coins)",
                            color = GudgudiGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Non-redeemable Entertainment notice
            Text(
                text = "🛡️ Pure Free-to-Play Entertainment. Virtual coins have NO real monetary or cash value and cannot be withdrawn or transferred.",
                fontSize = 10.sp,
                color = GudgudiTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetSent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Gudgudi Account Password", color = GudgudiGold) },
            text = {
                Column {
                    Text(
                        text = "Enter your registered email address to receive password reset instructions.",
                        fontSize = 13.sp,
                        color = GudgudiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        colors = royalTextFieldColors(),
                        singleLine = true
                    )
                    if (resetSent) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Reset link sent successfully to $resetEmail!",
                            color = GudgudiEmeraldLight,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { resetSent = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber)
                ) {
                    Text("Send Reset Link", color = GudgudiMaroonDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close", color = GudgudiTextSecondary)
                }
            },
            containerColor = GudgudiMaroonCard
        )
    }
}

@Composable
fun royalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GudgudiGold,
    unfocusedBorderColor = GudgudiMaroonBorder,
    focusedLabelColor = GudgudiGold,
    unfocusedLabelColor = GudgudiTextSecondary,
    focusedTextColor = GudgudiTextPrimary,
    unfocusedTextColor = GudgudiTextPrimary,
    cursorColor = GudgudiGold
)
