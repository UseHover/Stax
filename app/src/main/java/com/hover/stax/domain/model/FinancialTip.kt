package com.hover.stax.domain.model

import java.util.*

data class FinancialTip(
    val id: String,
    val title: String,
    val content: String,
    val snippet: String,
    val date: Date?,
    val shareCopy: String?,
    val deepLink: String?
)
