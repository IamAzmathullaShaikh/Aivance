package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

/**
 * A shimmer brush that sweeps across the skeleton surface.
 * Composed at call-site so it always animates fresh per skeleton.
 */
@Composable
fun rememberShimmerBrush(base: Color, highlight: Color): Brush {
    val transition = rememberInfiniteTransition(label = "SkeletonShimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate - 400f, 0f),
        end = Offset(translate, 400f)
    )
}

/**
 * A rounded skeleton block with a shimmer sweep.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val highlight = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
    val brush = rememberShimmerBrush(base, highlight)

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * A single skeleton text line of a given width.
 */
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp = 14.dp
) {
    SkeletonBox(
        modifier = modifier.width(width).height(height),
        shape = RoundedCornerShape(6.dp)
    )
}

/**
 * A skeleton card — commonly used in lists and grids.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AivanceTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showAvatar) {
            SkeletonBox(
                modifier = Modifier.size(44.dp),
                shape = CircleShape
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonText(width = 160.dp, height = 16.dp)
            SkeletonText(width = 220.dp, height = 12.dp)
        }
    }
    Spacer(Modifier.height(12.dp))
}

/**
 * A full skeleton list used while loading screens.
 */
@Composable
fun SkeletonList(
    itemCount: Int = 6,
    modifier: Modifier = Modifier,
    showAvatar: Boolean = false
) {
    Column(modifier = modifier.padding(16.dp)) {
        repeat(itemCount) {
            SkeletonCard(showAvatar = showAvatar)
        }
    }
}

/**
 * A skeleton dashboard layout: header + metric row + cards.
 */
@Composable
fun SkeletonDashboard(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        SkeletonText(width = 200.dp, height = 24.dp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonBox(modifier = Modifier.weight(1f).height(96.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(96.dp))
        }
        Spacer(Modifier.height(16.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(140.dp))
        Spacer(Modifier.height(16.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(96.dp))
        Spacer(Modifier.height(16.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(120.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SkeletonDashboardPreview() {
    AivanceTheme(darkTheme = true) {
        SkeletonDashboard()
    }
}
