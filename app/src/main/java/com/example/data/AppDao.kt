package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USERS ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    // --- MATCHES ---
    @Query("SELECT * FROM matches ORDER BY id DESC")
    fun getAllMatchesFlow(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: Long): Match?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match): Long

    @Update
    suspend fun updateMatch(match: Match)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: Long)

    // --- ENTRIES ---
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getAllEntriesFlow(): Flow<List<MatchEntry>>

    @Query("SELECT * FROM entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getEntriesForUserFlow(userId: String): Flow<List<MatchEntry>>

    @Query("SELECT * FROM entries WHERE matchId = :matchId")
    suspend fun getEntriesForMatch(matchId: Long): List<MatchEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MatchEntry)

    @Update
    suspend fun updateEntry(entry: MatchEntry)

    // --- PAYMENTS ---
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentProof>>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPaymentsForUserFlow(userId: String): Flow<List<PaymentProof>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentProof)

    @Update
    suspend fun updatePayment(payment: PaymentProof)

    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: String): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    // --- SUPPORT MESSAGES ---
    @Query("SELECT * FROM support_messages ORDER BY timestamp DESC")
    fun getAllSupportMessagesFlow(): Flow<List<SupportMessage>>

    @Query("SELECT * FROM support_messages WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSupportMessagesForUserFlow(userId: String): Flow<List<SupportMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportMessage(msg: SupportMessage)

    @Update
    suspend fun updateSupportMessage(msg: SupportMessage)
}
