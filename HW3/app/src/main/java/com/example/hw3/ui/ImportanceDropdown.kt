package com.example.hw3.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.hw3.R
import com.example.hw3.notifications.NotificationImportance

@Composable
fun ImportanceDropdown(
    selected: NotificationImportance,
    onSelected: (NotificationImportance) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.importance_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(selected.labelRes),
                    modifier = Modifier.weight(1f),
                )
                Text(text = stringResource(R.string.dropdown_arrow))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                NotificationImportance.values().forEach { importance ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(importance.labelRes)) },
                        onClick = {
                            onSelected(importance)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}