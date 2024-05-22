package org.mtali.core.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mtali.R

fun LazyListScope.boltHeader(
    modifier: Modifier = Modifier,
    @StringRes subtitle: Int
) = item {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 46.sp
        )
        Height(height = 12.dp)
        Text(
            text = stringResource(id = subtitle),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 18.sp
        )
    }
}