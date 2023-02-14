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
package com.hover.stax.presentation.sims.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.home.MainActivity
import com.hover.stax.home.NavHelper
import com.hover.stax.presentation.components.SecondaryButton
import com.hover.stax.ui.theme.StaxTheme

@Composable
internal fun LinkSimCard() {
    val context = LocalContext.current
    SecondaryButton(stringResource(id = R.string.link_sim_to_stax),
        onClick = { NavHelper(context as MainActivity).requestBasicPerms() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp)
            .shadow(elevation = 0.dp),
    )
}

@Composable
@Preview
private fun LinkSimCardPreview() {
    StaxTheme {
        Surface(modifier = Modifier.wrapContentSize().padding(24.dp), color = MaterialTheme.colors.background) {
            LinkSimCard()
        }
    }
}