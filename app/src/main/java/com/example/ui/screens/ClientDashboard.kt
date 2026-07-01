package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.theme.*

// --- CLIENT BOTTOM NAVIGATION HOST ---

@Composable
fun MainClientScreen(navController: NavController, viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PremiumDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("client_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.SportsScore, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PremiumDarkBg,
                        selectedTextColor = AccentCyan,
                        indicatorColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Payments") },
                    label = { Text("Payments", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PremiumDarkBg,
                        selectedTextColor = AccentCyan,
                        indicatorColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(badge = {
                            Badge(containerColor = GoldVIP) {
                                Text("PRO", color = PremiumDarkBg, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                        }) {
                            Icon(Icons.Default.Stars, contentDescription = "VIP")
                        }
                    },
                    label = { Text("VIP", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PremiumDarkBg,
                        selectedTextColor = AccentCyan,
                        indicatorColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        if (unreadCount > 0) {
                            BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PremiumDarkBg,
                        selectedTextColor = AccentCyan,
                        indicatorColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumDarkBg)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTab(navController, viewModel)
                1 -> PaymentsTab(viewModel)
                2 -> VipTab(navController, viewModel)
                3 -> ProfileTab(navController, viewModel)
            }
        }
    }
}

// --- HOME TAB (EVENTS LISTING) ---

@Composable
fun HomeTab(navController: NavController, viewModel: AppViewModel) {
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MatchCategory?>(null) }

    // Filtered lists
    val filteredMatches = matches.filter { match ->
        val matchesSearch = match.tournamentName.contains(searchQuery, ignoreCase = true) ||
                match.teamA.contains(searchQuery, ignoreCase = true) ||
                match.teamB.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || match.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // App header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Apex Predictions",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Pick match winners. Earn top payouts.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            // Minimal glowing live indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuccessGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("LIVE", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tournaments or teams...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = GlassBorder,
                focusedContainerColor = PremiumDarkSurface,
                unfocusedContainerColor = PremiumDarkSurface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Categories Chips List
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All Sports") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan,
                        selectedLabelColor = PremiumDarkBg,
                        containerColor = PremiumDarkSurface,
                        labelColor = TextPrimary
                    )
                )
            }
            items(MatchCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan,
                        selectedLabelColor = PremiumDarkBg,
                        containerColor = PremiumDarkSurface,
                        labelColor = TextPrimary
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Event List
        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SportsScore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No active matches found", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredMatches) { match ->
                    MatchCard(match = match, onJoinClick = {
                        navController.navigate("event_details/${match.id}")
                    })
                }
            }
        }
    }
}

// --- INDIVIDUAL MATCH CARD COMPONENT ---

@Composable
fun MatchCard(match: Match, onJoinClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
    ) {
        Column {
            // Match Header Banner with Image background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = match.matchImage,
                        error = rememberAsyncImagePainter("https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400")
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark shade overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, PremiumDarkSurface.copy(alpha = 0.95f))
                            )
                        )
                )

                // Category and Status Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(match.category.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    val statusColor = when (match.status) {
                        MatchStatus.OPEN -> SuccessGreen
                        MatchStatus.UPCOMING -> AccentCyan
                        MatchStatus.CLOSED -> ErrorRed
                        MatchStatus.COMPLETED -> TextSecondary
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(match.status.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Tournament Name overlayed
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = match.tournamentName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Teams & Score info
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team A
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PremiumDarkCard)
                                .border(1.dp, GlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                match.teamA.take(2).uppercase(),
                                color = AccentCyan,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(match.teamA, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    // VS indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VS", color = AccentPink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(
                            text = "${match.date} | ${match.time}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Team B
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PremiumDarkCard)
                                .border(1.dp, GlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                match.teamB.take(2).uppercase(),
                                color = AccentPink,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(match.teamB, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Stats Section (Entry Fee, Prize Pool, Slots)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PremiumDarkBg)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ENTRY FEE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("PKR ${match.entryFee.toInt()}", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PRIZE POOL", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("PKR ${match.prizePool.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SLOTS LEFT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${match.remainingSlots}/${match.totalSlots}",
                            color = if (match.remainingSlots < 10) ErrorRed else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Join/View Details Button
                val buttonText = when (match.status) {
                    MatchStatus.OPEN -> "JOIN PREDICTION"
                    MatchStatus.UPCOMING -> "VIEW MATCH DETAILS"
                    MatchStatus.CLOSED -> "CLOSED"
                    MatchStatus.COMPLETED -> "COMPLETED (Settled: ${match.predictedResult})"
                }
                val buttonEnabled = match.status == MatchStatus.OPEN || match.status == MatchStatus.UPCOMING

                Button(
                    onClick = onJoinClick,
                    enabled = buttonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (match.status == MatchStatus.OPEN) AccentCyan else PremiumDarkCard,
                        disabledContainerColor = PremiumDarkCard.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = if (match.status == MatchStatus.OPEN) PremiumDarkBg else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// --- PAYMENTS SCREEN ---

@Composable
fun PaymentsTab(viewModel: AppViewModel) {
    val context = LocalContext.current
    var paymentMethod by remember { mutableStateOf("EasyPaisa") }
    var transactionId by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var uploadSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val userPayments by viewModel.userPayments.collectAsStateWithLifecycle()

    // Mock payments detail parameters
    val accountEasyPaisa = "0300-1234567"
    val accountJazzCash = "0312-9876543"
    val accountBank = "Meezan Bank Ltd: 5506-0102030405"
    val accountTitle = "APEX ENTERPRISES"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Payment Portal", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text("Add prediction entries & membership balances", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Select payment method
        Text("1. CHOOSE TRANSACTION ROUTE", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("EasyPaisa", "JazzCash", "Bank Account").forEach { method ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (paymentMethod == method) AccentCyan else PremiumDarkSurface)
                        .border(1.dp, if (paymentMethod == method) AccentCyan else GlassBorder, RoundedCornerShape(10.dp))
                        .clickable { paymentMethod = method }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        method,
                        color = if (paymentMethod == method) PremiumDarkBg else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Show Instructions & QR details
        Card(
            colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ACCOUNT DETAILS", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                val currentAccountNo = when (paymentMethod) {
                    "EasyPaisa" -> accountEasyPaisa
                    "JazzCash" -> accountJazzCash
                    else -> accountBank
                }
                
                Text(
                    text = "Title: $accountTitle",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account: $currentAccountNo",
                        color = AccentCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Account", currentAccountNo)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Account details copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = GlassBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "How to Transfer:\n1. Open your $paymentMethod app.\n2. Initiate a transaction to the account details above.\n3. Make note of the exact TRANSACTION ID.\n4. Enter the amount & ID below to submit screenshot proof.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Step 3: Enter payment proof
        Text("2. UPLOAD TRANSACTION VERIFICATION", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = transferAmount,
            onValueChange = { transferAmount = it },
            label = { Text("Transferred Amount (PKR)", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, focusedContainerColor = PremiumDarkSurface, unfocusedContainerColor = PremiumDarkSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = transactionId,
            onValueChange = { transactionId = it },
            label = { Text("Transaction ID / Reference ID", color = TextSecondary) },
            placeholder = { Text("e.g. 52319203923") },
            modifier = Modifier.fillMaxWidth().testTag("transaction_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, focusedContainerColor = PremiumDarkSurface, unfocusedContainerColor = PremiumDarkSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Simulated file screenshot selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PremiumDarkSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = AccentCyan)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Screenshot file path selected:", color = TextSecondary, fontSize = 12.sp)
            }
            Text("receipt_proof.jpg", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amt = transferAmount.toDoubleOrNull()
                if (amt == null || amt <= 0) {
                    errorMessage = "Please enter a valid transfer amount."
                    return@Button
                }
                if (transactionId.isBlank()) {
                    errorMessage = "Please enter the Transaction ID."
                    return@Button
                }
                errorMessage = null

                viewModel.submitDirectPayment(
                    amount = amt,
                    method = paymentMethod,
                    txId = transactionId.trim(),
                    screenshotUri = "proof_screenshot_for_${transactionId}",
                    type = PaymentType.MATCH_ENTRY, // direct matching recharge
                    refId = "direct_wallet_recharge",
                    onSuccess = {
                        uploadSuccess = true
                        transferAmount = ""
                        transactionId = ""
                    },
                    onError = { errorMessage = it }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("payment_submit_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("SUBMIT TRANSACTION PROOF", color = PremiumDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        if (uploadSuccess) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().border(1.dp, SuccessGreen, RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Proof uploaded successfully! Waiting for Admin verification.", color = SuccessGreen, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Section
        Text("TRANSACTION HISTORY", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (userPayments.isEmpty()) {
            Text("No payments submitted yet.", color = TextSecondary, fontSize = 13.sp)
        } else {
            userPayments.forEach { payment ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(payment.paymentMethod, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("TX ID: ${payment.transactionId}", color = TextSecondary, fontSize = 11.sp)
                            Text("PKR ${payment.amount.toInt()} | ${payment.type.name}", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        val statusColor = when (payment.status) {
                            PaymentStatus.APPROVED -> SuccessGreen
                            PaymentStatus.PENDING -> AccentCyan
                            PaymentStatus.REJECTED -> ErrorRed
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(payment.status.name, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- VIP TAB (SUBSCRIPTION PLANS) ---

@Composable
fun VipTab(navController: NavController, viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isVip = currentUser?.isVip ?: false
    var selectedPlan by remember { mutableStateOf<String?>(null) }
    var inputTxId by remember { mutableStateOf("") }
    var joinSuccess by remember { mutableStateOf(false) }
    var joinError by remember { mutableStateOf<String?>(null) }

    val vipPlans = listOf(
        Triple("Monthly Elite", "1,500", "Perfect to test premium signals"),
        Triple("Quarterly Master", "3,500", "Best value for regular predictions"),
        Triple("Yearly Legend", "10,000", "Elite notification signals & grand events")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Elite Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(GoldVIP, AccentPink)))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = PremiumDarkBg, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("APEX ELITE CLUB", color = PremiumDarkBg, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isVip) "YOU ARE AN ACTIVE VIP MEMBER!" else "Unlock premium analytical forecasts & exclusive prediction events with elite odds.",
                    color = PremiumDarkBg,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (!isVip) {
            Text("SELECT VIP ACCESS PACKAGE", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Render plans
            vipPlans.forEach { plan ->
                val isSelected = selectedPlan == plan.first
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) PremiumDarkCard else PremiumDarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, if (isSelected) GoldVIP else GlassBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedPlan = plan.first },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plan.first, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(plan.third, color = TextSecondary, fontSize = 12.sp)
                        }
                        Text("PKR ${plan.second}", color = GoldVIP, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }

            if (selectedPlan != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("SUBMIT SUBSCRIPTION PAY ID", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Instructions: Transfer the exact plan amount to our EasyPaisa or JazzCash (on the Payments tab) and enter the Tx ID here to subscribe.", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputTxId,
                    onValueChange = { inputTxId = it },
                    label = { Text("Transaction ID Reference", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("vip_tx_id_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder),
                    shape = RoundedCornerShape(12.dp)
                )
                
                if (joinError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(joinError!!, color = ErrorRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (inputTxId.isBlank()) {
                            joinError = "Please input the Transaction ID."
                            return@Button
                        }
                        joinError = null
                        val amount = when (selectedPlan) {
                            "Monthly Elite" -> 1500.0
                            "Quarterly Master" -> 3500.0
                            else -> 10000.0
                        }

                        viewModel.submitDirectPayment(
                            amount = amount,
                            method = "EasyPaisa / JazzCash",
                            txId = inputTxId.trim(),
                            screenshotUri = "vip_sub_ref",
                            type = PaymentType.VIP_SUBSCRIBE,
                            refId = selectedPlan!!,
                            onSuccess = {
                                joinSuccess = true
                                inputTxId = ""
                                selectedPlan = null
                            },
                            onError = { joinError = it }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVIP),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("vip_apply_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("APPLY FOR VIP ACCESS", color = PremiumDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Already VIP
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, GoldVIP, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Active VIP Privileges:", color = GoldVIP, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⭐ Access to direct analyst forecast summaries.", color = TextPrimary, fontSize = 13.sp)
                    Text("⭐ Higher success rate premium alerts.", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("⭐ Priority customer support chat resolving under 15 mins.", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        if (joinSuccess) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().border(1.dp, SuccessGreen, RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("VIP Request Submitted! Approved in 15 mins.", color = SuccessGreen, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("PREMIUM ANALYST CORNER", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Analyst Card
        Card(
            colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isVip) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚡ TODAY'S HIGH-ODDS VIP ALERTS ⚡", color = AccentCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚽ Real Madrid vs Man City: Analytic algorithms predict high 2.5+ goals with primary dominance of Madrid attacking line in early 1st half. Outliers suggest a Team A win.",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "🏏 India vs Pakistan: Bowling averages at Islamabad venue heavily favor spin. If Pak bats first, total expected under 155.",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(PremiumDarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldVIP, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("VIP ANALYST CHIPS LOCKED", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Subscribe to Monthly, Quarterly, or Yearly plans above to view.", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- PROFILE TAB ---

@Composable
fun ProfileTab(navController: NavController, viewModel: AppViewModel) {
    val localContext = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val entries by viewModel.userEntries.collectAsStateWithLifecycle()
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf<String?>(null) }

    var showSupportDialog by remember { mutableStateOf(false) }
    var supportText by remember { mutableStateOf("") }

    val wonCount = entries.count { it.winLossStatus == WinLossStatus.WON }
    val lostCount = entries.count { it.winLossStatus == WinLossStatus.LOST }
    val pendingCount = entries.count { it.winLossStatus == WinLossStatus.PENDING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glow avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PremiumDarkCard)
                        .border(1.5.dp, AccentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentUser?.name ?: "User",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (currentUser?.isVip == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldVIP)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("VIP", color = PremiumDarkBg, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(text = currentUser?.email ?: "", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        text = "Phone: ${currentUser?.mobileNumber ?: ""}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Win Loss prediction stats dashboard
        Text("PREDICTION TRACKER", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Won Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WON", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(wonCount.toString(), color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            // Lost Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LOST", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(lostCount.toString(), color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            // Pending Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PENDING", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(pendingCount.toString(), color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // History list
        Text("YOUR ACTIVE PREDICTIONS", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (entries.isEmpty()) {
            Text("You haven't predicted on any matches yet.", color = TextSecondary, fontSize = 12.sp)
        } else {
            entries.forEach { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Predict Winner: ${entry.predictedOutcome}", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Transaction ID: ${entry.transactionId}", color = TextSecondary, fontSize = 11.sp)
                        }

                        // Label status
                        val badgeColor = when (entry.winLossStatus) {
                            WinLossStatus.PENDING -> when (entry.status) {
                                EntryStatus.PENDING -> TextSecondary
                                EntryStatus.APPROVED -> AccentCyan
                                EntryStatus.REJECTED -> ErrorRed
                            }
                            WinLossStatus.WON -> SuccessGreen
                            WinLossStatus.LOST -> ErrorRed
                        }
                        val badgeText = when (entry.winLossStatus) {
                            WinLossStatus.PENDING -> when (entry.status) {
                                EntryStatus.PENDING -> "PENDING VERIFICATION"
                                EntryStatus.APPROVED -> "VERIFIED"
                                EntryStatus.REJECTED -> "VERIFICATION REJECTED"
                            }
                            WinLossStatus.WON -> "WON"
                            WinLossStatus.LOST -> "LOST"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(badgeText, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Notifications
        Text("ALERTS & MESSAGES", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (notifications.isEmpty()) {
            Text("No new messages.", color = TextSecondary, fontSize = 12.sp)
        } else {
            notifications.forEach { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(note.title, color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (!note.isRead) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentPink))
                            }
                        }
                        Text(note.message, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Settings / Actions list
        Text("SETTINGS & SERVICES", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Support tickets toggle
        ListItem(
            headlineContent = { Text("Contact Customer Support", color = TextPrimary) },
            leadingContent = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentCyan) },
            colors = ListItemDefaults.colors(containerColor = PremiumDarkSurface),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .clickable { showSupportDialog = true }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password change
        ListItem(
            headlineContent = { Text("Change Password", color = TextPrimary) },
            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan) },
            colors = ListItemDefaults.colors(containerColor = PremiumDarkSurface),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .clickable { showPasswordDialog = true }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Delete Request
        ListItem(
            headlineContent = { Text("Request Account Deletion", color = ErrorRed) },
            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
            colors = ListItemDefaults.colors(containerColor = PremiumDarkSurface),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .clickable {
                    viewModel.requestDeleteAccount {
                        Toast.makeText(localContext, "Account deletion request submitted to Admins.", Toast.LENGTH_LONG).show()
                    }
                }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Logout
        Button(
            onClick = {
                viewModel.logout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("LOG OUT", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Dialogs
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("Change Password", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = oldPass,
                            onValueChange = { oldPass = it },
                            label = { Text("Old Password", color = TextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("New Password", color = TextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary)
                        )
                        if (passError != null) {
                            Text(passError!!, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.changePassword(oldPass, newPass,
                            onSuccess = {
                                Toast.makeText(localContext, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                showPasswordDialog = false
                                oldPass = ""
                                newPass = ""
                            },
                            onError = { passError = it }
                        )
                    }, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)) {
                        Text("Save", color = PremiumDarkBg)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel", color = AccentCyan) }
                },
                containerColor = PremiumDarkSurface
            )
        }

        if (showSupportDialog) {
            AlertDialog(
                onDismissRequest = { showSupportDialog = false },
                title = { Text("Submit Support Query", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = supportText,
                            onValueChange = { supportText = it },
                            label = { Text("Describe your issue / question", color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.submitSupportMessage(supportText) {
                            Toast.makeText(localContext, "Query submitted! Admins will reply soon.", Toast.LENGTH_SHORT).show()
                            showSupportDialog = false
                            supportText = ""
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)) {
                        Text("Submit", color = PremiumDarkBg)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSupportDialog = false }) { Text("Cancel", color = AccentCyan) }
                },
                containerColor = PremiumDarkSurface
            )
        }
    }
}

// --- EVENT DETAILS SCREEN ---

@Composable
fun EventDetailsScreen(matchId: Long, navController: NavController, viewModel: AppViewModel) {
    val context = LocalContext.current
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val match = matches.find { it.id == matchId }

    var selectedOutcome by remember { mutableStateOf<String?>(null) }
    var txIdInput by remember { mutableStateOf("") }
    var joinSuccess by remember { mutableStateOf(false) }
    var joinError by remember { mutableStateOf<String?>(null) }
    var paymentMethodSelected by remember { mutableStateOf("EasyPaisa") }
    var proofScreenshotBase64 by remember { mutableStateOf<String?>(null) }

    if (match == null) {
        Box(modifier = Modifier.fillMaxSize().background(PremiumDarkBg), contentAlignment = Alignment.Center) {
            Text("Match details not found.", color = TextPrimary)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Image with Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = match.matchImage,
                        error = rememberAsyncImagePainter("https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400")
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, PremiumDarkBg)))
                )

                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(PremiumDarkSurface.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            }

            // Tournament Title & Info
            Column(modifier = Modifier.padding(16.dp)) {
                Text(match.tournamentName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${match.category.name} | ${match.date} at ${match.time}", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))
                Text("RULES OF ENGAGEMENT", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. To join predictions, pick either Team A, Team B or a Draw outcome.\n2. Proceed to submit your Entry Fee of PKR ${match.entryFee.toInt()}.\n3. You must make this payment via EasyPaisa or JazzCash.\n4. Input the precise Transaction ID below to verify your entry slot.\n5. Matches settle immediately after sports analysts update official results.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PremiumDarkSurface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Entry Cost", color = TextSecondary, fontSize = 11.sp)
                        Text("PKR ${match.entryFee.toInt()}", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Grand Prize Pool", color = TextSecondary, fontSize = 11.sp)
                        Text("PKR ${match.prizePool.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Remaining Slots", color = TextSecondary, fontSize = 11.sp)
                        Text("${match.remainingSlots}/${match.totalSlots}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Selection Outcomes
                Text("PICK WINNING OUTCOME", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TEAM_A", "DRAW", "TEAM_B").forEach { outcome ->
                        val textRepresentation = when (outcome) {
                            "TEAM_A" -> match.teamA
                            "TEAM_B" -> match.teamB
                            else -> "Draw Match"
                        }
                        val isSelected = selectedOutcome == outcome
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentCyan else PremiumDarkSurface)
                                .border(1.dp, if (isSelected) AccentCyan else GlassBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedOutcome = outcome }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                textRepresentation,
                                color = if (isSelected) PremiumDarkBg else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                if (selectedOutcome != null) {
                    Text("VERIFY PAYMENT TO JOIN POOL", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Please transfer PKR ${match.entryFee.toInt()} and select your transaction method, attach screenshot, and enter Transaction ID.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("SELECT PAYMENT METHOD", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EasyPaisa", "JazzCash", "Bank").forEach { method ->
                            val selected = paymentMethodSelected == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) AccentCyan.copy(alpha = 0.15f) else PremiumDarkSurface)
                                    .border(1.dp, if (selected) AccentCyan else GlassBorder, RoundedCornerShape(8.dp))
                                    .clickable { paymentMethodSelected = method }
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(method, color = if (selected) AccentCyan else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("UPLOAD TRANSFER PROOF SCREENSHOT", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                proofScreenshotBase64 = "iVBORw0KGgoAAAANSUhEUgAAASwAAACWAQMAAABfCwZFAAAABlBMVEUAAAD///+l2Z/dAAAAMklEQVQI12NgGAWjYBSMglEwCkbBSAcMRoEBpDEp0pgaAEnTAJLGDyCNE0CaEEDSAAD//8KzA7E/Vv0VAAAAAElFTkSuQmCC"
                            }
                            .border(1.dp, if (proofScreenshotBase64 != null) AccentCyan else GlassBorder, RoundedCornerShape(12.dp)),
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (proofScreenshotBase64 != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("payment_receipt.jpg (1.4 MB) ✅", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                                    Text("TAP TO UPLOAD DEPOSIT SCREENSHOT", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = txIdInput,
                        onValueChange = { txIdInput = it },
                        label = { Text("Transaction ID (TID)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth().testTag("match_tx_id_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (joinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(joinError!!, color = ErrorRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (txIdInput.isBlank()) {
                                joinError = "Please input the Transaction ID."
                                return@Button
                            }
                            if (proofScreenshotBase64 == null) {
                                joinError = "Please tap above to upload the screenshot."
                                return@Button
                            }
                            joinError = null
                            viewModel.joinMatch(
                                match = match,
                                txId = txIdInput.trim(),
                                paymentMethod = paymentMethodSelected,
                                screenshotBase64 = proofScreenshotBase64,
                                onSuccess = {
                                    joinSuccess = true
                                    txIdInput = ""
                                    selectedOutcome = null
                                    proofScreenshotBase64 = null
                                },
                                onError = { joinError = it }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("join_pool_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SUBMIT AND ENROLL", color = PremiumDarkBg, fontWeight = FontWeight.Bold)
                    }
                }

                if (joinSuccess) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SuccessGreen, RoundedCornerShape(12.dp))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Join Request Saved! Admin verification takes up to 2 hours. Go to Profile Tab to monitor progress.", color = SuccessGreen, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
