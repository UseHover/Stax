package com.hover.stax.domain.model

data class FinancialTip(
    val id: String,
    val title: String,
    val content: String,
    val snippet: String,
    val date: Long?,
    val shareCopy: String?,
    val deepLink: String?
)
