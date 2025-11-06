package com.example.datossinmvvm.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestión de Usuarios - ROOM",
                        fontWeight = FontWeight.Bold
                    )
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
            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Agregar Usuario")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val data = getUsers(userDao)
                            usersList = data
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Listar Usuarios")
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            EliminarUltimoUsuario(userDao)
                            val data = getUsers(userDao)
                            usersList = data
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Eliminar Último")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Users
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
        }
    }
}

// Funciones de Base de Datos
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