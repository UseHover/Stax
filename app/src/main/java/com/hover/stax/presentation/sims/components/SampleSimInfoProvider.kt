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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.hover.sdk.mocks.MockSimInfo
import com.hover.sdk.sims.SimInfo

class SampleSimInfoProvider : PreviewParameterProvider<SimInfo> {
    override val values: Sequence<SimInfo> = sequenceOf(
        MockSimInfo(1, 0, "6350111010101", "63501", "0984234325", "0703535226", "Safaricom", "ke", false),
        MockSimInfo(2, 1, "6350222222222", "63502", null, null, "Airtel", "ke", false),
        MockSimInfo(3, -1, "6340122222222", "63401", null, null, null, "ng", false)
    )
}