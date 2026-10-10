package et.vbl.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import et.vbl.core.designsystem.theme.VblTheme
import et.vbl.core.model.PaymentProvider
import et.vbl.feature.home.ExpensesBubblesScreen
import et.vbl.feature.home.HomeScreen
import et.vbl.feature.loans.LoanApplicationScreen
import et.vbl.feature.loans.LoanCatalogScreen
import et.vbl.feature.notifications.NotificationsScreen
import et.vbl.feature.onboarding.BiometricSetupScreen
import et.vbl.feature.onboarding.OtpVerificationScreen
import et.vbl.feature.onboarding.PhoneInputScreen
import et.vbl.feature.onboarding.PinSetupScreen
import et.vbl.feature.onboarding.WelcomeScreen
import et.vbl.feature.payments.PaymentInstructionScreen
import et.vbl.feature.payments.PaymentTimelineScreen
import et.vbl.feature.payments.ProviderSelectScreen
import et.vbl.feature.payments.ReferenceEntryScreen
import et.vbl.feature.profile.KycUploadScreen
import et.vbl.feature.profile.ProfileScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VblTheme(darkTheme = false) {
                VblApp()
            }
        }
    }
}

@Composable
fun VblApp() {
    val navController = rememberNavController()
    var enteredPhone by remember { mutableStateOf("+251911223344") }

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        // ─── WELCOME HERO (SCREEN 2) ──────────────────────────────────
        composable("welcome") {
            WelcomeScreen(
                onNextClick = {
                    navController.navigate("phone_input")
                },
                onSkipToDashboard = {
                    navController.navigate("home")
                }
            )
        }

        // ─── ONBOARDING FLOW ───────────────────────────────────────────
        composable("phone_input") {
            PhoneInputScreen(
                onContinue = { phone ->
                    enteredPhone = phone
                    navController.navigate("otp_verification")
                }
            )
        }
        composable("otp_verification") {
            OtpVerificationScreen(
                phoneNumber = enteredPhone,
                onVerifyOtp = {
                    navController.navigate("pin_setup")
                }
            )
        }
        composable("pin_setup") {
            PinSetupScreen(
                onPinCreated = {
                    navController.navigate("biometric_setup")
                }
            )
        }
        composable("biometric_setup") {
            BiometricSetupScreen(
                onEnableBiometrics = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        // ─── MAIN APP CONTAINER (PERSISTENT DOCK ACROSS ALL TABS) ──────
        composable("home") {
            MainContainerScreen(
                onNavigateToPaymentInstruction = { provider ->
                    navController.navigate("payments_instruction/${provider.name}")
                },
                onNavigateToLoanApplication = { _ ->
                    navController.navigate("loan_application")
                },
                onNavigateToKyc = {
                    navController.navigate("kyc_upload")
                },
                onNotificationsClick = {
                    navController.navigate("notifications")
                },
                onSeeAllTransactions = {
                    navController.navigate("payments_timeline")
                },
                onLogout = {
                    navController.navigate("welcome") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // ─── EXPENSES BUBBLES ANALYTICS ────────────────────────────────
        composable("expenses_bubbles") {
            ExpensesBubblesScreen(
                onBackClick = { navController.popBackStack() },
                onSeeAllClick = { navController.navigate("payments_timeline") }
            )
        }

        // ─── PAYMENTS & DEPOSITS FLOW ──────────────────────────────────
        composable("payments_provider_select") {
            ProviderSelectScreen(
                onProviderSelected = { provider ->
                    navController.navigate("payments_instruction/${provider.name}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "payments_instruction/{providerName}",
            arguments = listOf(navArgument("providerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerName = backStackEntry.arguments?.getString("providerName") ?: PaymentProvider.TELEBIRR.name
            val provider = runCatching { PaymentProvider.valueOf(providerName) }.getOrDefault(PaymentProvider.TELEBIRR)

            PaymentInstructionScreen(
                provider = provider,
                onProceedToReference = {
                    navController.navigate("payments_reference/${provider.name}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "payments_reference/{providerName}",
            arguments = listOf(navArgument("providerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerName = backStackEntry.arguments?.getString("providerName") ?: PaymentProvider.TELEBIRR.name
            val provider = runCatching { PaymentProvider.valueOf(providerName) }.getOrDefault(PaymentProvider.TELEBIRR)

            ReferenceEntryScreen(
                provider = provider,
                onSubmitPayment = { _, _, _ ->
                    navController.navigate("payments_timeline")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("payments_timeline") {
            PaymentTimelineScreen(
                onViewHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onRetry = { navController.popBackStack() },
                onContactSupport = { navController.navigate("home") }
            )
        }

        // ─── LOANS FLOW ────────────────────────────────────────────────
        composable("loans_catalog") {
            LoanCatalogScreen(
                onApplyClick = { _ ->
                    navController.navigate("loan_application")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("loan_application") {
            LoanApplicationScreen(
                onSubmitApplication = { _, _, _ ->
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── NOTIFICATIONS ─────────────────────────────────────────────
        composable("notifications") {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── PROFILE & KYC ─────────────────────────────────────────────
        composable("profile") {
            ProfileScreen(
                onKycClick = {
                    navController.navigate("kyc_upload")
                },
                onLogoutClick = {
                    navController.navigate("phone_input") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("kyc_upload") {
            KycUploadScreen(
                onUploadDocument = { _, _ ->
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VblAppPreview() {
    VblTheme { VblApp() }
}
