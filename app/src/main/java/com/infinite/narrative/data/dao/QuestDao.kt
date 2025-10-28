package com.infinite.narrative.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.infinite.narrative.data.model.CausalCollection
import com.infinite.narrative.data.model.CollectionType
import com.infinite.narrative.data.model.ContextType
import com.infinite.narrative.data.model.NarrativeContext
import com.infinite.narrative.data.model.PlayerAttributes
import com.infinite.narrative.data.model.Quest
import com.infinite.narrative.data.model.QuestProgress
import com.infinite.narrative.data.model.QuestStage
import com.infinite.narrative.data.model.QuestStatus
import com.infinite.narrative.data.model.QuestType
import com.infinite.narrative.data.model.WorldStateSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {

    // Quest CRUD操作
    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuestById(id: String): Quest?

    @Query("SELECT * FROM quests WHERE questType = :type")
    fun getQuestsByType(type: QuestType): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE status = :status")
    fun getQuestsByStatus(status: QuestStatus): List<Quest>

    @Query("SELECT * FROM quests WHERE id IN (:ids)")
    suspend fun getQuestsByIds(ids: List<String>): List<Quest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<Quest>)

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    // QuestProgress操作
    @Query("SELECT * FROM quest_progresses WHERE questId = :questId AND playerId = :playerId")
    suspend fun getProgress(questId: String, playerId: String): QuestProgress?

    @Query("SELECT * FROM quest_progresses WHERE playerId = :playerId")
    fun getPlayerProgresses(playerId: String): List<QuestProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: QuestProgress)

    @Update
    suspend fun updateProgress(progress: QuestProgress)

    // QuestStage操作
    @Query("SELECT * FROM quest_stages WHERE questId = :questId ORDER BY stageNumber")
    suspend fun getStages(questId: String): List<QuestStage>

    @Query("SELECT * FROM quest_stages WHERE questId = :questId AND stageNumber = :stageNumber")
    suspend fun getStage(questId: String, stageNumber: Int): QuestStage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stage: QuestStage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<QuestStage>)

    // WorldStateSnapshot操作
    @Query("SELECT * FROM world_state_snapshots WHERE id = :id")
    suspend fun getSnapshot(id: String): WorldStateSnapshot?

    @Query("SELECT * FROM world_state_snapshots WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getRecentSnapshots(since: Long): Flow<List<WorldStateSnapshot>>

    @Query("SELECT * FROM world_state_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(): WorldStateSnapshot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: WorldStateSnapshot)

    @Delete
    suspend fun deleteSnapshot(snapshot: WorldStateSnapshot)

    // CausalCollection操作
    @Query("SELECT * FROM causal_collection WHERE id = :id")
    suspend fun getCollectionItem(id: String): CausalCollection?

    @Query("SELECT * FROM causal_collection WHERE playerId = :playerId")
    fun getPlayerCollection(playerId: String): Flow<List<CausalCollection>>

    @Query("SELECT * FROM causal_collection WHERE collectionType = :type")
    fun getCollectionByType(type: CollectionType): Flow<List<CausalCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CausalCollection)

    @Update
    suspend fun updateCollection(collection: CausalCollection)

    // NarrativeContext操作
    @Query("SELECT * FROM narrative_contexts WHERE id = :id")
    suspend fun getContext(id: String): NarrativeContext?

    @Query("SELECT * FROM narrative_contexts WHERE playerId = :playerId AND worldId = :worldId AND contextType = :type ORDER BY relevanceScore DESC, createdAt DESC")
    fun getContexts(
        playerId: String,
        worldId: String,
        type: ContextType
    ): Flow<List<NarrativeContext>>

    @Query("SELECT * FROM narrative_contexts WHERE relevanceScore > :minScore AND (expiresAt IS NULL OR expiresAt > :currentTime) ORDER BY relevanceScore DESC")
    fun getHighRelevanceContexts(minScore: Float, currentTime: Long): Flow<List<NarrativeContext>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: NarrativeContext)

    @Update
    suspend fun updateContext(context: NarrativeContext)

    @Delete
    suspend fun deleteContext(context: NarrativeContext)

    // 批量操作和事务
    @Transaction
    suspend fun startQuest(quest: Quest, playerId: String) {
        updateQuest(quest.copy(status = QuestStatus.IN_PROGRESS))
        insertProgress(
            QuestProgress(
                questId = quest.id,
                playerId = playerId,
                currentStage = 0,
                totalStages = getStages(quest.id).size
            )
        )
    }

    @Transaction
    suspend fun completeQuest(questId: String, playerId: String) {
        val quest = getQuestById(questId) ?: return
        updateQuest(
            quest.copy(
                status = QuestStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            )
        )
        updateProgress(
            QuestProgress(
                questId = questId,
                playerId = playerId,
                currentStage = getStages(questId).size,
                totalStages = getStages(questId).size,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    // 获取可接取的任务（检查前置条件）
    @Transaction
    suspend fun getAvailableQuests(playerId: String): List<Quest> {
        val completedQuests = getPlayerProgresses(playerId)
            .filter { it.currentStage == QuestStatus.COMPLETED.ordinal }
            .map { it.questId }

        return getQuestsByStatus(QuestStatus.AVAILABLE)
            .filter { quest ->
                quest.prerequisites.all { prerequisite ->
                    completedQuests.contains(prerequisite)
                }
            }
    }

    // 创建世界状态快照
    @Transaction
    suspend fun createWorldStateSnapshot(
        playerId: String,
        playerLevel: Int,
        playerAttributes: PlayerAttributes,
        unlockedWorlds: List<String>,
        activeQuests: List<String>,
        keyItems: List<String>,
        factionRelations: Map<String, Int>,
        narrativeAnchor: String,
        contextSummary: String
    ): String {
        val snapshotId = "snapshot_${System.currentTimeMillis()}_${playerId}"
        insertSnapshot(
            WorldStateSnapshot(
                id = snapshotId,
                timestamp = System.currentTimeMillis(),
                playerLevel = playerLevel,
                playerAttributes = playerAttributes,
                unlockedWorlds = unlockedWorlds,
                activeQuests = activeQuests,
                keyItems = keyItems,
                factionRelations = factionRelations,
                narrativeAnchor = narrativeAnchor,
                contextSummary = contextSummary
            )
        )
        return snapshotId
    }
}
