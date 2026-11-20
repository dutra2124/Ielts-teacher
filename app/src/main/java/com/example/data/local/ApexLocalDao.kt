package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApexLocalDao {

    // ---- Modules ----
    @Query("SELECT * FROM modules ORDER BY id ASC")
    fun observeModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules ORDER BY id ASC")
    suspend fun getAllModules(): List<ModuleEntity>

    @Query("SELECT COUNT(*) FROM modules")
    suspend fun countModules(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModules(items: List<ModuleEntity>)

    // ---- Vocabulary ----
    @Query("SELECT * FROM vocabulary ORDER BY moduleId ASC, word ASC")
    fun observeVocabulary(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary ORDER BY moduleId ASC, word ASC")
    suspend fun getAllVocabulary(): List<VocabularyEntity>

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun countVocabulary(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVocabulary(items: List<VocabularyEntity>)

    // ---- Sentence Drills ----
    @Query("SELECT * FROM sentence_drills ORDER BY id ASC")
    fun observeSentenceDrills(): Flow<List<SentenceDrillEntity>>

    @Query("SELECT * FROM sentence_drills ORDER BY id ASC")
    suspend fun getAllSentenceDrills(): List<SentenceDrillEntity>

    @Query("SELECT COUNT(*) FROM sentence_drills")
    suspend fun countSentenceDrills(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSentenceDrills(items: List<SentenceDrillEntity>)

    // ---- Exams ----
    @Query("SELECT * FROM exams ORDER BY year DESC, id ASC")
    fun observeExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY year DESC, id ASC")
    suspend fun getAllExams(): List<ExamEntity>

    @Query("SELECT COUNT(*) FROM exams")
    suspend fun countExams(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExams(items: List<ExamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExam(item: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :examId")
    suspend fun deleteExamById(examId: String)

    // ---- Personal Materials ----
    @Query("SELECT * FROM personal_materials ORDER BY updatedAt DESC, id DESC")
    fun observePersonalMaterials(): Flow<List<PersonalMaterialEntity>>

    @Query("SELECT * FROM personal_materials ORDER BY updatedAt DESC, id DESC")
    suspend fun getAllPersonalMaterials(): List<PersonalMaterialEntity>

    @Query("SELECT COUNT(*) FROM personal_materials")
    suspend fun countPersonalMaterials(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPersonalMaterials(items: List<PersonalMaterialEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPersonalMaterial(item: PersonalMaterialEntity)

    @Query("DELETE FROM personal_materials WHERE id = :materialId")
    suspend fun deletePersonalMaterialById(materialId: String)

    // ---- Audio Blobs ----
    @Query("SELECT * FROM audio_blobs ORDER BY createdAtMillis DESC")
    fun observeAudioBlobs(): Flow<List<AudioBlobEntity>>

    @Query("SELECT * FROM audio_blobs ORDER BY createdAtMillis DESC")
    suspend fun getAllAudioBlobs(): List<AudioBlobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAudioBlob(item: AudioBlobEntity)

    @Query("DELETE FROM audio_blobs WHERE id = :blobId")
    suspend fun deleteAudioBlobById(blobId: String)

    // ---- Study Logs ----
    @Query("SELECT * FROM study_logs ORDER BY createdAtMillis DESC LIMIT 60")
    fun observeStudyLogs(): Flow<List<StudyLogEntity>>

    @Query("SELECT * FROM study_logs ORDER BY createdAtMillis DESC LIMIT 200")
    suspend fun getAllStudyLogs(): List<StudyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyLog(item: StudyLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyLogs(items: List<StudyLogEntity>)
}
