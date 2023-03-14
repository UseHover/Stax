package com.hover.stax.send

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.views.composables.StaxHeader
import com.hover.stax.views.composables.StaxImage
import com.hover.stax.views.composables.StaxLayout
import com.hover.stax.views.composables.StaxModalCell

@Composable
fun PaymentTypeScreen(
    onClickBack: () -> Unit,
    actions: List<HoverAction>,
    onActionSelected: (HoverAction) -> Unit
) {

    StaxLayout(
        title = {
            StaxHeader(
                text = "Payment Type",
                onClickBack = onClickBack
            )
        },
        content = {
            items(actions) { action ->
                StaxModalCell(
                    modifier = Modifier
                        .heightIn(min = 70.dp),
                    onClick = { onActionSelected(action) },
                    header = action.name,
                    subHeader = "",
                    footer = "",
                    leftIcon = {
                        StaxImage(
                            imageUrl = action.from_institution_logo,
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.margin_34))
                                .clip(CircleShape)
                        )
                    }
                )
            }
        },
        footer = {
            // add button here
        }
    )
}