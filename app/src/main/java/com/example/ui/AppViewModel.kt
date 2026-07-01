package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // --- SESSION STATE ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _paymentSettings = MutableStateFlow(PaymentSettings())
    val paymentSettings: StateFlow<PaymentSettings> = _paymentSettings.asStateFlow()

    private var userListener: ValueEventListener? = null
    private var userRef: com.google.firebase.database.DatabaseReference? = null

    init {
        startSyncingMatches()
        startSyncingUsers()
        startSyncingPayments()
        startSyncingSupportMessages()
        startSyncingPaymentSettings()

        // Auto-recover session!
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.currentUser?.let { fbUser ->
            val email = fbUser.email
            if (email != null) {
                viewModelScope.launch {
                    val localUser = repository.getUserByEmail(email)
                    if (localUser != null) {
                        _currentUser.value = localUser
                        startListeningToCurrentUser(email)
                    }
                }
            }
        }
    }

    fun startListeningToCurrentUser(email: String) {
        stopListeningToCurrentUser()
        val safeEmail = email.replace(".", "_")
        userRef = getDatabase().getReference("users").child(safeEmail)
        userListener = userRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    viewModelScope.launch {
                        try {
                            val name = snapshot.child("name").getValue(String::class.java) ?: ""
                            val mobileNumber = snapshot.child("mobileNumber").getValue(String::class.java) ?: ""
                            val passwordHash = snapshot.child("passwordHash").getValue(String::class.java) ?: ""
                            val cnic = snapshot.child("cnic").getValue(String::class.java)
                            val easyPaisaNumber = snapshot.child("easyPaisaNumber").getValue(String::class.java)
                            val jazzCashNumber = snapshot.child("jazzCashNumber").getValue(String::class.java)
                            val statusStr = snapshot.child("status").getValue(String::class.java) ?: "PENDING_APPROVAL"
                            val status = try { UserStatus.valueOf(statusStr) } catch (e: Exception) { UserStatus.PENDING_APPROVAL }
                            val roleStr = snapshot.child("role").getValue(String::class.java) ?: "USER"
                            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
                            val isVip = snapshot.child("isVip").getValue(Boolean::class.java) ?: snapshot.child("vip").getValue(Boolean::class.java) ?: false
                            val vipExpiryDate = snapshot.child("vipExpiryDate").getValue(Long::class.java) ?: 0L
                            val profileImage = snapshot.child("profileImage").getValue(String::class.java)
                            val depositScreenshot = snapshot.child("depositScreenshot").getValue(String::class.java)
                            val rejectionReason = snapshot.child("rejectionReason").getValue(String::class.java)

                            val updatedUser = User(
                                email = email,
                                name = name,
                                mobileNumber = mobileNumber,
                                passwordHash = passwordHash,
                                cnic = cnic,
                                easyPaisaNumber = easyPaisaNumber,
                                jazzCashNumber = jazzCashNumber,
                                status = status,
                                role = role,
                                isVip = isVip,
                                vipExpiryDate = vipExpiryDate,
                                profileImage = profileImage,
                                depositScreenshot = depositScreenshot,
                                rejectionReason = rejectionReason
                            )
                            repository.insertUser(updatedUser)
                            _currentUser.value = updatedUser
                        } catch (e: Exception) {
                            // Ignored safely
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun stopListeningToCurrentUser() {
        userListener?.let {
            userRef?.removeEventListener(it)
        }
        userListener = null
        userRef = null
    }

    // --- REACTIVE FLOWS ---
    val allMatches: StateFlow<List<Match>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentProof>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEntries: StateFlow<List<MatchEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSupportMessages: StateFlow<List<SupportMessage>> = repository.allSupportMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DYNAMIC PER-USER FLOWS ---
    val userEntries: StateFlow<List<MatchEntry>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getEntriesForUser(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPayments: StateFlow<List<PaymentProof>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getPaymentsForUser(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<AppNotification>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getNotificationsForUser(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSupportMessages: StateFlow<List<SupportMessage>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getSupportMessagesForUser(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AUTH ACTIONS ---

    fun getDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://sportpredict-pro-1c275-default-rtdb.asia-southeast1.firebasedatabase.app")
    }

    fun syncUserToFirebase(user: User) {
        try {
            val safeEmail = user.email.replace(".", "_")
            getDatabase().getReference("users").child(safeEmail).setValue(user)
        } catch (e: Exception) {
            // Background sync error logged or ignored safely
        }
    }

    fun syncMatchToFirebase(match: Match) {
        try {
            val matchMap = mapOf(
                "id" to match.id,
                "tournamentName" to match.tournamentName,
                "teamA" to match.teamA,
                "teamB" to match.teamB,
                "date" to match.date,
                "time" to match.time,
                "entryFee" to match.entryFee,
                "prizePool" to match.prizePool,
                "totalSlots" to match.totalSlots,
                "remainingSlots" to match.remainingSlots,
                "matchImage" to match.matchImage,
                "status" to match.status.name,
                "category" to match.category.name,
                "predictedResult" to match.predictedResult,
                "leagueName" to match.leagueName,
                "reportImage" to match.reportImage
            )
            getDatabase().getReference("matches")
                .child(match.id.toString())
                .setValue(matchMap)
        } catch (e: Exception) {
            // Ignored safely
        }
    }

    private fun startSyncingMatches() {
        getDatabase().getReference("matches")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModelScope.launch {
                        try {
                            if (snapshot.exists()) {
                                val remoteMatches = mutableListOf<Match>()
                                for (child in snapshot.children) {
                                    val id = child.child("id").getValue(Long::class.java) ?: continue
                                    val tournamentName = child.child("tournamentName").getValue(String::class.java) ?: ""
                                    val teamA = child.child("teamA").getValue(String::class.java) ?: ""
                                    val teamB = child.child("teamB").getValue(String::class.java) ?: ""
                                    val date = child.child("date").getValue(String::class.java) ?: ""
                                    val time = child.child("time").getValue(String::class.java) ?: ""
                                    val entryFee = child.child("entryFee").getValue(Double::class.java) ?: 0.0
                                    val prizePool = child.child("prizePool").getValue(Double::class.java) ?: 0.0
                                    val totalSlots = child.child("totalSlots").getValue(Int::class.java) ?: 0
                                    val remainingSlots = child.child("remainingSlots").getValue(Int::class.java) ?: 0
                                    val matchImage = child.child("matchImage").getValue(String::class.java) ?: ""
                                    val statusStr = child.child("status").getValue(String::class.java) ?: "OPEN"
                                    val status = try { MatchStatus.valueOf(statusStr) } catch (e: Exception) { MatchStatus.OPEN }
                                    val categoryStr = child.child("category").getValue(String::class.java) ?: "CRICKET"
                                    val category = try { MatchCategory.valueOf(categoryStr) } catch (e: Exception) { MatchCategory.CRICKET }
                                    val predictedResult = child.child("predictedResult").getValue(String::class.java) ?: "PENDING"
                                    val leagueName = child.child("leagueName").getValue(String::class.java) ?: "League Match"
                                    val reportImage = child.child("reportImage").getValue(String::class.java)

                                    val match = Match(
                                        id = id,
                                        tournamentName = tournamentName,
                                        teamA = teamA,
                                        teamB = teamB,
                                        date = date,
                                        time = time,
                                        entryFee = entryFee,
                                        prizePool = prizePool,
                                        totalSlots = totalSlots,
                                        remainingSlots = remainingSlots,
                                        matchImage = matchImage,
                                        status = status,
                                        category = category,
                                        predictedResult = predictedResult,
                                        leagueName = leagueName,
                                        reportImage = reportImage
                                    )
                                    remoteMatches.add(match)
                                }
                                
                                for (m in remoteMatches) {
                                    repository.insertMatch(m)
                                }
                            } else {
                                // If Firebase is empty, let's sync our local pre-populated matches to Firebase!
                                repository.allMatches.firstOrNull()?.forEach { localMatch ->
                                    syncMatchToFirebase(localMatch)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Ignore
                }
            })
    }

    fun syncPaymentToFirebase(payment: PaymentProof) {
        try {
            getDatabase().getReference("payments")
                .child(payment.id.toString())
                .setValue(payment)
        } catch (e: Exception) {
            // Ignored safely
        }
    }

    private fun startSyncingPayments() {
        getDatabase().getReference("payments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModelScope.launch {
                        try {
                            if (snapshot.exists()) {
                                for (child in snapshot.children) {
                                    val id = child.child("id").getValue(Long::class.java) ?: continue
                                    val userId = child.child("userId").getValue(String::class.java) ?: ""
                                    val amount = child.child("amount").getValue(Double::class.java) ?: 0.0
                                    val paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: ""
                                    val transactionId = child.child("transactionId").getValue(String::class.java) ?: ""
                                    val screenshotUri = child.child("screenshotUri").getValue(String::class.java)
                                    val statusStr = child.child("status").getValue(String::class.java) ?: "PENDING"
                                    val status = try { PaymentStatus.valueOf(statusStr) } catch (e: Exception) { PaymentStatus.PENDING }
                                    val typeStr = child.child("type").getValue(String::class.java) ?: "MATCH_ENTRY"
                                    val type = try { PaymentType.valueOf(typeStr) } catch (e: Exception) { PaymentType.MATCH_ENTRY }
                                    val referenceId = child.child("referenceId").getValue(String::class.java) ?: ""
                                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                                    val payment = PaymentProof(
                                        id = id,
                                        userId = userId,
                                        amount = amount,
                                        paymentMethod = paymentMethod,
                                        transactionId = transactionId,
                                        screenshotUri = screenshotUri,
                                        status = status,
                                        type = type,
                                        referenceId = referenceId,
                                        timestamp = timestamp
                                    )
                                    repository.insertPayment(payment)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun startSyncingUsers() {
        getDatabase().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModelScope.launch {
                        try {
                            if (snapshot.exists()) {
                                for (child in snapshot.children) {
                                    val email = child.child("email").getValue(String::class.java) ?: continue
                                    val name = child.child("name").getValue(String::class.java) ?: ""
                                    val mobileNumber = child.child("mobileNumber").getValue(String::class.java) ?: ""
                                    val passwordHash = child.child("passwordHash").getValue(String::class.java) ?: ""
                                    val cnic = child.child("cnic").getValue(String::class.java)
                                    val easyPaisaNumber = child.child("easyPaisaNumber").getValue(String::class.java)
                                    val jazzCashNumber = child.child("jazzCashNumber").getValue(String::class.java)
                                    val statusStr = child.child("status").getValue(String::class.java) ?: "PENDING_APPROVAL"
                                    val status = try { UserStatus.valueOf(statusStr) } catch (e: Exception) { UserStatus.PENDING_APPROVAL }
                                    val roleStr = child.child("role").getValue(String::class.java) ?: "USER"
                                    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
                                    val isVip = child.child("isVip").getValue(Boolean::class.java) ?: child.child("vip").getValue(Boolean::class.java) ?: false
                                    val vipExpiryDate = child.child("vipExpiryDate").getValue(Long::class.java) ?: 0L
                                    val profileImage = child.child("profileImage").getValue(String::class.java)
                                    val depositScreenshot = child.child("depositScreenshot").getValue(String::class.java)
                                    val rejectionReason = child.child("rejectionReason").getValue(String::class.java)

                                    val u = User(
                                        email = email,
                                        name = name,
                                        mobileNumber = mobileNumber,
                                        passwordHash = passwordHash,
                                        cnic = cnic,
                                        easyPaisaNumber = easyPaisaNumber,
                                        jazzCashNumber = jazzCashNumber,
                                        status = status,
                                        role = role,
                                        isVip = isVip,
                                        vipExpiryDate = vipExpiryDate,
                                        profileImage = profileImage,
                                        depositScreenshot = depositScreenshot,
                                        rejectionReason = rejectionReason
                                    )
                                    repository.insertUser(u)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun startSyncingSupportMessages() {
        getDatabase().getReference("support_messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModelScope.launch {
                        try {
                            if (snapshot.exists()) {
                                for (child in snapshot.children) {
                                    val id = child.child("id").getValue(Long::class.java) ?: continue
                                    val userId = child.child("userId").getValue(String::class.java) ?: ""
                                    val userName = child.child("userName").getValue(String::class.java) ?: ""
                                    val message = child.child("message").getValue(String::class.java) ?: ""
                                    val reply = child.child("reply").getValue(String::class.java)
                                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                                    val supportMsg = SupportMessage(id, userId, userName, message, reply, timestamp)
                                    repository.insertSupportMessage(supportMsg)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun startSyncingPaymentSettings() {
        getDatabase().getReference("payment_settings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val easypaisaNumber = snapshot.child("easypaisaNumber").getValue(String::class.java) ?: "0311-2233445"
                        val easypaisaTitle = snapshot.child("easypaisaTitle").getValue(String::class.java) ?: "Report Badshah Official"
                        val jazzcashNumber = snapshot.child("jazzcashNumber").getValue(String::class.java) ?: "0322-5566778"
                        val jazzcashTitle = snapshot.child("jazzcashTitle").getValue(String::class.java) ?: "Report Badshah Official"
                        val bankName = snapshot.child("bankName").getValue(String::class.java) ?: "Meezan Bank Ltd"
                        val bankAccountNumber = snapshot.child("bankAccountNumber").getValue(String::class.java) ?: "1204-5566778899"
                        val bankAccountTitle = snapshot.child("bankAccountTitle").getValue(String::class.java) ?: "Report Badshah Pvt Ltd"
                        val depositAmount = snapshot.child("depositAmount").getValue(Double::class.java) ?: 10000.0

                        _paymentSettings.value = PaymentSettings(
                            easypaisaNumber = easypaisaNumber,
                            easypaisaTitle = easypaisaTitle,
                            jazzcashNumber = jazzcashNumber,
                            jazzcashTitle = jazzcashTitle,
                            bankName = bankName,
                            bankAccountNumber = bankAccountNumber,
                            bankAccountTitle = bankAccountTitle,
                            depositAmount = depositAmount
                        )
                    } else {
                        updatePaymentSettings(PaymentSettings())
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updatePaymentSettings(settings: PaymentSettings) {
        viewModelScope.launch {
            try {
                getDatabase().getReference("payment_settings").setValue(settings)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun login(emailInput: String, passwordText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        _authError.value = null
        val email = if (emailInput.contains("@")) emailInput else "$emailInput@reportbadshah.com"
        try {
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, passwordText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Fetch user details from Firebase Realtime Database
                        val safeEmail = email.replace(".", "_")
                        getDatabase().getReference("users").child(safeEmail)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    viewModelScope.launch {
                                        try {
                                            if (snapshot.exists()) {
                                                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                                                val mobileNumber = snapshot.child("mobileNumber").getValue(String::class.java) ?: ""
                                                val passwordHash = snapshot.child("passwordHash").getValue(String::class.java) ?: passwordText
                                                val cnic = snapshot.child("cnic").getValue(String::class.java)
                                                val easyPaisaNumber = snapshot.child("easyPaisaNumber").getValue(String::class.java)
                                                val jazzCashNumber = snapshot.child("jazzCashNumber").getValue(String::class.java)
                                                val statusStr = snapshot.child("status").getValue(String::class.java) ?: "PENDING_APPROVAL"
                                                val status = try { UserStatus.valueOf(statusStr) } catch (e: Exception) { UserStatus.PENDING_APPROVAL }
                                                val roleStr = snapshot.child("role").getValue(String::class.java) ?: "USER"
                                                val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
                                                val isVip = snapshot.child("vip").getValue(Boolean::class.java) ?: false
                                                val vipExpiryDate = snapshot.child("vipExpiryDate").getValue(Long::class.java) ?: 0L
                                                val profileImage = snapshot.child("profileImage").getValue(String::class.java)
                                                val depositScreenshot = snapshot.child("depositScreenshot").getValue(String::class.java)
                                                val rejectionReason = snapshot.child("rejectionReason").getValue(String::class.java)

                                                val fetchedUser = User(
                                                    email = email,
                                                    name = name,
                                                    mobileNumber = mobileNumber,
                                                    passwordHash = passwordHash,
                                                    cnic = cnic,
                                                    easyPaisaNumber = easyPaisaNumber,
                                                    jazzCashNumber = jazzCashNumber,
                                                    status = status,
                                                    role = role,
                                                    isVip = isVip,
                                                    vipExpiryDate = vipExpiryDate,
                                                    profileImage = profileImage,
                                                    depositScreenshot = depositScreenshot,
                                                    rejectionReason = rejectionReason
                                                )

                                                if (fetchedUser.status == UserStatus.SUSPENDED) {
                                                    _isLoading.value = false
                                                    onError("Your account has been suspended by the administrator.")
                                                    auth.signOut()
                                                } else {
                                                    // Sync to local database
                                                    repository.insertUser(fetchedUser)
                                                    _currentUser.value = fetchedUser
                                                    startListeningToCurrentUser(email)
                                                    _isLoading.value = false
                                                    onSuccess()
                                                }
                                            } else {
                                                // Create user in firebase database if auth succeeded but db was missing
                                                val newUser = User(
                                                    email = email,
                                                    name = email.substringBefore("@"),
                                                    mobileNumber = if (emailInput.contains("@")) "" else emailInput,
                                                    passwordHash = passwordText,
                                                     cnic = null,
                                                     easyPaisaNumber = null,
                                                     jazzCashNumber = null,
                                                    status = UserStatus.APPROVED,
                                                    role = UserRole.USER
                                                )
                                                syncUserToFirebase(newUser)
                                                repository.insertUser(newUser)
                                                _currentUser.value = newUser
                                                startListeningToCurrentUser(email)
                                                _isLoading.value = false
                                                onSuccess()
                                            }
                                        } catch (e: Exception) {
                                            _isLoading.value = false
                                            onError("Failed to parse user profile: ${e.message}")
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    _isLoading.value = false
                                    onError("Failed to read user data: ${error.message}")
                                }
                            })
                    } else {
                        // Attempt fallback for prepopulated room database users
                        viewModelScope.launch {
                            try {
                                val localUser = repository.getUserByEmail(email)
                                if (localUser != null && localUser.passwordHash == passwordText) {
                                    // Prepopulated/seeded user that doesn't exist in Firebase Auth yet! Let's register them on the fly.
                                    auth.createUserWithEmailAndPassword(email, passwordText)
                                        .addOnCompleteListener { createRegTask ->
                                            if (createRegTask.isSuccessful) {
                                                syncUserToFirebase(localUser)
                                                _currentUser.value = localUser
                                                startListeningToCurrentUser(email)
                                                _isLoading.value = false
                                                onSuccess()
                                            } else {
                                                _isLoading.value = false
                                                onError(task.exception?.message ?: "Login failed.")
                                            }
                                        }
                                } else {
                                    _isLoading.value = false
                                    onError(task.exception?.message ?: "Login failed.")
                                }
                            } catch (e: Exception) {
                                _isLoading.value = false
                                onError(task.exception?.message ?: "Login failed.")
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            onError("Login failed: ${e.message}")
        }
    }

    fun register(
        name: String,
        mobile: String,
        email: String,
        passwordText: String,
        cnic: String?,
        easyPaisa: String?,
        jazzCash: String?,
        depositScreenshot: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.value = true
        _authError.value = null
        try {
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, passwordText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val newUser = User(
                            email = email,
                            name = name,
                            mobileNumber = mobile,
                            passwordHash = passwordText,
                            cnic = if (cnic.isNullOrBlank()) null else cnic,
                            easyPaisaNumber = if (easyPaisa.isNullOrBlank()) null else easyPaisa,
                            jazzCashNumber = if (jazzCash.isNullOrBlank()) null else jazzCash,
                            status = UserStatus.PENDING_APPROVAL,
                            role = UserRole.USER,
                            depositScreenshot = depositScreenshot
                        )
                        // Save user profile to Firebase Database
                        val safeEmail = email.replace(".", "_")
                        getDatabase().getReference("users").child(safeEmail).setValue(newUser)
                            .addOnCompleteListener { dbTask ->
                                viewModelScope.launch {
                                    try {
                                        repository.insertUser(newUser)
                                        _currentUser.value = newUser
                                        startListeningToCurrentUser(email)
                                        _isLoading.value = false
                                        onSuccess()
                                    } catch (e: Exception) {
                                        _isLoading.value = false
                                        onError("Saved to cloud but failed to cache locally: ${e.message}")
                                    }
                                }
                            }
                    } else {
                        _isLoading.value = false
                        onError(task.exception?.message ?: "Registration failed.")
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            onError("Registration failed: ${e.message}")
        }
    }

    fun resubmitApplication(txId: String, method: String, screenshotBase64: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = _currentUser.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val updated = user.copy(
                    status = UserStatus.PENDING_APPROVAL,
                    depositScreenshot = screenshotBase64,
                    easyPaisaNumber = if (method == "EasyPaisa") txId else user.easyPaisaNumber,
                    rejectionReason = null
                )
                repository.insertUser(updated)
                syncUserToFirebase(updated)
                _currentUser.value = updated
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Resubmission failed")
            }
        }
    }

    fun refreshUserSession() {
        val email = _currentUser.value?.email ?: return
        val safeEmail = email.replace(".", "_")
        getDatabase().getReference("users").child(safeEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModelScope.launch {
                        try {
                            if (snapshot.exists()) {
                                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                                val mobileNumber = snapshot.child("mobileNumber").getValue(String::class.java) ?: ""
                                val passwordHash = snapshot.child("passwordHash").getValue(String::class.java) ?: ""
                                val cnic = snapshot.child("cnic").getValue(String::class.java)
                                val easyPaisaNumber = snapshot.child("easyPaisaNumber").getValue(String::class.java)
                                val jazzCashNumber = snapshot.child("jazzCashNumber").getValue(String::class.java)
                                val statusStr = snapshot.child("status").getValue(String::class.java) ?: "PENDING_APPROVAL"
                                val status = try { UserStatus.valueOf(statusStr) } catch (e: Exception) { UserStatus.PENDING_APPROVAL }
                                val roleStr = snapshot.child("role").getValue(String::class.java) ?: "USER"
                                val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
                                val isVip = snapshot.child("vip").getValue(Boolean::class.java) ?: false
                                val vipExpiryDate = snapshot.child("vipExpiryDate").getValue(Long::class.java) ?: 0L
                                val profileImage = snapshot.child("profileImage").getValue(String::class.java)

                                val freshUser = User(
                                    email = email,
                                    name = name,
                                    mobileNumber = mobileNumber,
                                    passwordHash = passwordHash,
                                    cnic = cnic,
                                    easyPaisaNumber = easyPaisaNumber,
                                    jazzCashNumber = jazzCashNumber,
                                    status = status,
                                    role = role,
                                    isVip = isVip,
                                    vipExpiryDate = vipExpiryDate,
                                    profileImage = profileImage
                                )
                                repository.insertUser(freshUser)
                                _currentUser.value = freshUser
                            } else {
                                val freshUser = repository.getUserByEmail(email)
                                if (freshUser != null) {
                                    _currentUser.value = freshUser
                                }
                            }
                        } catch (e: Exception) {
                            val freshUser = repository.getUserByEmail(email)
                            if (freshUser != null) {
                                _currentUser.value = freshUser
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    viewModelScope.launch {
                        val freshUser = repository.getUserByEmail(email)
                        if (freshUser != null) {
                            _currentUser.value = freshUser
                        }
                    }
                }
            })
    }

    fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {}
        _currentUser.value = null
        _authError.value = null
    }

    fun changePassword(oldPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = _currentUser.value ?: return
        if (user.passwordHash != oldPass) {
            onError("Incorrect old password.")
            return
        }
        _isLoading.value = true
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            firebaseUser.updatePassword(newPass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            val updated = user.copy(passwordHash = newPass)
                            repository.updateUser(updated)
                            syncUserToFirebase(updated)
                            _currentUser.value = updated
                            _isLoading.value = false
                            onSuccess()
                        }
                    } else {
                        _isLoading.value = false
                        onError(task.exception?.message ?: "Failed to update password in Firebase Auth.")
                    }
                }
        } else {
            viewModelScope.launch {
                val updated = user.copy(passwordHash = newPass)
                repository.updateUser(updated)
                _currentUser.value = updated
                _isLoading.value = false
                onSuccess()
            }
        }
    }

    fun requestDeleteAccount(onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // Send support ticket requesting deletion
            repository.insertSupportMessage(
                SupportMessage(
                    userId = user.email,
                    userName = user.name,
                    message = "ACCOUNT DELETION REQUEST: Please delete my account and associated data."
                )
            )
            onSuccess()
        }
    }

    // --- CLIENT TX ACTIONS ---

    fun joinMatch(
        match: Match,
        txId: String,
        paymentMethod: String,
        screenshotBase64: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (txId.isBlank()) {
            onError("Please enter a valid Transaction ID.")
            return
        }

        viewModelScope.launch {
            try {
                // Check if already joined
                val entries = repository.getEntriesForMatch(match.id)
                if (entries.any { it.userId == user.email }) {
                    onError("You have already submitted a proposal to unlock this report.")
                    return@launch
                }

                val payId = System.currentTimeMillis() + (0..100).random()
                // Create a payment proof entry
                val payment = PaymentProof(
                    id = payId,
                    userId = user.email,
                    amount = match.entryFee,
                    paymentMethod = paymentMethod,
                    transactionId = txId,
                    screenshotUri = screenshotBase64,
                    status = PaymentStatus.PENDING,
                    type = PaymentType.MATCH_ENTRY,
                    referenceId = match.id.toString(),
                    timestamp = payId
                )
                repository.insertPayment(payment)
                syncPaymentToFirebase(payment)

                // Create match entry (remains pending until payment approved)
                val entryId = System.currentTimeMillis() + (100..200).random()
                val entry = MatchEntry(
                    id = entryId,
                    userId = user.email,
                    matchId = match.id,
                    predictedOutcome = "REPORT_UNLOCK",
                    transactionId = txId,
                    status = EntryStatus.PENDING,
                    winLossStatus = WinLossStatus.PENDING,
                    timestamp = entryId
                )
                repository.insertEntry(entry)

                // Update remaining slots
                val updatedMatch = match.copy(remainingSlots = if (match.remainingSlots > 0) match.remainingSlots - 1 else 0)
                repository.updateMatch(updatedMatch)
                syncMatchToFirebase(updatedMatch)

                // Create user alert
                val notifId = System.currentTimeMillis() + (200..300).random()
                repository.insertNotification(
                    AppNotification(
                        id = notifId,
                        userId = user.email,
                        title = "Unlock Request Submitted",
                        message = "Your unlock proposal for '${match.tournamentName}' has been submitted! Waiting for admin approval of Tx ID: $txId."
                    )
                )

                onSuccess()
            } catch (e: Exception) {
                onError("Failed to submit proposal: ${e.message}")
            }
        }
    }

    fun submitDirectPayment(
        amount: Double,
        method: String,
        txId: String,
        screenshotUri: String?,
        type: PaymentType,
        refId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (txId.isBlank()) {
            onError("Transaction ID cannot be empty.")
            return
        }
        viewModelScope.launch {
            try {
                val payId = System.currentTimeMillis() + (0..100).random()
                val payment = PaymentProof(
                    id = payId,
                    userId = user.email,
                    amount = amount,
                    paymentMethod = method,
                    transactionId = txId,
                    screenshotUri = screenshotUri ?: "direct_payment",
                    status = PaymentStatus.PENDING,
                    type = type,
                    referenceId = refId,
                    timestamp = payId
                )
                repository.insertPayment(payment)
                syncPaymentToFirebase(payment)

                // Send Alert
                val notifId = System.currentTimeMillis() + (200..300).random()
                repository.insertNotification(
                    AppNotification(
                        id = notifId,
                        userId = user.email,
                        title = "Payment Submitted",
                        message = "Your proof for $method payment of PKR $amount is received. Ref: $refId. Verification takes up to 2 hours."
                    )
                )

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Submission failed")
            }
        }
    }

    fun submitSupportMessage(text: String, onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val msgId = System.currentTimeMillis() + (0..100).random()
            val msg = SupportMessage(
                id = msgId,
                userId = user.email,
                userName = user.name,
                message = text,
                reply = null,
                timestamp = msgId
            )
            repository.insertSupportMessage(msg)
            try {
                getDatabase().getReference("support_messages")
                    .child(msgId.toString())
                    .setValue(msg)
            } catch (e: Exception) {}
            onSuccess()
        }
    }

    // --- ADMIN PANEL ACTIONS ---

    fun approveUser(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email) ?: return@launch
            val updated = user.copy(status = UserStatus.APPROVED)
            repository.updateUser(updated)
            syncUserToFirebase(updated)

            // Notify user
            repository.insertNotification(
                AppNotification(
                    userId = email,
                    title = "Account Approved! 🎉",
                    message = "Congratulations! Your account registration has been verified and approved by the system administrators. You can now access all match predictions!"
                )
            )
        }
    }

    fun rejectUser(email: String, reason: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email) ?: return@launch
            val updated = user.copy(status = UserStatus.REJECTED, rejectionReason = reason)
            repository.updateUser(updated)
            syncUserToFirebase(updated)

            // Notify user
            val notifId = System.currentTimeMillis() + (200..300).random()
            repository.insertNotification(
                AppNotification(
                    id = notifId,
                    userId = email,
                    title = "Account Registration Status ❌",
                    message = "Your application was declined. Reason: $reason"
                )
            )
        }
    }

    fun suspendUser(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email) ?: return@launch
            val updated = user.copy(status = UserStatus.SUSPENDED)
            repository.updateUser(updated)
            syncUserToFirebase(updated)
        }
    }

    fun approvePayment(payment: PaymentProof) {
        viewModelScope.launch {
            // Update payment record to approved
            val updatedPayment = payment.copy(status = PaymentStatus.APPROVED)
            repository.updatePayment(updatedPayment)

            // Action based on payment type
            if (payment.type == PaymentType.MATCH_ENTRY) {
                // Approve the match entry associated with this transactionId
                val matchId = payment.referenceId.toLongOrNull() ?: 0L
                val allEntriesList = repository.getEntriesForMatch(matchId)
                val matchingEntry = allEntriesList.find { it.userId == payment.userId && it.transactionId == payment.transactionId }
                if (matchingEntry != null) {
                    repository.updateEntry(matchingEntry.copy(status = EntryStatus.APPROVED))
                }

                // Notify User
                repository.insertNotification(
                    AppNotification(
                        userId = payment.userId,
                        title = "Payment Approved ✅",
                        message = "Your payment for entry ID #${payment.id} has been verified! Your match prediction is now active."
                    )
                )
            } else if (payment.type == PaymentType.VIP_SUBSCRIBE) {
                // Grant VIP status to user
                val user = repository.getUserByEmail(payment.userId)
                if (user != null) {
                    val durationMonths = when (payment.referenceId.lowercase()) {
                        "yearly" -> 12
                        "quarterly" -> 3
                        else -> 1
                    }
                    val expiry = System.currentTimeMillis() + (durationMonths * 30L * 24 * 60 * 60 * 1000)
                    val updatedUser = user.copy(isVip = true, vipExpiryDate = expiry)
                    repository.updateUser(updatedUser)
                    syncUserToFirebase(updatedUser)
                }

                // Notify User
                repository.insertNotification(
                    AppNotification(
                        userId = payment.userId,
                        title = "VIP Membership Activated! 💎",
                        message = "Welcome to VIP Elite Club! Your ${payment.referenceId} subscription has been approved. Enjoy premium tips and priorities."
                    )
                )
            }
        }
    }

    fun rejectPayment(payment: PaymentProof) {
        viewModelScope.launch {
            val updatedPayment = payment.copy(status = PaymentStatus.REJECTED)
            repository.updatePayment(updatedPayment)

            if (payment.type == PaymentType.MATCH_ENTRY) {
                val matchId = payment.referenceId.toLongOrNull() ?: 0L
                val allEntriesList = repository.getEntriesForMatch(matchId)
                val matchingEntry = allEntriesList.find { it.userId == payment.userId && it.transactionId == payment.transactionId }
                if (matchingEntry != null) {
                    repository.updateEntry(matchingEntry.copy(status = EntryStatus.REJECTED))
                }
            }

            // Notify User
            repository.insertNotification(
                AppNotification(
                    userId = payment.userId,
                    title = "Payment Proof Declined ❌",
                    message = "Your payment proof of PKR ${payment.amount} has been declined. Please submit a valid transaction ID or contact support."
                )
            )
        }
    }

    fun createMatch(
        tournamentName: String,
        teamA: String,
        teamB: String,
        date: String,
        time: String,
        entryFee: Double,
        prizePool: Double,
        totalSlots: Int,
        matchImage: String,
        category: MatchCategory,
        leagueName: String = "League Match",
        reportImage: String? = null
    ) {
        viewModelScope.launch {
            val newMatch = Match(
                tournamentName = tournamentName,
                teamA = teamA,
                teamB = teamB,
                date = date,
                time = time,
                entryFee = entryFee,
                prizePool = prizePool,
                totalSlots = totalSlots,
                remainingSlots = totalSlots,
                matchImage = if (matchImage.isBlank()) "https://images.unsplash.com/photo-1540747737956-37872404478a?w=400" else matchImage,
                status = MatchStatus.OPEN,
                category = category,
                leagueName = leagueName,
                reportImage = reportImage
            )
            val generatedId = repository.insertMatch(newMatch)
            val matchWithId = newMatch.copy(id = generatedId)
            syncMatchToFirebase(matchWithId)

            // Alert Everyone
            val notifId = System.currentTimeMillis() + (200..300).random()
            repository.insertNotification(
                AppNotification(
                    id = notifId,
                    userId = "ALL",
                    title = "New Match Posted! 🚀",
                    message = "Tourney: $tournamentName. $teamA vs $teamB is now OPEN for report unlocks!"
                )
            )
        }
    }

    fun updateMatchStatus(matchId: Long, newStatus: MatchStatus, winningOutcome: String) {
        viewModelScope.launch {
            val match = repository.getMatchById(matchId) ?: return@launch
            val updatedMatch = match.copy(status = newStatus, predictedResult = winningOutcome)
            repository.updateMatch(updatedMatch)
            syncMatchToFirebase(updatedMatch)

            // If match is COMPLETED, we process predictions to resolve wins/losses!
            if (newStatus == MatchStatus.COMPLETED && winningOutcome != "PENDING") {
                val matchEntries = repository.getEntriesForMatch(matchId)
                for (entry in matchEntries) {
                    if (entry.status == EntryStatus.APPROVED) {
                        val isWin = entry.predictedOutcome == winningOutcome
                        val outcomeStatus = if (isWin) WinLossStatus.WON else WinLossStatus.LOST
                        repository.updateEntry(entry.copy(winLossStatus = outcomeStatus))

                        // Send user alerts
                        val title = if (isWin) "You Won! 🏆" else "Match Result Settled"
                        val message = if (isWin) {
                            "Spot on! Your prediction for ${match.teamA} vs ${match.teamB} won you a portion of the PKR ${match.prizePool} pool!"
                        } else {
                            "Nice try! Your prediction for ${match.teamA} vs ${match.teamB} was incorrect. Better luck in the next tournament!"
                        }
                        repository.insertNotification(
                            AppNotification(
                                userId = entry.userId,
                                title = title,
                                message = message
                            )
                        )
                    }
                }
            }
        }
    }

    fun deleteMatch(matchId: Long) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
            try {
                getDatabase().getReference("matches")
                    .child(matchId.toString())
                    .removeValue()
            } catch (e: Exception) {
                // Ignore safely
            }
        }
    }

    fun replyToSupport(supportId: Long, replyText: String) {
        viewModelScope.launch {
            val allMessages = repository.allSupportMessages.firstOrNull() ?: return@launch
            val msg = allMessages.find { it.id == supportId } ?: return@launch
            val updated = msg.copy(reply = replyText)
            repository.updateSupportMessage(updated)

            try {
                getDatabase().getReference("support_messages")
                    .child(supportId.toString())
                    .setValue(updated)
            } catch (e: Exception) {}

            // Notify user
            val notifId = System.currentTimeMillis() + (200..300).random()
            repository.insertNotification(
                AppNotification(
                    id = notifId,
                    userId = msg.userId,
                    title = "Support Ticket Replied",
                    message = "An Administrator has replied to your query: \"$replyText\""
                )
            )
        }
    }

    fun sendGlobalNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.insertNotification(
                AppNotification(
                    userId = "ALL",
                    title = title,
                    message = message
                )
            )
        }
    }
}

// --- FACTORY ---

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
