package com.webserveis.app.pomodoty.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_table")
data class ItemDB(
    @PrimaryKey
    val uid: String,
    val displayName: String
) {
}