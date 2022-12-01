/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.ui.theme.Border
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.ColorPrimary
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.TextGrey
import com.hover.stax.ui.theme.mainBackground

const val SECONDARY = 0
const val PRIMARY = 1
const val DISABLED = 2

@Composable
private fun StaxButton(text: String, icon: Int?, onClick: () -> Unit, buttonType: Int) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.wrapContentWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(13.dp),
        border = if (buttonType == SECONDARY) BorderStroke(1.dp, Border) else null,
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor(buttonType), disabledBackgroundColor = mainBackground),
        enabled = (buttonType != DISABLED)
    ) {
        Row(modifier = Modifier.wrapContentSize()) {
            if (icon != null) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.button,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

private fun backgroundColor(type: Int): Color {
    return if (type == PRIMARY) { BrightBlue } else { mainBackground }
}

private fun contentColor(type: Int): Color {
    return when (type) {
        PRIMARY -> ColorPrimary
        DISABLED -> TextGrey
        else -> OffWhite
    }
}

@Composable
fun PrimaryButton(text: String, icon: Int? = null, onClick: () -> Unit) {
    StaxButton(text = text, icon = icon, onClick = onClick, PRIMARY)
}

@Composable
fun SecondaryButton(text: String, icon: Int? = null, onClick: () -> Unit) {
    StaxButton(text = text, icon = icon, onClick = onClick, SECONDARY)
}

@Composable
fun DisabledButton(text: String, icon: Int? = null, onClick: () -> Unit) {
    StaxButton(text = text, icon = icon, onClick = onClick, DISABLED)
}