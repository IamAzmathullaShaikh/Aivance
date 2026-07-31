package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

enum class BannerTone {
    SUCCESS, ERROR, WARNING, INFO
}

private val BannerTone.icon: ImageVector
    get() = when (this) {
        BannerTone.SUCCESS -> Icons.Rounded.CheckCircle
        BannerTone.ERROR -> Icons.Rounded.Error
        BannerTone.WARNING -> Icons.Rounded.Warning
        BannerTone.INFO -> Icons.Rounded.Info
    }

/**
 * A dismissable/actionable banner for transient states.
 */
@Composable
fun AivanceBanner(
    message: String,
    tone: BannerTone = BannerTone.INFO,
    modifier: Modifier = Modifier,
    title: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val containerColor = when (tone) {
        BannerTone.SUCCESS -> AivanceTheme.colors.successContainer
        BannerTone.ERROR -> MaterialTheme.colorScheme.errorContainer
        BannerTone.WARNING -> AivanceTheme.colors.warningContainer
        BannerTone.INFO -> AivanceTheme.colors.infoContainer
    }
    val contentColor = when (tone) {
        BannerTone.SUCCESS -> AivanceTheme.colors.onSuccessContainer
        BannerTone.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        BannerTone.WARNING -> AivanceTheme.colors.onWarningContainer
        BannerTone.INFO -> AivanceTheme.colors.onInfoContainer
    }
    val iconColor = when (tone) {
        BannerTone.SUCCESS -> AivanceTheme.colors.success
        BannerTone.ERROR -> MaterialTheme.colorScheme.error
        BannerTone.WARNING -> AivanceTheme.colors.warning
        BannerTone.INFO -> AivanceTheme.colors.info
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AivanceTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(tone.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (title != null) {
                    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            if (actionText != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(actionText, color = contentColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BannerPreview() {
    AivanceTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AivanceBanner("Resume uploaded successfully", BannerTone.SUCCESS, title = "Done")
            AivanceBanner("Provider unavailable. Check your connection.", BannerTone.ERROR, actionText = "Retry", onAction = {})
            AivanceBanner("Low ATS match — add more keywords", BannerTone.WARNING)
            AivanceBanner("Your weekly goal is on track", BannerTone.INFO)
        }
    }
}
