package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.stax.R

@Composable
fun Logo(url: String?, contentDescription: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED).build(),
        contentDescription = contentDescription,
        placeholder = painterResource(id = R.drawable.img_placeholder),
        error = painterResource(id = R.drawable.img_placeholder),
        modifier = Modifier
            .size(dimensionResource(id = R.dimen.margin_34))
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}