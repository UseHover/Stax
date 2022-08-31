package com.hover.stax.utils.network

import com.hover.stax.domain.model.TokenInfo
import com.hover.stax.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.component.KoinComponent

class TokenAuthenticator(private val authRepository: AuthRepository) : Authenticator, KoinComponent {

    override fun authenticate(route: Route?, response: Response): Request? {
        val tokenInfo: TokenInfo? = getTokenInfo()

        if (!requestHasAccessToken(response.request) || tokenInfo == null) {
            return null
        }

        synchronized(this) {
            val newAccessToken = getTokenInfo()?.accessToken

            //token has been refreshed in another thread
            if (tokenInfo.accessToken != newAccessToken) {
                return rebuildRequest(response.request, newAccessToken!!)
            }

            //refresh token
            val refreshedTokenInfo = refreshToken()
            return rebuildRequest(response.request, refreshedTokenInfo.accessToken)
        }

    }

    private fun getTokenInfo(): TokenInfo? = runBlocking {
        withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            authRepository.getTokenInfo()
        }
    }

    private fun refreshToken(): TokenInfo = runBlocking {
        withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            authRepository.refreshTokenInfo()
        }
    }

    private fun requestHasAccessToken(request: Request): Boolean {
        val header = request.header("Authorization")
        return header != null && header.startsWith("Bearer ")
    }

    private fun rebuildRequest(request: Request, newAccessToken: String): Request = request.newBuilder()
        .header("Authorization", "Bearer $newAccessToken")
        .build()

}