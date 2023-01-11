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

import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.AuthResponse
import com.hover.stax.data.remote.dto.authorization.TokenResponse

interface AuthRepository {

    suspend fun authorizeClient(idToken: String): AuthResponse

    suspend fun fetchTokenInfo(code: String): TokenResponse

    suspend fun revokeToken()

    suspend fun uploadUserToStax(userDTO: UserUploadDto): StaxUserDto

    suspend fun updateUser(email: String, userDTO: UserUpdateDto): StaxUserDto
}