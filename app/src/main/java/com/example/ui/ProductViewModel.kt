package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // --- Cart State (ProductId -> Quantity) ---
    private val _cart = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val cart: StateFlow<Map<Int, Int>> = _cart.asStateFlow()

    // --- Reactive Database Streams ---
    val productsList: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrdersList: StateFlow<List<Order>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val registeredUsersCount: StateFlow<Int> = repository.getRegisteredUsersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // User-specific orders
    val userOrdersList: StateFlow<List<Order>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getOrdersForUser(user.phoneNumber)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All registered profiles list (for Admin review)
    val allUsersList: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search Query State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered products list based on search query
    val filteredProductsList: StateFlow<List<Product>> = combine(productsList, searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Notifications / Messages ---
    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    init {
        // Pre-populate data if DB is empty
        viewModelScope.launch {
            checkAndPrepopulateDb()
        }
    }

    private suspend fun checkAndPrepopulateDb() {
        val existingProducts = productsList.firstOrNull() ?: emptyList()
        if (existingProducts.isEmpty()) {
            // Seed sample products
            val sampleProducts = listOf(
                Product(
                    name = "Moy Oila Baxti (5 Litr)",
                    description = "Oliy navli tozalangan va hidsiz kungaboqar yog'i. Uy va restoran pishiriqlari uchun ideal mahsulot.",
                    price = 76000.0,
                    unit = "dona",
                    imagesJson = "ic_oil_1,ic_oil_2,ic_oil_3",
                    purchasedCount = 14,
                    likesCount = 8
                ),
                Product(
                    name = "Guruch Alanga (Premium)",
                    description = "Chiroyli, toza saralangan Alanga guruchi. Haqiqiy to'y oshi va milliy taomlar pishirish uchun eng yaxshi tanlov.",
                    price = 19000.0,
                    unit = "kg",
                    imagesJson = "ic_rice_1,ic_rice_2",
                    purchasedCount = 28,
                    likesCount = 12
                ),
                Product(
                    name = "Sut Saxovat 3.2%",
                    description = "Tabiiy sigir suti, pasterizatsiyalangan, yog'lilik darajasi 3.2%. Karopkada qulay qadoqlangan.",
                    price = 11500.0,
                    unit = "karopka",
                    imagesJson = "ic_milk_1,ic_milk_2",
                    purchasedCount = 42,
                    likesCount = 5
                ),
                Product(
                    name = "Pomidor Chorsu (Sershira)",
                    description = "Yangi uzilgan qizil pomidorlar, shirin va sershira. To'g'ridan-to'g'ri tuman dehqonlaridan keltirilgan.",
                    price = 14000.0,
                    unit = "kg",
                    imagesJson = "ic_tomato_1,ic_tomato_2",
                    purchasedCount = 19,
                    likesCount = 15
                )
            )
            for (p in sampleProducts) {
                repository.insertProduct(p)
            }
        }

        // Add default admin user if not present
        val adminPhone = "+998976144227"
        val existingAdmin = repository.getUserByPhone(adminPhone)
        if (existingAdmin == null) {
            val adminUser = User(
                phoneNumber = adminPhone,
                name = "Otabek Norboyev",
                password = "admin",
                avatarResName = "avatar_admin",
                isAdmin = true
            )
            repository.insertUser(adminUser)
        }

        // Seed some sample customer users
        val sampleCustomerPhone = "+998901234567"
        if (repository.getUserByPhone(sampleCustomerPhone) == null) {
            repository.insertUser(
                User(
                    phoneNumber = sampleCustomerPhone,
                    name = "Bahrom Rahmonov",
                    password = "123",
                    avatarResName = "avatar_2",
                    isAdmin = false
                )
            )
        }
    }

    // --- Search Actions ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Authentication Actions ---
    fun login(phone: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByPhone(phone)
            if (user != null && user.password == pass) {
                _currentUser.value = user
                _message.emit("Xush kelibsiz, ${user.name}!")
                onResult(true)
            } else {
                _message.emit("Telefon raqam yoki parol noto'g'ri!")
                onResult(false)
            }
        }
    }

    fun register(phone: String, name: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (phone.isBlank() || name.isBlank() || pass.isBlank()) {
                _message.emit("Iltimos, barcha maydonlarni to'ldiring!")
                onResult(false)
                return@launch
            }
            val existing = repository.getUserByPhone(phone)
            if (existing != null) {
                _message.emit("Bu telefon raqam allaqachon ro'yxatdan o'tgan!")
                onResult(false)
            } else {
                val newUser = User(
                    phoneNumber = phone,
                    name = name,
                    password = pass,
                    avatarResName = "avatar_${(1..8).random()}",
                    isAdmin = (phone == "+998976144227") // Automatic admin registration for this specific number
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
                _message.emit("Muvaffaqiyatli ro'yxatdan o'tdingiz!")
                onResult(true)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _cart.value = emptyMap()
        viewModelScope.launch {
            _message.emit("Tizimdan chiqildi.")
        }
    }

    fun changePassword(newPass: String, onResult: (Boolean) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            if (newPass.length < 3) {
                _message.emit("Parol kamida 3 ta belgidan iborat bo'lsin!")
                onResult(false)
                return@launch
            }
            repository.updatePassword(user.phoneNumber, newPass)
            _currentUser.value = user.copy(password = newPass)
            _message.emit("Parol muvaffaqiyatli o'zgartirildi!")
            onResult(true)
        }
    }

    fun updateProfile(name: String, avatarResName: String, onResult: (Boolean) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            if (name.isBlank()) {
                _message.emit("Ism bo'sh bo'lishi mumkin emas!")
                onResult(false)
                return@launch
            }
            repository.updateProfile(user.phoneNumber, name, avatarResName)
            _currentUser.value = user.copy(name = name, avatarResName = avatarResName)
            _message.emit("Profil muvaffaqiyatli yangilandi!")
            onResult(true)
        }
    }

    // --- Cart Actions ---
    fun addToCart(productId: Int) {
        val current = _cart.value.toMutableMap()
        val qty = current.getOrDefault(productId, 0)
        current[productId] = qty + 1
        _cart.value = current
        viewModelScope.launch {
            _message.emit("Savatga qo'shildi!")
        }
    }

    fun updateCartQuantity(productId: Int, qty: Int) {
        val current = _cart.value.toMutableMap()
        if (qty <= 0) {
            current.remove(productId)
        } else {
            current[productId] = qty
        }
        _cart.value = current
    }

    fun removeFromCart(productId: Int) {
        val current = _cart.value.toMutableMap()
        current.remove(productId)
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // --- Product Management (Admin Actions) ---
    fun addProduct(name: String, description: String, price: Double, unit: String, imagesList: List<String>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || description.isBlank() || price <= 0.0) {
                _message.emit("Iltimos, ma'lumotlarni to'g'ri to'ldiring!")
                onResult(false)
                return@launch
            }
            // Use fallback icons if none specified
            val imagesString = if (imagesList.isEmpty()) {
                "ic_product_placeholder"
            } else {
                imagesList.joinToString(",")
            }
            val newProduct = Product(
                name = name,
                description = description,
                price = price,
                unit = unit,
                imagesJson = imagesString
            )
            repository.insertProduct(newProduct)
            _message.emit("Yangi tovar qo'shildi!")
            onResult(true)
        }
    }

    fun updateProduct(product: Product, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (product.name.isBlank() || product.description.isBlank() || product.price <= 0.0) {
                _message.emit("Iltimos, ma'lumotlarni to'g'ri to'ldiring!")
                onResult(false)
                return@launch
            }
            repository.updateProduct(product)
            _message.emit("Tovar tahrirlandi!")
            onResult(true)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _message.emit("Tovar o'chirildi!")
        }
    }

    // --- Likes Actions ---
    fun toggleLike(productId: Int) {
        val user = _currentUser.value
        if (user == null) {
            viewModelScope.launch {
                _message.emit("Layk bosish uchun tizimga kiring!")
            }
            return
        }
        viewModelScope.launch {
            repository.toggleLike(productId, user.phoneNumber)
        }
    }

    fun getLikesCountForProduct(productId: Int): Flow<Int> = flow {
        repository.getLikesForProduct(productId).collect {
            emit(it.size)
        }
    }

    fun hasUserLikedProduct(productId: Int, phone: String): Flow<Boolean> = flow {
        repository.getLikesForProduct(productId).collect { list ->
            emit(list.contains(phone))
        }
    }

    // --- Comments Actions ---
    fun getCommentsForProduct(productId: Int): Flow<List<Comment>> {
        return repository.getCommentsForProduct(productId)
    }

    fun addComment(productId: Int, text: String) {
        val user = _currentUser.value
        if (user == null) {
            viewModelScope.launch {
                _message.emit("Fikr qoldirish uchun tizimga kiring!")
            }
            return
        }
        if (text.isBlank()) return
        viewModelScope.launch {
            val comment = Comment(
                productId = productId,
                userPhoneNumber = user.phoneNumber,
                userName = user.name,
                commentText = text
            )
            repository.insertComment(comment)
            _message.emit("Fikr qo'shildi!")
        }
    }

    fun replyToComment(commentId: Int, replyText: String) {
        val user = _currentUser.value
        if (user == null || !user.isAdmin) return
        if (replyText.isBlank()) return
        viewModelScope.launch {
            repository.addAdminReply(commentId, replyText)
            _message.emit("Admin javobi saqlandi!")
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            repository.deleteComment(commentId)
            _message.emit("Fikr o'chirildi.")
        }
    }

    // --- Checkout & Ordering ---
    fun placeOrder(
        customPhone: String,
        customName: String,
        lat: Double,
        lng: Double,
        address: String,
        onResult: (Boolean) -> Unit
    ) {
        val user = _currentUser.value ?: return
        val currentCart = _cart.value
        if (currentCart.isEmpty()) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val products = productsList.value.associateBy { it.id }
            var total = 0.0
            
            // Build simple string: "id1:qty1,id2:qty2"
            val itemsList = mutableListOf<String>()
            for ((pId, qty) in currentCart) {
                val p = products[pId]
                if (p != null) {
                    total += p.price * qty
                    itemsList.add("$pId:$qty")
                }
            }

            val itemsJsonString = itemsList.joinToString(",")

            val newOrder = Order(
                customerPhone = if (customPhone.isNotBlank()) customPhone else user.phoneNumber,
                customerName = if (customName.isNotBlank()) customName else user.name,
                itemsJson = itemsJsonString,
                totalAmount = total,
                latitude = lat,
                longitude = lng,
                addressName = address,
                status = "Kutilmoqda"
            )

            val orderId = repository.insertOrder(newOrder)
            if (orderId > 0) {
                clearCart()
                _message.emit("Buyurtmangiz qabul qilindi!")
                onResult(true)
            } else {
                _message.emit("Xatolik yuz berdi. Qaytadan urinib ko'ring!")
                onResult(false)
            }
        }
    }

    // Update order status (Admin sends ship-out, or user clicks "Oldim" / Completed)
    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            val completedTime = if (newStatus == "Yetkazildi") System.currentTimeMillis() else null
            repository.updateOrderStatus(orderId, newStatus, completedTime)
            if (newStatus == "Yetkazildi") {
                _message.emit("Mahsulotlar topshirildi! Hisobingizga qo'shildi.")
            } else {
                _message.emit("Buyurtma holati yangilandi: $newStatus")
            }
        }
    }

    // --- Statistical Computations ---
    // Combined stats for Admin Dashboard
    // Bir oyda va bir kunda necha pul buyurtma olindi.
    // "tovar oldim bosilgandan keyin hisobga qo'shilsin"
    val revenueStats: StateFlow<RevenueStats> = allOrdersList.map { orders ->
        val completedOrders = orders.filter { it.status == "Yetkazildi" && it.completedTimestamp != null }
        
        var todaySum = 0.0
        var monthSum = 0.0
        
        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayMonth = now.get(Calendar.MONTH)
        val todayDay = now.get(Calendar.DAY_OF_MONTH)

        for (order in completedOrders) {
            val orderCal = Calendar.getInstance()
            orderCal.timeInMillis = order.completedTimestamp ?: order.orderTimestamp

            val orderYear = orderCal.get(Calendar.YEAR)
            val orderMonth = orderCal.get(Calendar.MONTH)
            val orderDay = orderCal.get(Calendar.DAY_OF_MONTH)

            // Today's match
            if (orderYear == todayYear && orderMonth == todayMonth && orderDay == todayDay) {
                todaySum += order.totalAmount
            }

            // Month's match
            if (orderYear == todayYear && orderMonth == todayMonth) {
                monthSum += order.totalAmount
            }
        }

        RevenueStats(
            todayRevenue = todaySum,
            monthlyRevenue = monthSum,
            completedOrdersCount = completedOrders.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RevenueStats())

    data class RevenueStats(
        val todayRevenue: Double = 0.0,
        val monthlyRevenue: Double = 0.0,
        val completedOrdersCount: Int = 0
    )
}

// ViewModel provider factory
class ProductViewModelFactory(
    private val application: Application,
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
