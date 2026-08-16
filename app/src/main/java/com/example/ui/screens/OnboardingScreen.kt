package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

data class OnboardingStep(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val badgeText: String,
    val iconBgColor: Color
)

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onFinishOnboarding: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                title = "Geometric Puzzle Hub",
                description = "Master mind-sharpening classics including ZIP pathfinding, Mini Sudoku, Tango logic, and Word Search.",
                iconEmoji = "🧩",
                badgeText = "5 IN 1 COLLECTION",
                iconBgColor = AccentAmberBg
            ),
            OnboardingStep(
                title = "Daily Challenges & Fire Streaks",
                description = "Train your brain every day. Complete the daily curated grid to build high streaks and collect golden bonuses.",
                iconEmoji = "🔥",
                badgeText = "DAILY STREAKS",
                iconBgColor = AccentOrangeBg
            ),
            OnboardingStep(
                title = "Achievements & Customizations",
                description = "Unlock achievements, climb solver ranks, and customize your app with persistent themes and audio settings.",
                iconEmoji = "⚡",
                badgeText = "LEVEL UP",
                iconBgColor = AccentPurpleBg
            )
        )
    }

    var currentStepIdx by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIdx]
    val isLastStep = currentStepIdx == steps.size - 1

    Scaffold(
        containerColor = PureWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with App Identity and Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryIndigo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🧠", fontSize = 18.sp)
                        }
                    }
                    Text(
                        text = "Eazy Solve",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Slate900
                    )
                }

                if (!isLastStep) {
                    TextButton(
                        onClick = {
                            SoundManager.playTap()
                            viewModel.completeOnboarding()
                            onFinishOnboarding()
                        },
                        modifier = Modifier.testTag("skip_onboarding_btn")
                    ) {
                        Text(
                            text = "SKIP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            color = Slate500
                        )
                    }
                }
            }

            // Step Content
            AnimatedContent(
                targetState = currentStepIdx,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step_content",
                modifier = Modifier.weight(1f)
            ) { targetIdx ->
                val currStep = steps[targetIdx]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Feature Icon
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = currStep.iconBgColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200.copy(alpha = 0.6f)),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(currStep.iconEmoji, fontSize = 54.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Slate100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Text(
                            text = currStep.badgeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate800,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currStep.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center,
                        color = Slate900,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currStep.description,
                        fontSize = 14.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Bottom Controls: Step Indicators & Action Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { idx ->
                        val isSelected = idx == currentStepIdx
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) Slate900 else Slate200)
                        )
                    }
                }

                Button(
                    onClick = {
                        SoundManager.playTap()
                        if (isLastStep) {
                            viewModel.completeOnboarding()
                            onFinishOnboarding()
                        } else {
                            currentStepIdx++
                        }
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_action_btn")
                ) {
                    Text(
                        text = if (isLastStep) "GET STARTED" else "CONTINUE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp,
                        color = PureWhite
                    )
                }
            }
        }
    }
}
