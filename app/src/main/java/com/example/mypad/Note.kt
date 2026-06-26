package com.example.mypad

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val orderIndex: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @Ignore
    val title: String get() = content.trim().lines().firstOrNull { it.isNotBlank() } ?: "无标题"
}
