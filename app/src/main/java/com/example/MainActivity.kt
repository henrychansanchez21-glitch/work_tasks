package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.FirebaseKeyManager
import com.example.data.VideoUserStore
import com.example.ui.KeySentinelAuthScreen
import com.example.ui.VideoCatalogScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        val context = LocalContext.current
        val keyManager = remember { FirebaseKeyManager(context) }
        val userStore = remember { VideoUserStore(context) }

        var isUnlocked by remember {
          mutableStateOf(keyManager.getActiveKey() != null)
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Crossfade(targetState = isUnlocked, label = "ScreenTransition") { unlocked ->
            if (unlocked) {
              VideoCatalogScreen(
                keyManager = keyManager,
                userStore = userStore,
                onLogout = { isUnlocked = false }
              )
            } else {
              KeySentinelAuthScreen(
                keyManager = keyManager,
                onKeyValidated = { isUnlocked = true }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}

