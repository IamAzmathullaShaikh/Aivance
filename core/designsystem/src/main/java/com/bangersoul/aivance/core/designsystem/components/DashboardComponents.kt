package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.core.designsystem.theme.Zinc700
import com.bangersoul.aivance.core.designsystem.theme.Zinc800
import com.bangersoul.aivance.core.designsystem.theme.Zinc900

/**
 * A base card with subtle elevation/border and rounded corners (16dp).
 */
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = AivanceTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, Zinc800),
            content = { content() }
        )
    } else {
        Card(
            modifier = modifier,
            shape = AivanceTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, Zinc800),
            content = { content() }
        )
    }
}

/**
 * A smaller card displaying a label and a value (e.g., ATS Score).
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trend: String? = null,
    trendColor: Color = DarkAccent
) {
    DashboardCard(modifier = modifier) {
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
                    color = MaterialTheme.colorScheme.secondary
                )
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            trend?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = trendColor
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor
                    )
                }
            }
        }
    }
}

/**
 * A title and optional "View All" or CTA link.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    ctaText: String? = null,
    onCtaClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (ctaText != null && onCtaClick != null) {
            TextButton(onClick = onCtaClick) {
                Text(
                    text = ctaText,
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkAccent
                )
                Spacer(Modifier.width(AivanceTheme.spacing.extraSmall))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = DarkAccent
                )
            }
        }
    }
}

/**
 * A refined button style for quick actions.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = AivanceTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(AivanceTheme.spacing.small))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * A linear progress bar with a smooth animation from 0 to current value.
 */
@Composable
fun AnimatedProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = DarkAccent,
    trackColor: Color = Zinc900
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )

    LaunchedEffect(progress) {
        animatedProgress = progress
    }

    LinearProgressIndicator(
        progress = { progressAnimation },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

/**
 * A small tag/chip for status or metrics.
 */
@Composable
fun MetricChip(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Zinc800,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = containerColor,
        border = BorderStroke(1.dp, Zinc700)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * A card specifically for empty states within sections.
 */
@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Inbox,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    DashboardCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AivanceTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(AivanceTheme.spacing.medium))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(AivanceTheme.spacing.extraSmall))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (actionText != null && onActionClick != null) {
                Spacer(Modifier.height(AivanceTheme.spacing.medium))
                ActionButton(
                    text = actionText,
                    onClick = onActionClick,
                    icon = Icons.Rounded.Add
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun DashboardComponentsPreview() {
    AivanceTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionHeader(
                title = "Overview",
                ctaText = "View Details",
                onCtaClick = {}
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    label = "ATS Score",
                    value = "85%",
                    trend = "+5% this week",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Applications",
                    value = "12",
                    modifier = Modifier.weight(1f)
                )
            }

            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profile Completion", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    AnimatedProgress(progress = 0.75f)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricChip(label = "In Progress")
                        MetricChip(label = "Updated Today", containerColor = DarkAccent.copy(alpha = 0.1f), contentColor = DarkAccent)
                    }
                }
            }

            ActionButton(
                text = "Apply Now",
                onClick = {},
                icon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth()
            )

            EmptyStateCard(
                title = "No Jobs Tracked",
                description = "Start by adding a job you've applied to or interested in.",
                actionText = "Add Job",
                onActionClick = {}
            )
        }
    }
}
