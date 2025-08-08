package com.openknights.model

import java.time.LocalDate

data class Schedule(
    val id: Long,
    val term: String,
    val contestStart: LocalDate?,
    val preRoundStart: LocalDate?,
    val finalRoundStart: LocalDate?,
    val contestEnd: LocalDate?
)