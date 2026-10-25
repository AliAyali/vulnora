package com.aliayali.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeHeader(isScanning: Boolean) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.padding(top = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "V",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "VULNORA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                )

                Text(
                    text = "WEB SECURITY SCANNER",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(20.dp))

        Surface(
            shape = CircleShape,
            color = if (isScanning) {
                colors.primaryContainer
            } else {
                colors.secondaryContainer
            },
        ) {
            Text(
                text = if (isScanning) "Scanning" else "Ready",
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 7.dp,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (isScanning) {
                    colors.onPrimaryContainer
                } else {
                    colors.onSecondaryContainer
                },
            )
        }
    }
}