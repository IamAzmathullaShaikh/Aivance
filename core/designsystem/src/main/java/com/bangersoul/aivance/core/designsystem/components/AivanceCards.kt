package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

/**
 * A metric card — label + big value + optional trend.
 */
@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trend: String? = null,
    trendColor: Color = AivanceTheme.colors.success,
    onClick: (() -> Unit)? = null
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(AivanceTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.extraSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                icon?.let {
                    Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(16.dp), tint = iconTint)
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            trend?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor,
                    maxLines = 1
                )
            }
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = { content() })
    } else {
        Card(modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = { content() })
    }
}

/**
 * An insight card — icon + headline insight text.
 */
@Composable
fun InsightCard(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = AivanceTheme.colors.accent,
    onClick: (() -> Unit)? = null
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(AivanceTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                Surface(shape = RoundedCornerShape(10.dp), color = iconTint.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconTint)
                    }
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)), content = { content() })
    } else {
        Card(modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)), content = { content() })
    }
}

/**
 * An action card — tappable tile with icon, title, and subtitle.
 */
@Composable
fun ActionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(AivanceTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.small)
        ) {
            if (icon != null) {
                Surface(shape = RoundedCornerShape(12.dp), color = iconTint.copy(alpha = 0.12f), modifier = Modifier.size(40.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconTint)
                    }
                }
            }
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = { content() })
    } else {
        Card(modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = { content() })
    }
}

/**
 * A progress card — title + animated progress bar + value label.
 */
@Composable
fun ProgressCard(
    title: String,
    progress: Float,
    modifier: Modifier = Modifier,
    valueLabel: String? = null,
    subtitle: String? = null,
    progressColor: Color = AivanceTheme.colors.accent,
    onClick: (() -> Unit)? = null
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(AivanceTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                valueLabel?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = progressColor)
                }
            }
            AnimatedProgress(
                progress = progress.coerceIn(0f, 1f),
                color = progressColor,
                modifier = Modifier.fillMaxWidth()
            )
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = { content() })
    } else {
        Card(modifier = modifier, shape = AivanceTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = { content() })
    }
}

/**
 * A status chip with semantic coloring.
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.INFO
) {
    val (container, content) = when (tone) {
        BannerTone.SUCCESS -> AivanceTheme.colors.successContainer to AivanceTheme.colors.onSuccessContainer
        BannerTone.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BannerTone.WARNING -> AivanceTheme.colors.warningContainer to AivanceTheme.colors.onWarningContainer
        BannerTone.INFO -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = container
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * A workspace card — the standard container for list items and grouped content.
 */
@Composable
fun AivanceWorkspaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = AivanceTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            content = { content() }
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = AivanceTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            content = { content() }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CardPreview() {
    AivanceTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(label = "ATS Score", value = "85", trend = "+5% this week", icon = Icons.Rounded.TrendingUp)
            InsightCard(text = "Your resume matches 9 of 12 required keywords", icon = Icons.Rounded.Lightbulb)
            ActionCard(title = "Optimize Resume", subtitle = "Improve your ATS match by 12%", icon = Icons.Rounded.Description)
            ProgressCard(title = "Profile Completion", progress = 0.75f, valueLabel = "75%")
            StatusChip(text = "Applied", tone = BannerTone.INFO)
            StatusChip(text = "Offer", tone = BannerTone.SUCCESS)
        }
    }
}
