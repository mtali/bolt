package org.mtali.core.designsystem.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun Height(height: Dp) = Spacer(modifier = Modifier.height(height))

@Composable
fun Width(width: Dp) = Spacer(modifier = Modifier.width(width))

fun LazyListScope.height(height: Dp) = item { Height(height) }

fun LazyListScope.width(width: Dp) = item { Width(width) }

