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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hover.stax.views.theme.Border
import com.hover.stax.views.theme.mainBackground

@Composable
fun StaxCard(content: @Composable ColumnScope.() -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, end = 6.dp, bottom = 13.dp, start = 6.dp),
        elevation = 3.dp,
        backgroundColor = mainBackground,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            content = content,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, end = 13.dp, bottom = 13.dp, start = 13.dp)
        )
    }
}