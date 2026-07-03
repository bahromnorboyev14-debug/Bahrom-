package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users WHERE isAdmin = 0")
    fun getRegisteredUsersCount(): Flow<Int>

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET password = :newPassword WHERE phoneNumber = :phone")
    suspend fun updatePassword(phone: String, newPassword: String)

    @Query("UPDATE users SET name = :name, avatarResName = :avatarResName WHERE phoneNumber = :phone")
    suspend fun updateProfile(phone: String, name: String, avatarResName: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET purchasedCount = purchasedCount + :qty WHERE id = :productId")
    suspend fun incrementPurchasedCount(productId: Int, qty: Int)

    @Query("UPDATE products SET likesCount = :likesCount WHERE id = :productId")
    suspend fun updateLikesCount(productId: Int, likesCount: Int)
}

@Dao
interface ProductLikeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: ProductLike)

    @Query("DELETE FROM product_likes WHERE productId = :productId AND userPhoneNumber = :phone")
    suspend fun deleteLike(productId: Int, phone: String)

    @Query("SELECT EXISTS(SELECT 1 FROM product_likes WHERE productId = :productId AND userPhoneNumber = :phone)")
    suspend fun hasLiked(productId: Int, phone: String): Boolean

    @Query("SELECT userPhoneNumber FROM product_likes WHERE productId = :productId")
    fun getLikesForProduct(productId: Int): Flow<List<String>>
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE productId = :productId ORDER BY timestamp DESC")
    fun getCommentsForProduct(productId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("UPDATE comments SET adminReply = :reply, adminReplyTimestamp = :timestamp WHERE id = :commentId")
    suspend fun addAdminReply(commentId: Int, reply: String, timestamp: Long)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Int)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderTimestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY orderTimestamp DESC")
    fun getOrdersForUser(phone: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("UPDATE orders SET status = :status, completedTimestamp = :completedTimestamp WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String, completedTimestamp: Long?)
}

@Database(
    entities = [User::class, Product::class, ProductLike::class, Comment::class, Order::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun productLikeDao(): ProductLikeDao
    abstract fun commentDao(): CommentDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yuk_tarqatish_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
