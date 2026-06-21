package com.example.piecework.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity): Long

    @Query("SELECT * FROM products ORDER BY id")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProduct(id: Long): ProductEntity?

    @Query("UPDATE products SET name = :name, unitPriceCents = :unitPriceCents WHERE id = :productId")
    suspend fun updateProduct(
        productId: Long,
        name: String,
        unitPriceCents: Long
    )

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int
}
