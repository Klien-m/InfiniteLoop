package com.infinite.narrative.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.infinite.narrative.data.model.Item
import com.infinite.narrative.data.model.ItemOwnership
import com.infinite.narrative.data.model.ItemType
import com.infinite.narrative.data.model.Rarity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    // Item CRUD操作
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: String): Item?

    @Query("SELECT * FROM items WHERE itemType = :type")
    fun getItemsByType(type: ItemType): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE rarity = :rarity")
    fun getItemsByRarity(rarity: Rarity): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id IN (:ids)")
    suspend fun getItemsByIds(ids: List<String>): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    // ItemOwnership操作
    @Query("SELECT * FROM item_ownerships WHERE itemId = :itemId")
    suspend fun getOwnershipsByItem(itemId: String): List<ItemOwnership>

    @Query("SELECT * FROM item_ownerships WHERE ownerId = :ownerId")
    suspend fun getItemsByOwner(ownerId: String): List<ItemOwnership>

    @Query("SELECT * FROM item_ownerships WHERE itemId = :itemId AND ownerId = :ownerId")
    suspend fun getOwnership(itemId: String, ownerId: String): ItemOwnership?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnership(ownership: ItemOwnership)

    @Update
    suspend fun updateOwnership(ownership: ItemOwnership)

    @Delete
    suspend fun deleteOwnership(ownership: ItemOwnership)

    // 批量操作
    @Transaction
    suspend fun insertItemWithOwnership(item: Item, ownerId: String, quantity: Int = 1) {
        insertItem(item)
        insertOwnership(
            ItemOwnership(
                itemId = item.id,
                ownerId = ownerId,
                quantity = quantity
            )
        )
    }

    @Transaction
    suspend fun transferItem(itemId: String, fromOwner: String, toOwner: String, quantity: Int) {
        val ownership = getOwnership(itemId, fromOwner)
        if (ownership != null && ownership.quantity >= quantity) {
            if (ownership.quantity == quantity) {
                deleteOwnership(ownership)
            } else {
                updateOwnership(ownership.copy(quantity = ownership.quantity - quantity))
            }

            val toOwnership = getOwnership(itemId, toOwner)
            if (toOwnership != null) {
                updateOwnership(toOwnership.copy(quantity = toOwnership.quantity + quantity))
            } else {
                insertOwnership(ItemOwnership(itemId, toOwner, quantity))
            }
        }
    }

    // 查询玩家拥有的所有物品
    @Transaction
    @Query("SELECT i.* FROM items i INNER JOIN item_ownerships io ON i.id = io.itemId WHERE io.ownerId = :playerId")
    fun getPlayerItems(playerId: String): Flow<List<Item>>

    // 查询特定类型物品的数量
    @Query("SELECT SUM(io.quantity) FROM item_ownerships io WHERE io.ownerId = :playerId AND io.itemId IN (SELECT id FROM items WHERE itemType = :itemType)")
    suspend fun getPlayerItemCountByType(playerId: String, itemType: ItemType): Int

    // 检查玩家是否拥有特定物品
    @Query("SELECT EXISTS(SELECT 1 FROM item_ownerships WHERE ownerId = :playerId AND itemId = :itemId)")
    suspend fun playerHasItem(playerId: String, itemId: String): Boolean
}
