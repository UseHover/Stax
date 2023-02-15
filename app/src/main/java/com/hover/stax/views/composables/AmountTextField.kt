package com.hover.stax.views.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmountTextField(
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Unspecified)
            .padding(16.dp)
            .fillMaxWidth()
            .scaleClickAnimation(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = amount,
            color = Color.White,
            fontSize = 32.sp
        )
    }
}

enum class State { Pressed, Idle }

fun Modifier.scaleClickAnimation(
    enabled: Boolean = true
): Modifier = composed {

    var state by remember { mutableStateOf(State.Idle) }

    @Suppress("MagicNumber")
    val scale by animateFloatAsState(
        targetValue = if (enabled && state == State.Pressed) {
            0.97f
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 200)
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {}
        )
        .pointerInput(state) {
            awaitPointerEventScope {
                state = if (state == State.Pressed) {
                    waitForUpOrCancellation()
                    State.Idle
                } else {
                    awaitFirstDown(false)
                    State.Pressed
                }
            }
        }
}

@Preview
@Composable
fun AmountTextFieldPreview() {
    AmountTextField(amount = "10000")
}