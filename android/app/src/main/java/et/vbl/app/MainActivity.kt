package et.vbl.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import et.vbl.core.designsystem.theme.VblTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VblTheme {
                VblApp()
            }
        }
    }
}

@Composable
fun VblApp() {
    // Navigation host — wired in Phase 1
    // NavHost(navController, startDestination = ...) { ... }
}

@Preview(showBackground = true)
@Composable
private fun VblAppPreview() {
    VblTheme { VblApp() }
}
