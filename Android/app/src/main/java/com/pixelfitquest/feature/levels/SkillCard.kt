package com.pixelfitquest.feature.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.progression.SkillBranch
import com.pixelfitquest.feature.progression.SkillLoadout
import com.pixelfitquest.feature.progression.SkillTree
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.spacing

@Composable
fun SkillCard(
    level: Int,
    skills: SkillLoadout,
    coins: Int,
    respecDaysRemaining: Int,
    onSpend: (SkillBranch) -> Unit,
    onRespec: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val unspent = skills.unspent(level)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerMd))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerMd))
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.skills_points, unspent),
            color = ImperialGold,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        SkillRow(SkillBranch.FORM, skills.form, stringResource(R.string.skill_form), unspent, onSpend)
        SkillRow(SkillBranch.IRON, skills.iron, stringResource(R.string.skill_iron), unspent, onSpend)
        SkillRow(SkillBranch.VITALITY, skills.vitality, stringResource(R.string.skill_vitality), unspent, onSpend)
        val respecLabel = when {
            skills.spent() <= 0 -> stringResource(R.string.skill_respec_empty)
            respecDaysRemaining > 0 -> stringResource(R.string.skill_respec_wait, respecDaysRemaining)
            else -> stringResource(R.string.skill_respec, SkillTree.RESPEC_COST)
        }
        val canRespec = skills.spent() > 0 && respecDaysRemaining == 0 && coins >= SkillTree.RESPEC_COST
        Text(
            text = respecLabel,
            color = if (canRespec) TorchAmber else SilverSlate,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = spacing.xxs)
                .clickable(enabled = canRespec, onClick = onRespec),
        )
    }
}

@Composable
private fun SkillRow(
    branch: SkillBranch,
    rank: Int,
    label: String,
    unspent: Int,
    onSpend: (SkillBranch) -> Unit,
) {
    val canSpend = unspent > 0 && rank < SkillTree.MAX_RANK
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = SilverSteel, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            text = "$rank/${SkillTree.MAX_RANK}",
            color = SilverSlate,
            fontSize = 12.sp,
        )
        Text(
            text = stringResource(R.string.skill_spend),
            color = if (canSpend) VitalGreen else SilverSlate,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable(enabled = canSpend) { onSpend(branch) },
        )
    }
}
