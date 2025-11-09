package com.example.datossinmvvm.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

sealed class Screen(val title: String) {
    object Users : Screen("Usuarios")
    object Products : Screen("Productos")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Usuarios"
                )
            },
            label = { Text("Usuarios") },
            selected = currentScreen is Screen.Users,
            onClick = { onScreenSelected(Screen.Users) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Productos"
                )
            },
            label = { Text("Productos") },
            selected = currentScreen is Screen.Products,
            onClick = { onScreenSelected(Screen.Products) }
        )
    }
}