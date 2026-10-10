package et.vbl.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import et.vbl.core.model.PaymentProvider
import et.vbl.feature.home.ExpensesBubblesScreen
import et.vbl.feature.home.FloatingBottomDock
import et.vbl.feature.home.HomeScreen
import et.vbl.feature.loans.LoanCatalogScreen
import et.vbl.feature.loans.LoanProductUiModel
import et.vbl.feature.payments.ProviderSelectScreen
import et.vbl.feature.profile.ProfileScreen

@Composable
fun MainContainerScreen(
    initialTab: Int = 0,
    onNavigateToPaymentInstruction: (PaymentProvider) -> Unit = {},
    onNavigateToLoanApplication: (LoanProductUiModel) -> Unit = {},
    onNavigateToKyc: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var currentTab by rememberSaveable { mutableIntStateOf(initialTab) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab Content with smooth crossfade
        Crossfade(
            targetState = currentTab,
            label = "TabCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    HomeScreen(
                        showBottomDock = false,
                        currentTab = 0,
                        onTabSelected = { currentTab = it },
                        onDepositClick = { currentTab = 1 },
                        onTransferClick = { currentTab = 1 },
                        onLoanClick = { currentTab = 3 },
                        onWithdrawClick = { currentTab = 3 },
                        onAnalyticsClick = { currentTab = 2 },
                        onScanClick = { currentTab = 3 },
                        onProfileClick = { currentTab = 4 },
                        onNotificationsClick = onNotificationsClick,
                        onSeeAllTransactionsClick = onSeeAllTransactions
                    )
                }

                1 -> {
                    ProviderSelectScreen(
                        onProviderSelected = onNavigateToPaymentInstruction,
                        onBackClick = { currentTab = 0 }
                    )
                }

                2 -> {
                    ExpensesBubblesScreen(
                        onBackClick = { currentTab = 0 },
                        onSeeAllClick = onSeeAllTransactions
                    )
                }

                3 -> {
                    LoanCatalogScreen(
                        onApplyClick = onNavigateToLoanApplication,
                        onBackClick = { currentTab = 0 }
                    )
                }

                4 -> {
                    ProfileScreen(
                        onKycClick = onNavigateToKyc,
                        onLogoutClick = onLogout,
                        onBackClick = { currentTab = 0 }
                    )
                }
            }
        }

        // Persistent Floating Bottom Dock across the 4 main tabs (hidden when on Profile)
        if (currentTab in 0..3) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                FloatingBottomDock(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        }
    }
}
