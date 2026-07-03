package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Comment
import com.example.data.Product
import com.example.ui.theme.*

// --- Custom Avatar Helper ---
@Composable
fun UserAvatar(
    avatarResName: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    // Generate high quality initials or vector shapes dynamically using visual colors
    val colorPair = when (avatarResName) {
        "avatar_1" -> Pair(Color(0xFFE0F2F1), Color(0xFF00796B))
        "avatar_2" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        "avatar_3" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "avatar_4" -> Pair(Color(0xFFE1F5FE), Color(0xFF0277BD))
        "avatar_5" -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
        "avatar_6" -> Pair(Color(0xFFFCE4EC), Color(0xFFC2185B))
        "avatar_7" -> Pair(Color(0xFFEFEBE9), Color(0xFF4E342E))
        "avatar_admin" -> Pair(Color(0xFFECEFF1), Color(0xFF37474F))
        else -> Pair(Color(0xFFE0F7FA), Color(0xFF006064))
    }

    val icon = when (avatarResName) {
        "avatar_admin" -> Icons.Default.AdminPanelSettings
        else -> Icons.Default.Person
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(2.dp, CircleShape)
            .background(colorPair.first, CircleShape)
            .border(1.dp, colorPair.second.copy(alpha = 0.3f), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Avatar",
            tint = colorPair.second,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

// --- Dynamic Icon for Products ---
@Composable
fun ProductIcon(
    productName: String,
    imagesJson: String,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val nameLower = productName.lowercase()
    val (bgGradient, icon, iconColor) = when {
        nameLower.contains("moy") || nameLower.contains("yog'") || nameLower.contains("oil") -> {
            Triple(
                Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFD54F))),
                Icons.Default.WaterDrop,
                Color(0xFFF57C00)
            )
        }
        nameLower.contains("guruch") || nameLower.contains("rice") || nameLower.contains("un") -> {
            Triple(
                Brush.linearGradient(listOf(Color(0xFFECEFF1), Color(0xFFB0BEC5))),
                Icons.Default.Grain,
                Color(0xFF455A64)
            )
        }
        nameLower.contains("sut") || nameLower.contains("milk") || nameLower.contains("qatiq") -> {
            Triple(
                Brush.linearGradient(listOf(Color(0xFFE1F5FE), Color(0xFF81D4FA))),
                Icons.Default.LocalCafe,
                Color(0xFF1976D2)
            )
        }
        nameLower.contains("pomidor") || nameLower.contains("tomato") || nameLower.contains("olma") -> {
            Triple(
                Brush.linearGradient(listOf(Color(0xFFFFEBEE), Color(0xFFEF9A9A))),
                Icons.Default.Eco,
                Color(0xFFD32F2F)
            )
        }
        nameLower.contains("kartoshka") || nameLower.contains("potato") || nameLower.contains("sabzi") -> {
            Triple(
                Brush.linearGradient(listOf(Color(0xFFEFEBE9), Color(0xFFBCAAA4))),
                Icons.Default.Spa,
                Color(0xFF5D4037)
            )
        }
        else -> {
            Triple(
                Brush.linearGradient(listOf(Color(0xFFE0F2F1), Color(0xFF80CBC4))),
                Icons.Default.Inventory2,
                Color(0xFF00796B)
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(bgGradient, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Mahsulot",
            tint = iconColor,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

// --- Order Status Pill ---
@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "Kutilmoqda" -> Triple(Color(0xFFFFF3E0), OrangeAccent, "Kutilmoqda ⏳")
        "Yo'lda" -> Triple(Color(0xFFE1F5FE), BlueAccent, "Yo'lda 🚚")
        "Yetkazildi" -> Triple(Color(0xE8F5E9), GreenAccent, "Yetkazildi ✅")
        else -> Triple(Color(0xFFECEFF1), Color(0xFF37474F), status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// --- Currency Formatter ---
fun formatPrice(amount: Double): String {
    val integerPart = amount.toLong()
    val formatted = String.format("%,d", integerPart).replace(',', ' ')
    return "$formatted so'm"
}

// --- Beautiful Header Hero Banner ---
@Composable
fun HeroBanner(
    title: String,
    subtitle: String,
    gradient: Brush = Brush.linearGradient(listOf(TealPrimary, TealSecondary))
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(gradient)
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Subtle background decoration
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.maxDimension * 0.4f,
                center = Offset(size.width * 0.9f, size.height * 0.1f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = size.maxDimension * 0.2f,
                center = Offset(size.width * 0.1f, size.height * 0.9f)
            )
        }

        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }
    }
}

// --- Interactive District Maps Picker (Simulated) ---
@Composable
fun InteractiveMapPicker(
    selectedLat: Double,
    selectedLng: Double,
    onLocationSelected: (Double, Double, String) -> Unit
) {
    // List of 8 simulated districts in Tashkent/Tuman region representing shop locations
    val regions = remember {
        listOf(
            SimulatedRegion("Yunusobod tumani", 41.3644, 69.2847, Offset(200f, 120f), Color(0xFFE0F2F1)),
            SimulatedRegion("Chilonzor tumani", 41.2728, 69.1911, Offset(110f, 260f), Color(0xFFFFF3E0)),
            SimulatedRegion("Uchtepa tumani", 41.2989, 69.1764, Offset(80f, 190f), Color(0xFFF3E5F5)),
            SimulatedRegion("Yashnobod tumani", 41.2995, 69.3243, Offset(310f, 240f), Color(0xFFE8F5E9)),
            SimulatedRegion("Sergeli tumani", 41.2212, 69.2144, Offset(150f, 340f), Color(0xFFE1F5FE)),
            SimulatedRegion("Mirzo Ulug'bek tumani", 41.3323, 69.3456, Offset(290f, 110f), Color(0xFFFCE4EC)),
            SimulatedRegion("Yakkasaroy tumani", 41.2858, 69.2559, Offset(200f, 250f), Color(0xFFFFFDE7)),
            SimulatedRegion("Olmazor tumani", 41.3533, 69.2201, Offset(120f, 100f), Color(0xFFECEFF1))
        )
    }

    var activeRegion by remember {
        mutableStateOf(regions.find { it.name == "Yunusobod tumani" } ?: regions.first())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📍 Yetkazib berish hududini tanlang",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TealPrimary
            )
            Text(
                text = "Akangiz yuk tarqatadigan tumanlar xaritasi. Tanlash uchun tumanga bosing:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Canvas drawing simulated regional layout map!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFFE0F2F1).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, TealPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Find closest region by offset distance
                            val closest = regions.minByOrNull { region ->
                                val dx = region.offset.x - offset.x
                                val dy = region.offset.y - offset.y
                                dx * dx + dy * dy
                            }
                            if (closest != null) {
                                activeRegion = closest
                                onLocationSelected(closest.lat, closest.lng, closest.name)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val mapWidth = size.width
                    val mapHeight = size.height

                    // Draw connecting delivery roads
                    val roadPaint = Stroke(width = 4f, cap = StrokeCap.Round)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, mapHeight * 0.5f),
                        end = Offset(mapWidth, mapHeight * 0.5f),
                        pathEffect = null,
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(mapWidth * 0.5f, 0f),
                        end = Offset(mapWidth * 0.5f, mapHeight),
                        pathEffect = null,
                        strokeWidth = 6f
                    )

                    // Draw districts boundaries as soft blobs
                    regions.forEach { region ->
                        // Scale offset according to dynamic canvas size
                        val scaleX = mapWidth / 400f
                        val scaleY = mapHeight / 400f
                        val actualOffset = Offset(region.offset.x * scaleX, region.offset.y * scaleY)

                        drawCircle(
                            color = region.color.copy(alpha = if (region.name == activeRegion.name) 0.8f else 0.4f),
                            radius = 42.dp.toPx(),
                            center = actualOffset
                        )

                        // Draw boundary border
                        drawCircle(
                            color = if (region.name == activeRegion.name) TealPrimary else Color.LightGray,
                            radius = 42.dp.toPx(),
                            center = actualOffset,
                            style = Stroke(width = if (region.name == activeRegion.name) 4f else 2f)
                        )
                    }

                    // Draw labels
                    regions.forEach { region ->
                        val scaleX = mapWidth / 400f
                        val scaleY = mapHeight / 400f
                        val actualOffset = Offset(region.offset.x * scaleX, region.offset.y * scaleY)

                        // Draw a dot for center
                        drawCircle(
                            color = if (region.name == activeRegion.name) TealPrimary else Color.DarkGray,
                            radius = 5.dp.toPx(),
                            center = actualOffset
                        )
                    }
                }

                // Overlay interactive textual tags
                regions.forEach { region ->
                    val density = LocalDensity.current
                    val scaleX = 400f
                    val scaleY = 400f
                    
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (region.offset.x - 50f).dp,
                                y = (region.offset.y - 25f).dp
                            )
                            .shadow(1.dp, RoundedCornerShape(4.dp))
                            .background(
                                if (region.name == activeRegion.name) TealPrimary else Color.White,
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                if (region.name == activeRegion.name) TealPrimary else Color.LightGray,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = region.name.replace(" tumani", ""),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (region.name == activeRegion.name) Color.White else Color.Black
                        )
                    }
                }

                // Highlight Selected Pin
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "📍 Koordinatalar: ${String.format("%.4f", activeRegion.lat)}, ${String.format("%.4f", activeRegion.lng)}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Selected region card details
            Surface(
                color = TealPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Tanlangan hudud",
                        tint = TealPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = activeRegion.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TealPrimary
                        )
                        Text(
                            text = "Geolokatsiya ma'lumotlari yuk mashinasi GPS tizimiga yuboriladi.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

data class SimulatedRegion(
    val name: String,
    val lat: Double,
    val lng: Double,
    val offset: Offset, // Relative coordinates for drawing on 400x400 map
    val color: Color
)
