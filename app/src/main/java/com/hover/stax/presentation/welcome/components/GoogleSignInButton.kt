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
package com.hover.stax.presentation.welcome.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.views.theme.NavGrey
import com.hover.stax.views.theme.OffWhite
import com.hover.stax.views.theme.StaxTheme

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = NavGrey,
            contentColor = OffWhite
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.margin_8)),
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "",
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                text = stringResource(id = R.string.continue_with_google),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
fun GoogleSignInButtonPreview() {
    StaxTheme {
        Surface(color = MaterialTheme.colors.background) {
            GoogleSignInButton {}
        }
    }
}