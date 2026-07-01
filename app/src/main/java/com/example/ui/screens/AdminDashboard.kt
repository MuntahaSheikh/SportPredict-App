package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(navController: NavController, viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Collect variables
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val supports by viewModel.allSupportMessages.collectAsStateWithLifecycle()

    val pendingUsersCount = users.count { it.status == UserStatus.PENDING_APPROVAL }
    val pendingPaymentsCount = payments.count { it.status == PaymentStatus.PENDING }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(PremiumDarkSurface)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apex Control Center", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout", tint = ErrorRed)
                    }
                }

                // Horizontal scrollable tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PremiumDarkSurface,
                    contentColor = AccentCyan,
                    edgePadding = 16.dp
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Stats", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Users", modifier = Modifier.padding(vertical = 14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (pendingUsersCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.clip(CircleShape).background(AccentPink).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(pendingUsersCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("Matches", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Payments", modifier = Modifier.padding(vertical = 14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (pendingPaymentsCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.clip(CircleShape).background(SuccessGreen).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(pendingPaymentsCount.toString(), color = PremiumDarkBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                        Text("Tickets", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }) {
                        Text("Broadcasts", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
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
                0 -> StatsTab(users, matches, payments)
                1 -> UsersTab(users, viewModel)
                2 -> MatchesTab(matches, viewModel)
                3 -> PaymentsTabAdmin(payments, viewModel)
                4 -> TicketsTab(supports, viewModel)
                5 -> BroadcastTab(viewModel)
            }
        }
    }
}

// --- TAB 0: ANALYTICS STATS ---

@Composable
fun StatsTab(users: List<User>, matches: List<Match>, payments: List<PaymentProof>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("SYSTEM REPORT", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Large summary analytics cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL USERS", color = TextSecondary, fontSize = 11.sp)
                    Text(users.size.toString(), color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVE EVENTS", color = TextSecondary, fontSize = 11.sp)
                    Text(matches.count { it.status == MatchStatus.OPEN }.toString(), color = AccentCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PENDING VERIFY", color = TextSecondary, fontSize = 11.sp)
                    Text(payments.count { it.status == PaymentStatus.PENDING }.toString(), color = AccentPink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
            val totalRecharged = payments.filter { it.status == PaymentStatus.APPROVED }.sumOf { it.amount }.toInt()
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL TURNOVER", color = TextSecondary, fontSize = 11.sp)
                    Text("PKR $totalRecharged", color = SuccessGreen, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text("QUICK ALERTS SUMMARY", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        val pendingApprovals = users.filter { it.status == UserStatus.PENDING_APPROVAL }
        if (pendingApprovals.isEmpty()) {
            Text("All registration requests processed! Beautiful job.", color = TextSecondary, fontSize = 13.sp)
        } else {
            pendingApprovals.forEach { pendingUser ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pendingUser.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(pendingUser.email, color = TextSecondary, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentPink.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("PENDING", color = AccentPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 1: USERS MANAGEMENT ---

@Composable
fun UsersTab(users: List<User>, viewModel: AppViewModel) {
    var userToReject by remember { mutableStateOf<User?>(null) }
    var rejectReasonInput by remember { mutableStateOf("") }

    if (userToReject != null) {
        AlertDialog(
            onDismissRequest = { userToReject = null },
            title = { Text("Decline Profile Registration", color = TextPrimary) },
            text = {
                Column {
                    Text("Provide a specific reason why you are declining the profile of ${userToReject!!.name}:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        label = { Text("Decline Reason", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, focusedBorderColor = AccentCyan, unfocusedBorderColor = GlassBorder, cursorColor = AccentCyan
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = rejectReasonInput.ifBlank { "Unverified deposit transaction screenshot. Please submit a valid receipt." }
                        viewModel.rejectUser(userToReject!!.email, reason)
                        userToReject = null
                        rejectReasonInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Decline", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToReject = null; rejectReasonInput = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PremiumDarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users) { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(user.email, color = TextSecondary, fontSize = 12.sp)
                            Text("Mobile: ${user.mobileNumber}", color = TextSecondary, fontSize = 12.sp)
                        }

                        // Display Role & Status Badge
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (user.role == UserRole.ADMIN) AccentCyan.copy(alpha = 0.15f) else AccentPink.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(user.role.name, color = if (user.role == UserRole.ADMIN) AccentCyan else AccentPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val statusColor = when (user.status) {
                                UserStatus.APPROVED -> SuccessGreen
                                UserStatus.PENDING_APPROVAL -> AccentCyan
                                UserStatus.REJECTED -> ErrorRed
                                UserStatus.SUSPENDED -> TextSecondary
                            }
                            Text(user.status.name, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Optional details (CNIC, EasyPaisa, JazzCash)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("CNIC: ${user.cnic ?: "Not Provided"}", color = TextSecondary, fontSize = 11.sp)
                    Text("EP No: ${user.easyPaisaNumber ?: "Not Provided"} | JC No: ${user.jazzCashNumber ?: "Not Provided"}", color = TextSecondary, fontSize = 11.sp)

                    // Actions (for normal users)
                    if (user.role == UserRole.USER) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (user.status == UserStatus.PENDING_APPROVAL || user.status == UserStatus.REJECTED) {
                                Button(
                                    onClick = { viewModel.approveUser(user.email) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    modifier = Modifier.padding(horizontal = 4.dp).testTag("approve_user_${user.email.substringBefore("@")}"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Approve", color = PremiumDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            if (user.status == UserStatus.PENDING_APPROVAL) {
                                Button(
                                    onClick = { userToReject = user },
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                    modifier = Modifier.padding(horizontal = 4.dp).testTag("reject_user_${user.email.substringBefore("@")}"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reject", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            if (user.status == UserStatus.APPROVED) {
                                OutlinedButton(
                                    onClick = { viewModel.suspendUser(user.email) },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Suspend", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: MATCHES MANAGEMENT (CREATE / COMPLETED RESOLUTION) ---

@Composable
fun MatchesTab(matches: List<Match>, viewModel: AppViewModel) {
    val context = LocalContext.current
    var showCreateForm by remember { mutableStateOf(false) }

    // Form states
    var tourneyName by remember { mutableStateOf("") }
    var teamA by remember { mutableStateOf("") }
    var teamB by remember { mutableStateOf("") }
    var entryFee by remember { mutableStateOf("") }
    var prizePool by remember { mutableStateOf("") }
    var totalSlots by remember { mutableStateOf("") }
    var dateVal by remember { mutableStateOf("2026-07-01") }
    var timeVal by remember { mutableStateOf("18:00") }
    var categoryVal by remember { mutableStateOf(MatchCategory.CRICKET) }

    var resolvingMatchId by remember { mutableStateOf<Long?>(null) }
    var winningOutcome by remember { mutableStateOf("TEAM_A") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MATCH MANAGEMENT", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showCreateForm = !showCreateForm },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(if (showCreateForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = PremiumDarkBg)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showCreateForm) "Hide Form" else "New Match", color = PremiumDarkBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Create match form
        if (showCreateForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("ADD SPORTS FORECAST EVENT", color = AccentCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = tourneyName, onValueChange = { tourneyName = it }, label = { Text("Tournament / League Title") }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = teamA, onValueChange = { teamA = it }, label = { Text("Team A") }, modifier = Modifier.weight(1f).padding(end = 4.dp))
                        OutlinedTextField(value = teamB, onValueChange = { teamB = it }, label = { Text("Team B") }, modifier = Modifier.weight(1f).padding(start = 4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("Fee (PKR)") }, modifier = Modifier.weight(1f).padding(end = 4.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = prizePool, onValueChange = { prizePool = it }, label = { Text("Prize (PKR)") }, modifier = Modifier.weight(1f).padding(start = 4.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = totalSlots, onValueChange = { totalSlots = it }, label = { Text("Slots") }, modifier = Modifier.weight(1f).padding(end = 4.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = dateVal, onValueChange = { dateVal = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f).padding(start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("SPORT CATEGORY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MatchCategory.values().forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (categoryVal == cat) AccentCyan else PremiumDarkBg)
                                    .clickable { categoryVal = cat }
                                    .padding(8.dp)
                            ) {
                                Text(cat.name, color = if (categoryVal == cat) PremiumDarkBg else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val fee = entryFee.toDoubleOrNull()
                            val prize = prizePool.toDoubleOrNull()
                            val slots = totalSlots.toIntOrNull()

                            if (tourneyName.isBlank() || teamA.isBlank() || teamB.isBlank() || fee == null || prize == null || slots == null) {
                                Toast.makeText(context, "Please fill out all fields correctly.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.createMatch(
                                tournamentName = tourneyName,
                                teamA = teamA,
                                teamB = teamB,
                                date = dateVal,
                                time = timeVal,
                                entryFee = fee,
                                prizePool = prize,
                                totalSlots = slots,
                                matchImage = "", // Uses default fallback
                                category = categoryVal
                            )

                            Toast.makeText(context, "New sports prediction posted successfully!", Toast.LENGTH_SHORT).show()
                            showCreateForm = false
                            tourneyName = ""
                            teamA = ""
                            teamB = ""
                            entryFee = ""
                            prizePool = ""
                            totalSlots = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.fillMaxWidth().testTag("add_match_submit")
                    ) {
                        Text("PUBLISH MATCH EVENT", color = PremiumDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List of matches with settle option
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(matches) { match ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(match.tournamentName, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("${match.teamA} VS ${match.teamB} (${match.category.name})", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Status: ${match.status.name}", color = TextSecondary, fontSize = 11.sp)
                            }
                            IconButton(onClick = { viewModel.deleteMatch(match.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Match", tint = ErrorRed)
                            }
                        }

                        // Settle controls
                        if (match.status == MatchStatus.OPEN || match.status == MatchStatus.UPCOMING) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = GlassBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            if (resolvingMatchId == match.id) {
                                Text("SELECT WINNING OUTCOME", color = AccentPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("TEAM_A", "DRAW", "TEAM_B").forEach { outcome ->
                                        val display = when (outcome) {
                                            "TEAM_A" -> match.teamA
                                            "TEAM_B" -> match.teamB
                                            else -> "Draw"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (winningOutcome == outcome) AccentCyan else PremiumDarkBg)
                                                .clickable { winningOutcome = outcome }
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(display, color = if (winningOutcome == outcome) PremiumDarkBg else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateMatchStatus(match.id, MatchStatus.COMPLETED, winningOutcome)
                                            resolvingMatchId = null
                                            Toast.makeText(context, "Match settled and payouts credited!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        modifier = Modifier.weight(1f).testTag("settle_confirm_${match.id}")
                                    ) {
                                        Text("RESOLVE & CREDIT", color = PremiumDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { resolvingMatchId = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = PremiumDarkBg),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("CANCEL", color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { resolvingMatchId = match.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                    modifier = Modifier.fillMaxWidth().testTag("settle_match_${match.id}"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("SETTLE MATCH OUTCOME", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Settled outcome: ${match.predictedResult}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: PAYMENTS VERIFICATION AUDIT ---

@Composable
fun PaymentsTabAdmin(payments: List<PaymentProof>, viewModel: AppViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (payments.isEmpty()) {
            item {
                Text("No payment uploads found in system.", color = TextSecondary)
            }
        }

        items(payments) { payment ->
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("User: ${payment.userId}", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Amount: PKR ${payment.amount.toInt()}", color = AccentCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Method: ${payment.paymentMethod}", color = TextSecondary, fontSize = 11.sp)
                            Text("Type: ${payment.type.name} (${payment.referenceId})", color = AccentPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Display status
                        val col = when (payment.status) {
                            PaymentStatus.APPROVED -> SuccessGreen
                            PaymentStatus.PENDING -> AccentCyan
                            PaymentStatus.REJECTED -> ErrorRed
                        }
                        Text(payment.status.name, color = col, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TRANSACTION ID: ${payment.transactionId}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("Screenshot URI: ${payment.screenshotUri}", color = TextSecondary, fontSize = 11.sp)

                    // Approval triggers
                    if (payment.status == PaymentStatus.PENDING) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.approvePayment(payment) },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.padding(horizontal = 4.dp).testTag("approve_payment_${payment.id}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Verify & Credit", color = PremiumDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.rejectPayment(payment) },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                modifier = Modifier.padding(horizontal = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Decline", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 4: TICKETS (REPLY SYSTEM) ---

@Composable
fun TicketsTab(messages: List<SupportMessage>, viewModel: AppViewModel) {
    val context = LocalContext.current
    var activeReplyId by remember { mutableStateOf<Long?>(null) }
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (messages.isEmpty()) {
            item {
                Text("No support tickets submitted yet.", color = TextSecondary)
            }
        }

        items(messages) { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = PremiumDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("User: ${msg.userName} (${msg.userId})", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(msg.message, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (msg.reply != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PremiumDarkBg)
                                .padding(8.dp)
                        ) {
                            Text("Admin Reply: ${msg.reply}", color = SuccessGreen, fontSize = 12.sp)
                        }
                    } else {
                        if (activeReplyId == msg.id) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                label = { Text("Write your reply...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (replyText.isBlank()) return@Button
                                        viewModel.replyToSupport(msg.id, replyText.trim())
                                        activeReplyId = null
                                        replyText = ""
                                        Toast.makeText(context, "Reply dispatched!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    modifier = Modifier.testTag("submit_reply_${msg.id}")
                                ) {
                                    Text("Send", color = PremiumDarkBg, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { activeReplyId = null; replyText = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumDarkBg)
                                ) {
                                    Text("Cancel", color = TextPrimary)
                                }
                            }
                        } else {
                            Button(
                                onClick = { activeReplyId = msg.id },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("reply_ticket_${msg.id}")
                            ) {
                                Text("Answer Query", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 5: GLOBAL ALERTS BROADCASTS ---

@Composable
fun BroadcastTab(viewModel: AppViewModel) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("SYSTEM NOTIFICATION BROADCAST", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Send real-time alerts to all registered user accounts instantly.", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notification Title") },
            placeholder = { Text("e.g. Server Maintenance or Match Update") },
            modifier = Modifier.fillMaxWidth().testTag("broadcast_title_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = msg,
            onValueChange = { msg = it },
            label = { Text("Notification Message") },
            placeholder = { Text("e.g. Enjoy 20% higher winning pools tonight only!") },
            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("broadcast_message_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (title.isBlank() || msg.isBlank()) {
                    Toast.makeText(context, "Please fill out both title and message.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                viewModel.sendGlobalNotification(title.trim(), msg.trim())
                Toast.makeText(context, "Broadcast dispatched to all users!", Toast.LENGTH_SHORT).show()
                title = ""
                msg = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            modifier = Modifier.fillMaxWidth().testTag("broadcast_submit_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("DISPATCH BROADCAST NOTIFICATION", color = PremiumDarkBg, fontWeight = FontWeight.Bold)
        }
    }
}
