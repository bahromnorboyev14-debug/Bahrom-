package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ProductRepository(
    private val userDao: UserDao,
    private val productDao: ProductDao,
    private val productLikeDao: ProductLikeDao,
    private val commentDao: CommentDao,
    private val orderDao: OrderDao
) {
    // Users
    fun getAllUsers() = userDao.getAllUsers()
    fun getRegisteredUsersCount(): Flow<Int> = userDao.getRegisteredUsersCount()
    suspend fun getUserByPhone(phone: String): User? = userDao.getUserByPhone(phone)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updatePassword(phone: String, newPassword: String) = userDao.updatePassword(phone, newPassword)
    suspend fun updateProfile(phone: String, name: String, avatarResName: String) = userDao.updateProfile(phone, name, avatarResName)

    // Products
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun incrementPurchasedCount(productId: Int, qty: Int) = productDao.incrementPurchasedCount(productId, qty)

    // Likes
    suspend fun toggleLike(productId: Int, phone: String) {
        val alreadyLiked = productLikeDao.hasLiked(productId, phone)
        if (alreadyLiked) {
            productLikeDao.deleteLike(productId, phone)
        } else {
            productLikeDao.insertLike(ProductLike(productId, phone))
        }
        // Update product's like count
        val currentLikes = productLikeDao.getLikesForProduct(productId).firstOrNull() ?: emptyList()
        productDao.updateLikesCount(productId, currentLikes.size)
    }

    suspend fun hasLiked(productId: Int, phone: String): Boolean = productLikeDao.hasLiked(productId, phone)
    fun getLikesForProduct(productId: Int): Flow<List<String>> = productLikeDao.getLikesForProduct(productId)

    // Comments
    fun getCommentsForProduct(productId: Int): Flow<List<Comment>> = commentDao.getCommentsForProduct(productId)
    suspend fun insertComment(comment: Comment) = commentDao.insertComment(comment)
    suspend fun addAdminReply(commentId: Int, reply: String) {
        commentDao.addAdminReply(commentId, reply, System.currentTimeMillis())
    }
    suspend fun deleteComment(commentId: Int) = commentDao.deleteComment(commentId)

    // Orders
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()
    fun getOrdersForUser(phone: String): Flow<List<Order>> = orderDao.getOrdersForUser(phone)
    suspend fun getOrderById(id: Int): Order? = orderDao.getOrderById(id)
    suspend fun insertOrder(order: Order): Long = orderDao.insertOrder(order)
    suspend fun updateOrderStatus(orderId: Int, status: String, completedTimestamp: Long? = null) {
        orderDao.updateOrderStatus(orderId, status, completedTimestamp)
        
        // If the order status is "Yetkazildi" (completed), increment purchasedCount for each items in the order
        if (status == "Yetkazildi") {
            val orderObj = orderDao.getOrderById(orderId)
            if (orderObj != null) {
                // Parse itemsJson (format: "id1:qty1,id2:qty2,...")
                parseItemsJson(orderObj.itemsJson).forEach { (productId, qty) ->
                    productDao.incrementPurchasedCount(productId, qty)
                }
            }
        }
    }

    // Helper to parse itemsJson: e.g., "1:2,5:1" -> Map of {1 to 2, 5 to 1}
    private fun parseItemsJson(json: String): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        if (json.isEmpty()) return result
        try {
            val tokens = json.split(",")
            for (token in tokens) {
                val parts = token.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull()
                    val qty = parts[1].toIntOrNull()
                    if (id != null && qty != null) {
                        result[id] = qty
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
