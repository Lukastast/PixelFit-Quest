package com.pixelfitquest.feature.levels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
import com.pixelfitquest.feature.levels.cosmetics.HomeThemeVisuals
import com.pixelfitquest.feature.levels.model.CosmeticDefinition
import com.pixelfitquest.feature.levels.model.CosmeticKind
import com.pixelfitquest.feature.levels.model.LevelUpResult
import com.pixelfitquest.feature.progression.SkillBranch
import com.pixelfitquest.feature.progression.SkillLoadout
import com.pixelfitquest.feature.progression.SkillTree
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LevelUpDialog(
    result: LevelUpResult,
    skills: SkillLoadout = SkillLoadout(),
    onSpendSkill: (SkillBranch) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    // Staggered entrance animations
    val entranceScale = remember { Animatable(0.4f) }
    val entranceAlpha = remember { Animatable(0f) }
    val contentOffset = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, tween(250))
    }
    LaunchedEffect(Unit) {
        entranceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            )
        )
    }
    LaunchedEffect(Unit) {
        contentOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, delayMillis = 100, easing = FastOutSlowInEasing),
        )
    }

    // Continuous celebration ambiance animations
    val infiniteTransition = rememberInfiniteTransition(label = "LevelUpCelebration")
    val sunburstRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "SunburstRotation",
    )
    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ParticleTime",
    )
    val emblemPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "EmblemPulse",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f)),
            contentAlignment = Alignment.Center,
        ) {
            // 16-bit rotating sunburst rays
            LevelUpSunburst(
                rotationDegrees = sunburstRotation,
                modifier = Modifier.size(540.dp),
            )

            // Floating 16-bit pixel sparkles and embers
            LevelUpParticles(
                time = particleTime,
                modifier = Modifier.fillMaxSize(),
            )

            // Main celebration modal card
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .heightIn(max = 620.dp)
                    .padding(horizontal = spacing.md, vertical = spacing.sm)
                    .graphicsLayer {
                        scaleX = entranceScale.value
                        scaleY = entranceScale.value
                        alpha = entranceAlpha.value
                    }
                    .clip(RoundedCornerShape(spacing.cornerMd))
                    .background(SlateDeep.copy(alpha = 0.98f))
                    .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd)),
            ) {
                // Classic 16-bit SNES corner brackets
                PixelCornerBrackets(modifier = Modifier.fillMaxSize())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Header Ribbon
                    LevelUpHeaderRibbon()

                    // Central Hero Level Crest
                    HeroLevelCrest(
                        result = result,
                        pulseScale = emblemPulse,
                    )

                    // Staggered rewards container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationY = contentOffset.value
                            },
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Coins reward chip
                        if (result.coinsGranted > 0) {
                            CoinsRewardChip(coinsGranted = result.coinsGranted)
                        }

                        // Skill points gain & distribution
                        val unspent = skills.unspent(result.current.level)
                        if (unspent > 0) {
                            SkillAllocationSection(
                                unspent = unspent,
                                skills = skills,
                                onSpendSkill = onSpendSkill,
                            )
                        }

                        // Cosmetic unlocks showcase
                        if (result.newlyUnlocked.isNotEmpty()) {
                            CosmeticUnlocksSection(newlyUnlocked = result.newlyUnlocked)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action button
                        PixelArtButton(
                            onClick = onDismiss,
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                        ) {
                            Text(
                                text = "CONTINUE",
                                fontFamily = determination,
                                color = ImperialGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelUpHeaderRibbon() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SlateGroove)
            .border(BorderStroke(1.dp, ImperialGold.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "★",
                color = ImperialGold,
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(R.string.levels_level_up_title).uppercase(),
                fontFamily = determination,
                color = ImperialGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Text(
                text = "★",
                color = ImperialGold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun HeroLevelCrest(
    result: LevelUpResult,
    pulseScale: Float,
) {
    val spacing = MaterialTheme.spacing
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .clip(RoundedCornerShape(spacing.cornerSm))
                .background(SlateGroove)
                .border(BorderStroke(2.dp, ImperialGold), RoundedCornerShape(spacing.cornerSm)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "LEVEL",
                    fontFamily = determination,
                    fontSize = 11.sp,
                    color = SilverSlate,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "${result.current.level}",
                    fontFamily = determination,
                    fontSize = 40.sp,
                    color = ImperialGold,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp,
                )
            }
        }

        // Level Transition Badge
        if (result.leveledUp) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerXs))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "Lv ${result.previous.level}",
                        fontFamily = determination,
                        fontSize = 11.sp,
                        color = SilverSlate,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "➔",
                        fontSize = 11.sp,
                        color = TorchAmber,
                    )
                    Text(
                        text = "Lv ${result.current.level}",
                        fontFamily = determination,
                        fontSize = 11.sp,
                        color = VitalGreen,
                        fontWeight = FontWeight.Bold,
                    )
                    if (result.levelsGained > 1) {
                        Text(
                            text = "(+${result.levelsGained})",
                            fontFamily = determination,
                            fontSize = 10.sp,
                            color = ImperialGold,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinsRewardChip(coinsGranted: Int) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(SlateGroove)
            .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerXs))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.levels_level_up_coins, coinsGranted).uppercase(),
                fontFamily = determination,
                color = RewardGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SkillAllocationSection(
    unspent: Int,
    skills: SkillLoadout,
    onSpendSkill: (SkillBranch) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateSurface.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerSm))
            .padding(spacing.xs),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.skills_points, unspent).uppercase(),
                fontFamily = determination,
                color = ImperialGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "TAP TO SPEND",
                fontFamily = determination,
                color = SilverSlate,
                fontSize = 10.sp,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SkillPickNode(
                label = stringResource(R.string.skill_form_short).uppercase(),
                rank = skills.form,
                branch = SkillBranch.FORM,
                canSpend = unspent > 0,
                onSpend = onSpendSkill,
                modifier = Modifier.weight(1f),
            )
            SkillPickNode(
                label = stringResource(R.string.skill_iron_short).uppercase(),
                rank = skills.iron,
                branch = SkillBranch.IRON,
                canSpend = unspent > 0,
                onSpend = onSpendSkill,
                modifier = Modifier.weight(1f),
            )
            SkillPickNode(
                label = stringResource(R.string.skill_vitality_short).uppercase(),
                rank = skills.vitality,
                branch = SkillBranch.VITALITY,
                canSpend = unspent > 0,
                onSpend = onSpendSkill,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SkillPickNode(
    label: String,
    rank: Int,
    branch: SkillBranch,
    canSpend: Boolean,
    onSpend: (SkillBranch) -> Unit,
    modifier: Modifier = Modifier,
) {
    val open = canSpend && rank < SkillTree.MAX_RANK
    val isMax = rank >= SkillTree.MAX_RANK
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(if (open) SlateGroove else SlateDeep)
            .border(
                BorderStroke(1.dp, if (open) ImperialGold else SlateBorderSubtle),
                RoundedCornerShape(spacing.cornerXs),
            )
            .clickable(enabled = open) { onSpend(branch) }
            .padding(horizontal = 6.dp, vertical = 6.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                fontFamily = determination,
                color = if (open) ImperialGold else SilverSteel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (isMax) "MAX" else "$rank/${SkillTree.MAX_RANK}",
                fontFamily = determination,
                color = if (isMax) VitalGreen else SilverSlate,
                fontSize = 10.sp,
            )
            if (open) {
                Text(
                    text = "+1",
                    fontFamily = determination,
                    color = VitalGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CosmeticUnlocksSection(newlyUnlocked: List<CosmeticDefinition>) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.levels_unlocked_header).uppercase(),
            fontFamily = determination,
            color = ImperialGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        newlyUnlocked.take(3).forEach { def ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateSurface.copy(alpha = 0.90f))
                    .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerXs))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = HomeThemeVisuals.previewRes(def)),
                        contentDescription = def.name,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = def.name,
                            fontFamily = determination,
                            color = PlatinumWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = cosmeticKindLabel(def.kind),
                            fontFamily = determination,
                            color = CrystalCyan,
                            fontSize = 10.sp,
                        )
                    }
                    Text(
                        text = stringResource(R.string.levels_unlocked).uppercase(),
                        fontFamily = determination,
                        color = VitalGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(VitalGreen.copy(alpha = 0.15f))
                            .border(1.dp, VitalGreen.copy(alpha = 0.4f), RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

private fun cosmeticKindLabel(kind: CosmeticKind): String = when (kind) {
    CosmeticKind.HOME_THEME -> "HOME THEME"
    CosmeticKind.CHARACTER_SKIN -> "CHARACTER SKIN"
    CosmeticKind.TITLE -> "TITLE"
}

/**
 * 16-bit retro sunburst ray effect drawn behind the modal.
 */
@Composable
private fun LevelUpSunburst(
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val center = this.center
        val radius = max(size.width, size.height) * 1.1f
        val rayCount = 16
        val anglePerRay = 360f / rayCount
        for (i in 0 until rayCount step 2) {
            val startAngle = rotationDegrees + i * anglePerRay
            val endAngle = startAngle + anglePerRay
            val path = Path().apply {
                moveTo(center.x, center.y)
                val rad1 = Math.toRadians(startAngle.toDouble())
                val rad2 = Math.toRadians(endAngle.toDouble())
                lineTo(
                    center.x + (radius * cos(rad1)).toFloat(),
                    center.y + (radius * sin(rad1)).toFloat(),
                )
                lineTo(
                    center.x + (radius * cos(rad2)).toFloat(),
                    center.y + (radius * sin(rad2)).toFloat(),
                )
                close()
            }
            drawPath(
                path = path,
                color = ImperialGold.copy(alpha = 0.08f),
            )
        }
    }
}

private data class PixelParticle(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val sizeDp: Float,
    val speed: Float,
    val swayFreq: Float,
    val swayAmp: Float,
    val color: Color,
    val isDiamond: Boolean,
)

/**
 * Floating 16-bit pixel sparkles and embers.
 */
@Composable
private fun LevelUpParticles(
    time: Float,
    modifier: Modifier = Modifier,
) {
    val particles = remember {
        val colors = listOf(ImperialGold, TorchAmber, PlatinumWhite, CrystalCyan, VitalGreen, EmberOrange)
        val random = Random(1337)
        List(26) { i ->
            PixelParticle(
                initialXRatio = random.nextFloat(),
                initialYRatio = random.nextFloat(),
                sizeDp = (3 + random.nextInt(4)).toFloat(),
                speed = 0.15f + random.nextFloat() * 0.25f,
                swayFreq = 1f + random.nextFloat() * 2f,
                swayAmp = 8f + random.nextFloat() * 14f,
                color = colors[i % colors.size],
                isDiamond = random.nextBoolean(),
            )
        }
    }

    Canvas(modifier = modifier) {
        val density = this.density
        particles.forEach { p ->
            val yNorm = ((p.initialYRatio - time * p.speed) % 1f + 1f) % 1f
            val y = yNorm * size.height
            val sway = sin(time * 2f * PI.toFloat() * p.swayFreq + p.initialXRatio * 10f) * p.swayAmp * density
            val x = (p.initialXRatio * size.width + sway).coerceIn(0f, size.width)
            val pSize = p.sizeDp * density
            val alpha = (sin(yNorm * PI.toFloat()) * 0.85f).coerceIn(0.1f, 1f)

            if (p.isDiamond) {
                rotate(45f, pivot = Offset(x, y)) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - pSize / 2f, y - pSize / 2f),
                        size = Size(pSize, pSize),
                    )
                }
            } else {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - pSize / 2f, y - pSize / 2f),
                    size = Size(pSize, pSize),
                )
            }
        }
    }
}

/**
 * Classic 16-bit SNES corner brackets in ImperialGold and PlatinumWhite.
 */
@Composable
private fun PixelCornerBrackets(modifier: Modifier = Modifier) {
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
