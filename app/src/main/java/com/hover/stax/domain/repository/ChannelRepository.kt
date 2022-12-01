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
package com.hover.stax.domain.repository

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel

interface ChannelRepository {

    suspend fun presentSims(): List<SimInfo>

    suspend fun getChannelsByIds(ids: List<Int>): List<Channel>

    suspend fun getChannelsByCountryCode(ids: IntArray, countryCode: String): List<Channel>

    suspend fun filterChannels(countryCode: String, actions: List<HoverAction>): List<Channel>
}