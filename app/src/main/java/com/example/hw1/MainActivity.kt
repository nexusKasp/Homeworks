package com.example.hw1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hw1.ui.theme.HW1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HW1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FirstScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun FirstScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val name = stringResource(R.string.full_name)
    val group = stringResource(R.string.group_number)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = name)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = group)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val intent = Intent(context, SecondActivity::class.java).apply {
                putExtra(SecondActivity.EXTRA_DATA, "$name/$group")
            }
            context.startActivity(intent)
        }) {
            Text(text = stringResource(R.string.open_second_screen))
        }
    }
}