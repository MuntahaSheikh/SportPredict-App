package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- ENUMS ---

enum class UserStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED
}

enum class UserRole {
    USER,
    ADMIN
}

enum class MatchStatus {
    UPCOMING,
    OPEN,
    CLOSED,
    COMPLETED
}

enum class MatchCategory {
    CRICKET,
    FOOTBALL,
    TENNIS,
    RACING,
    CARD_GAMES
}

enum class EntryStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class WinLossStatus {
    PENDING,
    WON,
    LOST
}

enum class PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class PaymentType {
    MATCH_ENTRY,
    VIP_SUBSCRIBE
}

// --- ENTITIES ---

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String, // Unique identifier (Email)
    val name: String,
    val mobileNumber: String,
    val passwordHash: String,
    val cnic: String?,
    val easyPaisaNumber: String?,
    val jazzCashNumber: String?,
    val status: UserStatus = UserStatus.PENDING_APPROVAL,
    val role: UserRole = UserRole.USER,
    val isVip: Boolean = false,
    val vipExpiryDate: Long = 0L,
    val profileImage: String? = null,
    val depositScreenshot: String? = null,
    val rejectionReason: String? = null
)

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tournamentName: String,
    val teamA: String,
    val teamB: String,
    val date: String,
    val time: String,
    val entryFee: Double,
    val prizePool: Double,
    val totalSlots: Int,
    val remainingSlots: Int,
    val matchImage: String,
    val status: MatchStatus = MatchStatus.OPEN,
    val category: MatchCategory = MatchCategory.CRICKET,
    val predictedResult: String = "PENDING", // "TEAM_A", "TEAM_B", "DRAW", "PENDING"
    val leagueName: String = "League Match",
    val reportImage: String? = null
)

data class PaymentSettings(
    val easypaisaNumber: String = "0311-2233445",
    val easypaisaTitle: String = "Report Badshah Official",
    val jazzcashNumber: String = "0322-5566778",
    val jazzcashTitle: String = "Report Badshah Official",
    val bankName: String = "Meezan Bank Ltd",
    val bankAccountNumber: String = "1204-5566778899",
    val bankAccountTitle: String = "Report Badshah Pvt Ltd",
    val depositAmount: Double = 10000.0
)

@Entity(tableName = "entries")
data class MatchEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String, // Email of joining user
    val matchId: Long,
    val predictedOutcome: String, // e.g. "TEAM_A", "TEAM_B", "DRAW"
    val transactionId: String,
    val status: EntryStatus = EntryStatus.PENDING,
    val winLossStatus: WinLossStatus = WinLossStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentProof(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String, // Email of user
    val amount: Double,
    val paymentMethod: String, // "EasyPaisa", "JazzCash", "Bank Account"
    val transactionId: String,
    val screenshotUri: String?, // Mocked uri string or dummy text
    val status: PaymentStatus = PaymentStatus.PENDING,
    val type: PaymentType, // MATCH_ENTRY or VIP_SUBSCRIBE
    val referenceId: String, // matchId (as string) or vipPlanTitle
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String, // Email of receiver, or "ALL"
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "support_messages")
data class SupportMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String, // Email of user
    val userName: String,
    val message: String,
    val reply: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
