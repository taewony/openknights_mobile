package com.openknights.feature.project.projectdetail.component

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.openknights.ui.KnightsTopAppBar
import com.openknights.ui.TopAppBarNavigationType
import com.openknights.designsystem.theme.KnightsTheme
import com.openknights.feature.R

import androidx.compose.ui.res.stringResource

@Composable
internal fun ProjectDetailTopAppBar(
    onBackClick: () -> Unit,
) {
    KnightsTopAppBar(
        title = stringResource(id = R.string.project_detail_screen_title),
        navigationIconContentDescription = null,
        navigationType = TopAppBarNavigationType.Back,
        onNavigationClick = onBackClick,
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ProjectDetailTopAppBarPreview() {
    KnightsTheme {
        ProjectDetailTopAppBar(
            onBackClick = { }
        )
    }
}