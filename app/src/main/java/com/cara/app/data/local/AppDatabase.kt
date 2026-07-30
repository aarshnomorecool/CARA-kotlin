package com.cara.app.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// Caches the last successful /recommendations response per user, as the raw
// JSON body, so Home can fall back to "showing saved results — offline" when
// a live fetch fails. See CLAUDE_android.md "Local cache" — this is a plain
// cache, not a source of truth; the backend is.
@Entity(tableName = "cached_recommendations")
data class CachedRecommendationEntity(
    @PrimaryKey val userId: Int,
    val responseJson: String,
    val cachedAt: Long,
)

@Dao
interface CachedRecommendationDao {
    @Query("SELECT * FROM cached_recommendations WHERE userId = :userId")
    suspend fun get(userId: Int): CachedRecommendationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedRecommendationEntity)
}

@Database(entities = [CachedRecommendationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedRecommendationDao(): CachedRecommendationDao
}

object DatabaseProvider {
    @Volatile private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "cara.db")
                .build()
                .also { instance = it }
        }
}
