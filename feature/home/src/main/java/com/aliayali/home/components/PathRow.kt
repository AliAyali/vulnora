package com.aliayali.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliayali.home.model.TestedPathUi

@Composable
fun PathRow(
    path: TestedPathUi,
    onUrlClick: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val isHttp200 = path.statusCode == 200 && !path.isTarget

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                path.isFinding -> colors.errorContainer
                isHttp200 -> colors.primaryContainer
                else -> colors.surfaceContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = when {
                    path.isFinding -> colors.error
                    isHttp200 -> colors.primary
                    path.isTarget -> colors.secondaryContainer
                    else -> colors.surfaceVariant
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when {
                            path.isFinding -> "!"
                            isHttp200 -> "200"
                            path.isTarget -> "◎"
                            else -> "·"
                        },
                        color = when {
                            path.isFinding -> colors.onError
                            isHttp200 -> colors.onPrimary
                            path.isTarget -> colors.onSecondaryContainer
                            else -> colors.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = path.url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUrlClick(path.url) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        path.isFinding -> colors.onErrorContainer
                        isHttp200 -> colors.onPrimaryContainer
                        else -> colors.primary
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = when {
                        path.isTarget ->
                            "Target baseline · reference response"

                        path.isFinding ->
                            "Confirmed finding · inspect evidence"

                        path.statusCode == null ->
                            "Request failed or no HTTP response"

                        path.statusCode == 200 ->
                            "HTTP 200 · successful response; inspect content"

                        path.statusCode in 300..399 ->
                            "Redirect response"

                        path.statusCode == 401 || path.statusCode == 403 ->
                            "Access restricted"

                        path.statusCode == 404 ->
                            "Not found"

                        else ->
                            "HTTP response received"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        path.isFinding -> colors.onErrorContainer
                        isHttp200 -> colors.onPrimaryContainer
                        else -> colors.onSurfaceVariant
                    },
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    path.isFinding -> colors.error
                    isHttp200 -> colors.primary
                    path.statusCode == null -> colors.surfaceVariant
                    path.statusCode in 300..399 -> colors.tertiaryContainer
                    else -> colors.surfaceVariant
                },
            ) {
                Text(
                    text = path.statusCode?.toString() ?: "ERR",
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 6.dp,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        path.isFinding -> colors.onError
                        isHttp200 -> colors.onPrimary
                        path.statusCode in 300..399 ->
                            colors.onTertiaryContainer
                        else -> colors.onSurfaceVariant
                    },
                )
            }
        }
    }
}