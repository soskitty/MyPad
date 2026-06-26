package com.example.mypad

import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY orderIndex ASC")
    fun getAll(): List<Note>

    @Insert
    fun insert(note: Note): Long

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    fun deleteByIds(ids: List<Long>)

    @Query("UPDATE notes SET orderIndex = :orderIndex WHERE id = :id")
    fun updateOrder(id: Long, orderIndex: Int)

    @Query("UPDATE notes SET content = :content, updatedAt = :updatedAt WHERE id = :id")
    fun updateContent(id: Long, content: String, updatedAt: Long = System.currentTimeMillis())
}
