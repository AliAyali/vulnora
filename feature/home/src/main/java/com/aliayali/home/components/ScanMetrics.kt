package com.aliayali.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aliayali.home.model.HomeUiData

@Composable
fun ScanMetrics(data: HomeUiData) {
    val http200Count = data.testedPaths.count {
        it.statusCode == 200 && !it.isTarget
    }

    val confirmedCount = maxOf(
        data.findingsCount,
        data.testedPaths.count { it.isFinding },
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Paths checked",
            value = if (data.totalCount > 0) {
                "${data.testedCount}/${data.totalCount}"
            } else {
                data.testedCount.toString()
            },
        )

        MetricCard(
            modifier = Modifier.weight(1f),
            label = "HTTP 200",
            value = http200Count.toString(),
        )

        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Confirmed",
            value = confirmedCount.toString(),
            isWarning = confirmedCount > 0,
        )
    }
}