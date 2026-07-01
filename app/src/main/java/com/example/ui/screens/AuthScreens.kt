package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.UserStatus
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// --- SPLASH SCREEN ---

@Composable
fun SplashScreen(navController: NavController, viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        if (currentUser != null) {
            viewModel.refreshUserSession()
            val freshUser = viewModel.currentUser.value
            if (freshUser != null) {
                if (freshUser.role == com.example.data.UserRole.ADMIN) {
                    navController.navigate("admin_dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    when (freshUser.status) {
                        UserStatus.APPROVED -> {
                            navController.navigate("main_client") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        UserStatus.PENDING_APPROVAL -> {
                            navController.navigate("pending_approval") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        UserStatus.REJECTED -> {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        UserStatus.SUSPENDED -> {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                }
            } else {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } else {
            navController.navigate("onboarding") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumDarkBg),
        contentAlignment = Alignment.Center
    ) {
        // Gradient glow in background
        Box(
            modifier = Modifier
                .size(300.dp)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(AccentCyan.copy(alpha = 0.15f), Color.Transparent),
                            center = center,
                            radius = size.width / 1.5f
                        )
                    )
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SportsBasketball,
                contentDescription = "App Logo",
                tint = AccentCyan,
                modifier = Modifier
                    .size(90.dp)
                    .animateContentSize()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "APEX PREDICTIONS",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Elite Sports Forecasting Platform",
                fontSize = 14.sp,
                color = AccentPink,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- ONBOARDING SCREEN ---

data class OnboardingPageData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(navController: NavController) {
    var currentPage by remember { mutableIntStateOf(0) }
    
    val onboardingPages = listOf(
        OnboardingPageData(
            Icons.Default.TrendingUp,
            "Predict & Dominate",
            "Predict match outcomes across Football, Cricket, Tennis, and ESports with professional real-time stats."
        ),
        OnboardingPageData(
            Icons.Default.AccountBalanceWallet,
            "Secure Proof Submissions",
            "Easily submit entries with EasyPaisa, JazzCash, or bank transfers and upload transaction IDs for instant verification."
        ),
        OnboardingPageData(
            Icons.Default.VerifiedUser,
            "VIP Prediction Alerts",
            "Unlock elite access, exclusive high-reward prediction cards, and push notifications directly from sports analysts."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumDarkBg)
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { navController.navigate("login") }
                    .padding(8.dp)
            )
        }

        // Animated Content Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val page = onboardingPages[currentPage]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Large Glowing Icon
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .drawBehind {
                            drawCircle(
                                Brush.radialGradient(
                                    colors = listOf(AccentCyan.copy(alpha = 0.12f), Color.Transparent),
                                    center = center,
                                    radius = size.width / 1.2f
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(72.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = page.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = page.description,
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Indicator dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (index == currentPage) 16.dp else 8.dp, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (index == currentPage) AccentCyan else TextSecondary.copy(alpha = 0.4f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Primary Action Button
            Button(
                onClick = {
                    if (currentPage < onboardingPages.size - 1) {
                        currentPage++
                    } else {
                        navController.navigate("login")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_action_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentPage == onboardingPages.size - 1) "GET STARTED" else "NEXT",
                    color = PremiumDarkBg,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- LOGIN SCREEN ---

@Composable
fun LoginScreen(navController: NavController, viewModel: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsBasketball,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Sign in to join active prediction pools",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Form
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email Address", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentCyan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = GlassBorder,
                    cursorColor = AccentCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = GlassBorder,
                    cursorColor = AccentCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // Show Local or ViewModel errors
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both email and password."
                        return@Button
                    }
                    viewModel.login(
                        emailInput = email.trim(),
                        passwordText = password,
                        onSuccess = {
                            val user = viewModel.currentUser.value ?: return@login
                            if (user.role == com.example.data.UserRole.ADMIN) {
                                navController.navigate("admin_dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                when (user.status) {
                                    UserStatus.APPROVED -> {
                                        navController.navigate("main_client") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    UserStatus.PENDING_APPROVAL -> {
                                        navController.navigate("pending_approval") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    else -> {
                                        errorMessage = "Account status is ${user.status}."
                                    }
                                }
                            }
                        },
                        onError = { errorMessage = it }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PremiumDarkBg, modifier = Modifier.size(24.dp))
                } else {
                    Text("SIGN IN", color = PremiumDarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick demo helpers
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DEMO ACCOUNTS (Click to autofill):", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "User", 
                            color = AccentCyan, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { 
                                email = "user@apex.com"
                                password = "user123"
                            }
                        )
                        Text(
                            "Admin", 
                            color = AccentCyan, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { 
                                email = "admin@apex.com"
                                password = "admin123"
                            }
                        )
                        Text(
                            "Pending User", 
                            color = AccentCyan, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { 
                                email = "pending@apex.com"
                                password = "pending123"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "Register",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { navController.navigate("register") }
                        .testTag("to_register_button")
                )
            }
        }
    }
}

// --- REGISTER SCREEN ---

@Composable
fun RegisterScreen(navController: NavController, viewModel: AppViewModel) {
    var step by remember { mutableStateOf(1) }
    
    // Step 1 State
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var easyPaisaNumber by remember { mutableStateOf("") }
    var jazzCashNumber by remember { mutableStateOf("") }
    
    // Step 2 State
    var transactionId by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("EasyPaisa") }
    var depositScreenshot by remember { mutableStateOf<String?>(null) }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val paymentSettings by viewModel.paymentSettings.collectAsStateWithLifecycle()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Identity Logo
            Text(
                text = "REPORT BADSHAH",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = AccentCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "Sports Match Analysis & Reporting Platform",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1. Personal Info",
                    color = if (step == 1) AccentCyan else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp).size(16.dp)
                )
                Text(
                    text = "2. Deposit Verification",
                    color = if (step == 2) AccentCyan else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (step == 1) {
                // --- STEP 1: PERSONAL INFORMATION ---
                Text(
                    text = "Create Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Full Name", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it; errorMessage = null },
                    label = { Text("WhatsApp Number (Mobile)", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AccentCyan) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_mobile_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Create Password", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("reg_pass_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirm Password", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = AccentCyan) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cnic,
                    onValueChange = { cnic = it; errorMessage = null },
                    label = { Text("CNIC (Optional)", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = AccentCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("xxxxx-xxxxxxx-x", color = TextSecondary.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = easyPaisaNumber,
                    onValueChange = { easyPaisaNumber = it; errorMessage = null },
                    label = { Text("Your EasyPaisa Mobile Number", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AccentCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = jazzCashNumber,
                    onValueChange = { jazzCashNumber = it; errorMessage = null },
                    label = { Text("Your JazzCash Mobile Number (Optional)", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = AccentCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || mobile.isBlank() || password.isBlank() || confirmPassword.isBlank() || easyPaisaNumber.isBlank()) {
                            errorMessage = "Name, WhatsApp Mobile, Password, and EasyPaisa accounts are required."
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        if (mobile.trim().length < 7) {
                            errorMessage = "Please enter a valid WhatsApp Number."
                            return@Button
                        }
                        errorMessage = null
                        step = 2
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_step_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONTINUE TO VERIFICATION DEPOSIT", color = PremiumDarkBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

            } else {
                // --- STEP 2: DEPOSIT VERIFICATION ---
                Text(
                    text = "Verify Security Deposit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "To eliminate spam, bots, and ensure report exclusivity, Report Badshah requires a refundable profile verification deposit of PKR ${paymentSettings.depositAmount.toInt()}. This security deposit is fully credited to your dashboard wallet upon profile approval.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                // Official Payment Methods Panel
                Text(
                    text = "Official Deposit Accounts:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Easypaisa Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("EasyPaisa Wallet", color = AccentPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(paymentSettings.easypaisaNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Title: ${paymentSettings.easypaisaTitle}", color = TextSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(paymentSettings.easypaisaNumber))
                            android.widget.Toast.makeText(context, "Easypaisa account copied!", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentCyan)
                        }
                    }
                }

                // Jazzcash Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("JazzCash Wallet", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(paymentSettings.jazzcashNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Title: ${paymentSettings.jazzcashTitle}", color = TextSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(paymentSettings.jazzcashNumber))
                            android.widget.Toast.makeText(context, "JazzCash account copied!", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentCyan)
                        }
                    }
                }

                // Bank Account Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(paymentSettings.bankName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(paymentSettings.bankAccountNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Title: ${paymentSettings.bankAccountTitle}", color = TextSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(paymentSettings.bankAccountNumber))
                            android.widget.Toast.makeText(context, "Bank Account number copied!", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Payment Method Dropdown (Pseudo-spinner)
                Text("Select Deposit Destination wallet:", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("EasyPaisa", "JazzCash", "Bank").forEach { method ->
                        val selected = paymentMethod == method
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (selected) AccentCyan.copy(alpha = 0.15f) else PremiumDarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paymentMethod = method }
                                .border(1.dp, if (selected) AccentCyan else GlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text(method, color = if (selected) AccentCyan else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transaction ID input
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it; errorMessage = null },
                    label = { Text("Receipt Transaction ID (TRX ID)", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = AccentCyan) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_txid_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Screenshot Uploader
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable {
                            // Instant Screenshot Simulator: Load a high fidelity mock receipt Base64
                            depositScreenshot = "iVBORw0KGgoAAAANSUhEUgAAASwAAACWAQMAAABfCwZFAAAABlBMVEUAAAD///+l2Z/dAAAAMklEQVQI12NgGAWjYBSMglEwCkbBSAcMRoEBpDEp0pgaAEnTAJLGDyCNE0CaEEDSAAD//8KzA7E/Vv0VAAAAAElFTkSuQmCC"
                        }
                        .border(1.dp, if (depositScreenshot != null) AccentCyan else GlassBorder, RoundedCornerShape(12.dp)),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (depositScreenshot != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("deposit_receipt.jpg (1.8 MB) ✅", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Tap to re-select", color = TextSecondary, fontSize = 11.sp)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("TAP TO ATTACH TRANSACTION SCREENSHOT", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Only JPEG/PNG supported", color = TextSecondary.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { step = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumDarkSurface),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("BACK", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (transactionId.isBlank()) {
                                errorMessage = "Please enter your Transaction ID (TRX ID)."
                                return@Button
                            }
                            if (depositScreenshot == null) {
                                errorMessage = "Please tap above to attach your deposit screenshot."
                                return@Button
                            }

                            // Auto-generate safe email from mobile
                            val cleanEmail = "${mobile.trim()}@reportbadshah.com"
                            
                            viewModel.register(
                                name = name.trim(),
                                mobile = mobile.trim(),
                                email = cleanEmail,
                                passwordText = password,
                                cnic = if (cnic.isBlank()) null else cnic,
                                easyPaisa = easyPaisaNumber,
                                jazzCash = if (jazzCashNumber.isBlank()) null else jazzCashNumber,
                                depositScreenshot = depositScreenshot,
                                onSuccess = {
                                    navController.navigate("pending_approval") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onError = { errorMessage = it }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                            .testTag("finish_register_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = PremiumDarkBg, modifier = Modifier.size(24.dp))
                        } else {
                            Text("SUBMIT PROFILE FOR REVIEW", color = PremiumDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "Login",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("login") }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- PENDING APPROVAL SCREEN ---

@Composable
fun PendingApprovalScreen(navController: NavController, viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val paymentSettings by viewModel.paymentSettings.collectAsStateWithLifecycle()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current

    // Inline Resubmission Form fields
    var retryTxId by remember { mutableStateOf("") }
    var retryMethod by remember { mutableStateOf("EasyPaisa") }
    var retryScreenshot by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Periodically poll for approval (emulating push notifications / socket)
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(4000)
            viewModel.refreshUserSession()
            val user = viewModel.currentUser.value
            if (user != null && user.status == UserStatus.APPROVED) {
                navController.navigate("main_client") {
                    popUpTo("pending_approval") { inclusive = true }
                }
                break
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (currentUser?.status == UserStatus.REJECTED) {
                // --- DECLINED / REJECTED SCREEN ---
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .drawBehind {
                            drawCircle(
                                Brush.radialGradient(
                                    colors = listOf(ErrorRed.copy(alpha = 0.2f), Color.Transparent),
                                    center = center,
                                    radius = size.width / 1.1f
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Application Declined",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Rejection Reason Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DECLINE REASON FROM ADMINISTRATOR:",
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentUser?.rejectionReason ?: "Your deposit verification screenshot could not be verified by our system. Please check your transaction details and upload a valid receipt screenshot.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Inline Resubmission Form
                Text(
                    text = "Resubmit Deposit Details:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Payment selector
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("EasyPaisa", "JazzCash", "Bank").forEach { method ->
                        val selected = retryMethod == method
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (selected) AccentCyan.copy(alpha = 0.15f) else PremiumDarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { retryMethod = method }
                                .border(1.dp, if (selected) AccentCyan else GlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                Text(method, color = if (selected) AccentCyan else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = retryTxId,
                    onValueChange = { retryTxId = it; errorMessage = null },
                    label = { Text("New Receipt TRX ID", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = AccentCyan) },
                    modifier = Modifier.fillMaxWidth().testTag("retry_txid_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Screenshot Uploader
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable {
                            retryScreenshot = "iVBORw0KGgoAAAANSUhEUgAAASwAAACWAQMAAABfCwZFAAAABlBMVEUAAAD///+l2Z/dAAAAMklEQVQI12NgGAWjYBSMglEwCkbBSAcMRoEBpDEp0pgaAEnTAJLGDyCNE0CaEEDSAAD//8KzA7E/Vv0VAAAAAElFTkSuQmCC"
                        }
                        .border(1.dp, if (retryScreenshot != null) AccentCyan else GlassBorder, RoundedCornerShape(12.dp)),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (retryScreenshot != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                                Text("new_deposit_receipt.jpg (1.5 MB) ✅", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                                Text("ATTACH FRESH SCREENSHOT", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(retryTxId, color = ErrorRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (retryTxId.isBlank()) {
                            errorMessage = "Please enter your receipt transaction ID."
                            return@Button
                        }
                        if (retryScreenshot == null) {
                            errorMessage = "Please attach a new receipt screenshot."
                            return@Button
                        }

                        viewModel.resubmitApplication(
                            txId = retryTxId.trim(),
                            method = retryMethod,
                            screenshotBase64 = retryScreenshot,
                            onSuccess = {
                                retryTxId = ""
                                retryScreenshot = null
                                android.widget.Toast.makeText(context, "Resubmitted successfully!", android.widget.Toast.LENGTH_LONG).show()
                            },
                            onError = { errorMessage = it }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resubmit_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = PremiumDarkBg, modifier = Modifier.size(24.dp))
                    } else {
                        Text("RESUBMIT PROOF FOR RE-AUDIT", color = PremiumDarkBg, fontWeight = FontWeight.Bold)
                    }
                }

            } else {
                // --- PENDING SCREEN ---
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .drawBehind {
                            drawCircle(
                                Brush.radialGradient(
                                    colors = listOf(AccentPink.copy(alpha = 0.15f), Color.Transparent),
                                    center = center,
                                    radius = size.width / 1.1f
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = AccentPink,
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Account Under Review",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your security deposit receipt is being verified by our audit team. Standard profile audits take 10-15 minutes.\n\nYou do not need to refresh this screen. Your dashboard will unlock instantly in real-time as soon as the audit completes.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CURRENT AUDIT STATUS: PENDING VERIFICATION",
                            color = AccentPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Admin can approve or reject this profile in the User Approvals panel.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("pending_approval") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "SIGN OUT / BACK TO LOGIN", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
