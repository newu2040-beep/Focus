package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OnboardingStep(
            title = "Plan What Truly Matters",
            description = "Identify your primary focus each day. Keep your tasks minimal, calm, and manageable.",
            icon = Icons.Outlined.CheckCircle
        ),
        OnboardingStep(
            title = "Deep Focus Without Noise",
            description = "Start focused sessions with zero friction. Accurate intervals designed for flow.",
            icon = Icons.Outlined.Timer
        ),
        OnboardingStep(
            title = "Reflect & Grow Mindfully",
            description = "Track your daily completion and capture thoughts without cluttered dashboards.",
            icon = Icons.Outlined.SelfImprovement
        )
    )

    var currentStep by remember { mutableStateOf(0) }
    val step = steps[currentStep]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Step Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == currentStep) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_continue_button"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (currentStep == steps.size - 1) "Get Started" else "Continue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
