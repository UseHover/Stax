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
package com.hover.stax.domain.use_case

import com.hover.stax.storage.user.entity.StaxUser
import com.hover.stax.storage.user.repository.StaxUserRepository

class StaxUserUseCase(private val staxUserRepository: StaxUserRepository) {

    val user = staxUserRepository.getUserAsync()

    suspend fun saveUser(user: StaxUser) = staxUserRepository.saveUser(user)

    suspend fun deleteUser(user: StaxUser) = staxUserRepository.deleteUser(user)
}