package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
enum class Phase(val label: String) {
    PLANED("예정"),
    REGISTERED("등록"),
    PRELIMINARY_SUBMITTED("예선 제출"),
    PRELIMINARY_PASSED("예선 통과"),
    FINAL_SUBMITTED("본선 제출"),
    FINALIST("본선 진출"),
    PRESENTATION("본선 발표"),
    AWARDED_GRAND("대상 수상"),
    AWARDED_EXCELLENCE("최우수상 수상"),
    AWARDED_ENCOURAGEMENT("우수상 수상"),
    DELETED("삭제"),
    FINISHED("종료")
}