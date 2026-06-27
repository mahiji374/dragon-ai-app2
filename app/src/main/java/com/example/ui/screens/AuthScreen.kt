package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.DragonViewModel
import com.example.ui.utils.LanguageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: DragonViewModel,
    langCode: String,
    onAuthSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(gameStateCurrent()) {
        viewModel.authState.value = null // reset prior error messages
    }

    LaunchedEffect(gameStateSuccess(authState)) {
        if (authState == "SUCCESS") {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E17)) // Deep majestic midnight black
    ) {
        // Futuristic abstract gold & dark cyber ambient background shape
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1B18).copy(alpha = 0.8f),
                            Color(0xFF0F0E17)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Premium Brand Logo Element representing Dragon AI
            Image(
                painter = painterResource(id = R.drawable.img_dragon_logo),
                contentDescription = "Dragon AI Logo",
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .align(Alignment.CenterHorizontally)
                    .testTag("brand_logo_image"),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DRAGON AI",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.primary, // Gorgeous gold
                textAlign = TextAlign.Center
            )

            Text(
                text = "PRIVATE GENERATIVE VIDEO PLATFORM",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Private Portal Access Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF181622) // Luxury dark gray surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SECURE PORTAL LOGIN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = "Enter whitelisted credentials to access video synthesis tools.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Secured Email") },
                        placeholder = { Text("operator@dragon.ai") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Access Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (authState != null && authState != "LOADING" && authState != "SUCCESS") {
                        Text(
                            text = authState ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Main Sign In Trigger Button
                    Button(
                        onClick = {
                            viewModel.registerOrLogin(email)
                        },
                        enabled = authState != "LOADING",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        )
                    ) {
                        if (authState == "LOADING") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "DECRYPT & ACCESS PORTAL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Whitelisted accounts helper display for convenience
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1B24).copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "🔒 PORTAL WHITE-LIST PRE-APPROVAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Only system-approved operator emails can gain clearance. Try logging in with:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "khawajashahidabbas471@gmail.com",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "admin@dragon.ai",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Any pre-configured email ending in @dragon.ai",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Helpers to cleanly isolate state triggers
@Composable
private fun gameStateCurrent(): Int = 0

@Composable
private fun gameStateSuccess(state: String?): String? = state
