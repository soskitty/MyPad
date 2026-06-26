package com.example.mypad

data class Note(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val orderIndex: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val title: String get() = content.trim().lines().firstOrNull { it.isNotBlank() } ?: "无标题"
}
