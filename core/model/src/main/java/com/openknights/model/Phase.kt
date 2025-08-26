package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
enum class Phase(val label: String) {
    PLANED("PLANED"),
    REGISTERED("REGISTERED"),
    PRELIMINARY_SUBMITTED("PRELIMINARY_SUBMITTED"),
    PRELIMINARY_PASSED("PRELIMINARY_PASSED"),
    FINAL_SUBMITTED("FINAL_SUBMITTED"),
    FINALIST("본선 진출"),
    PRESENTATION("본선 발표"),
    AWARDED_GRAND("대상 수상"),
    AWARDED_EXCELLENCE("최우수상 수상"),
    AWARDED_ENCOURAGEMENT("우수상 수상"),
    DELETED("DELETED"),
    FINISHED("FINISHED")
}