package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ADMIN,
    JUDGE_PRELIMINARY,
    JUDGE_FINAL,
    STAFF,
    MENTOR,
    TEAM_MEMBER,
    TEAM_LEADER,
    GUEST
}