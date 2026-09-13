package com.pixelfitquest.components.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pixelfitquest.ui.theme.spacing

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AccountCenterCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onCardClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md)
        ) {
            Column(modifier = Modifier.weight(1f)) { Text(title) }
            Icon(icon, contentDescription = "Icon")
        }
    }
}

@Composable
fun Modifier.card(): Modifier {
    val spacing = MaterialTheme.spacing
    return this.padding(start = spacing.md, end = spacing.md, bottom = spacing.xs)
}
