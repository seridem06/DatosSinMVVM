package com.example.datossinmvvm.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datossinmvvm.data.User
import com.example.datossinmvvm.data.UserDatabase
import com.example.datossinmvvm.data.UserDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val database = remember { UserDatabase.getDatabase(context) }
    val userDao = remember { database.userDao() }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var usersList by remember { mutableStateOf("") }
    var showUserForm by remember { mutableStateOf(false) }
    var showUserList by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestión de Usuarios - ROOM",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón para Agregar Usuario
                    IconButton(
                        onClick = {
                            showUserForm = true
                            showUserList = false
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
                    }

                    // Botón para Listar Usuarios
                    IconButton(
                        onClick = {
                            showUserList = true
                            showUserForm = false
                            coroutineScope.launch {
                                val data = getUsers(userDao)
                                usersList = data
                            }
                        }
                    ) {
                        Icon(Icons.Default.List, contentDescription = "Listar Usuarios")
                    }

                    // Menú desplegable para más opciones
                    Box {
                        IconButton(
                            onClick = { expandedMenu = true }
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }

                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Eliminar Último Usuario") },
                                onClick = {
                                    expandedMenu = false
                                    coroutineScope.launch {
                                        EliminarUltimoUsuario(userDao)
                                        val data = getUsers(userDao)
                                        usersList = data
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Limpiar Lista") },
                                onClick = {
                                    expandedMenu = false
                                    usersList = ""
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mostrar formulario para agregar usuarios
            if (showUserForm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Agregar Nuevo Usuario",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Input Fields
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Apellido") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (firstName.isNotBlank() && lastName.isNotBlank()) {
                                    coroutineScope.launch {
                                        AgregarUsuario(
                                            user = User(0, firstName, lastName),
                                            dao = userDao
                                        )
                                        firstName = ""
                                        lastName = ""
                                        // Actualizar la lista después de agregar
                                        val data = getUsers(userDao)
                                        usersList = data
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = firstName.isNotBlank() && lastName.isNotBlank()
                        ) {
                            Text("Guardar Usuario")
                        }
                    }
                }
            }

            // Mostrar lista de usuarios
            if (showUserList || usersList.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Usuarios Registrados:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = usersList.ifEmpty { "No hay usuarios registrados" },
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Mensaje cuando no hay nada seleccionado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Selecciona una opción del menú superior",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Usa los iconos de la barra superior para:\n• Agregar usuarios\n• Listar usuarios\n• Más opciones",
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Funciones de Base de Datos (se mantienen igual)
suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
        Log.i("User", "Usuario agregado exitosamente")
    } catch (e: Exception) {
        Log.e("User", "Error al agregar usuario: ${e.message}")
    }
}

suspend fun getUsers(dao: UserDao): String {
    return try {
        val users = dao.getAll()
        if (users.isEmpty()) {
            "No hay usuarios registrados"
        } else {
            users.joinToString("\n") { user ->
                "ID: ${user.uid} - ${user.firstName} ${user.lastName}"
            }
        }
    } catch (e: Exception) {
        "Error al obtener usuarios: ${e.message}"
    }
}

suspend fun EliminarUltimoUsuario(dao: UserDao) {
    try {
        dao.deleteLastUser()
        Log.i("User", "Último usuario eliminado exitosamente")
    } catch (e: Exception) {
        Log.e("User", "Error al eliminar último usuario: ${e.message}")
    }
}