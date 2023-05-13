/*
 * Copyright 2023 Stax
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
package com.hover.stax.data.util

import com.hover.stax.database.models.StaxUser
import com.hover.stax.model.StaxUserDto

/**
 * Mapper to convert a [StaxUserDto] to a [StaxUser].
 */
fun StaxUserDto.toStaxUser(): StaxUser {
    return StaxUser(
        id = data.attributes.id,
        username = data.attributes.username,
        email = data.attributes.email,
        isMapper = data.attributes.isVerifiedMapper,
        marketingOptedIn = data.attributes.marketingOptedIn,
        transactionCount = data.attributes.transactionCount,
        bountyTotal = data.attributes.bountyTotal,
        totalPoints = data.attributes.totalPoints,
    )
}