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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.Zinc800

enum class ChatBubbleRole {
    Interviewer, Candidate
}

@Composable
fun ChatBubble(
    text: String,
    timestamp: String,
    role: ChatBubbleRole,
    modifier: Modifier = Modifier,
) {
    val isInterviewer = role == ChatBubbleRole.Interviewer
    val backgroundColor = if (isInterviewer) Zinc800 else MaterialTheme.colorScheme.primary
    val contentColor = if (isInterviewer) Color.White else MaterialTheme.colorScheme.onPrimary
    val alignment = if (isInterviewer) Alignment.Start else Alignment.End
    
    val bubbleShape = if (isInterviewer) {
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AivanceTheme.spacing.extraSmall),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = backgroundColor,
            contentColor = contentColor,
            shape = bubbleShape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = AivanceTheme.spacing.medium,
                    vertical = AivanceTheme.spacing.small
                )
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(AivanceTheme.spacing.extraSmall))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TypingIndicator")
    
    @Composable
    fun pulsingDot(delay: Int) {
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = delay, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "DotAlpha"
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = alpha))
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Zinc800)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        pulsingDot(0)
        pulsingDot(200)
        pulsingDot(400)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ChatComponentsPreview() {
    AivanceTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChatBubble(
                text = "Hello! I'm your AI interviewer. How are you today?",
                timestamp = "10:00 AM",
                role = ChatBubbleRole.Interviewer
            )
            ChatBubble(
                text = "I'm doing great, thank you for asking! I'm excited for the interview.",
                timestamp = "10:01 AM",
                role = ChatBubbleRole.Candidate
            )
            
            TypingIndicator()
        }
    }
}
