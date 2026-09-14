package com.ailisi.jinianri.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** 图标名 -> ImageVector（分类选择器用） */
val CategoryIconOptions: List<Pair<String, ImageVector>> = listOf(
    "category" to Icons.Filled.Category,
    "cake" to Icons.Filled.Cake,
    "favorite" to Icons.Filled.Favorite,
    "school" to Icons.Filled.School,
    "work" to Icons.Filled.Work,
    "star" to Icons.Filled.Star,
    "celebration" to Icons.Filled.Celebration,
    "home" to Icons.Filled.Home,
    "flight" to Icons.Filled.Flight,
    "pets" to Icons.Filled.Pets,
    "restaurant" to Icons.Filled.Restaurant,
    "shopping" to Icons.Filled.ShoppingBag,
    "sports" to Icons.Filled.SportsEsports,
    "music" to Icons.Filled.MusicNote,
    "hospital" to Icons.Filled.LocalHospital,
    "flag" to Icons.Filled.Flag,
)

fun iconOf(name: String): ImageVector =
    CategoryIconOptions.firstOrNull { it.first == name }?.second ?: Icons.Filled.Category

/** 颜色选择（分类编辑用） */
val CategoryColorOptions: List<Long> = listOf(
    0xFFE91E63, 0xFF6750A4, 0xFFFF9800, 0xFF4CAF50,
    0xFF2196F3, 0xFF009688, 0xFFFF5722, 0xFF9C27B0,
    0xFF795548, 0xFF607D8B, 0xFFF44336, 0xFF3F51B5,
)

fun colorOf(argb: Long): Color = Color(argb.toInt())
