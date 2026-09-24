package com.pixelfitquest.feature.levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.levels.model.CosmeticDefinition
import com.pixelfitquest.feature.levels.model.LevelUpResult
import com.pixelfitquest.feature.progression.SkillBranch
import com.pixelfitquest.feature.progression.SkillLoadout
import com.pixelfitquest.feature.progression.SkillTree
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.SilverSteel

@Composable
fun LevelUpDialog(
    result: LevelUpResult,
    skills: SkillLoadout = SkillLoadout(),
    onSpendSkill: (SkillBranch) -> Unit = {},
    onDismiss: () -> Unit,
    onOpenRewards: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .height(
                    when {
                        skills.unspent(result.current.level) > 0 -> 420.dp
                        result.coinsGranted > 0 || result.newlyUnlocked.isNotEmpty() -> 320.dp
                        else -> 280.dp
                    },
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.questloginboard_wider),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.levels_level_up_title),
                    color = ImperialGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.levels_level_up_body, result.current.level),
                    color = ImperialGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (result.coinsGranted > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.levels_level_up_coins, result.coinsGranted),
                        color = RewardGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                val unspent = skills.unspent(result.current.level)
                if (unspent > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.skills_points, unspent),
                        color = SilverSteel,
                        fontSize = 12.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkillPick(stringResource(R.string.skill_form_short), skills.form, SkillBranch.FORM, onSpendSkill)
                        SkillPick(stringResource(R.string.skill_iron_short), skills.iron, SkillBranch.IRON, onSpendSkill)
                        SkillPick(stringResource(R.string.skill_vitality_short), skills.vitality, SkillBranch.VITALITY, onSpendSkill)
                    }
                }
                if (result.newlyUnlocked.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.levels_unlocked_header),
                        color = SilverSteel,
                        fontSize = 12.sp,
                    )
                    result.newlyUnlocked.take(3).forEach { def ->
                        Text(
                            text = unlockLine(def),
                            color = ImperialGold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                PixelArtButton(
                    onClick = onOpenRewards,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(200.dp, 44.dp),
                ) {
                    Text(stringResource(R.string.levels_view_rewards), color = SilverSteel, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                PixelArtButton(
                    onClick = onDismiss,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(200.dp, 44.dp),
                ) {
                    Text(stringResource(R.string.levels_later), color = SilverSteel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun unlockLine(def: CosmeticDefinition): String = "${def.name}  ·  Lv ${def.unlockLevel}"

@Composable
private fun SkillPick(
    label: String,
    rank: Int,
    branch: SkillBranch,
    onSpend: (SkillBranch) -> Unit,
) {
    val open = rank < SkillTree.MAX_RANK
    Text(
        text = label,
        color = if (open) ImperialGold else SilverSteel,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(enabled = open) { onSpend(branch) },
    )
}
