package com.aliayali.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliayali.model.FindingSeverity
import com.aliayali.model.SecurityFinding

@Composable
fun SecurityFindingRow(finding: SecurityFinding) {
    val colors = MaterialTheme.colorScheme
    val highSeverity = finding.severity == FindingSeverity.CRITICAL || finding.severity == FindingSeverity.HIGH
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
    ) {
        Column(Modifier.padding(14.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (highSeverity) colors.errorContainer else colors.tertiaryContainer,
            ) {
                Text(
                    text = finding.severity.name,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (highSeverity) colors.onErrorContainer else colors.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(9.dp))
            Text(
                text = finding.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = finding.affectedUrl,
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
            )
            finding.evidence?.takeIf(String::isNotBlank)?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Evidence: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            finding.recommendation?.takeIf(String::isNotBlank)?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Recommended action: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface,
                )
            }
        }
    }
}
