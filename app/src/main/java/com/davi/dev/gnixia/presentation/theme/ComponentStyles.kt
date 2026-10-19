package com.davi.dev.gnixia.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.then
import androidx.compose.ui.unit.dp

/** Styles for the app's custom components. Material components keep their own defaults. */
object GnixiaStyles {

    private val chatBubbleBase = Style {
        width(0.85f)
        contentPadding(horizontal = 10.dp, vertical = 6.dp)
    }

    val userBubble: Style = chatBubbleBase then Style {
        background(GnixiaColorScheme.primaryContainer)
        shape(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
    }

    val modelBubble: Style = chatBubbleBase then Style {
        background(GnixiaColorScheme.surfaceContainer)
        shape(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
    }
}
