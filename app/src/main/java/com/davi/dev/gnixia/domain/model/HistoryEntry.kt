package com.davi.dev.gnixia.domain.model

data class HistoryEntry(
    val id: Long,
    val question: String,
    val answer: String,
    val createdAt: Long
)
