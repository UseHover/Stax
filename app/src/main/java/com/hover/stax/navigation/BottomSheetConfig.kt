package com.hover.stax.navigation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import com.hover.stax.ui.theme.LargeRadius
import com.hover.stax.ui.theme.SmallRadius

@Immutable
data class BottomSheetConfig(
    val sheetShape: Shape,
    val showScrim: Boolean
)

val DefaultBottomSheetConfig = BottomSheetConfig(
    RoundedCornerShape(
        topStart = LargeRadius,
        topEnd = LargeRadius
    ),
    true
)