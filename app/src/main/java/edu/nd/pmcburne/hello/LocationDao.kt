package edu.nd.pmcburne.hello

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ")
    fun getLocations(): Flow<List<Location>>

    @Query("SELECT * FROM locations")
    suspend fun getLocationsOnce(): List<Location>

    @Upsert
    suspend fun upsertLocations(games: List<Location>)
}