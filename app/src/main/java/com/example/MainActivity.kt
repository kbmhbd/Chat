package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MessengerTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MessengerApplication

        setContent {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(app.authRepository)
            )
            val chatViewModel: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(app.chatRepository)
            )
            val storyViewModel: StoryViewModel = viewModel(
                factory = StoryViewModelFactory(app.storyRepository)
            )
            val callViewModel: CallViewModel = viewModel(
                factory = CallViewModelFactory(app.callRepository)
            )
            val paymentViewModel: PaymentViewModel = viewModel(
                factory = PaymentViewModelFactory(app.paymentRepository)
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(app.settingsRepository)
            )
            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(app.adminRepository)
            )

            val settings by settingsViewModel.settings.collectAsState()

            MessengerTheme(
                themeMode = settings.themeMode,
                accentHex = settings.accentColorHex
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        chatViewModel = chatViewModel,
                        storyViewModel = storyViewModel,
                        callViewModel = callViewModel,
                        paymentViewModel = paymentViewModel,
                        settingsViewModel = settingsViewModel,
                        adminViewModel = adminViewModel
                    )
                }
            }
        }
    }
}
