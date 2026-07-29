package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.Zinc800

/**
 * A premium card representing a step in a roadmap.
 */
@Composable
fun StepCard(
    title: String,
    description: String,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Zinc800),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = Zinc800,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * A wrapper for [StepCard] that adds a timeline indicator (line and dot).
 */
@Composable
fun TimelineItem(
    title: String,
    description: String,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isLast: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline Column
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 28.dp) // Start line after the dot
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Zinc800)
                )
            }
            
            // Dot
            Box(
                modifier = Modifier
                    .padding(top = 22.dp) // Aligned roughly with the title of the StepCard
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary else Zinc800
                    )
            )
        }

        // Content
        StepCard(
            title = title,
            description = description,
            isCompleted = isCompleted,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun StepCardPreview() {
    AivanceTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            StepCard(
                title = "Optimize Resume",
                description = "Update your resume with the keywords matched for this job description.",
                isCompleted = false,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun TimelineItemPreview() {
    AivanceTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            TimelineItem(
                title = "Profile Analysis",
                description = "Comparing your skills with the job requirements.",
                isCompleted = true,
                onCheckedChange = {}
            )
            TimelineItem(
                title = "Skill Gap Identification",
                description = "Identifying missing skills and recommended courses.",
                isCompleted = false,
                onCheckedChange = {}
            )
            TimelineItem(
                title = "Resume Tailoring",
                description = "Generating a tailored resume for this specific role.",
                isCompleted = false,
                onCheckedChange = {},
                isLast = true
            )
        }
    }
}
