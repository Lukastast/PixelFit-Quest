package com.pixelfitquest.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.model.SetReviewState
import com.pixelfitquest.ui.theme.determination

@Composable
fun SetReviewOverlay(
    review: SetReviewState,
    onAcceptCandidate: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMerge: (Int) -> Unit,
    onAdd: () -> Unit,
    onAdjustRom: (Int, Int) -> Unit,
    onSetRom: (Int, Int) -> Unit,
    onRedo: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1.4f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
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
            }
            items(review.reps, key = { it.index }) { rep ->
                RepRow(
                    rep = rep,
                    canMerge = rep.index < review.reps.lastIndex,
                    onAccept = { onAcceptCandidate(rep.index) },
                    onRemove = { onRemove(rep.index) },
                    onMerge = { onMerge(rep.index) },
                    onAdjustRom = { delta -> onAdjustRom(rep.index, delta) },
                    onSetRom = { percent -> onSetRom(rep.index, percent) },
                )
            }
        }

        Column(
            modifier = Modifier
                .width(180.dp)
                .fillMaxHeight()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
            Spacer(Modifier.height(4.dp))
            OverlayAction(stringResource(R.string.set_review_add), Color(0xFF2E7D32), onAdd)
            OverlayAction(stringResource(R.string.set_review_redo), Color(0xFF8A6A2F), onRedo)
            Spacer(Modifier.weight(1f))
            OverlayAction(stringResource(R.string.set_review_confirm), Color(0xFF1565C0), onConfirm)
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
) {
    val bg = if (rep.accepted) Color(0xFF2A3A4A) else Color(0xFF4A2A2A)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .padding(8.dp),
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
                    (rep.stabilityScore ?: 0f).toInt(),
                ),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
            )
        }
        if (rep.tags.isNotEmpty()) {
            Text(
                text = rep.tags.joinToString(" · "),
                color = Color(0xFFFFCC80),
                fontSize = 11.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!rep.accepted) {
                TextLink(stringResource(R.string.set_review_accept), Color(0xFF81C784), onAccept)
            }
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp),
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
        Spacer(Modifier.width(4.dp))
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
                RoundedCornerShape(6.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun OverlayAction(label: String, color: Color, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontFamily = determination,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
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
            .padding(top = 4.dp, end = 4.dp),
    )
}

private fun gradeColor(score: Float): Color = when {
    score >= 90 -> Color.Green
    score >= 70 -> Color.Yellow
    score >= 50 -> Color(0xFFFFA500)
    else -> Color.Red
}
