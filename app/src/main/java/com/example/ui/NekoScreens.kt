package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. REUSABLE GRADIENT BACKGROUNDS AND COMPONENT SHAPES
// ==========================================

@Composable
fun GlowGradientBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCosmicBlack)
            .drawBehind {
                // Drawing dynamic top-left and bottom-right aesthetic glowing circles
                drawCircle(
                    color = NeonPurple.copy(alpha = 0.15f),
                    radius = 450f,
                    center = androidx.compose.ui.geometry.Offset(0f, 0f)
                )
                drawCircle(
                    color = FlameOrange.copy(alpha = 0.12f),
                    radius = 400f,
                    center = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.8f)
                )
            }
    ) {
        content()
    }
}

// Custom Glassmorphic Card decoration helper
fun Modifier.glassCard(): Modifier = this
    .background(SurfaceGlassDark.copy(alpha = 0.75f), shape = RoundedCornerShape(16.dp))
    .border(1.dp, BorderPurple.copy(alpha = 0.35f), shape = RoundedCornerShape(16.dp))

@Composable
fun StarRatingBar(rating: Double, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating Star",
            tint = GoldStar,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f", rating),
            color = TextLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Generates high quality stylized gradient placeholders for anime assets
@Composable
fun AnimePlaceholderImage(
    index: Int,
    title: String,
    modifier: Modifier = Modifier,
    isBanner: Boolean = false
) {
    val gradients = listOf(
        Brush.horizontalGradient(listOf(Color(0xFF3B0061), Color(0xFFFF5200))),
        Brush.verticalGradient(listOf(Color(0xFF0D0317), Color(0xFF9E00FF))),
        Brush.radialGradient(listOf(Color(0xFFFF4500), Color(0xFF4B0082))),
        Brush.horizontalGradient(listOf(Color(0xFF7A00FF), Color(0xFF140D2B))),
        Brush.verticalGradient(listOf(Color(0xFF2E0F38), Color(0xFFFF8C00))),
        Brush.linearGradient(listOf(Color(0xFF4B0082), Color(0xFF000000)))
    )
    val chosenBrush = gradients[index % gradients.size]

    Box(
        modifier = modifier
            .background(chosenBrush)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Overlay a futuristic glowing grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val step = if (isBanner) 40f else 30f
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, size.height)
                        )
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(size.width, y)
                        )
                        y += step
                    }
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isBanner) Icons.Default.Tv else Icons.Default.MovieFilter,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(if (isBanner) 36.dp else 24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = if (isBanner) 16.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "NEKOSTREAM ULTRA HD",
                color = FlameOrange.copy(alpha = 0.9f),
                fontSize = 8.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ==========================================
// 2. MAIN NAVIGATOR ENTRY POINT
// ==========================================

@Composable
fun NekoStreamApp(viewModel: NekoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    AnimateTransitionScreen(currentScreen) { screen ->
        when (screen) {
            "Splash" -> SplashScreen(
                onSplashFinished = {
                    viewModel.navigateTo("AuthLogin")
                }
            )
            "AuthLogin" -> LoginScreen(viewModel)
            "AuthSignup" -> SignupScreen(viewModel)
            "Hub" -> MainHub(viewModel)
            "Details" -> DetailsScreen(viewModel)
            "Player" -> PlayerScreen(viewModel)
        }
    }
}

@Composable
fun AnimateTransitionScreen(
    currentScreen: String,
    content: @Composable (String) -> Unit
) {
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        },
        label = "AppScreenTransition"
    ) { targetState ->
        content(targetState)
    }
}

// ==========================================
// 3. SPLASH SCREEN (Premium Glow)
// ==========================================

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2200) // Beautiful splash hold time
        onSplashFinished()
    }

    GlowGradientBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scaleAnim(scaleAnim.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Custom Geometric Neon Canvas Icon instead of empty raw SVG
                Canvas(modifier = Modifier.size(110.dp)) {
                    val pathWidth = size.width
                    val pathHeight = size.height

                    // Draw outer futuristic neon glowing shield
                    drawRoundRect(
                        color = NeonPurple,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(pathWidth, pathHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                    )

                    // Draw glowing inner core and anime fire energy lines
                    drawCircle(
                        color = FlameOrange,
                        radius = 20f,
                        center = androidx.compose.ui.geometry.Offset(pathWidth / 2f, pathHeight / 2f)
                    )

                    // Cute stylized anime cat ears on top
                    val earPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(10f, 0f)
                        lineTo(35f, -30f)
                        lineTo(60f, 0f)
                        close()
                    }
                    drawPath(earPath, color = NeonPurple)

                    val rightEarPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(pathWidth - 60f, 0f)
                        lineTo(pathWidth - 35f, -30f)
                        lineTo(pathWidth - 10f, 0f)
                        close()
                    }
                    drawPath(rightEarPath, color = NeonPurple)
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = FlameOrange,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NekoStream",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextLight,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.testTag("app_logo")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "YOUR PREMIUM ANIME MULTIVERSE",
                color = FlameOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = NeonPurple,
                strokeWidth = 3.dp,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

// Helper to scale composable elements
fun Modifier.scaleAnim(scale: Float): Modifier = this.drawBehind {
    val matrix = android.graphics.Matrix()
    matrix.setScale(scale, scale, size.width / 2f, size.height / 2f)
}

// ==========================================
// 4. AUTHENTICATION SCREENS (Cinematic Dark Glow)
// ==========================================

@Composable
fun LoginScreen(viewModel: NekoViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    GlowGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Brand Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MovieFilter,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NekoStream",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextLight
                )
            }

            Text(
                text = "Unlock Instant Cinematic Anime Streams",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Auth Login Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WELCOME BACK, OTAKU!",
                        color = FlameOrange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Profile Nickname") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurple) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderPurple,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = SurfaceGlassDark,
                            unfocusedContainerColor = SurfaceGlassDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonPurple) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderPurple,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = SurfaceGlassDark,
                            unfocusedContainerColor = SurfaceGlassDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurple) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderPurple,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = SurfaceGlassDark,
                            unfocusedContainerColor = SurfaceGlassDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Confirm Button
                    Button(
                        onClick = {
                            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Nya! Please fill out all parameters first!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.loginUser(username, email)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        Text("SIGN IN", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Guest Login Option
                    OutlinedButton(
                        onClick = { viewModel.loginAsGuest() },
                        border = BorderStroke(1.dp, FlameOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("ENTER GUEST OTAKU MODE (NO SIGNUP COMPULSORY)", color = FlameOrange, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mock OAuth logins
            Text(text = "Or authorize via secure 1-tap keys", color = TextMuted, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        viewModel.loginUser("GoogleMaster", "google@nekostream.com")
                        Toast.makeText(context, "Authenticated using Google API!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Sign-In", color = Color.White)
                }

                Button(
                    onClick = {
                        viewModel.loginUser("FirebaseGamer", "firebase@nekostream.com")
                        Toast.makeText(context, "Authenticated using Firebase Auth API!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB2B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Firebase Auth", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { viewModel.navigateTo("AuthSignup") }) {
                Text("Don't have an Otaku account? Register instead", color = FlameOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SignupScreen(viewModel: NekoViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    GlowGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Register Otaku Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextLight
            )

            Text(
                text = "Join global streaming realms instantly",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEW OTAKU REGISTRATION",
                        color = FlameOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Choose Nickname") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurple) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderPurple,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = SurfaceGlassDark,
                            unfocusedContainerColor = SurfaceGlassDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Active Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonPurple) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderPurple,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = SurfaceGlassDark,
                            unfocusedContainerColor = SurfaceGlassDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Secure Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurple) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderPurple,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = SurfaceGlassDark,
                            unfocusedContainerColor = SurfaceGlassDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it },
                        label = { Text("Optional Referral / Promo Code") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = FlameOrange) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = BorderPurple,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = SurfaceGlassDark,
                            unfocusedContainerColor = SurfaceGlassDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Nya! Please supply all key metrics!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.loginUser(username, email)
                                Toast.makeText(context, "Welcome on board! Saved in database.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("PROCEED AND CONNECT", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { viewModel.navigateTo("AuthLogin") }) {
                Text("Click here to go back to Login", color = FlameOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 5. MAIN HUB SCREEN (With Bottom Bar Nav)
// ==========================================

@Composable
fun MainHub(viewModel: NekoViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val username = when (val state = authState) {
        is AuthState.Authenticated -> state.username
        else -> "Guest"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceGlassDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("Home", color = if (activeTab == 0) FlameOrange else TextMuted, fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = if (activeTab == 0) FlameOrange else TextMuted) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = BorderPurple)
                    )

                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("Search", color = if (activeTab == 1) FlameOrange else TextMuted, fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = if (activeTab == 1) HighlightSecondary() else TextMuted) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = BorderPurple)
                    )

                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("NekoAI", color = if (activeTab == 2) FlameOrange else TextMuted, fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Recommendations", tint = if (activeTab == 2) FlameOrange else TextMuted) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = BorderPurple)
                    )

                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        label = { Text("Profile", color = if (activeTab == 3) FlameOrange else TextMuted, fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = if (activeTab == 3) FlameOrange else TextMuted) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = BorderPurple)
                    )

                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        label = { Text("Admin", color = if (activeTab == 4) FlameOrange else TextMuted, fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Admin Area", tint = if (activeTab == 4) FlameOrange else TextMuted) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = BorderPurple)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DeepCosmicBlack)
            ) {
                when (activeTab) {
                    0 -> HomeTabScreen(viewModel, username)
                    1 -> SearchTabScreen(viewModel)
                    2 -> NekoAiTabScreen(viewModel)
                    3 -> ProfileTabScreen(viewModel, username)
                    4 -> AdminTabScreen(viewModel)
                }
            }
        }
    }
}

fun HighlightSecondary(): Color = FlameOrange

// ==========================================
// 5A. HOME TAB (Carousel Banner, Slider Columns)
// ==========================================

@Composable
fun HomeTabScreen(viewModel: NekoViewModel, username: String) {
    val trending by viewModel.trendingAnimes.collectAsStateWithLifecycle()
    val popular by viewModel.popularAnimes.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAddedAnimes.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val allList by viewModel.allAnimes.collectAsStateWithLifecycle()

    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    var currentAlertIndex by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(6000)
            if (alerts.isNotEmpty()) {
                currentAlertIndex = (currentAlertIndex + 1) % alerts.size
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Header with Profile Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Konnichiwa, $username",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Grab your popcorn and stream Nya! 🍿",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Profile Avatar Indicator
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonPurple)
                        .border(1.5.dp, FlameOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐱", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trending Alerts Box
            if (alerts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceGlassDark, RoundedCornerShape(8.dp))
                        .border(0.5.dp, FlameOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alerts[currentAlertIndex],
                        color = FlameOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Horizontal Hero Banner Carousel - Clicking plays immediately (e.g., Solo Leveling)
        item {
            Text(
                text = "FEATURED SENSATION",
                color = TextLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Let's choose the top anime item if available, or build a gorgeous custom slider
            val featuredAnime = allList.firstOrNull()
            if (featuredAnime != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, FlameOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.selectAnime(featuredAnime.id) }
                ) {
                    // Full-size rich background image
                    AnimePlaceholderImage(
                        index = featuredAnime.bannerIndex,
                        title = featuredAnime.title,
                        modifier = Modifier.fillMaxSize(),
                        isBanner = true
                    )

                    // Vertical bottom fade overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(FlameOrange, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("HOT CRITICALS", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            StarRatingBar(featuredAnime.rating)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = featuredAnime.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = featuredAnime.genres,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(SurfaceGlassDark, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading featured cosmic logs...", color = TextMuted, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Continue Watching Column (Progress Indicators)
        if (continueWatching.isNotEmpty()) {
            item {
                Text(
                    text = "CONTINUE WATCHING",
                    color = FlameOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    items(continueWatching) { cw ->
                        val relatedAnime = allList.find { it.id == cw.animeId }
                        if (relatedAnime != null) {
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable {
                                        viewModel.selectAnime(cw.animeId)
                                    },
                                colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
                                border = BorderStroke(0.5.dp, BorderPurple)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AnimePlaceholderImage(
                                        index = relatedAnime.posterIndex,
                                        title = relatedAnime.title,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = relatedAnime.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = "Ep ${cw.episodeNumber} in progress",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val ratio = cw.progressSeconds.toFloat() / cw.durationSeconds
                                        LinearProgressIndicator(
                                            progress = { ratio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp),
                                            color = FlameOrange,
                                            trackColor = BorderPurple.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trending Sections
        item {
            AnimeRowSlider(
                title = "TRENDING SENSATIONS ⭐",
                animes = trending,
                onAnimeClicked = { viewModel.selectAnime(it.id) }
            )
        }

        // Popular List
        item {
            AnimeRowSlider(
                title = "POPULAR IN JAPAN 🔥",
                animes = popular,
                onAnimeClicked = { viewModel.selectAnime(it.id) }
            )
        }

        // Recently Added List
        item {
            AnimeRowSlider(
                title = "RECENT RELEASES 🆕",
                animes = recentlyAdded,
                onAnimeClicked = { viewModel.selectAnime(it.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AnimeRowSlider(
    title: String,
    animes: List<Anime>,
    onAnimeClicked: (Anime) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            color = TextLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (animes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(SurfaceGlassDark, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Prepopulating items...", color = TextMuted, fontSize = 11.sp)
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(animes) { anime ->
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, BorderPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { onAnimeClicked(anime) }
                    ) {
                        Column {
                            AnimePlaceholderImage(
                                index = anime.posterIndex,
                                title = anime.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            )

                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = anime.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StarRatingBar(anime.rating)
                                    Text(
                                        text = "${anime.views / 1000}k views",
                                        color = TextMuted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5B. SEARCH TAB (Full Filters by Genres)
// ==========================================

@Composable
fun SearchTabScreen(viewModel: NekoViewModel) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val rawResults by viewModel.filteredAnimes.collectAsStateWithLifecycle()
    val activeGenreFilter by viewModel.selectedGenreFilter.collectAsStateWithLifecycle()

    val genres = listOf("All", "Action", "Sci-Fi", "Supernatural", "Slice of Life", "Romance", "Shonen", "Fantasy", "Cyberpunk")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Explore Neko Databases",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Custom Glowing Neon Search Input
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search title, keywords or description...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FlameOrange) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextLight)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FlameOrange,
                unfocusedBorderColor = BorderPurple,
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight,
                focusedContainerColor = SurfaceGlassDark,
                unfocusedContainerColor = SurfaceGlassDark
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("search_anime_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontally Scrolling Genre Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(genres) { g ->
                val isSelected = (activeGenreFilter == g) || (activeGenreFilter == null && g == "All")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) FlameOrange else SurfaceGlassDark)
                        .border(1.dp, if (isSelected) FlameOrange else BorderPurple, RoundedCornerShape(20.dp))
                        .clickable {
                            if (g == "All") viewModel.setGenreFilter(null) else viewModel.setGenreFilter(g)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = g,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Results List
        if (rawResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .glassCard(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐱 Nya~!", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching records found. Let's explore alternatives!",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(rawResults) { anime ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassDark)
                            .border(0.5.dp, BorderPurple, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectAnime(anime.id) }
                    ) {
                        Column {
                            AnimePlaceholderImage(
                                index = anime.posterIndex,
                                title = anime.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            )
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = anime.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StarRatingBar(anime.rating)
                                    Text(
                                        text = "${anime.views / 1000}k",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5C. NEKOAI TAB (Dynamic recommendations powered by Gemini REST)
// ==========================================

@Composable
fun NekoAiTabScreen(viewModel: NekoViewModel) {
    val messages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.aiGenerating.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }
    val keyboardController = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Hold visual auto-scrolling
    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Glowing Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FlameOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NekoAI Studio recommendations",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powered by Google Gemini 3.5 Flash",
                        color = FlameOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Session", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Conversation Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .glassCard()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                for (msg in messages) {
                    val isNeko = msg.sender == "neko"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isNeko) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNeko) BorderPurple.copy(alpha = 0.5f) else FlameOrange.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isNeko) 4.dp else 16.dp,
                                bottomEnd = if (isNeko) 16.dp else 4.dp
                            ),
                            border = BorderStroke(1.dp, if (isNeko) NeonPurple.copy(alpha = 0.5f) else FlameOrange.copy(alpha = 0.4f)),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isNeko) "🐱 NEKOAI mascot" else "👤 YOU",
                                    fontSize = 10.sp,
                                    color = if (isNeko) NeonPurple else FlameOrange,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    text = msg.text,
                                    color = TextLight,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                if (isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = FlameOrange,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NekoAI is scanning recommendations...",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Line
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask NekoAI: e.g. 'I want intense cyberpunk action'...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlameOrange,
                    unfocusedBorderColor = BorderPurple,
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedContainerColor = SurfaceGlassDark,
                    unfocusedContainerColor = SurfaceGlassDark
                ),
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_reco_prompt")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank() && !isGenerating) {
                        viewModel.sendAiChatQuery(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isGenerating) SurfaceGlassDark else NeonPurple)
                    .border(1.dp, FlameOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==========================================
// 5D. PROFILE TAB (Offline Downloads, History)
// ==========================================

@Composable
fun ProfileTabScreen(viewModel: NekoViewModel, username: String) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val history by viewModel.watchHistory.collectAsStateWithLifecycle()
    val allList by viewModel.allAnimes.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0: History, 1: Downloads, 2: Watchlist

    var backgroundPrefetch by remember { mutableStateOf(true) }
    var highQualityStream by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(FlameOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🦊", fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(text = username, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Rank: Legendary Admiral Otaku", color = FlameOrange, fontSize = 11.sp, fontWeight = FontWeight.Black)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = "Email: info@nekostream.com", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Tab selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val subTabs = listOf("Recents 📅", "Downloads (${downloads.size}) ⬇️", "Watchlist ⭐")
                subTabs.forEachIndexed { idx, label ->
                    val isSelected = activeSubTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonPurple else SurfaceGlassDark)
                            .border(0.5.dp, if (isSelected) FlameOrange else BorderPurple)
                            .clickable { activeSubTab = idx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Sub Tab Contents
        when (activeSubTab) {
            0 -> {
                if (history.isEmpty()) {
                    item {
                        EmptyProfileBox("Your watch history is empty. Play some episodes first!")
                    }
                } else {
                    items(history) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.selectAnime(record.animeId) },
                            colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
                            border = BorderStroke(0.5.dp, BorderPurple)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AnimePlaceholderImage(
                                    index = record.posterIndex,
                                    title = record.animeTitle,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = record.animeTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Played chapter context log", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                if (downloads.isEmpty()) {
                    item {
                        EmptyProfileBox("No offline video downloads currently. Click 'Download' inside any anime details timeline!")
                    }
                } else {
                    items(downloads) { d ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
                            border = BorderStroke(0.5.dp, BorderPurple)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (d.status == "Completed") Icons.Default.CheckCircle else Icons.Default.DownloadForOffline,
                                    contentDescription = null,
                                    tint = if (d.status == "Completed") FlameOrange else NeonPurple,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${d.animeTitle} - Episode ${d.episodeNumber}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LinearProgressIndicator(
                                            progress = { d.progress },
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(4.dp),
                                            color = FlameOrange,
                                            trackColor = BorderPurple.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (d.status == "Completed") "Completed Offline" else "Downloading: ${(d.progress * 100).toInt()}%",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteDownloadedEpisode(d.episodeId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val fList = allList.filter { a -> favorites.any { f -> f.animeId == a.id } }
                if (fList.isEmpty()) {
                    item {
                        EmptyProfileBox("No books in Watchlist. Heart any anime show to save here!")
                    }
                } else {
                    items(fList) { anime ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.selectAnime(anime.id) },
                            colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
                            border = BorderStroke(0.5.dp, BorderPurple)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AnimePlaceholderImage(
                                    index = anime.posterIndex,
                                    title = anime.title,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = anime.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = anime.genres, color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Settings Section
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SYSTEM CONFIGURATOR",
                color = FlameOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Background Episode Pre-fetch", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Increases buffer rates offline", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = backgroundPrefetch,
                            onCheckedChange = { backgroundPrefetch = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = FlameOrange, checkedTrackColor = NeonPurple)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dynamic HDR High-Fidelity Streaming", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Force 4K streams on high bandwidth", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = highQualityStream,
                            onCheckedChange = { highQualityStream = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = FlameOrange, checkedTrackColor = NeonPurple)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("PURGE SESSION & LOGOUT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProfileBox(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(SurfaceGlassDark, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = msg,
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ==========================================
// 5E. ADMIN BOARD TAB (Upload / Edit Anime Catalog)
// ==========================================

@Composable
fun AdminTabScreen(viewModel: NekoViewModel) {
    val allCatalog by viewModel.allAnimes.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var ratingStr by remember { mutableStateOf("4.8") }
    var genresInput by remember { mutableStateOf("Action, Shonen") }
    var selectedCategory by remember { mutableStateOf("Trending") }
    var episodesCountStr by remember { mutableStateOf("12") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "NekoStream Director Admin board",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manually deploy anime nodes to local Room Database",
                color = FlameOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "LAUNCH NEW ANIME VESSEL",
                        color = FlameOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Anime Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, focusedBorderColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth().testTag("admin_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Synopsis Details") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, focusedBorderColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ratingStr,
                            onValueChange = { ratingStr = it },
                            label = { Text("Score (e.g. 4.9)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, focusedBorderColor = NeonPurple),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = episodesCountStr,
                            onValueChange = { episodesCountStr = it },
                            label = { Text("Ep Count") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, focusedBorderColor = NeonPurple),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = genresInput,
                        onValueChange = { genresInput = it },
                        label = { Text("Genres tags (comma-separated)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, focusedBorderColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Database Segment Destination:", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val cats = listOf("Trending", "Popular", "Recently Added")
                        cats.forEach { c ->
                            val isChosen = selectedCategory == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) FlameOrange else BorderPurple.copy(alpha = 0.3f))
                                    .border(0.5.dp, if (isChosen) FlameOrange else BorderPurple)
                                    .clickable { selectedCategory = c }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = c, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val rVal = ratingStr.toDoubleOrNull() ?: 4.5
                            val eVal = episodesCountStr.toIntOrNull() ?: 12
                            if (title.isBlank() || desc.isBlank()) {
                                Toast.makeText(context, "Nya! Please input clear name and synapse properties!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.adminUploadAnime(title, desc, rVal, genresInput, selectedCategory, eVal)
                                Toast.makeText(context, "DEPLOYED SUCCESS! Refreshed in catalog.", Toast.LENGTH_SHORT).show()
                                title = ""
                                desc = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        modifier = Modifier.fillMaxWidth().testTag("admin_submit_button")
                    ) {
                        Text("DEPLOY CODE TO DATABASE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "REGISTERED ANIME RECORDS DATABASE (${allCatalog.size})",
                color = TextLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(allCatalog) { anime ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
                border = BorderStroke(0.5.dp, BorderPurple)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        AnimePlaceholderImage(
                            index = anime.posterIndex,
                            title = anime.title,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = anime.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Id: ${anime.id} | Cat: ${anime.category}", color = TextMuted, fontSize = 9.sp)
                        }
                    }

                    IconButton(onClick = {
                        viewModel.adminDeleteAnime(anime.id)
                        Toast.makeText(context, "REMOVED node successfully!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Trash", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. ANIME DETAIL SCREEN (Backdrop Fade, Episode trigger, Downloads)
// ==========================================

@Composable
fun DetailsScreen(viewModel: NekoViewModel) {
    val anime by viewModel.selectedAnime.collectAsStateWithLifecycle()
    val episodes by viewModel.selectedAnimeEpisodes.collectAsStateWithLifecycle()
    val downloadRecords by viewModel.downloads.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (anime == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FlameOrange)
        }
        return
    }

    val activeAnime = anime!!
    val isFavFlow = viewModel.checkFavoriteFlow(activeAnime.id).collectAsStateWithLifecycle(initialValue = false)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCosmicBlack)
    ) {
        // Upper Poster banner with full overlay gradients
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AnimePlaceholderImage(
                    index = activeAnime.bannerIndex,
                    title = activeAnime.title,
                    modifier = Modifier.fillMaxSize(),
                    isBanner = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, DeepCosmicBlack)
                            )
                        )
                )

                // Safe bar Back button
                IconButton(
                    onClick = { viewModel.navigateTo("Hub") },
                    modifier = Modifier
                        .padding(top = 40.dp, start = 16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }

        // Details Panel
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Elevated floating poster card
                    Box(
                        modifier = Modifier
                            .offset(y = (-40).dp)
                            .size(width = 110.dp, height = 160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, FlameOrange, RoundedCornerShape(8.dp))
                    ) {
                        AnimePlaceholderImage(
                            index = activeAnime.posterIndex,
                            title = activeAnime.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeAnime.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRatingBar(activeAnime.rating)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${activeAnime.views / 1000}k Stream Views",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontally wrap genres pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            activeAnime.genres.split(",").forEach { g ->
                                Box(
                                    modifier = Modifier
                                        .background(BorderPurple.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(text = g.trim(), color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Favorite, Share action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val firstEpisode = episodes.firstOrNull()
                            if (firstEpisode != null) {
                                viewModel.selectEpisodeAndPlay(firstEpisode.id)
                            } else {
                                Toast.makeText(context, "No chapters compiled!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).height(46.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("STREAM NOW", color = Color.White, fontWeight = FontWeight.Black)
                    }

                    IconButton(
                        onClick = { viewModel.toggleFavorite(activeAnime.id) },
                        modifier = Modifier
                            .background(SurfaceGlassDark, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderPurple, RoundedCornerShape(10.dp))
                            .size(46.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavFlow.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = if (isFavFlow.value) Color.Red else Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Share Anime")
                                putExtra(Intent.EXTRA_TEXT, "Stream ${activeAnime.title} on NekoStream app! It's incredible Nya!")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share NekoStream"))
                        },
                        modifier = Modifier
                            .background(SurfaceGlassDark, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderPurple, RoundedCornerShape(10.dp))
                            .size(46.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "SYNOPSIS",
                    color = FlameOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = activeAnime.description,
                    color = TextLight,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                Text(
                    text = "EPISODES LIST CHECKLIST (${episodes.size})",
                    color = TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // Chapters Scroll Cards
        if (episodes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Assemble chapters logs in settings...", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(episodes) { ep ->
                val dlRecord = downloadRecords.find { it.episodeId == ep.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
                    border = BorderStroke(0.5.dp, BorderPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.selectEpisodeAndPlay(ep.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FlameOrange.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = FlameOrange)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Episode ${ep.episodeNumber}: ${ep.title}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ep.description,
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Download buttons
                        if (dlRecord == null) {
                            IconButton(
                                onClick = {
                                    viewModel.downloadEpisode(ep.id, activeAnime.id, activeAnime.title, ep.episodeNumber)
                                    Toast.makeText(context, "Simulated downloading sequence started!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download Offline", tint = TextLight)
                            }
                        } else {
                            if (dlRecord.status == "Completed") {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded Offline", tint = FlameOrange)
                            } else {
                                CircularProgressIndicator(
                                    progress = { dlRecord.progress },
                                    modifier = Modifier.size(24.dp),
                                    color = FlameOrange,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// 7. VIDEO PLAYER SCREEN (Landscape controller simulator, progress syncing)
// ==========================================

@Composable
fun PlayerScreen(viewModel: NekoViewModel) {
    val activeAnime by viewModel.selectedAnime.collectAsStateWithLifecycle()
    val activeEpisode by viewModel.selectedEpisode.collectAsStateWithLifecycle()
    val totalChapters by viewModel.selectedAnimeEpisodes.collectAsStateWithLifecycle()

    var isPlaying by remember { mutableStateOf(true) }
    var mockTimeSec by remember { mutableStateOf(10) }
    var chosenQuality by remember { mutableStateOf("1080p") }
    var selectedSubtitle by remember { mutableStateOf("English Sub") }

    var showMenuQuality by remember { mutableStateOf(false) }
    var showMenuSub by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val anime = activeAnime
    val ep = activeEpisode

    if (anime == null || ep == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FlameOrange)
        }
        return
    }

    // Playback time looping logic
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (mockTimeSec < ep.durationSeconds) {
                mockTimeSec += 5 // Fast forward simulated updates
                // Sync position in Room dynamically!
                viewModel.saveProgress(anime.id, ep.episodeNumber, mockTimeSec, ep.durationSeconds)
            } else {
                isPlaying = false
                // Play Next Chapter automatic triggers
                val nextEp = totalChapters.find { it.episodeNumber == ep.episodeNumber + 1 }
                if (nextEp != null) {
                    Toast.makeText(context, "Completed! Loading Episode ${nextEp.episodeNumber} automatically!", Toast.LENGTH_SHORT).show()
                    viewModel.selectEpisodeAndPlay(nextEp.id)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Bar controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("Details") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = anime.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Chapter ${ep.episodeNumber}: ${ep.title}", color = FlameOrange, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                Row {
                    // Subtitles menu selector
                    Box {
                        IconButton(onClick = { showMenuSub = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Languages", tint = Color.White)
                        }
                        DropdownMenu(expanded = showMenuSub, onDismissRequest = { showMenuSub = false }) {
                            DropdownMenuItem(text = { Text("English Subs") }, onClick = { selectedSubtitle = "English Sub"; showMenuSub = false })
                            DropdownMenuItem(text = { Text("Spanish Subs") }, onClick = { selectedSubtitle = "Spanish Sub"; showMenuSub = false })
                            DropdownMenuItem(text = { Text("Japanese Original") }, onClick = { selectedSubtitle = "Japanese Audio Only"; showMenuSub = false })
                        }
                    }

                    // Quality menu selector
                    Box {
                        IconButton(onClick = { showMenuQuality = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Quality", tint = Color.White)
                        }
                        DropdownMenu(expanded = showMenuQuality, onDismissRequest = { showMenuQuality = false }) {
                            DropdownMenuItem(text = { Text("1080p Ultra HD") }, onClick = { chosenQuality = "1080p"; showMenuQuality = false })
                            DropdownMenuItem(text = { Text("720p HD") }, onClick = { chosenQuality = "720p"; showMenuQuality = false })
                            DropdownMenuItem(text = { Text("360p Mobile Saver") }, onClick = { chosenQuality = "360p"; showMenuQuality = false })
                        }
                    }
                }
            }

            // Cinematic Horizontal video simulator with glowing lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceGlassDark)
                    .border(2.dp, NeonPurple, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AnimePlaceholderImage(
                    index = anime.bannerIndex,
                    title = "NEKOSTREAM VIDEO LIVE SCREEN",
                    modifier = Modifier.fillMaxSize()
                )

                // Simulates a running subtitle text overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "[ $selectedSubtitle: Nya! Let's unleash our absolute ultimate resonance domain! ]",
                            color = Color.Yellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If paused, overlay pause logo
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { isPlaying = true }, modifier = Modifier.size(60.dp)) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(60.dp))
                        }
                    }
                }
            }

            // Interactive Comments block inside Player
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .glassCard()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("ACTIVE EPISODE COMMENTS Realtime discussion", color = FlameOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    var newCommentText by remember { mutableStateOf("") }
                    val feedbackList by viewModel.getCommentsFlow(ep.id).collectAsStateWithLifecycle(initialValue = emptyList())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("What are your assessments?", fontSize = 10.sp) },
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                viewModel.postComment(ep.id, newCommentText)
                                newCommentText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Share", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(feedbackList) { c ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "${c.username}:", color = FlameOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = c.text, color = TextLight, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // Player timeline indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                val remMins = (ep.durationSeconds - mockTimeSec) / 60
                val elapsedMins = mockTimeSec / 60

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Elapsed: ${elapsedMins}m", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Quality: $chosenQuality", color = FlameOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Remaining: ${remMins}m", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val positionRatio = mockTimeSec.toFloat() / ep.durationSeconds
                LinearProgressIndicator(
                    progress = { positionRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FlameOrange,
                    trackColor = BorderPurple
                )
            }

            // Lower interactive control deck
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip intro button
                Button(
                    onClick = {
                        mockTimeSec = minOf(mockTimeSec + 90, ep.durationSeconds) // skips 1.5 mins
                        Toast.makeText(context, "Skipped anime opening intro!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BorderPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SKIP INTRO ⏭️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Play/Pause Action Icon
                IconButton(onClick = { isPlaying = !isPlaying }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = "Trigger Status Play",
                        tint = FlameOrange,
                        modifier = Modifier.size(52.dp)
                    )
                }

                // Force Next Episode Trigger
                val nextNode = totalChapters.find { it.episodeNumber == ep.episodeNumber + 1 }
                IconButton(
                    onClick = {
                        if (nextNode != null) {
                            viewModel.selectEpisodeAndPlay(nextNode.id)
                        } else {
                            Toast.makeText(context, "No subsequent chapters found!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = nextNode != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Forward Chapter",
                        tint = if (nextNode != null) Color.White else TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
