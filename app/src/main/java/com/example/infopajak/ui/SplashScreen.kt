package com.example.infopajak.ui

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.infopajak.R
import com.example.infopajak.ui.theme.PrimaryBlue
import com.example.infopajak.ui.theme.PrimaryDarkBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Build Coil ImageLoader supporting animated GIF
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val gifPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(R.drawable.money_animated)
            .build(),
        imageLoader = imageLoader
    )

    // Breathing pulse animation
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.05f else 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    // Fade in animation for the whole content
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "contentAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3200) // Show splash for ~3.2 seconds
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A2540),
                        PrimaryDarkBlue,
                        PrimaryBlue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(alpha = alpha)
        ) {
            // Animated Money GIF Container
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFFFB300).copy(alpha = 0.5f))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFE082).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = gifPainter,
                    contentDescription = "Animasi Uang",
                    modifier = Modifier.size(175.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Surface(
                color = Color(0xFFFFB300),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "INFO REALISASI PAJAK",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = Color(0xFF0F172A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Name
            Text(
                text = "PENERIMAAN",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Bapenda Provinsi Riau",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Version at bottom
        Text(
            text = "v1.0.0",
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
