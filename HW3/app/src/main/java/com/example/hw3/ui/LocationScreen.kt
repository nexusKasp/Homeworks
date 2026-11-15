package com.example.hw3.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.hw3.MainViewModel
import com.example.hw3.R
import com.example.hw3.Screen
import com.example.hw3.location.LocationState

@Composable
fun LocationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    BackHandler { viewModel.openScreen(Screen.MAIN) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) viewModel.loadLocation() else viewModel.onLocationPermissionDenied()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
    ) {
        Text(
            text = locationText(viewModel.locationState),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                if (viewModel.hasLocationPermission()) {
                    viewModel.loadLocation()
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.button_get_location))
        }

        OutlinedButton(
            onClick = { viewModel.openScreen(Screen.MAIN) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.button_back))
        }
    }
}

@Composable
private fun locationText(state: LocationState): String {
    return when (state) {
        is LocationState.Hint -> stringResource(R.string.location_hint)
        is LocationState.PermissionDenied -> stringResource(R.string.location_permission_denied)
        is LocationState.Loading -> stringResource(R.string.location_loading)
        is LocationState.Unavailable -> stringResource(R.string.location_unavailable)
        is LocationState.Result -> stringResource(
            R.string.location_result,
            state.latitude,
            state.longitude,
        )
    }
}