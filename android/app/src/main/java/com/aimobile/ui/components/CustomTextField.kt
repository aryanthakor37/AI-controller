package com.aimobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aimobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    singleLine: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            isError   -> Danger
            isFocused -> Primary
            else      -> BorderColor
        },
        animationSpec = tween(200),
        label = "tf_border"
    )
    val labelColor by animateColorAsState(
        targetValue = when {
            isError   -> Danger
            isFocused -> Primary
            else      -> TextSub
        },
        animationSpec = tween(200),
        label = "tf_label"
    )

    OutlinedTextField(
        value              = value,
        onValueChange      = onValueChange,
        label              = { Text(label) },
        modifier           = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        visualTransformation = visualTransformation,
        keyboardOptions    = keyboardOptions,
        shape              = RoundedCornerShape(14.dp),
        isError            = isError,
        singleLine         = singleLine,
        leadingIcon        = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor    = CardBg,
            unfocusedContainerColor  = CardBg,
            errorContainerColor      = CardBg,
            focusedBorderColor       = borderColor,
            unfocusedBorderColor     = borderColor,
            errorBorderColor         = borderColor,
            focusedLabelColor        = labelColor,
            unfocusedLabelColor      = labelColor,
            errorLabelColor          = labelColor,
            cursorColor              = Primary,
            focusedTextColor         = TextPrimary,
            unfocusedTextColor       = TextPrimary,
            focusedLeadingIconColor   = if (isFocused) Primary else TextSub,
            unfocusedLeadingIconColor = TextSub,
            errorLeadingIconColor     = Danger
        )
    )
}
