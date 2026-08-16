package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.ui.theme.*

enum class NavTab {
    HOME,
    DAILY,
    BADGES,
    PROFILE
}

@Composable
fun EnhancedBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isDailyCompleted: Boolean = false,
    unclaimedBadgesCount: Int = 0,
    isCloudSynced: Boolean = true,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = PrimaryIndigo.copy(alpha = 0.15f),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EnhancedNavItem(
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = currentTab == NavTab.HOME,
                onClick = {
                    if (currentTab != NavTab.HOME) {
                        HapticManager.playTap()
                        SoundManager.playTap()
                        onTabSelected(NavTab.HOME)
                    }
                },
                testTag = "nav_tab_home",
                modifier = Modifier.weight(1f)
            )

            EnhancedNavItem(
                label = "Daily",
                selectedIcon = Icons.Filled.CalendarToday,
                unselectedIcon = Icons.Outlined.CalendarToday,
                isSelected = currentTab == NavTab.DAILY,
                badgeContent = {
                    if (isDailyCompleted) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = AccentOrange,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                },
                onClick = {
                    HapticManager.playTap()
                    SoundManager.playTap()
                    onTabSelected(NavTab.DAILY)
                },
                testTag = "nav_tab_daily",
                modifier = Modifier.weight(1f)
            )

            EnhancedNavItem(
                label = "Badges",
                selectedIcon = Icons.Filled.EmojiEvents,
                unselectedIcon = Icons.Outlined.EmojiEvents,
                isSelected = currentTab == NavTab.BADGES,
                badgeCount = unclaimedBadgesCount,
                onClick = {
                    HapticManager.playTap()
                    SoundManager.playTap()
                    onTabSelected(NavTab.BADGES)
                },
                testTag = "nav_tab_badges",
                modifier = Modifier.weight(1f)
            )

            EnhancedNavItem(
                label = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.PersonOutline,
                isSelected = currentTab == NavTab.PROFILE,
                badgeContent = if (isCloudSynced) {
                    {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                        )
                    }
                } else null,
                onClick = {
                    HapticManager.playTap()
                    SoundManager.playTap()
                    onTabSelected(NavTab.PROFILE)
                },
                testTag = "nav_tab_profile",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EnhancedNavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    badgeContent: (@Composable () -> Unit)? = null
) {
    val extendedColors = LocalExtendedColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryIndigo else extendedColors.textMuted,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navIconColor"
    )

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navPillBg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(pillBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = PrimaryIndigo),
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .scale(scale)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(23.dp)
                )

                if (badgeCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = AccentAmber,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (badgeCount > 9) "9+" else "$badgeCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else if (badgeContent != null) {
                    Box(modifier = Modifier.offset(x = 4.dp, y = (-2).dp)) {
                        badgeContent()
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = iconColor
            )

            // Micro Active Pill Indicator
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(width = 12.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PrimaryIndigo, AccentViolet)
                            )
                        )
                )
            }
        }
    }
}
