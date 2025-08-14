package com.openknights.app.feature.project.component.chip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openknights.designsystem.theme.KnightsColor
import com.openknights.designsystem.theme.KnightsTheme
import com.openknights.ui.TextChip

@Composable
internal fun TrackChip(text: String) {
    TextChip(
        text = text,
        containerColor = KnightsColor.Blue01,
        textColor = KnightsColor.White,
    )
}

@Composable
internal fun TrackChip(stringRes: Int) {
    TrackChip(
        text = stringResource(id = stringRes),
    )
}

@Preview
@Composable
private fun PreviewTrackChip() {
    KnightsTheme {
        Column(modifier = Modifier.padding(8.dp)) {
            TrackChip(text = "본선")
            TrackChip(text = "예선")
            TrackChip(text = "ETC")
        }
    }
}
