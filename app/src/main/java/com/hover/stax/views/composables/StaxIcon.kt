package com.hover.stax.views.composables

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.stax.R

@Composable
fun StaxIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = "",
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun StaxImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = "", // TODO - add content description
        placeholder = painterResource(id = R.drawable.img_placeholder),
        error = painterResource(id = R.drawable.img_placeholder),
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Preview
@Composable
fun StaxIconPreview() {
    StaxIcon(
        imageVector = Icons.Rounded.Check
    )
}
