package com.example.hw2

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage

object PasswordHolder {
    val generated: String by lazy { generatePassword() }

    private fun generatePassword(length: Int = 10): String {
        val letters = ('a'..'z') + ('A'..'Z')
        val digits = ('0'..'9').toList()
        val all = letters + digits
        val chars = mutableListOf(letters.random(), digits.random())
        repeat(length - 2) { chars.add(all.random()) }
        return chars.shuffled().joinToString("")
    }
}

const val SUCCESS_IMAGE = "https://placehold.co/400x400/4CAF50/FFFFFF/png?text=Success"
const val FAIL_IMAGE = "https://placehold.co/400x400/F44336/FFFFFF/png?text=Wrong+password"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PasswordHolder.generated
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(onSuccess = { email, password ->
                navController.navigate("result/${Uri.encode(email)}/${Uri.encode(password)}")
            })
        }
        composable(
            route = "result/{email}/{password}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { entry ->
            val email = entry.arguments?.getString("email").orEmpty()
            val password = entry.arguments?.getString("password").orEmpty()
            ResultScreen(
                email = email,
                enteredPassword = password,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun AuthScreen(onSuccess: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        emailError = when {
            email.isBlank() -> "Введите e-mail"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Некорректный e-mail"
            else -> null
        }
        passwordError = when {
            password.isEmpty() -> "Введите пароль"
            password.length < 8 ||
                    !password.any { it in 'a'..'z' || it in 'A'..'Z' } ||
                    !password.any { it in '0'..'9' } ->
                "Минимум 8 символов, латиница и цифры"
            else -> null
        }
        if (emailError == null && passwordError == null) {
            onSuccess(email, password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("E-mail") },
            singleLine = true,
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Пароль") },
            singleLine = true,
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { submit() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { submit() }, modifier = Modifier.fillMaxWidth()) {
            Text("Далее")
        }
    }
}

@Composable
fun ResultScreen(email: String, enteredPassword: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val generated = PasswordHolder.generated
    val matches = enteredPassword == generated
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = if (matches) SUCCESS_IMAGE else FAIL_IMAGE,
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(email)
        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = generated,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Сгенерированный пароль") },
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { showPassword = !showPassword }) {
                Text(if (showPassword) "Скрыть" else "Показать")
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("password", generated))
                Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Скопировать пароль")
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("На первый экран")
        }
    }
}