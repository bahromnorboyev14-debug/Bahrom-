package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val password: String,
    val avatarResName: String = "avatar_1", // Default avatar identifier
    val isAdmin: Boolean = false
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val unit: String, // "dona", "kg", or "karopka"
    val imagesJson: String, // Comma separated image placeholders or custom base64/URI
    val purchasedCount: Int = 0,
    val likesCount: Int = 0
)

@Entity(tableName = "product_likes", primaryKeys = ["productId", "userPhoneNumber"])
data class ProductLike(
    val productId: Int,
    val userPhoneNumber: String
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val userPhoneNumber: String,
    val userName: String,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val adminReply: String? = null,
    val adminReplyTimestamp: Long? = null
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerPhone: String,
    val customerName: String,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val itemsJson: String, // List of bought items (JSON representation)
    val totalAmount: Double,
    val latitude: Double, // Google Maps simulated coordinates
    val longitude: Double,
    val addressName: String, // District / delivery description
    val status: String = "Kutilmoqda", // "Kutilmoqda" (Pending), "Yo'lda" (In Transit), "Yetkazildi" (Delivered / Completed)
    val completedTimestamp: Long? = null // Set when user clicks "Oldim"
)
