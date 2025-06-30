package com.example.gestprom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestprom.R
import com.example.gestprom.ui.theme.AppTheme
import com.example.gestprom.viewmodels.AuthState
import com.example.gestprom.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenRegistro(
    authViewModel: AuthViewModel = viewModel(),
    onAtrasClick: () -> Unit = {},
    onRegistroSuccess: () -> Unit = {}
) {
    var matricula by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()

    // Maneja cambios de estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onRegistroSuccess()
            }
            is AuthState.Error -> {
                // El error se muestra en la UI
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear Cuenta",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAtrasClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = AppTheme.colors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.BackgroundPrimary
                )
            )
        },
        containerColor = AppTheme.colors.BackgroundPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.imagenlogo),
                contentDescription = "Logo GestProm",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 20.dp)
            )

            // Título
            Text(
                text = "GestProm",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Crea tu cuenta nueva",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            // Campo de Nombre Completo
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    authViewModel.clearError()
                },
                label = {
                    Text(
                        "Nombre Completo",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Nombre",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AppTheme.colors.ButtonPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )

            // Campo de Matrícula
            OutlinedTextField(
                value = matricula,
                onValueChange = {
                    matricula = it
                    authViewModel.clearError()
                },
                label = {
                    Text(
                        "Matrícula",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Matrícula",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AppTheme.colors.ButtonPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )

            // Campo de Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    authViewModel.clearError()
                },
                label = {
                    Text(
                        "Correo Electrónico",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AppTheme.colors.ButtonPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            // Campo de Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    authViewModel.clearError()
                },
                label = {
                    Text(
                        "Contraseña",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Contraseña",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Text(
                            text = if (passwordVisible) "Ocultar" else "Mostrar",
                            color = AppTheme.colors.ButtonPrimary,
                            fontSize = 12.sp
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AppTheme.colors.ButtonPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Campo de Confirmar Contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    authViewModel.clearError()
                },
                label = {
                    Text(
                        "Confirmar Contraseña",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirmar Contraseña",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    TextButton(
                        onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                    ) {
                        Text(
                            text = if (confirmPasswordVisible) "Ocultar" else "Mostrar",
                            color = AppTheme.colors.ButtonPrimary,
                            fontSize = 12.sp
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AppTheme.colors.ButtonPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Mensaje de error
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Botón de Registro
            Button(
                onClick = {
                    // Validaciones
                    when {
                        nombre.isBlank() -> {
                            // Show validation error
                        }
                        matricula.length < 6 -> {
                            // Show validation error
                        }
                        email.isBlank() -> {
                            // Show validation error
                        }
                        !email.contains("@") || !email.contains(".") -> {
                            // Show validation error
                        }
                        password.length < 6 -> {
                            // Show validation error
                        }
                        password != confirmPassword -> {
                            // Show validation error
                        }
                        else -> {
                            authViewModel.register(nombre, matricula, email, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = authState !is AuthState.Loading && 
                         nombre.isNotBlank() && 
                         matricula.length >= 6 && 
                         email.isNotBlank() && 
                         email.contains("@") && 
                         email.contains(".") && 
                         password.length >= 6 && 
                         password == confirmPassword
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "CREAR CUENTA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Texto informativo
            Text(
                text = "Al crear una cuenta, aceptas nuestros términos y condiciones",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenRegistroPreview() {
    ScreenRegistro()
}