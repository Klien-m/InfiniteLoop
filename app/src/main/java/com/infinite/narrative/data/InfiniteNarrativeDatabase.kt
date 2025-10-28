package com.infinite.narrative.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.infinite.narrative.data.dao.CharacterDao
import com.infinite.narrative.data.dao.ItemDao
import com.infinite.narrative.data.dao.QuestDao
import com.infinite.narrative.data.model.CausalCollection
import com.infinite.narrative.data.model.Character
import com.infinite.narrative.data.model.CharacterFaction
import com.infinite.narrative.data.model.CharacterRelation
import com.infinite.narrative.data.model.Item
import com.infinite.narrative.data.model.ItemOwnership
import com.infinite.narrative.data.model.ItemTypeConverters
import com.infinite.narrative.data.model.NarrativeContext
import com.infinite.narrative.data.model.PlayerAttributes
import com.infinite.narrative.data.model.Quest
import com.infinite.narrative.data.model.QuestProgress
import com.infinite.narrative.data.model.QuestStage
import com.infinite.narrative.data.model.WorldStateSnapshot

@Database(
    entities = [
        Character::class,
        CharacterRelation::class,
        CharacterFaction::class,
        Item::class,
        ItemOwnership::class,
        Quest::class,
        QuestProgress::class,
        QuestStage::class,
        WorldStateSnapshot::class,
        CausalCollection::class,
        NarrativeContext::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(ItemTypeConverters::class, PlayerAttributesTypeConverter::class)
abstract class InfiniteNarrativeDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun itemDao(): ItemDao
    abstract fun questDao(): QuestDao

    companion object {
        const val DATABASE_NAME = "infinite_narrative.db"
    }
}

/**
 * PlayerAttributes类型转换器
 */
class PlayerAttributesTypeConverter {
    private val gson = com.google.gson.Gson()

    @androidx.room.TypeConverter
    fun fromPlayerAttributes(attributes: PlayerAttributes): String {
        return gson.toJson(attributes)
    }

    @androidx.room.TypeConverter
    fun toPlayerAttributes(attributes: String): PlayerAttributes {
        return gson.fromJson(attributes, PlayerAttributes::class.java)
    }
}
