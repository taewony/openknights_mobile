package com.openknights.app.feature.project.component.chip

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.openknights.designsystem.theme.KnightsColor
import com.openknights.ui.TextChip
import com.openknights.feature.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun TimeChip(dateTime: LocalDateTime) {
    val pattern = stringResource(id = R.string.session_time_fmt)
    val formatter = remember { DateTimeFormatter.ofPattern(pattern) }
    val time = remember { dateTime.toLocalTime() }

    TextChip(
        text = formatter.format(time),
        containerColor = KnightsColor.Blue02A30,
        textColor = MaterialTheme.colorScheme.surfaceContainerLow,
    )
}

@Preview
@Composable
private fun TimeChipPreview() {
    TimeChip(LocalDateTime.of(2022, 1, 1, 10, 22))
}
