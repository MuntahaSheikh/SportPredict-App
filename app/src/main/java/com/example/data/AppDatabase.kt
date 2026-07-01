package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Match::class,
        MatchEntry::class,
        PaymentProof::class,
        AppNotification::class,
        SupportMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apex_predictions_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.appDao()

                    // 1. Prepopulate Admin and default approved User
                    dao.insertUser(
                        User(
                            email = "admin@apex.com",
                            name = "Apex Administrator",
                            mobileNumber = "+923001234567",
                            passwordHash = "admin123", // Simple check in ViewModel
                            cnic = "37405-1234567-1",
                            easyPaisaNumber = "03001234567",
                            jazzCashNumber = "03121234567",
                            status = UserStatus.APPROVED,
                            role = UserRole.ADMIN
                        )
                    )

                    dao.insertUser(
                        User(
                            email = "user@apex.com",
                            name = "Muntaha Sheikh",
                            mobileNumber = "+923129876543",
                            passwordHash = "user123",
                            cnic = "37405-9876543-2",
                            easyPaisaNumber = "03129876543",
                            jazzCashNumber = "03339876543",
                            status = UserStatus.APPROVED,
                            role = UserRole.USER,
                            isVip = false
                        )
                    )

                    dao.insertUser(
                        User(
                            email = "pending@apex.com",
                            name = "Zahir Khan",
                            mobileNumber = "+923215554433",
                            passwordHash = "pending123",
                            cnic = "37405-1112223-3",
                            easyPaisaNumber = "03215554433",
                            jazzCashNumber = "03455554433",
                            status = UserStatus.PENDING_APPROVAL,
                            role = UserRole.USER
                        )
                    )

                    // 2. Prepopulate matches
                    dao.insertMatch(
                        Match(
                            id = 1L,
                            tournamentName = "ICC T20 World Cup Grand Finale",
                            teamA = "India",
                            teamB = "Pakistan",
                            date = "2026-07-15",
                            time = "19:00",
                            entryFee = 150.0,
                            prizePool = 10000.0,
                            totalSlots = 100,
                            remainingSlots = 64,
                            matchImage = "https://images.unsplash.com/photo-1531415080290-bc98545ab3ef?w=400", // Cricket ground
                            status = MatchStatus.OPEN,
                            category = MatchCategory.CRICKET
                        )
                    )

                    dao.insertMatch(
                        Match(
                            id = 2L,
                            tournamentName = "UEFA Champions League Semis",
                            teamA = "Real Madrid",
                            teamB = "Manchester City",
                            date = "2026-07-20",
                            time = "21:45",
                            entryFee = 200.0,
                            prizePool = 15000.0,
                            totalSlots = 80,
                            remainingSlots = 12,
                            matchImage = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400", // Soccer Stadium
                            status = MatchStatus.OPEN,
                            category = MatchCategory.FOOTBALL
                        )
                    )

                    dao.insertMatch(
                        Match(
                            id = 3L,
                            tournamentName = "Wimbledon Men's Championship",
                            teamA = "Carlos Alcaraz",
                            teamB = "Novak Djokovic",
                            date = "2026-07-12",
                            time = "14:00",
                            entryFee = 100.0,
                            prizePool = 5000.0,
                            totalSlots = 50,
                            remainingSlots = 50,
                            matchImage = "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=400", // Tennis
                            status = MatchStatus.UPCOMING,
                            category = MatchCategory.TENNIS
                        )
                    )

                    dao.insertMatch(
                        Match(
                            id = 4L,
                            tournamentName = "Formula 1 Monaco Grand Prix",
                            teamA = "Red Bull Racing",
                            teamB = "Ferrari",
                            date = "2026-06-25",
                            time = "16:00",
                            entryFee = 50.0,
                            prizePool = 8000.0,
                            totalSlots = 150,
                            remainingSlots = 0,
                            matchImage = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?w=400", // Racing Car
                            status = MatchStatus.COMPLETED,
                            category = MatchCategory.RACING,
                            predictedResult = "TEAM_A",
                            leagueName = "Monaco GP"
                        )
                    )

                    // 3. Prepopulate default app notifications
                    dao.insertNotification(
                        AppNotification(
                            userId = "ALL",
                            title = "Welcome to Report Badshah!",
                            message = "Start unlocking premium analyst report JPEGs on your favorite live sports matches! Join the Support Desk anytime for live inquiries."
                        )
                    )
                }
            }
        }
    }
}
