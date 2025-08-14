package com.openknights.feature.project.projectdetail.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openknights.designsystem.theme.KnightsTheme
import com.openknights.designsystem.theme.knightsTypography
import com.openknights.model.Role
import com.openknights.model.User
import com.openknights.ui.NetworkImage
import com.openknights.feature.R

/**
 * `ProjectDetailSpeaker`는 프로젝트에 참여한 발표자(사용자)의 정보를 표시하는 Composable입니다.
 * `MaterialTheme.knightsTypography`를 사용하여 커스텀 정의된 텍스트 스타일을 적용합니다.
 */
@Composable
internal fun ProjectDetailSpeaker(
    user: User,
    role: Role,
    modifier: Modifier = Modifier,
) {
    // Row를 사용하여 이미지와 텍스트를 가로로 배치합니다.
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically, // 세로 중앙 정렬
        horizontalArrangement = Arrangement.spacedBy(16.dp) // 이미지와 텍스트 사이 간격
    ) {
        // 왼쪽: 아바타 이미지
        // user.imageUrl이 유효하면 네트워크 이미지를, 그렇지 않으면 로컬 기본 아바타 이미지를 표시합니다.
        if (user.imageUrl.isNullOrEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.default_avatar),
                contentDescription = "Default Avatar",
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
            )
        } else {
            NetworkImage(
                imageUrl = user.imageUrl,
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape),
                contentDescription = "User Avatar"
            )
        }

        // 오른쪽: 텍스트 정보들을 담는 Column
        Column {
            Text(
                text = role.name, // 역할 이름
                style = MaterialTheme.knightsTypography.labelSmallM,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = user.name, // 사용자 이름
                style = MaterialTheme.knightsTypography.titleMediumB,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            // 역할/이름과 자기소개 사이의 간격
            // Spacer(Modifier.height(16.dp)) // 이 부분은 디자인에 따라 조절하거나 제거할 수 있습니다.

            Text(
                text = user.introduction, // 자기소개
                style = MaterialTheme.knightsTypography.titleSmallR140,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Preview
@Composable
private fun ProjectDetailSpeakerPreview() {
    KnightsTheme {
        ProjectDetailSpeaker(
            user = User("xxx", name = "xxx"), // FakeUsers.users.first(),
            role = Role.TEAM_LEADER
        )
    }
}