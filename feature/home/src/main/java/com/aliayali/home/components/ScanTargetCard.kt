package com.aliayali.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliayali.home.model.HomeUiData
import com.aliayali.model.ScanProfile

@Composable
fun ScanTargetCard(
    data: HomeUiData,
    onUrlChange: (String) -> Unit,
    onProfileChange: (ScanProfile) -> Unit,
    onStartScan: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Scan target",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = data.url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !data.isScanning,
                singleLine = true,
                placeholder = {
                    Text("https://example.com")
                },
                label = {
                    Text("Website URL")
                },
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(
                    onGo = { onStartScan() },
                ),
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Choose scan profile",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Select the wordlist that best matches your target.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            val profiles = ScanProfile.entries.toList()

            profiles.chunked(2).forEach { rowProfiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowProfiles.forEach { profile ->
                        ProfileOptionCard(
                            profile = profile,
                            selected = data.profile == profile,
                            enabled = !data.isScanning,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onProfileChange(profile)
                            },
                        )
                    }

                    if (rowProfiles.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Selected: ${data.profile.displayName()}",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = data.profile.description(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )

            if (data.errorMessage != null) {
                Spacer(Modifier.height(10.dp))

                Text(
                    text = data.errorMessage,
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onStartScan,
                enabled = !data.isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (data.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(19.dp)
                            .width(19.dp),
                        strokeWidth = 2.dp,
                    )

                    Spacer(Modifier.width(10.dp))
                    Text("Scanning…")
                } else {
                    Text("Start security scan")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Only scan websites you have permission to test.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileOptionCard(
    profile: ScanProfile,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier.clickable(
            enabled = enabled,
            onClick = onClick,
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                colors.primaryContainer
            } else {
                colors.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = if (selected) {
                    "✓ ${profile.displayName()}"
                } else {
                    profile.displayName()
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) {
                    colors.onPrimaryContainer
                } else {
                    colors.onSurface
                },
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "${profile.expectedPathCount()} paths",
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    colors.onPrimaryContainer
                } else {
                    colors.primary
                },
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = profile.shortDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    colors.onPrimaryContainer
                } else {
                    colors.onSurfaceVariant
                },
            )
        }
    }
}

private fun ScanProfile.displayName(): String = when (this) {
    ScanProfile.COMMON -> "Common"
    ScanProfile.WORDPRESS -> "WordPress"
    ScanProfile.DRUPAL -> "Drupal"
    ScanProfile.JOOMLA -> "Joomla"
    ScanProfile.API -> "API"
    ScanProfile.FRAMEWORK -> "Framework"
}

private fun ScanProfile.expectedPathCount(): Int = when (this) {
    ScanProfile.COMMON -> 500
    ScanProfile.WORDPRESS -> 500
    ScanProfile.DRUPAL -> 21
    ScanProfile.JOOMLA -> 141
    ScanProfile.API -> 109
    ScanProfile.FRAMEWORK -> 253
}

private fun ScanProfile.shortDescription(): String = when (this) {
    ScanProfile.COMMON -> "General web paths"
    ScanProfile.WORDPRESS -> "Core, plugins, themes"
    ScanProfile.DRUPAL -> "Modules and themes"
    ScanProfile.JOOMLA -> "Components and admin"
    ScanProfile.API -> "API and documentation"
    ScanProfile.FRAMEWORK -> "App and config paths"
}

private fun ScanProfile.description(): String = when (this) {
    ScanProfile.COMMON ->
        "General web paths for administration, configuration, backups, and common exposed files."

    ScanProfile.WORDPRESS ->
        "WordPress-specific paths for core files, admin, REST endpoints, plugins, themes, and uploads."

    ScanProfile.DRUPAL ->
        "Drupal-specific paths for core, modules, themes, and installation."

    ScanProfile.JOOMLA ->
        "Joomla-specific paths for administrator pages, components, modules, and plugins."

    ScanProfile.API ->
        "API routes, OpenAPI or Swagger documentation, health checks, and GraphQL endpoints."

    ScanProfile.FRAMEWORK ->
        "Common framework-related configuration, diagnostics, storage, and build paths."
}