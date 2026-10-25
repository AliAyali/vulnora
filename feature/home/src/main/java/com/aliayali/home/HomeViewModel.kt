package com.aliayali.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliayali.domain.StartScanUseCase
import com.aliayali.home.model.HomeUiData
import com.aliayali.home.model.TestedPathUi
import com.aliayali.model.ScanProfile
import com.aliayali.model.ScanProgress
import com.aliayali.model.ScanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startScanUseCase: StartScanUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Success())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onUrlChange(value: String) {
        updateData {
            it.copy(
                url = value,
                testedCount = 0,
                totalCount = 0,
                testedPaths = emptyList(),
                securityFindings = emptyList(),
                findingsCount = 0,
                progress = 0f,
                errorMessage = null,
                scanCompleted = false,
            )
        }
    }

    fun onProfileChange(profile: ScanProfile) {
        if (currentData().isScanning) return
        updateData {
            it.copy(
                profile = profile,
                testedCount = 0,
                totalCount = 0,
                testedPaths = emptyList(),
                securityFindings = emptyList(),
                findingsCount = 0,
                progress = 0f,
                errorMessage = null,
                scanCompleted = false,
            )
        }
    }

    fun startScan() {
        val data = currentData()
        if (data.isScanning) return

        val input = data.url.trim()
        if (input.isBlank()) {
            updateData { it.copy(errorMessage = "Enter a website URL to scan.") }
            return
        }

        val withScheme = if (
            input.startsWith("https://", ignoreCase = true) ||
            input.startsWith("http://", ignoreCase = true)
        ) input else "https://$input"

        val uri = runCatching { URI(withScheme) }.getOrNull()
        val validUrl = uri != null &&
            (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
            !uri.host.isNullOrBlank() &&
            uri.rawUserInfo == null &&
            uri.rawQuery == null &&
            uri.rawFragment == null

        if (!validUrl) {
            updateData {
                it.copy(errorMessage = "Enter a valid HTTP or HTTPS base URL without query parameters or a fragment.")
            }
            return
        }

        val normalizedUrl = withScheme.trim().trimEnd('/')
        val selectedProfile = data.profile

        updateData {
            HomeUiData(
                url = normalizedUrl,
                profile = selectedProfile,
                isScanning = true,
                progress = 0f,
                testedPaths = emptyList(),
                securityFindings = emptyList(),
                findingsCount = 0,
                testedCount = 0,
                totalCount = 0,
            )
        }

        viewModelScope.launch {
            try {
                val result = startScanUseCase(
                    url = normalizedUrl,
                    profile = selectedProfile,
                ) { progress -> applyProgress(progress) }

                updateData { current ->
                    val countableFindings = result.findings.count {
                        it.id.startsWith("exposure-") || it.id == "directory-listing"
                    }
                    current.copy(
                        isScanning = false,
                        progress = if (result.status == ScanStatus.COMPLETED) 1f else current.progress,
                        securityFindings = result.findings,
                        findingsCount = countableFindings,
                        errorMessage = result.errorMessage,
                        scanCompleted = result.status == ScanStatus.COMPLETED,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                updateData {
                    it.copy(
                        isScanning = false,
                        errorMessage = exception.message
                            ?: "Unexpected error (${exception.javaClass.simpleName}).",
                    )
                }
            }
        }
    }

    private fun applyProgress(progress: ScanProgress) {
        val isTarget = progress.testedCount == 0
        val row = TestedPathUi(
            url = progress.url,
            statusCode = progress.statusCode,
            isFinding = progress.isFinding,
            isTarget = isTarget,
        )

        updateData { current ->
            val updatedRows = if (current.testedPaths.any { it.url == row.url }) {
                current.testedPaths.map { existing -> if (existing.url == row.url) row else existing }
            } else {
                current.testedPaths + row
            }
            val testedCount = maxOf(current.testedCount, progress.testedCount)
            current.copy(
                testedPaths = updatedRows,
                testedCount = testedCount,
                totalCount = progress.totalCount,
                progress = if (progress.totalCount > 0) {
                    (testedCount.toFloat() / progress.totalCount).coerceIn(0f, 1f)
                } else {
                    0f
                },
            )
        }
    }

    private fun currentData(): HomeUiData =
        (_uiState.value as? HomeUiState.Success)?.data ?: HomeUiData()

    private fun updateData(transform: (HomeUiData) -> HomeUiData) {
        _uiState.update { state ->
            when (state) {
                is HomeUiState.Success -> state.copy(data = transform(state.data))
                is HomeUiState.Error -> HomeUiState.Success(data = transform(HomeUiData()))
                HomeUiState.Loading -> HomeUiState.Success(data = transform(HomeUiData()))
            }
        }
    }
}
