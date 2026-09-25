package com.pixelfitquest.feature.levels

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.levels.model.LevelProgress
import com.pixelfitquest.feature.progression.SkillBranch
import com.pixelfitquest.feature.progression.SkillLoadout
import com.pixelfitquest.feature.progression.SkillTree
import com.pixelfitquest.feature.streak.model.WeeklyStreakSnapshot
import com.pixelfitquest.ui.theme.CrystalCyan
import com.pixelfitquest.ui.theme.EmberOrange
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.SlateSurface
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Composable
fun XpStatusDialog(
    progress: LevelProgress,
    skills: SkillLoadout,
    coins: Int,
    streakSnapshot: WeeklyStreakSnapshot,
    onSpendSkill: (SkillBranch) -> Unit,
    onRespecSkills: () -> Unit = {},
    respecDaysRemaining: Int = 0,
    onStreakClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val unspent = skills.unspent(progress.level)

    // Calculate days until streak resets
    val now = remember { LocalDate.now() }
    val daysUntilReset = remember(streakSnapshot.weekStart, now) {
        if (streakSnapshot.weekStart == LocalDate.EPOCH) {
            val currentMonday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ChronoUnit.DAYS.between(now, currentMonday.plusDays(7)).coerceAtLeast(0)
        } else {
            ChronoUnit.DAYS.between(now, streakSnapshot.weekStart.plusDays(7)).coerceAtLeast(0)
        }
    }

    val isStreakDoneThisWeek = streakSnapshot.weekGoalMet ||
        streakSnapshot.sessionsThisWeek >= streakSnapshot.targetSessionsPerWeek

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.74f)),
            contentAlignment = Alignment.Center,
        ) {
            // Main card overlay
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .heightIn(max = 640.dp)
                    .padding(horizontal = spacing.md, vertical = spacing.sm)
                    .clip(RoundedCornerShape(spacing.cornerMd))
                    .background(SlateDeep.copy(alpha = 0.98f))
                    .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd)),
            ) {
                // 16-bit corner brackets
                XpCornerBrackets(modifier = Modifier.fillMaxSize())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Header
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "HERO PROGRESSION",
                            fontFamily = determination,
                            color = ImperialGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }

                    // Top quick stats: Coins & Weekly Streak
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Coins Card (compact, matched height)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(spacing.cornerSm))
                                .background(SlateSurface.copy(alpha = 0.85f))
                                .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerSm))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = "Coins",
                                    modifier = Modifier.size(18.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Column {
                                    Text(
                                        text = "$coins",
                                        fontFamily = determination,
                                        color = RewardGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Coins",
                                        color = SilverSlate,
                                        fontSize = 9.sp,
                                    )
                                }
                            }
                        }

                        // Weekly Streak Card (expanded space, matched height)
                        val grayFilter = remember {
                            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(spacing.cornerSm))
                                .background(SlateSurface.copy(alpha = 0.85f))
                                .border(
                                    BorderStroke(1.dp, if (isStreakDoneThisWeek) ImperialGold.copy(alpha = 0.4f) else SlateBorderSubtle),
                                    RoundedCornerShape(spacing.cornerSm),
                                )
                                .then(
                                    if (onStreakClick != null) Modifier.clickable { onStreakClick() }
                                    else Modifier
                                )
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.streak),
                                    contentDescription = "Streak",
                                    colorFilter = if (isStreakDoneThisWeek) null else grayFilter,
                                    alpha = if (isStreakDoneThisWeek) 1f else 0.4f,
                                    modifier = Modifier.size(22.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${streakSnapshot.currentStreakWeeks} Wk Streak",
                                        fontFamily = determination,
                                        color = if (isStreakDoneThisWeek) ImperialGold else SilverSteel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = if (isStreakDoneThisWeek) {
                                            "${streakSnapshot.sessionsThisWeek}/${streakSnapshot.targetSessionsPerWeek} done • Streak active!"
                                        } else {
                                            "${streakSnapshot.sessionsThisWeek}/${streakSnapshot.targetSessionsPerWeek} done • resets in $daysUntilReset ${if (daysUntilReset == 1L) "day" else "days"}"
                                        },
                                        fontFamily = determination,
                                        color = if (isStreakDoneThisWeek) VitalGreen else TorchAmber,
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                    )
                                }
                            }
                        }
                    }

                    // Big Prominent XP Bar Section
                    BigXpBarSection(progress = progress)

                    // Skill Points Header & Explanations
                    SkillSectionCard(
                        unspent = unspent,
                        progress = progress,
                        skills = skills,
                        respecDaysRemaining = respecDaysRemaining,
                        onSpendSkill = onSpendSkill,
                        onRespecSkills = onRespecSkills,
                    )

                    // Close Button
                    PixelArtButton(
                        onClick = onDismiss,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                    ) {
                        Text(
                            text = "CLOSE",
                            fontFamily = determination,
                            color = SilverSteel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Large, prominent XP progress bar showing current XP into level and remaining XP until next level.
 */
@Composable
private fun BigXpBarSection(progress: LevelProgress) {
    val spacing = MaterialTheme.spacing
    val animatedFraction by animateFloatAsState(
        targetValue = if (progress.isMaxLevel) 1f else progress.xpFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "xpFillAnimation",
    )
    val missingXp = if (progress.isMaxLevel) 0 else (progress.xpToNext - progress.xpIntoLevel).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateSurface.copy(alpha = 0.90f))
            .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerSm))
            .padding(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Level and percentage row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "LEVEL ${progress.level}",
                    fontFamily = determination,
                    color = ImperialGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (progress.isMaxLevel) "MAX LEVEL" else "${(progress.xpFraction * 100).toInt()}%",
                    fontFamily = determination,
                    color = if (progress.isMaxLevel) VitalGreen else PlatinumWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Big XP Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SlateGroove)
                    .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                // Filled progress track
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = animatedFraction.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(TorchAmber, ImperialGold),
                            ),
                        ),
                )

                // Overlay Text inside bar
                Text(
                    text = if (progress.isMaxLevel) {
                        "MAXIMUM LEVEL REACHED"
                    } else {
                        "${progress.xpIntoLevel} / ${progress.xpToNext} XP"
                    },
                    fontFamily = determination,
                    color = PlatinumWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }

            // Missing XP Callout & Total XP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (progress.isMaxLevel) {
                        "You reached the level cap!"
                    } else {
                        "Missing $missingXp XP to Level ${progress.level + 1}"
                    },
                    fontFamily = determination,
                    color = if (progress.isMaxLevel) VitalGreen else TorchAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Total XP: ${progress.totalXp}",
                    color = SilverSlate,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

/**
 * Skill points overview and cards explaining Form, Iron, and Vitality branches.
 */
@Composable
private fun SkillSectionCard(
    unspent: Int,
    progress: LevelProgress,
    skills: SkillLoadout,
    respecDaysRemaining: Int,
    onSpendSkill: (SkillBranch) -> Unit,
    onRespecSkills: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateSurface.copy(alpha = 0.90f))
            .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerSm))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Skill header with points available
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "✦ SKILL POINTS: $unspent",
                    fontFamily = determination,
                    color = ImperialGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (unspent > 0) "Points ready to spend! Tap + to upgrade." else "1 skill point gained every level up",
                    color = if (unspent > 0) VitalGreen else SilverSlate,
                    fontSize = 10.sp,
                    fontWeight = if (unspent > 0) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        // 1. Form Skill Explanation Card
        SkillDetailCard(
            title = "FORM",
            rank = skills.form,
            branch = SkillBranch.FORM,
            currentBonus = "+${SkillTree.formPercent(skills.form)}% Bonus XP",
            description = "Increases XP gained on workout sets completed with 90% or higher precision form (+3% per rank).",
            nextBonus = if (skills.form < SkillTree.MAX_RANK) "+${SkillTree.formPercent(skills.form + 1)}% XP on 90+ sets" else null,
            canSpend = unspent > 0 && skills.form < SkillTree.MAX_RANK,
            onSpend = onSpendSkill,
        )

        // 2. Iron Skill Explanation Card
        SkillDetailCard(
            title = "IRON",
            rank = skills.iron,
            branch = SkillBranch.IRON,
            currentBonus = "+${SkillTree.ironPercent(skills.iron)}% Workout Coins",
            description = "Boosts coin rewards earned upon finishing and logging completed workout sessions (+4% per rank).",
            nextBonus = if (skills.iron < SkillTree.MAX_RANK) "+${SkillTree.ironPercent(skills.iron + 1)}% workout coins" else null,
            canSpend = unspent > 0 && skills.iron < SkillTree.MAX_RANK,
            onSpend = onSpendSkill,
        )

        // 3. Vitality Skill Explanation Card
        SkillDetailCard(
            title = "VITALITY",
            rank = skills.vitality,
            branch = SkillBranch.VITALITY,
            currentBonus = "+${SkillTree.vitalityPercent(skills.vitality)}% Daily Coins",
            description = "Multiplies coins rewarded from daily step milestones and restful sleep tracking (+5% per rank).",
            nextBonus = if (skills.vitality < SkillTree.MAX_RANK) "+${SkillTree.vitalityPercent(skills.vitality + 1)}% vitality coins" else null,
            canSpend = unspent > 0 && skills.vitality < SkillTree.MAX_RANK,
            onSpend = onSpendSkill,
        )

        // Respec action if points were spent
        if (skills.spent() > 0) {
            val canRespec = respecDaysRemaining == 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (canRespec) {
                        "Reset all points (${SkillTree.RESPEC_COST} coins)"
                    } else {
                        "Respec available in $respecDaysRemaining days"
                    },
                    color = if (canRespec) TorchAmber else SilverSlate,
                    fontSize = 11.sp,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = canRespec, onClick = onRespecSkills),
                )
            }
        }
    }
}

/**
 * Detailed card for a single skill branch explaining current bonus, next bonus, and description.
 */
@Composable
private fun SkillDetailCard(
    title: String,
    rank: Int,
    branch: SkillBranch,
    currentBonus: String,
    description: String,
    nextBonus: String?,
    canSpend: Boolean,
    onSpend: (SkillBranch) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val isMax = rank >= SkillTree.MAX_RANK

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(SlateGroove)
            .border(
                BorderStroke(1.dp, if (canSpend) ImperialGold.copy(alpha = 0.5f) else SlateBorderSubtle),
                RoundedCornerShape(spacing.cornerXs),
            )
            .padding(10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Header row with title, current bonus, rank, and upgrade button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = title,
                        fontFamily = determination,
                        color = ImperialGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "· $currentBonus",
                        fontFamily = determination,
                        color = if (rank > 0) VitalGreen else SilverSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = if (isMax) "MAX" else "$rank/${SkillTree.MAX_RANK}",
                        fontFamily = determination,
                        color = if (isMax) VitalGreen else SilverSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    if (canSpend) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(VitalGreen)
                                .clickable { onSpend(branch) }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+ UPGRADE",
                                fontFamily = determination,
                                color = SlateDeep,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // Description of what the skill does
            Text(
                text = description,
                color = SilverSteel,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )

            // Next rank preview if not at max
            if (nextBonus != null) {
                Text(
                    text = "Next: $nextBonus",
                    color = SilverSlate,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

/**
 * 16-bit SNES corner brackets in ImperialGold and PlatinumWhite.
 */
@Composable
private fun XpCornerBrackets(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cornerSize = 12.dp.toPx()
        val thickness = 2.5.dp.toPx()
        val gold = ImperialGold
        val white = PlatinumWhite

        // Top-left
        drawRect(gold, Offset(0f, 0f), Size(cornerSize, thickness))
        drawRect(gold, Offset(0f, 0f), Size(thickness, cornerSize))
        drawRect(white, Offset(0f, 0f), Size(thickness, thickness))

        // Top-right
        drawRect(gold, Offset(size.width - cornerSize, 0f), Size(cornerSize, thickness))
        drawRect(gold, Offset(size.width - thickness, 0f), Size(thickness, cornerSize))
        drawRect(white, Offset(size.width - thickness, 0f), Size(thickness, thickness))

        // Bottom-left
        drawRect(gold, Offset(0f, size.height - thickness), Size(cornerSize, thickness))
        drawRect(gold, Offset(0f, size.height - cornerSize), Size(thickness, cornerSize))
        drawRect(white, Offset(0f, size.height - thickness), Size(thickness, thickness))

        // Bottom-right
        drawRect(gold, Offset(size.width - cornerSize, size.height - thickness), Size(cornerSize, thickness))
        drawRect(gold, Offset(size.width - thickness, size.height - cornerSize), Size(thickness, cornerSize))
        drawRect(white, Offset(size.width - thickness, size.height - thickness), Size(thickness, thickness))
    }
}
