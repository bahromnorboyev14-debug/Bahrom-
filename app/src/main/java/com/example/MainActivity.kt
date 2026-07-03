package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ProductRepository(
            userDao = database.userDao(),
            productDao = database.productDao(),
            productLikeDao = database.productLikeDao(),
            commentDao = database.commentDao(),
            orderDao = database.orderDao()
        )

        // Instantiate ViewModel
        val viewModel: ProductViewModel by viewModels {
            ProductViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current

                // Listen for Toast messages from ViewModel reactively
                LaunchedEffect(Unit) {
                    viewModel.message.collectLatest { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: ProductViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Catalog, 1: Cart, 2: Aloqa, 3: Profile
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showAdminDashboard by remember { mutableStateOf(false) }

    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val cartCount = cart.values.sum()

    // Overlay Views
    if (showAdminDashboard) {
        AdminDashboardScreen(
            viewModel = viewModel,
            onClose = { showAdminDashboard = false }
        )
    } else if (selectedProduct != null) {
        ProductDetailView(
            product = selectedProduct!!,
            viewModel = viewModel,
            onClose = { selectedProduct = null }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Katalog") },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Katalog") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Savat") },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge {
                                            Text("$cartCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Savat")
                            }
                        }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Aloqa") },
                        icon = { Icon(Icons.Default.PhoneInTalk, contentDescription = "Aloqa") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text("Profil") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") }
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> ProductsScreen(
                        viewModel = viewModel,
                        onProductSelected = { selectedProduct = it }
                    )
                    1 -> CartAndMonitoringScreen(viewModel = viewModel)
                    2 -> ContactScreen()
                    3 -> ProfileScreen(
                        viewModel = viewModel,
                        onAdminDashboardClicked = { showAdminDashboard = true }
                    )
                }
            }
        }
    }
}
