package com.openknights.feature.project.projectdetail.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.openknights.ui.OutlineChip
import com.openknights.designsystem.theme.KnightsTheme
import com.openknights.model.Project

@Composable
internal fun ProjectDetailChips(project: Project) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        project.phase?.let { phase ->
            OutlineChip(text = phase.label, borderColor = MaterialTheme.colorScheme.primary, textColor = MaterialTheme.colorScheme.onSurface)
        }
    }
}
