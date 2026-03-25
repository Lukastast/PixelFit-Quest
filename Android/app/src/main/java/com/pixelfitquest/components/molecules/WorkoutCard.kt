package com.pixelfitquest.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.model.Workout
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fourth_and_more),
            contentDescription = null,
            modifier = Modifier.size(120.dp, 140.dp),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = workout.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = workout.date.formatDate(),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy")
    .withZone(ZoneId.systemDefault())

fun String.formatDate(): String {
    return try {
        val instant = Instant.parse(this)
        dateFormatter.format(instant)
    } catch (e: Exception) {
        "Unknown date"
    }
}
