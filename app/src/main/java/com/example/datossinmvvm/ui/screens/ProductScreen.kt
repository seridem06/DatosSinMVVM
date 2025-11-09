package com.example.datossinmvvm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datossinmvvm.data.Product
import com.example.datossinmvvm.data.UserDatabase
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen() {
    val context = LocalContext.current
    val database = remember { UserDatabase.getDatabase(context) }
    val productDao = remember { database.productDao() }

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Form fields
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Cargar productos al iniciar
    LaunchedEffect(Unit) {
        productDao.getAllProducts().collect { productList ->
            products = productList
        }
    }

    // Función para formatear precio en Soles
    fun formatPrice(price: Double): String {
        val decimalFormat = DecimalFormat("#,##0.00")
        return "S/. ${decimalFormat.format(price)}"
    }

    // Función para limpiar el formulario
    fun clearForm() {
        productName = ""
        productDescription = ""
        productPrice = ""
        productQuantity = ""
        editingProduct = null
    }

    // Función para guardar producto
    fun saveProduct() {
        if (productName.isBlank() || productPrice.isBlank() || productQuantity.isBlank()) {
            return
        }

        coroutineScope.launch {
            try {
                val price = productPrice.toDoubleOrNull() ?: 0.0
                val quantity = productQuantity.toIntOrNull() ?: 0

                val product = Product(
                    name = productName,
                    description = productDescription,
                    price = price,
                    quantity = quantity
                )

                if (editingProduct != null) {
                    // Actualizar producto existente
                    productDao.updateProduct(product.copy(id = editingProduct!!.id))
                } else {
                    // Insertar nuevo producto
                    productDao.insertProduct(product)
                }

                clearForm()
                showDialog = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestión de Productos - CRUD",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            clearForm()
                            showDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clearForm()
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (products.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay productos registrados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Presiona el botón + para agregar un nuevo producto",
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(products) { product ->
                        ProductItem(
                            product = product,
                            onEdit = {
                                editingProduct = product
                                productName = product.name
                                productDescription = product.description
                                productPrice = product.price.toString()
                                productQuantity = product.quantity.toString()
                                showDialog = true
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    productDao.deleteProduct(product)
                                }
                            },
                            formatPrice = ::formatPrice
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Dialog para agregar/editar producto
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    clearForm()
                },
                title = {
                    Text(
                        if (editingProduct != null) "Editar Producto" else "Agregar Producto",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Nombre del Producto *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = productDescription,
                            onValueChange = { productDescription = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = productPrice,
                            onValueChange = {
                                // Validar que solo se ingresen números y punto decimal
                                if (it.matches(Regex("^\\d*\\.?\\d*$")) || it.isEmpty()) {
                                    productPrice = it
                                }
                            },
                            label = { Text("Precio (S/.) *") },
                            placeholder = { Text("0.00") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = productQuantity,
                            onValueChange = {
                                // Validar que solo se ingresen números
                                if (it.matches(Regex("^\\d*$")) || it.isEmpty()) {
                                    productQuantity = it
                                }
                            },
                            label = { Text("Cantidad *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { saveProduct() },
                        enabled = productName.isNotBlank() &&
                                productPrice.isNotBlank() &&
                                productQuantity.isNotBlank()
                    ) {
                        Text(if (editingProduct != null) "Actualizar" else "Guardar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            clearForm()
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductItem(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    formatPrice: (Double) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatPrice(product.price),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (product.description.isNotBlank()) {
                Text(
                    text = product.description,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Cantidad: ${product.quantity}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}