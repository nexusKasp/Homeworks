package com.example.hw3.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.hw3.MainViewModel
import com.example.hw3.R
import com.example.hw3.Screen

@Composable
fun NotificationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasActive = viewModel.activeIds.isNotEmpty()

    val showMessage: (Int) -> Unit = { messageRes ->
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
    ) {
        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.title = it },
            label = { Text(text = stringResource(R.string.field_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = viewModel.text,
            onValueChange = { viewModel.text = it },
            label = { Text(text = stringResource(R.string.field_text)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.isNonCancellable = !viewModel.isNonCancellable },
        ) {
            Checkbox(
                checked = viewModel.isNonCancellable,
                onCheckedChange = { viewModel.isNonCancellable = it },
            )
            Text(text = stringResource(R.string.checkbox_non_cancellable))
        }

        ImportanceDropdown(
            selected = viewModel.importance,
            onSelected = { viewModel.importance = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val error = viewModel.sendNotification()
                if (error != null) showMessage(error)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.button_send))
        }

        Text(
            text = stringResource(R.string.section_manage),
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = if (hasActive) {
                stringResource(R.string.active_ids, viewModel.activeIds.joinToString())
            } else {
                stringResource(R.string.active_none)
            },
        )

        OutlinedTextField(
            value = viewModel.editId,
            onValueChange = { newValue -> viewModel.editId = newValue.filter { it.isDigit() } },
            label = { Text(text = stringResource(R.string.field_notification_id)) },
            enabled = hasActive,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
            Button(
                onClick = {
                    val error = viewModel.editNotification()
                    if (error != null) showMessage(error)
                },
                enabled = hasActive,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(R.string.button_edit))
            }
            OutlinedButton(
                onClick = { viewModel.deleteAllNotifications() },
                enabled = hasActive,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(R.string.button_delete_all))
            }
        }

        OutlinedButton(
            onClick = { viewModel.openScreen(Screen.LOCATION) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.button_open_location))
        }
    }
}