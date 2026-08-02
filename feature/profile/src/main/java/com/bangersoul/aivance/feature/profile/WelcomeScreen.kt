package com.bangersoul.aivance.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import kotlinx.coroutines.delay

private val NavyDeep = Color(0xFF0B1220)
private val Charcoal = Color(0xFF09090B)
private val GlowBlue = Color(0xFF3B82F6)

private data class FeatureBullet(val titleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val featureBullets = listOf(
    FeatureBullet(R.string.feature_resume_intelligence, Icons.Rounded.AutoAwesome),
    FeatureBullet(R.string.feature_ats_optimization, Icons.Rounded.CheckCircle),
    FeatureBullet(R.string.feature_job_discovery, Icons.Rounded.CheckCircle),
    FeatureBullet(R.string.feature_recruiter_intelligence, Icons.Rounded.CheckCircle),
    FeatureBullet(R.string.feature_interview_preparation, Icons.Rounded.CheckCircle),
    FeatureBullet(R.string.feature_career_analytics, Icons.Rounded.CheckCircle)
)

/**
 * v2 premium Welcome screen — a full-screen brand statement that introduces
 * the Career Operating System before authentication.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        started = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDeep, Charcoal)
                )
            )
    ) {
        // Soft radial glow behind the logo
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
                .size(260.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GlowBlue.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo with soft glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .shadow(28.dp, RoundedCornerShape(32.dp), spotColor = GlowBlue.copy(alpha = 0.45f))
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlowBlue.copy(alpha = 0.16f))
                    .size(96.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = stringResource(R.string.splash_logo_desc),
                    modifier = Modifier.size(48.dp),
                    tint = GlowBlue
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.welcome_brand),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.welcome_tagline),
                style = MaterialTheme.typography.titleMedium,
                color = GlowBlue.copy(alpha = 0.9f),
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.welcome_quote),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.welcome_headline),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Staggered feature bullets
            featureBullets.forEachIndexed { index, feature ->
                AnimatedVisibility(
                    visible = started,
                    enter = fadeIn(
                        animationSpec = tween(500, delayMillis = 150 + index * 120)
                    ) + slideInVertically(
                        animationSpec = tween(500, delayMillis = 150 + index * 120),
                        initialOffsetY = { it / 3 }
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "✦  " + stringResource(feature.titleRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(44.dp))

            // Shimmer Get Started button
            ShimmerButton(
                text = stringResource(R.string.get_started),
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(58.dp)
            )

            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.TextButton(onClick = onSkip) {
                Text(
                    text = stringResource(R.string.skip_for_now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Full-width CTA with a travelling shimmer sweep across the surface.
 */
@Composable
private fun ShimmerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmer by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        DarkAccent,
                        Color(0xFF60A5FA),
                        DarkAccent
                    )
                )
            )
    ) {
        // Shimmer sweep overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        startX = shimmer * 800f,
                        endX = shimmer * 800f + 420f,
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0f)
                        )
                    )
                )
        )
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1220)
@Composable
private fun WelcomeScreenPreview() {
    AivanceTheme(darkTheme = true) { WelcomeScreen() }
}
