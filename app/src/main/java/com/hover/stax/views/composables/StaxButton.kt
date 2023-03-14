package com.hover.stax.views.composables

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.ui.theme.AlphaDisabled
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.ColorPrimaryDark
import com.hover.stax.ui.theme.Shapes
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun StaxButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    colors: ButtonColors,
    shape: Shape,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier,
        colors = colors,
        content = content,
        shape = shape
    )
}

@Composable
fun StaxOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors,
    shape: Shape,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        content = content,
        shape = shape
    )
}

@Composable
fun StaxPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    title: String
) {
    StaxButton(
        onClick = onClick,
        isEnabled = isEnabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = BrightBlue,
            contentColor = ColorPrimaryDark,
            disabledContentColor = ColorPrimaryDark.copy(alpha = AlphaDisabled)
        ),
        shape = Shapes.large,
        content = {
            StaxPrimaryButtonText(text = title)
        }
    )
}

@Composable
fun StaxOutlinedPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector
) {
    StaxOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = Shapes.large,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.primary)
    ) {
        Icon(imageVector = icon, contentDescription = "", modifier = Modifier.padding(5.dp))
        Divider()
        StaxPrimaryButtonText(text = title, textAllCaps = false)
    }
}

@Preview
@Composable
fun StaxPrimaryButtonDarkPreview() {
    StaxTheme(darkTheme = true) {
        StaxPrimaryButton(
            onClick = { },
            isEnabled = true,
            title = "Stax",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// TODO - Theming needs to get fixed - so dark mode would work well
@Preview
@Composable
fun StaxPrimaryButtonDisableDarkPreview() {
    StaxTheme(darkTheme = true) {
        StaxPrimaryButton(
            onClick = { },
            isEnabled = false,
            title = "Stax",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun StaxPrimaryButtonDisableLightPreview() {
    StaxTheme(darkTheme = false) {
        StaxPrimaryButton(
            onClick = { },
            isEnabled = false,
            title = "Stax",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun StaxPrimaryButtonLightPreview() {
    StaxTheme {
        StaxPrimaryButton(
            onClick = { },
            isEnabled = true,
            title = "Stax",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun StaxPrimaryOutlinedButtonLightPreview() {
    StaxTheme(darkTheme = false) {
        StaxOutlinedPrimaryButton(
            onClick = { },
            title = "Stax",
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Save
        )
    }
}

// TODO - Theming needs to get fixed - so dark mode would work well
@Preview
@Composable
fun StaxPrimaryOutlinedButtonDarkPreview() {
    StaxTheme(darkTheme = true) {
        StaxOutlinedPrimaryButton(
            onClick = { },
            title = "Stax",
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Save
        )
    }
}