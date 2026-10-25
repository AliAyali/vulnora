
package com.aliayali.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aliayali.home.components.EmptyScanActivity
import com.aliayali.home.components.HomeHeader
import com.aliayali.home.components.PathRow
import com.aliayali.home.components.ScanActivityHeader
import com.aliayali.home.components.ScanMetrics
import com.aliayali.home.components.ScanTargetCard
import com.aliayali.home.components.SecurityFindingRow
import com.aliayali.home.model.HomeUiData
import com.aliayali.model.ScanProfile

private enum class PathFilter {
    ALL,
    HTTP_200,
    CONFIRMED,
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onUrlChange: (String) -> Unit,
    onProfileChange: (ScanProfile) -> Unit,
    onStartScan: () -> Unit,
) {
    val data = (uiState as? HomeUiState.Success)?.data ?: HomeUiData()
    val colors = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current

    var pathFilter by remember {
        mutableStateOf(PathFilter.ALL)
    }

    val http200Paths = data.testedPaths.filter {
        it.statusCode == 200 && !it.isTarget
    }

    val confirmedPaths = data.testedPaths.filter {
        it.isFinding
    }

    val visiblePaths = when (pathFilter) {
        PathFilter.ALL -> data.testedPaths
        PathFilter.HTTP_200 -> http200Paths
        PathFilter.CONFIRMED -> confirmedPaths
    }

    Scaffold(
        containerColor = colors.background,
    ) { innerPadding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "top-spacer") {
                Spacer(Modifier.height(6.dp))
            }

            item(key = "home-header") {
                HomeHeader(
                    isScanning = data.isScanning,
                )
            }

            item(key = "page-title") {
                Column {
                    Text(
                        text = "Website security",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.onBackground,
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Inspect website paths, HTTP responses, and potential exposures.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            item(key = "scan-target-card") {
                ScanTargetCard(
                    data = data,
                    onUrlChange = onUrlChange,
                    onProfileChange = onProfileChange,
                    onStartScan = onStartScan,
                )
            }

            item(key = "scan-metrics") {
                ScanMetrics(data)
            }

            if (data.isScanning) {
                item(key = "scan-progress") {
                    Column {
                        Text(
                            text = "Scanning in progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )

                        Spacer(Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = {
                                data.progress.coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                        )
                    }
                }
            }

            item(key = "activity-header") {
                ScanActivityHeader(
                    isScanning = data.isScanning,
                )
            }

            if (
                data.testedPaths.isEmpty() &&
                data.securityFindings.isEmpty()
            ) {
                item(key = "empty-activity") {
                    EmptyScanActivity(
                        isScanning = data.isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    )
                }
            }

            // Security findings
            if (data.securityFindings.isNotEmpty()) {
                item(key = "finding-header") {
                    Text(
                        text = "Findings and manual reviews",
                        modifier = Modifier.padding(
                            top = 4.dp,
                            bottom = 2.dp,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                }

                items(
                    items = data.securityFindings,
                    key = {
                        "finding:${it.id}:${it.affectedUrl}"
                    },
                ) { finding ->
                    SecurityFindingRow(finding)
                }
            }

            // Tested paths title
            item(key = "paths-header") {
                Text(
                    text = "Tested paths (${data.testedCount}/${data.totalCount})",
                    modifier = Modifier.padding(
                        top = 6.dp,
                        bottom = 2.dp,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Horizontal filters
            item(key = "path-filters") {
                Column {
                    Text(
                        text = "Filter responses",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(6.dp))

                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState(),
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = pathFilter == PathFilter.ALL,
                            onClick = {
                                pathFilter = PathFilter.ALL
                            },
                            label = {
                                Text(
                                    "All (${data.testedPaths.size})",
                                )
                            },
                        )

                        FilterChip(
                            selected =
                                pathFilter == PathFilter.HTTP_200,
                            onClick = {
                                pathFilter = PathFilter.HTTP_200
                            },
                            label = {
                                Text(
                                    "HTTP 200 (${http200Paths.size})",
                                )
                            },
                        )

                        FilterChip(
                            selected =
                                pathFilter == PathFilter.CONFIRMED,
                            onClick = {
                                pathFilter = PathFilter.CONFIRMED
                            },
                            label = {
                                Text(
                                    "Confirmed (${confirmedPaths.size})",
                                )
                            },
                        )
                    }
                }
            }

            // Filtered paths
            if (visiblePaths.isEmpty()) {
                item(key = "empty-filter-result") {
                    Text(
                        text = when (pathFilter) {
                            PathFilter.ALL ->
                                if (
                                    data.testedPaths.isEmpty() &&
                                    data.securityFindings.isEmpty()
                                ) {
                                    "Start a scan to see tested paths."
                                } else {
                                    "No paths have been tested yet."
                                }

                            PathFilter.HTTP_200 ->
                                "No HTTP 200 responses have been recorded yet."

                            PathFilter.CONFIRMED ->
                                "No confirmed path exposures have been recorded yet."
                        },
                        modifier = Modifier.padding(
                            vertical = 16.dp,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            } else {
                items(
                    items = visiblePaths,
                    key = {
                        "path:${it.url}"
                    },
                ) { path ->
                    PathRow(
                        path = path,
                        onUrlClick = { url ->
                            runCatching {
                                uriHandler.openUri(url)
                            }
                        },
                    )
                }
            }

            // Footer
            item(key = "footer") {
                Text(
                    text = "VULNORA · only scan systems you own or are authorized to test",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 16.dp,
                        ),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}