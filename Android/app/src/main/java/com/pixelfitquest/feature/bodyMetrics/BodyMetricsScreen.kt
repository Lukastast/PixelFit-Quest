package com.pixelfitquest.feature.bodyMetrics

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.ui.theme.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMetricsScreen(
    onBack: () -> Unit,
    viewModel: BodyMetricsViewModel = hiltViewModel(),
) {
    val userData by viewModel.userData.collectAsState(initial = null)

    var heightInput by remember { mutableStateOf("") }
    var armLengthInput by remember { mutableStateOf("") }

    LaunchedEffect(userData?.height) {
        heightInput = userData?.height?.toString().orEmpty()
    }
    LaunchedEffect(userData?.armLength) {
        armLengthInput = userData?.armLength?.let { formatArmLength(it) }.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.info_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_desc),
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onBack)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.body_metrics_title),
                    style = typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(
                R.string.current_height,
                userData?.height?.toString() ?: "--"
            ),
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.enter_height_hint),
            color = Color.White,
            fontSize = 12.sp
        )

        MetricInputField(
            value = heightInput,
            onValueChange = { raw ->
                if (raw.length <= 3) heightInput = raw.filter { it.isDigit() }
            },
            keyboardType = KeyboardType.Number,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(
                R.string.current_arm_length,
                userData?.armLength?.let { formatArmLength(it) } ?: "--"
            ),
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.enter_arm_length_hint),
            color = Color.White,
            fontSize = 12.sp
        )

        MetricInputField(
            value = armLengthInput,
            onValueChange = { raw ->
                armLengthInput = filterDecimalInput(raw, maxLen = 5)
            },
            keyboardType = KeyboardType.Decimal,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.body_metrics_help),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PixelArtButton(
            onClick = {
                val height = heightInput.toIntOrNull()
                val arm = armLengthInput.toFloatOrNull()
                viewModel.saveBoth(height, arm)
            },
            imageRes = R.drawable.button_unclicked,
            pressedRes = R.drawable.button_clicked,
            modifier = Modifier.size(220.dp, 60.dp)
        ) {
            Text(stringResource(R.string.body_metrics_save), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetricInputField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
) {
    Box(
        modifier = Modifier.size(200.dp, 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.inputfield),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.9f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black
            )
        )
    }
}

private fun formatArmLength(value: Float): String {
    return if (value == value.toInt().toFloat()) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

private fun filterDecimalInput(raw: String, maxLen: Int): String {
    val filtered = buildString {
        var sawDot = false
        for (c in raw) {
            when {
                c.isDigit() -> append(c)
                c == '.' && !sawDot -> {
                    append(c)
                    sawDot = true
                }
            }
        }
    }
    return if (filtered.length <= maxLen) filtered else filtered.take(maxLen)
}
