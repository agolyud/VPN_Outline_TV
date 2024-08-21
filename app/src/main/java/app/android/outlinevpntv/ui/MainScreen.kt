package app.android.outlinevpntv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import app.android.outlinevpntv.R

@Composable
fun MainScreen(
    isConnected: Boolean,
    onConnectClick: (String) -> Unit,
    onDisconnectClick: () -> Unit
) {
    var ssUrl by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val waveHeight = height * 0.6f
            val wavePath = Path().apply {
                moveTo(0f, waveHeight)
                cubicTo(
                    width * 0.25f, waveHeight - 100f,
                    width * 0.75f, waveHeight + 100f,
                    width, waveHeight
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = wavePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFA0DEFF),
                        Color(0xFFFFF9D0)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo), // Убедитесь, что ваш логотип добавлен в ресурсы
                contentDescription = "Logo",
                modifier = Modifier
                    .size(250.dp)
                    .padding(top = 16.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = ssUrl,
                onValueChange = { ssUrl = it },
                label = { Text("Введите ключ") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isConnected) {
                                listOf(
                                    Color(0xFF5EFFB5),
                                    Color(0xFF2C7151)
                                )
                            } else {
                                listOf(
                                    Color(0xFFE57373),
                                    Color(0xFFFF8A65)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable {
                        try {
                            if (isConnected) {
                                onDisconnectClick()
                            } else {
                                onConnectClick(ssUrl.text)
                            }
                        } catch (e: IllegalArgumentException) {
                            errorMessage = e.message
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Crossfade(
                        targetState = isConnected,
                        animationSpec = tween(600)
                    ) { connected ->
                        Icon(
                            imageVector = if (connected) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Text(
                        text = if (isConnected) "OFF" else "ON",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            errorMessage?.let { message ->
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen(
        onConnectClick = {},
        onDisconnectClick = {},
        isConnected = false
    )
}