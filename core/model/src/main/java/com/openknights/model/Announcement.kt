package com.openknights.model

data class Announcement(
  var id: String = "",
  var title: String = "",
  var body: String = "",
  var publishAt: Long = 0L,
  var endAt: Long = 0L,
  var createdBy: String = "",
  var priority: Int = 0
)