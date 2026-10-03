package com.pixelfitquest.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.analysis.BAR_EVEN_DEG
import com.pixelfitquest.feature.workout.analysis.CoachingTags
import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.TAG_CLIP_POSE
import com.pixelfitquest.feature.workout.model.SetReviewState
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import java.util.Locale

@Composable
fun SetReviewOverlay(
    review: SetReviewState,
    currentWeight: Float = 0f,
    onAdjustWeight: ((Float) -> Unit)? = null,
    onAcceptCandidate: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMerge: (Int) -> Unit,
    onAdd: () -> Unit,
    onAdjustRom: (Int, Int) -> Unit,
    onSetRom: (Int, Int) -> Unit,
    onToggleAssisted: (Int) -> Unit,
    unilateral: Boolean = false,
    side: String? = null,
    onSide: (String) -> Unit = {},
    onNotesChanged: (String) -> Unit = {},
    onRedo: () -> Unit,
    onConfirm: () -> Unit,
) {
    val spacing = LocalSpacing.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(spacing.sm),
    ) {
        val landscape = maxWidth > maxHeight
        val listContent: LazyListScope.() -> Unit = {
            item {
                Text(
                    text = stringResource(
                        R.string.set_review_title,
                        review.setNumber,
                        review.exerciseName,
                    ),
                    color = Color.White,
                    fontFamily = determination,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.set_review_count, review.acceptedCount),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                )
                if (TAG_CLIP_POSE in review.analysis.flags) {
                    Text(
                        text = stringResource(R.string.tag_clip_pose),
                        color = Color(0xFFFFCC80),
                        fontSize = 12.sp,
                    )
                }
                if (unilateral) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = spacing.xxs),
                    ) {
                        RomChip(
                            label = stringResource(R.string.set_side_left),
                            selected = side == "L",
                            onClick = { onSide("L") },
                        )
                        RomChip(
                            label = stringResource(R.string.set_side_right),
                            selected = side == "R",
                            onClick = { onSide("R") },
                        )
                    }
                }
            }
            items(review.reps, key = { it.index }) { rep ->
                val rowIndex = review.reps.indexOfFirst { it.index == rep.index }
                RepRow(
                    rep = rep,
                    canMerge = shouldOfferMerge(review.reps, rowIndex),
                    onAccept = { onAcceptCandidate(rep.index) },
                    onRemove = { onRemove(rep.index) },
                    onMerge = { onMerge(rep.index) },
                    onAdjustRom = { delta -> onAdjustRom(rep.index, delta) },
                    onSetRom = { percent -> onSetRom(rep.index, percent) },
                    onToggleAssisted = { onToggleAssisted(rep.index) },
                )
            }
            item(key = "set_notes_section") {
                SetNotesCard(
                    setNumber = review.setNumber,
                    initialNotes = review.notes,
                    onNotesChanged = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (landscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing.scale(6)),
                    content = listContent,
                )
                SetReviewActions(
                    review = review,
                    currentWeight = currentWeight,
                    onAdjustWeight = onAdjustWeight,
                    onAdd = onAdd,
                    onRedo = onRedo,
                    onConfirm = onConfirm,
                    modifier = Modifier
                        .width(spacing.scale(180))
                        .fillMaxHeight(),
                    stacked = true,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.scale(6)),
                    content = listContent,
                )
                SetReviewActions(
                    review = review,
                    currentWeight = currentWeight,
                    onAdjustWeight = onAdjustWeight,
                    onAdd = onAdd,
                    onRedo = onRedo,
                    onConfirm = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    stacked = false,
                )
            }
        }
    }
}

@Composable
private fun SetReviewActions(
    review: SetReviewState,
    currentWeight: Float,
    onAdjustWeight: ((Float) -> Unit)?,
    onAdd: () -> Unit,
    onRedo: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    stacked: Boolean,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(spacing.cornerMd))
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        val logOnly = review.analysis.flags.contains("log_only")
        if (logOnly) {
            Text(
                text = stringResource(R.string.set_review_log_only_title),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = stringResource(R.string.set_review_log_only_hint),
                color = Color.Gray,
                fontSize = 12.sp,
            )
        } else {
            Text(
                text = stringResource(R.string.set_review_form, review.meanFormScore.toInt()),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = stringResource(R.string.set_review_samples, review.sampleCount),
                color = Color.Gray,
                fontSize = 12.sp,
            )
        }

        if (onAdjustWeight != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(Color(0xFF252525))
                    .padding(horizontal = spacing.xs, vertical = spacing.scale(4)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEIGHT",
                    fontFamily = determination,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.scale(3))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(Color(0xFF383838))
                            .clickable { onAdjustWeight(-2.5f) }
                            .padding(horizontal = spacing.scale(5), vertical = spacing.scale(2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-2.5", fontFamily = determination, fontSize = 10.sp, color = Color.White)
                    }
                    Text(
                        text = if (currentWeight % 1f == 0f) "${currentWeight.toInt()} kg" else String.format(Locale.US, "%.1f kg", currentWeight),
                        fontFamily = determination,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGold,
                        modifier = Modifier.padding(horizontal = spacing.xxs)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(Color(0xFF383838))
                            .clickable { onAdjustWeight(2.5f) }
                            .padding(horizontal = spacing.scale(5), vertical = spacing.scale(2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+2.5", fontFamily = determination, fontSize = 10.sp, color = VitalGreen)
                    }
                }
            }
        }
        if (stacked) {
            Spacer(Modifier.height(spacing.xxs))
            OverlayAction(
                stringResource(R.string.set_review_add),
                Color(0xFF2E7D32),
                onAdd,
                Modifier.fillMaxWidth(),
            )
            OverlayAction(
                stringResource(R.string.set_review_redo),
                Color(0xFF8A6A2F),
                onRedo,
                Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
            OverlayAction(
                stringResource(R.string.set_review_confirm),
                Color(0xFF1565C0),
                onConfirm,
                Modifier.fillMaxWidth(),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                OverlayAction(
                    stringResource(R.string.set_review_add),
                    Color(0xFF2E7D32),
                    onAdd,
                    Modifier.weight(1f),
                )
                OverlayAction(
                    stringResource(R.string.set_review_redo),
                    Color(0xFF8A6A2F),
                    onRedo,
                    Modifier.weight(1f),
                )
                OverlayAction(
                    stringResource(R.string.set_review_confirm),
                    Color(0xFF1565C0),
                    onConfirm,
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RepRow(
    rep: DetectedRep,
    canMerge: Boolean,
    onAccept: () -> Unit,
    onRemove: () -> Unit,
    onMerge: () -> Unit,
    onAdjustRom: (Int) -> Unit,
    onSetRom: (Int) -> Unit,
    onToggleAssisted: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val bg = if (rep.accepted) Color(0xFF2A3A4A) else Color(0xFF4A2A2A)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(spacing.cornerSm))
            .padding(spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.set_review_rep_index, rep.index + 1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.score_out_of_100, rep.formScore.toInt()),
                color = gradeColor(rep.formScore),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
        RomOverrideRow(
            percent = rep.romScore.toInt(),
            onAdjust = onAdjustRom,
            onSet = onSetRom,
        )
        if (!rep.isManual && (rep.stabilityScore != null || rep.tempoScore != null)) {
            Text(
                text = stringResource(
                    R.string.set_review_rep_meta,
                    (rep.eccentricMs / 100) / 10f,
                    (rep.concentricMs / 100) / 10f,
                ),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
            )
        }
        if (!rep.isManual && (rep.levelDeg != null || rep.twistDeg != null)) {
            Text(
                text = stringResource(
                    R.string.set_review_rep_imbalance,
                    levelLabel(rep.levelDeg ?: 0f),
                    twistLabel(rep.twistDeg ?: 0f),
                ),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
            )
        }
        val visibleTags = CoachingTags.visible(rep.tags)
        if (visibleTags.isNotEmpty()) {
            Text(
                text = visibleTags.map { tag ->
                    CoachingTags.labelRes(tag)?.let { res -> stringResource(res) } ?: tag
                }.joinToString(" · "),
                color = Color(0xFFFFCC80),
                fontSize = 11.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.scale(10))) {
            if (!rep.accepted) {
                TextLink(stringResource(R.string.set_review_accept), Color(0xFF81C784), onAccept)
            }
            TextLink(
                stringResource(if (rep.assisted) R.string.set_review_assisted else R.string.set_review_assist),
                if (rep.assisted) ImperialGold else Color(0xFFFFE082),
                onToggleAssisted,
            )
            TextLink(stringResource(R.string.set_review_remove), Color(0xFFEF9A9A), onRemove)
            if (canMerge) {
                TextLink(stringResource(R.string.set_review_merge), Color(0xFF90CAF9), onMerge)
            }
        }
    }
}

@Composable
private fun RomOverrideRow(
    percent: Int,
    onAdjust: (Int) -> Unit,
    onSet: (Int) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.scale(6)),
        modifier = Modifier.padding(top = spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.set_review_rom_label),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
        )
        RomChip("−", onClick = { onAdjust(-10) })
        Text(
            text = stringResource(R.string.set_review_rom_percent, percent),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        RomChip("+", onClick = { onAdjust(10) })
        Spacer(Modifier.width(spacing.xxs))
        RomChip("50%", selected = percent == 50, onClick = { onSet(50) })
        RomChip("100%", selected = percent == 100, onClick = { onSet(100) })
    }
}

@Composable
private fun RomChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(
                if (selected) Color(0xFF1565C0) else Color(0xFF3A4A5A),
                RoundedCornerShape(LocalSpacing.current.scale(6)),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = LocalSpacing.current.xs, vertical = LocalSpacing.current.xxs),
    )
}

@Composable
private fun OverlayAction(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = Color.White,
        fontFamily = determination,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(color, RoundedCornerShape(LocalSpacing.current.cornerSm))
            .clickable(onClick = onClick)
            .padding(vertical = LocalSpacing.current.scale(10)),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun TextLink(label: String, color: Color, onClick: () -> Unit) {
    Text(
        text = label,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(top = LocalSpacing.current.xxs, end = LocalSpacing.current.xxs),
    )
}

private fun gradeColor(score: Float): Color = when {
    score >= 90 -> Color.Green
    score >= 70 -> Color.Yellow
    score >= 50 -> Color(0xFFFFA500)
    else -> Color.Red
}

private fun shouldOfferMerge(reps: List<DetectedRep>, index: Int): Boolean {
    if (index < 0 || index >= reps.lastIndex) return false
    return looksLikeSplit(reps[index]) || looksLikeSplit(reps[index + 1])
}

private fun looksLikeSplit(rep: DetectedRep): Boolean =
    "candidate" in rep.tags || "truncated" in rep.tags

private fun levelLabel(deg: Float): String {
    val mag = kotlin.math.abs(deg).toInt()
    return when {
        deg > BAR_EVEN_DEG -> "R $mag°"
        deg < -BAR_EVEN_DEG -> "L $mag°"
        else -> "even"
    }
}

private fun twistLabel(deg: Float): String {
    val mag = kotlin.math.abs(deg).toInt()
    return when {
        deg > BAR_EVEN_DEG -> "$mag° toward head"
        deg < -BAR_EVEN_DEG -> "$mag° toward hip"
        else -> "even"
    }
}

@Composable
private fun SetNotesCard(
    setNumber: Int,
    initialNotes: String,
    onNotesChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    var text by rememberSaveable(setNumber) { mutableStateOf(initialNotes) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep, RoundedCornerShape(spacing.cornerSm))
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerSm))
            .padding(spacing.xs),
        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.set_notes_title),
                color = ImperialGold,
                fontFamily = determination,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
            if (text.isNotBlank()) {
                Text(
                    text = "${text.length} chars",
                    color = SilverSlate,
                    fontSize = 10.sp,
                )
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onNotesChanged(it)
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.set_notes_hint),
                    color = SilverSlate.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 12.sp,
            ),
            shape = RoundedCornerShape(spacing.cornerXs),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ImperialGold,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = SlateGroove,
                unfocusedContainerColor = SlateGroove,
                cursorColor = ImperialGold,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
        )
    }
}
