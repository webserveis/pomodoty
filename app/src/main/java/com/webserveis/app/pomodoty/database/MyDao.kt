package com.webserveis.app.pomodoty.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg entry: ItemDB)

    @Update
    suspend fun update(vararg entry: ItemDB)

    @Delete
    suspend fun delete(vararg entry: ItemDB)

    @Query("DELETE FROM items_table")
    fun nukeTable()

    @Query("SELECT * FROM items_table")
    fun getAllItemList(): Flow<List<ItemDB>>

    @Query("SELECT * FROM items_table WHERE uid=:uid")
    fun getItemByUID(uid: String): Flow<ItemDB>

}