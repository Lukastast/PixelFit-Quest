package com.pixelfitquest.feature.levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.pixelfitquest.ui.theme.RewardGold

@Composable
fun LevelUpDialog(
    result: LevelUpResult,
    onDismiss: () -> Unit,
    onOpenRewards: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .height(280.dp)
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
                    color = RewardGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.levels_level_up_body, result.current.level),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (result.newlyUnlocked.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.levels_unlocked_header),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                    )
                    result.newlyUnlocked.take(3).forEach { def ->
                        Text(
                            text = unlockLine(def),
                            color = RewardGold,
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
                    Text(stringResource(R.string.levels_view_rewards), color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                PixelArtButton(
                    onClick = onDismiss,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(200.dp, 44.dp),
                ) {
                    Text(stringResource(R.string.levels_later), color = Color.White)
                }
            }
        }
    }
}

private fun unlockLine(def: CosmeticDefinition): String = "${def.name}  ·  Lv ${def.unlockLevel}"
