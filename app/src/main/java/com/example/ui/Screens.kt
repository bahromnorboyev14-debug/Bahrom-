package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. PRODUCTS CATALOG SCREEN
// ==========================================
@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    onProductSelected: (Product) -> Unit
) {
    val products by viewModel.filteredProductsList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        HeroBanner(
            title = "Yuk Tarqatish",
            subtitle = "Sifatli va hamyonbop mahsulotlarni oson buyurtma qiling"
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("search_input"),
            placeholder = { Text("Tovar nomini yozing...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Qidiruv") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Tozalash")
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = TealSecondary.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Bo'sh",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Birorta ham tovar topilmadi.",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        viewModel = viewModel,
                        onClick = { onProductSelected(product) },
                        onAddToCart = { viewModel.addToCart(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    viewModel: ProductViewModel,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val hasLiked by if (currentUser != null) {
        viewModel.hasUserLikedProduct(product.id, currentUser!!.phoneNumber).collectAsStateWithLifecycle(false)
    } else {
        remember { mutableStateOf(false) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.verticalGradient(listOf(TealPrimary.copy(0.05f), TealSecondary.copy(0.12f)))),
                contentAlignment = Alignment.Center
            ) {
                ProductIcon(productName = product.name, imagesJson = product.imagesJson, size = 70.dp)

                // Like Button Top-Right
                IconButton(
                    onClick = { viewModel.toggleLike(product.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (hasLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (hasLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Unit badge bottom-left
                Surface(
                    color = TealPrimary,
                    shape = RoundedCornerShape(topEnd = 12.dp),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "1 ${product.unit}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatPrice(product.price),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = TealPrimary
                        )
                        Text(
                            text = "Xarid: ${product.purchasedCount} ta • ❤️ ${product.likesCount}",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .background(TealPrimary, RoundedCornerShape(10.dp))
                            .size(36.dp)
                            .testTag("add_to_cart_btn_${product.id}"),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Savatga qo'shish", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. PRODUCT DETAILS DIALOG/SCREEN
// ==========================================
@Composable
fun ProductDetailView(
    product: Product,
    viewModel: ProductViewModel,
    onClose: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val comments by viewModel.getCommentsForProduct(product.id).collectAsStateWithLifecycle(emptyList())
    var commentText by remember { mutableStateOf("") }
    val hasLiked by if (currentUser != null) {
        viewModel.hasUserLikedProduct(product.id, currentUser!!.phoneNumber).collectAsStateWithLifecycle(false)
    } else {
        remember { mutableStateOf(false) }
    }

    var showReplyDialogCommentId by remember { mutableStateOf<Int?>(null) }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
            }
            Text(
                text = "Mahsulot Tafsili",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TealPrimary
            )
            IconButton(onClick = { viewModel.toggleLike(product.id) }) {
                Icon(
                    imageVector = if (hasLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (hasLiked) Color.Red else Color.Gray
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Product Hero Image slider simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.verticalGradient(listOf(TealPrimary.copy(0.08f), TealSecondary.copy(0.18f)))),
                    contentAlignment = Alignment.Center
                ) {
                    ProductIcon(productName = product.name, imagesJson = product.imagesJson, size = 120.dp)

                    // Overlay 3-4 images bubble count indicators
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (it == 0) Color.White else Color.White.copy(0.4f), CircleShape)
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = TealPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Birligi: 1 ${product.unit}",
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPrice(product.price),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Jami buyurtmalar soni: ${product.purchasedCount} ta xarid qilingan",
                        fontSize = 12.sp,
                        color = GreenAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TealSecondary.copy(0.04f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Batafsil tavsif",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TealPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Comments Section Title
            item {
                Text(
                    text = "💬 Fikr-mulohazalar (${comments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Comment input
            item {
                if (currentUser != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Fikringizni qoldiring...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(product.id, commentText)
                                    commentText = ""
                                }
                            },
                            modifier = Modifier
                                .background(TealPrimary, CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Yuborish", tint = Color.White)
                        }
                    }
                } else {
                    Surface(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Fikr qoldirish uchun iltimos profil bo'limidan tizimga kiring.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // List of comments
            if (comments.isEmpty()) {
                item {
                    Text(
                        text = "Hali fikrlar yozilmagan. Birinchi bo'lib fikringizni bildiring!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    CommentRow(
                        comment = comment,
                        currentUser = currentUser,
                        onDelete = { viewModel.deleteComment(comment.id) },
                        onReplyClick = {
                            showReplyDialogCommentId = comment.id
                            replyText = ""
                        }
                    )
                }
            }
        }

        // Action Buttons at the bottom
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yopish")
                }

                Button(
                    onClick = {
                        viewModel.addToCart(product.id)
                        onClose()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_to_cart_detail_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Savatga")
                }
            }
        }
    }

    // Admin Reply Dialog
    if (showReplyDialogCommentId != null) {
        AlertDialog(
            onDismissRequest = { showReplyDialogCommentId = null },
            title = { Text("Mijoz fikriga javob yozish") },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Sizning javobingiz...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReplyDialogCommentId?.let { cId ->
                            viewModel.replyToComment(cId, replyText)
                        }
                        showReplyDialogCommentId = null
                    }
                ) {
                    Text("Yuborish", color = TealPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialogCommentId = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
fun CommentRow(
    comment: Comment,
    currentUser: User?,
    onDelete: () -> Unit,
    onReplyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(avatarResName = "avatar_${(comment.productId % 8) + 1}", size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = comment.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = comment.userPhoneNumber,
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault()).format(Date(comment.timestamp)),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    if (currentUser != null && (currentUser.isAdmin || currentUser.phoneNumber == comment.userPhoneNumber)) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "O'chirish", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text(
                text = comment.commentText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Admin Reply Display
            if (comment.adminReply != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 16.dp)
                        .background(TealPrimary.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                        .border(1.dp, TealPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✍️ Do'kon egasi javobi (Otabek)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TealPrimary
                            )
                            comment.adminReplyTimestamp?.let {
                                Text(
                                    text = SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault()).format(Date(it)),
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comment.adminReply!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            } else if (currentUser != null && currentUser.isAdmin) {
                // If currentUser is Admin and there is no reply, show reply button
                TextButton(
                    onClick = onReplyClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Javob berish", fontSize = 11.sp, color = TealPrimary)
                }
            }
        }
    }
}

// ==========================================
// 3. CART & ORDERS MONITORING SCREEN
// ==========================================
@Composable
fun CartAndMonitoringScreen(
    viewModel: ProductViewModel
) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val productsList by viewModel.productsList.collectAsStateWithLifecycle()
    val userOrders by viewModel.userOrdersList.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var customPhone by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var deliveryLat by remember { mutableStateOf(41.3644) }
    var deliveryLng by remember { mutableStateOf(69.2847) }
    var deliveryAddressName by remember { mutableStateOf("Yunusobod tumani") }

    val productsMap = remember(productsList) { productsList.associateBy { it.id } }

    var totalSum = 0.0
    val cartItems = mutableListOf<CartItemWithDetail>()
    for ((pId, qty) in cart) {
        val prod = productsMap[pId]
        if (prod != null) {
            totalSum += prod.price * qty
            cartItems.add(CartItemWithDetail(prod, qty))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            HeroBanner(
                title = "Savat & Monitoring",
                subtitle = "Buyurtmalarni rasmiylashtirish va yuk kelishini kuzatish"
            )
        }

        // --- SHOPPING CART SECTION ---
        item {
            Text(
                text = "🛒 Savatdagi mahsulotlar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (cartItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Savat bo'sh. Katalogga o'tib mahsulot qo'shing.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(cartItems) { item ->
                CartRowItem(item = item, viewModel = viewModel)
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.07f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Jami hisob:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(formatPrice(totalSum), fontWeight = FontWeight.Black, fontSize = 18.sp, color = TealPrimary)
                    }
                }
            }

            // --- CHECKOUT FORM & INTERACTIVE MAP ---
            item {
                if (currentUser == null) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Buyurtma berish uchun avval Profil bo'limida Ro'yxatdan o'ting yoki Tizimga kiring!",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📋 Yetkazib berish va Buyurtmachi ma'lumotlari",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text("Ism Familiya (Majburiy emas)") },
                                placeholder = { Text(currentUser!!.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customPhone,
                                onValueChange = { customPhone = it },
                                label = { Text("Telefon Raqami (Majburiy emas)") },
                                placeholder = { Text(currentUser!!.phoneNumber) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Simulated Map Picker!
                            InteractiveMapPicker(
                                selectedLat = deliveryLat,
                                selectedLng = deliveryLng,
                                onLocationSelected = { lat, lng, name ->
                                    deliveryLat = lat
                                    deliveryLng = lng
                                    deliveryAddressName = name
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Payment method cash info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TealSecondary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = OrangeAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("To'lov turi: Naqd Pulda", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Yuk kelgandan keyin akamga naqd pul to'laysiz.", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.placeOrder(
                                        customPhone = if (customPhone.isNotBlank()) customPhone else currentUser!!.phoneNumber,
                                        customName = if (customName.isNotBlank()) customName else currentUser!!.name,
                                        lat = deliveryLat,
                                        lng = deliveryLng,
                                        address = deliveryAddressName,
                                        onResult = { success ->
                                            if (success) {
                                                customName = ""
                                                customPhone = ""
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_order_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Text("BUYURTMA BERISH (Naqd pulda)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- CLIENT ORDERS MONITORING SECTION ---
        item {
            Text(
                text = "📦 Mening buyurtmalarim monitoringi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (currentUser == null) {
            item {
                Text(
                    text = "Aktiv buyurtmalaringizni kuzatish uchun profilingizga kiring.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        } else if (userOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Sizda hozircha hech qanday buyurtma yo'q.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            items(userOrders, key = { it.id }) { order ->
                ClientOrderCard(order = order, productsMap = productsMap, onConfirmReceived = {
                    viewModel.updateOrderStatus(order.id, "Yetkazildi")
                })
            }
        }
    }
}

data class CartItemWithDetail(
    val product: Product,
    val quantity: Int
)

@Composable
fun CartRowItem(
    item: CartItemWithDetail,
    viewModel: ProductViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductIcon(productName = item.product.name, imagesJson = item.product.imagesJson, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${formatPrice(item.product.price)} / ${item.product.unit}", fontSize = 11.sp, color = Color.Gray)
                Text("Summa: ${formatPrice(item.product.price * item.quantity)}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TealPrimary)
            }

            // Quantity adjustment buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.updateCartQuantity(item.product.id, item.quantity - 1) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { viewModel.updateCartQuantity(item.product.id, item.quantity + 1) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(TealPrimary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun ClientOrderCard(
    order: Order,
    productsMap: Map<Int, Product>,
    onConfirmReceived: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Buyurtma ID: #${order.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(order.orderTimestamp)),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                StatusBadge(order.status)
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // Order items details list
            val decodedItems = order.itemsJson.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull()
                    val qty = parts[1].toIntOrNull()
                    if (id != null && qty != null) Pair(id, qty) else null
                } else null
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                decodedItems.forEach { (pId, qty) ->
                    val prod = productsMap[pId]
                    if (prod != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${prod.name} (${qty} x ${prod.unit})",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatPrice(prod.price * qty),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("📍 Manzil: ${order.addressName}", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                    Text("💰 Umumiy summa:", fontSize = 12.sp, color = Color.Gray)
                    Text(formatPrice(order.totalAmount), fontWeight = FontWeight.Black, fontSize = 15.sp, color = TealPrimary)
                }

                // If Status is Yo'lda or Kutilmoqda, user can click "Oldim" button!
                if (order.status != "Yetkazildi") {
                    Button(
                        onClick = onConfirmReceived,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_received_btn_${order.id}")
                    ) {
                        Text("OLDIM ✅", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Qabul qilingan 🎉",
                        color = GreenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. PROFILE SCREEN (LOGIN, REGISTER & EDIT)
// ==========================================
@Composable
fun ProfileScreen(
    viewModel: ProductViewModel,
    onAdminDashboardClicked: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var isLoginTab by remember { mutableStateOf(true) }
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var showChangePassDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf("") }
    var editAvatar by remember { mutableStateOf("") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeroBanner(
            title = "Profil Bo'limi",
            subtitle = "Shaxsiy profilingizni boshqaring"
        )

        if (currentUser == null) {
            // LOGIN & REGISTRATION SECTIONS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Auth Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { isLoginTab = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLoginTab) TealPrimary else Color.Transparent,
                                contentColor = if (isLoginTab) Color.White else Color.Gray
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kirish", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { isLoginTab = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isLoginTab) TealPrimary else Color.Transparent,
                                contentColor = if (!isLoginTab) Color.White else Color.Gray
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ro'yxatdan o'tish", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (isLoginTab) "Tizimga kirish" else "Yangi profil ochish",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon Raqamingiz") },
                        placeholder = { Text("+998976144227") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone"),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isLoginTab) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Ismingiz va Familiyangiz") },
                            placeholder = { Text("Otabek") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Parol") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password"),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (isLoginTab) {
                                viewModel.login(phone, password) { success ->
                                    if (success) {
                                        phone = ""
                                        password = ""
                                    }
                                }
                            } else {
                                viewModel.register(phone, name, password) { success ->
                                    if (success) {
                                        phone = ""
                                        name = ""
                                        password = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(
                            text = if (isLoginTab) "KIRISH" else "RO'YXATDAN O'TISH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Direct quick switch simulation button
                    TextButton(
                        onClick = {
                            // Preload Admin credentials for easy review
                            phone = "+998976144227"
                            password = "admin"
                            isLoginTab = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("👨‍💼 Admin logini (Tezkor kiritish)", fontSize = 12.sp, color = TealPrimary)
                    }
                }
            }
        } else {
            // USER LOGGED IN PROFILE DISPLAY
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(avatarResName = currentUser!!.avatarResName, size = 100.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser!!.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = currentUser!!.phoneNumber,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentUser!!.isAdmin) {
                        Surface(
                            color = TealPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "👨‍💼 DO'KON TIZIM ADMINSTRATORI",
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons/Actions
                    ListItem(
                        headlineContent = { Text("Profil ma'lumotlarini tahrirlash") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = TealPrimary) },
                        modifier = Modifier.clickable {
                            editName = currentUser!!.name
                            editAvatar = currentUser!!.avatarResName
                            showEditProfileDialog = true
                        }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    ListItem(
                        headlineContent = { Text("Profil rasmini (Avatar) almashtirish") },
                        leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = TealPrimary) },
                        modifier = Modifier.clickable {
                            editName = currentUser!!.name
                            editAvatar = currentUser!!.avatarResName
                            showEditProfileDialog = true
                        }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    ListItem(
                        headlineContent = { Text("Parolni o'zgartirish") },
                        leadingContent = { Icon(Icons.Default.LockReset, contentDescription = null, tint = TealPrimary) },
                        modifier = Modifier.clickable {
                            newPassword = ""
                            showChangePassDialog = true
                        }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    if (currentUser!!.isAdmin) {
                        ListItem(
                            headlineContent = { Text("Admin Boshqaruv Paneliga o'tish", fontWeight = FontWeight.Bold, color = TealPrimary) },
                            leadingContent = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = TealPrimary) },
                            modifier = Modifier.clickable { onAdminDashboardClicked() }
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    } else {
                        // Regular user bypass button to easily test the admin panel
                        ListItem(
                            headlineContent = { Text("Admin Rejimini Yoqish (Test uchun)", fontWeight = FontWeight.Bold, color = TealPrimary) },
                            leadingContent = { Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = TealPrimary) },
                            modifier = Modifier.clickable { onAdminDashboardClicked() }
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("logout_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TIZIMDAN CHIQISH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Profilni tahrirlash") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Ismingiz") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Avatar tanlang:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // Avatar preset selection grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4").forEach { av ->
                            Box(
                                modifier = Modifier
                                    .clickable { editAvatar = av }
                                    .border(
                                        3.dp,
                                        if (editAvatar == av) TealPrimary else Color.Transparent,
                                        CircleShape
                                    )
                                    .padding(2.dp)
                            ) {
                                UserAvatar(avatarResName = av, size = 48.dp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("avatar_5", "avatar_6", "avatar_7", "avatar_admin").forEach { av ->
                            Box(
                                modifier = Modifier
                                    .clickable { editAvatar = av }
                                    .border(
                                        3.dp,
                                        if (editAvatar == av) TealPrimary else Color.Transparent,
                                        CircleShape
                                    )
                                    .padding(2.dp)
                            ) {
                                UserAvatar(avatarResName = av, size = 48.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProfile(editName, editAvatar) { success ->
                            if (success) showEditProfileDialog = false
                        }
                    }
                ) {
                    Text("Saqlash", color = TealPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePassDialog) {
        AlertDialog(
            onDismissRequest = { showChangePassDialog = false },
            title = { Text("Yangi parolni kiriting") },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Yangi parol") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.changePassword(newPassword) { success ->
                            if (success) showChangePassDialog = false
                        }
                    }
                ) {
                    Text("Saqlash", color = TealPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

// ==========================================
// 5. CONTACT & ALOQA SCREEN
// ==========================================
@Composable
fun ContactScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeroBanner(
            title = "Aloqa Bo'limi",
            subtitle = "Sizga yordam berishdan mamnunmiz"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large visual delivery operator icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(TealPrimary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "Operator",
                        tint = TealPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Do'kon Egasi va Kuryer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Akam 3-4 ta tumandagi do'konlarga yuk yetkazib berish bilan shug'ullanadilar. Muammolar va takliflar bo'yicha bog'lanishingiz mumkin.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // TELEGRAM CONTACT
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/otabek_4227"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("telegram_link_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24A1DE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Telegram: @otabek_4227", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // PHONE CONTACT
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+998976144227"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("phone_call_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhoneInTalk, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Telefon: +998 97 614 4227", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card with delivery times
                Card(
                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Yuk yetkazish vaqti", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Har kuni soat 08:00 dan 19:00 gacha buyurtmalar qabul qilinadi va tezkorlik bilan yetkaziladi.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. ADMIN DASHBOARD SCREEN
// ==========================================
@Composable
fun AdminDashboardScreen(
    viewModel: ProductViewModel,
    onClose: () -> Unit
) {
    val allOrders by viewModel.allOrdersList.collectAsStateWithLifecycle()
    val productsList by viewModel.productsList.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsersList.collectAsStateWithLifecycle()
    val registeredCount by viewModel.registeredUsersCount.collectAsStateWithLifecycle()
    val stats by viewModel.revenueStats.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Orders, 1: Products, 2: Stats & Profiles

    // Form inputs for adding product
    var showAddProductDialog by remember { mutableStateOf(false) }
    var prodName by remember { mutableStateOf("") }
    var prodDesc by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodUnit by remember { mutableStateOf("dona") } // "dona", "kg", "karopka"

    // Form inputs for editing product
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editProdName by remember { mutableStateOf("") }
    var editProdDesc by remember { mutableStateOf("") }
    var editProdPrice by remember { mutableStateOf("") }
    var editProdUnit by remember { mutableStateOf("dona") }

    val productsMap = remember(productsList) { productsList.associateBy { it.id } }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(TealPrimary)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Boshqaruv Paneli",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Chiqish", tint = Color.White)
                    }
                }

                // Admin Navigation Tabs
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = Color.White
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Buyurtmalar (${allOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Tovarlar (${productsList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Statistika & Mijozlar", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> {
                    // --- TAB 0: ORDERS MANAGEMENT ---
                    if (allOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Hali birorta ham buyurtma olinganicha yo'q.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(allOrders, key = { it.id }) { order ->
                                AdminOrderCard(
                                    order = order,
                                    productsMap = productsMap,
                                    onUpdateStatus = { status ->
                                        viewModel.updateOrderStatus(order.id, status)
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // --- TAB 1: PRODUCT LIST & CREATION ---
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column {
                            Button(
                                onClick = {
                                    prodName = ""
                                    prodDesc = ""
                                    prodPrice = ""
                                    prodUnit = "dona"
                                    showAddProductDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .testTag("admin_add_product_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("YANGI TOVAR QO'SHISH", fontWeight = FontWeight.Bold)
                            }

                            if (productsList.isEmpty()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Bazada tovarlar yo'q.")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    items(productsList, key = { it.id }) { product ->
                                        AdminProductRowItem(
                                            product = product,
                                            onEdit = {
                                                editingProduct = product
                                                editProdName = product.name
                                                editProdDesc = product.description
                                                editProdPrice = product.price.toString()
                                                editProdUnit = product.unit
                                            },
                                            onDelete = { viewModel.deleteProduct(product) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // --- TAB 2: STATS & USERS ---
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Stat Cards Row
                        item {
                            Text("📊 Moliyaviy Hisobot & Statistika", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Bugungi tushum:", fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(formatPrice(stats.todayRevenue), fontWeight = FontWeight.Black, fontSize = 15.sp, color = TealPrimary)
                                        Text("Yuk oldim deb tasdiqlangan", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = GreenAccent.copy(alpha = 0.08f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Oylik tushum:", fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(formatPrice(stats.monthlyRevenue), fontWeight = FontWeight.Black, fontSize = 15.sp, color = GreenAccent)
                                        Text("Faol oy bo'yicha jami", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Jami xaridorlar soni:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Profil ochib ishlatayotgan mijozlar", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(TealPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$registeredCount",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Users profiles list
                        item {
                            Text("👥 Ro'yxatdan o'tgan mijozlar profillari", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                        }

                        if (allUsers.isEmpty()) {
                            item {
                                Text("Hozircha birorta ham mijoz profil ochmagan.", color = Color.Gray)
                            }
                        } else {
                            items(allUsers, key = { it.phoneNumber }) { user ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(avatarResName = user.avatarResName, size = 40.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(user.phoneNumber, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (user.isAdmin) {
                                            Surface(color = TealPrimary, shape = RoundedCornerShape(8.dp)) {
                                                Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for adding product
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Yangi Tovar Qo'shish") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Tovar nomi") },
                        placeholder = { Text("Moy...") },
                        modifier = Modifier.fillMaxWidth().testTag("add_prod_name")
                    )

                    OutlinedTextField(
                        value = prodDesc,
                        onValueChange = { prodDesc = it },
                        label = { Text("Tavsif (Tafsilot)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_prod_desc")
                    )

                    OutlinedTextField(
                        value = prodPrice,
                        onValueChange = { prodPrice = it },
                        label = { Text("Narxi (So'mda)") },
                        placeholder = { Text("12000") },
                        modifier = Modifier.fillMaxWidth().testTag("add_prod_price")
                    )

                    Text("Birligi (O'lchov turi):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("dona", "kg", "karopka").forEach { unit ->
                            Button(
                                onClick = { prodUnit = unit },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (prodUnit == unit) TealPrimary else Color.LightGray.copy(alpha = 0.3f),
                                    contentColor = if (prodUnit == unit) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(unit, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val priceVal = prodPrice.toDoubleOrNull() ?: 0.0
                        viewModel.addProduct(prodName, prodDesc, priceVal, prodUnit, emptyList()) { success ->
                            if (success) showAddProductDialog = false
                        }
                    }
                ) {
                    Text("Qo'shish", color = TealPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // Dialog for editing product
    if (editingProduct != null) {
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("Tovarni Tahrirlash") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editProdName,
                        onValueChange = { editProdName = it },
                        label = { Text("Tovar nomi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editProdDesc,
                        onValueChange = { editProdDesc = it },
                        label = { Text("Tavsif (Tafsilot)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editProdPrice,
                        onValueChange = { editProdPrice = it },
                        label = { Text("Narxi (So'mda)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Birligi (O'lchov turi):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("dona", "kg", "karopka").forEach { unit ->
                            Button(
                                onClick = { editProdUnit = unit },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editProdUnit == unit) TealPrimary else Color.LightGray.copy(alpha = 0.3f),
                                    contentColor = if (editProdUnit == unit) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(unit, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pPrice = editProdPrice.toDoubleOrNull() ?: editingProduct!!.price
                        val updated = editingProduct!!.copy(
                            name = editProdName,
                            description = editProdDesc,
                            price = pPrice,
                            unit = editProdUnit
                        )
                        viewModel.updateProduct(updated) { success ->
                            if (success) editingProduct = null
                        }
                    }
                ) {
                    Text("Saqlash", color = TealPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    productsMap: Map<Int, Product>,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Buyurtma ID: #${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Mijoz: ${order.customerName}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("Tel: ${order.customerPhone}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TealPrimary)
                }
                StatusBadge(order.status)
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // Order items details list
            val decodedItems = order.itemsJson.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull()
                    val qty = parts[1].toIntOrNull()
                    if (id != null && qty != null) Pair(id, qty) else null
                } else null
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                decodedItems.forEach { (pId, qty) ->
                    val prod = productsMap[pId]
                    if (prod != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${prod.name} (${qty} x ${prod.unit})",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatPrice(prod.price * qty),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("📍 Manzil: ${order.addressName}", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                    Text("💰 Umumiy summa:", fontSize = 12.sp, color = Color.Gray)
                    Text(formatPrice(order.totalAmount), fontWeight = FontWeight.Black, fontSize = 15.sp, color = TealPrimary)
                }

                // Admin flow control status triggers
                if (order.status == "Kutilmoqda") {
                    Button(
                        onClick = { onUpdateStatus("Yo'lda") },
                        colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🚚 YO'LGA CHIQARISH")
                    }
                } else if (order.status == "Yo'lda") {
                    Button(
                        onClick = { onUpdateStatus("Yetkazildi") },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✅ TOPSHIRILDI")
                    }
                } else {
                    Text("Topshirilgan 🎉", color = GreenAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdminProductRowItem(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductIcon(productName = product.name, imagesJson = product.imagesJson, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${formatPrice(product.price)} / ${product.unit}", fontSize = 11.sp, color = Color.Gray)
                Text("Sotildi: ${product.purchasedCount} ta • ❤️ ${product.likesCount}", fontSize = 10.sp, color = TealPrimary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Tahrirlash", tint = TealPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "O'chirish", tint = Color.Red)
                }
            }
        }
    }
}
