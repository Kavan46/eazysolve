package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var startAnim by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.1f) }

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logo_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "text_alpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        HapticManager.playSuccess()
        SoundManager.playLevelComplete()
        
        // Smooth progress simulation
        for (i in 1..10) {
            delay(130)
            progress = i * 0.1f
        }
        delay(250)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF312E81),
                        Color(0xFF1E3A8A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background glowing circles
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseGlow)
                .clip(CircleShape)
                .background(PrimaryIndigo.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulseGlow * 0.9f)
                .clip(CircleShape)
                .background(AccentViolet.copy(alpha = 0.35f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Eazy Solve Games Mascot Icon
            Surface(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale)
                    .shadow(24.dp, shape = RoundedCornerShape(34.dp), ambientColor = Color(0xFF6366F1), spotColor = Color(0xFFA855F7)),
                shape = RoundedCornerShape(34.dp),
                color = Color(0xFF1E1B4B),
                border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFF818CF8).copy(alpha = 0.8f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_eazy_solve_logo),
                    contentDescription = "Eazy Solve Games Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Game Name Title Banner
            Text(
                text = "Eazy Solve",
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                letterSpacing = (-0.5).sp,
                color = Color.White,
                modifier = Modifier.alpha(alpha)
            )

            Text(
                text = "GAMES",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 6.sp,
                color = AccentAmber,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline Pill
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.alpha(alpha)
            ) {
                Text(
                    text = "✨ Think. Play. Solve. ✨",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Animated Loading Progress Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(180.dp)
                    .alpha(alpha)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = AccentAmber,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading puzzles...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
