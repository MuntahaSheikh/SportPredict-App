package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {

    // --- USERS ---
    suspend fun getUserByEmail(email: String): User? {
        return dao.getUserByEmail(email)
    }

    val allUsers: Flow<List<User>> = dao.getAllUsersFlow()

    suspend fun insertUser(user: User) {
        dao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        dao.updateUser(user)
    }

    // --- MATCHES ---
    val allMatches: Flow<List<Match>> = dao.getAllMatchesFlow()

    suspend fun getMatchById(matchId: Long): Match? {
        return dao.getMatchById(matchId)
    }

    suspend fun insertMatch(match: Match): Long {
        return dao.insertMatch(match)
    }

    suspend fun updateMatch(match: Match) {
        dao.updateMatch(match)
    }

    suspend fun deleteMatch(matchId: Long) {
        dao.deleteMatch(matchId)
    }

    // --- ENTRIES ---
    val allEntries: Flow<List<MatchEntry>> = dao.getAllEntriesFlow()

    fun getEntriesForUser(userId: String): Flow<List<MatchEntry>> {
        return dao.getEntriesForUserFlow(userId)
    }

    suspend fun getEntriesForMatch(matchId: Long): List<MatchEntry> {
        return dao.getEntriesForMatch(matchId)
    }

    suspend fun insertEntry(entry: MatchEntry) {
        dao.insertEntry(entry)
    }

    suspend fun updateEntry(entry: MatchEntry) {
        dao.updateEntry(entry)
    }

    // --- PAYMENTS ---
    val allPayments: Flow<List<PaymentProof>> = dao.getAllPaymentsFlow()

    fun getPaymentsForUser(userId: String): Flow<List<PaymentProof>> {
        return dao.getPaymentsForUserFlow(userId)
    }

    suspend fun insertPayment(payment: PaymentProof) {
        dao.insertPayment(payment)
    }

    suspend fun updatePayment(payment: PaymentProof) {
        dao.updatePayment(payment)
    }

    // --- NOTIFICATIONS ---
    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>> {
        return dao.getNotificationsForUserFlow(userId)
    }

    suspend fun insertNotification(notification: AppNotification) {
        dao.insertNotification(notification)
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        dao.markAllAsRead(userId)
    }

    // --- SUPPORT MESSAGES ---
    val allSupportMessages: Flow<List<SupportMessage>> = dao.getAllSupportMessagesFlow()

    fun getSupportMessagesForUser(userId: String): Flow<List<SupportMessage>> {
        return dao.getSupportMessagesForUserFlow(userId)
    }

    suspend fun insertSupportMessage(msg: SupportMessage) {
        dao.insertSupportMessage(msg)
    }

    suspend fun updateSupportMessage(msg: SupportMessage) {
        dao.updateSupportMessage(msg)
    }
}
