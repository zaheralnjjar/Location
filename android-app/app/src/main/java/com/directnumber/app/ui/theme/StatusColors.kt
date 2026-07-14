package com.directnumber.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Success/warning are not part of Material3's default ColorScheme, so they are
 * carried alongside it via CompositionLocal instead of hard-coding colors in components.
 */
@Immutable
data class StatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
)

val LocalStatusColors = staticCompositionLocalOf {
    StatusColors(
        success = SuccessLight,
        onSuccess = Color.White,
        successContainer = SuccessContainerLight,
        warning = WarningLight,
        warningContainer = WarningContainerLight,
    )
}

val lightStatusColors = StatusColors(
    success = SuccessLight,
    onSuccess = Color.White,
    successContainer = SuccessContainerLight,
    warning = WarningLight,
    warningContainer = WarningContainerLight,
)

val darkStatusColors = StatusColors(
    success = SuccessDark,
    onSuccess = Color(0xFF00390F),
    successContainer = SuccessContainerDark,
    warning = WarningDark,
    warningContainer = WarningContainerDark,
)

object DirectNumberStatus {
    val colors: StatusColors
        @Composable
        get() = LocalStatusColors.current
}
