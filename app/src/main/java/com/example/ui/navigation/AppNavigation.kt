package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesomeMotion
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.entity.StoryEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CallType
import com.example.ui.components.NavigationItemData
import com.example.ui.components.ResponsiveScaffold
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.ai.AiAssistantScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.call.ActiveCallScreen
import com.example.ui.screens.call.CallListScreen
import com.example.ui.screens.chat.*
import com.example.ui.screens.payment.PaymentWalletScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.profile.UserProfileScreen
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.story.CreateStoryScreen
import com.example.ui.screens.story.StoryListScreen
import com.example.ui.screens.story.StoryViewerScreen
import com.example.ui.viewmodel.*
import com.example.util.Localization

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAIN = "main"
    const val CONVERSATION = "conversation/{chatId}"
    const val CONVERSATION_DETAILS = "conversation_details/{chatId}"
    const val MEDIA_GALLERY = "media_gallery/{chatId}"
    const val CREATE_GROUP = "create_group"
    const val GLOBAL_SEARCH = "global_search"
    const val ACTIVE_CALL = "active_call"
    const val CREATE_STORY = "create_story"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val WALLET = "wallet"
    const val AI_ASSISTANT = "ai_assistant"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    fun conversation(chatId: String) = "conversation/$chatId"
    fun conversationDetails(chatId: String) = "conversation_details/$chatId"
    fun mediaGallery(chatId: String) = "media_gallery/$chatId"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    storyViewModel: StoryViewModel,
    callViewModel: CallViewModel,
    paymentViewModel: PaymentViewModel,
    settingsViewModel: SettingsViewModel,
    adminViewModel: AdminViewModel,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val language = settings.language

    val currentCallState by callViewModel.currentCallState.collectAsState()
    var activeStoryToView by remember { mutableStateOf<StoryEntity?>(null) }

    val startDestination = if (currentUser != null) Routes.MAIN else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // --- AUTH ROUTES ---
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                language = language,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                language = language,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                language = language,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- MAIN TABS ROUTE ---
        composable(Routes.MAIN) {
            var selectedTab by remember { mutableStateOf(0) }

            val navItems = listOf(
                NavigationItemData(
                    route = "chats",
                    label = Localization.getString("chats", language),
                    selectedIcon = Icons.Filled.ChatBubble,
                    unselectedIcon = Icons.Outlined.ChatBubbleOutline,
                    testTag = "tab_chats"
                ),
                NavigationItemData(
                    route = "calls",
                    label = Localization.getString("calls", language),
                    selectedIcon = Icons.Filled.Call,
                    unselectedIcon = Icons.Outlined.Call,
                    testTag = "tab_calls"
                ),
                NavigationItemData(
                    route = "stories",
                    label = Localization.getString("stories", language),
                    selectedIcon = Icons.Filled.AutoAwesome,
                    unselectedIcon = Icons.Outlined.AutoAwesomeMotion,
                    testTag = "tab_stories"
                ),
                NavigationItemData(
                    route = "settings",
                    label = Localization.getString("settings", language),
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    testTag = "tab_settings"
                )
            )

            ResponsiveScaffold(
                navigationItems = navItems,
                selectedItemIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            ) {
                when (selectedTab) {
                    0 -> ChatListScreen(
                        chatViewModel = chatViewModel,
                        storyViewModel = storyViewModel,
                        currentUser = currentUser,
                        language = language,
                        onChatClick = { chatId ->
                            navController.navigate(Routes.conversation(chatId))
                        },
                        onStoryClick = { story ->
                            activeStoryToView = story
                        },
                        onCreateStoryClick = {
                            navController.navigate(Routes.CREATE_STORY)
                        },
                        onNewChatClick = {
                            navController.navigate(Routes.GLOBAL_SEARCH)
                        },
                        onCreateGroupClick = {
                            navController.navigate(Routes.CREATE_GROUP)
                        },
                        onSearchClick = {
                            navController.navigate(Routes.GLOBAL_SEARCH)
                        },
                        onProfileClick = {
                            navController.navigate(Routes.PROFILE)
                        }
                    )
                    1 -> CallListScreen(
                        callViewModel = callViewModel,
                        chatViewModel = chatViewModel,
                        language = language,
                        onStartCall = { otherId, name, avatar, type ->
                            callViewModel.startCall(otherId, name, avatar, type)
                            navController.navigate(Routes.ACTIVE_CALL)
                        }
                    )
                    2 -> StoryListScreen(
                        storyViewModel = storyViewModel,
                        currentUser = currentUser,
                        language = language,
                        onStoryClick = { story ->
                            activeStoryToView = story
                        },
                        onCreateStoryClick = {
                            navController.navigate(Routes.CREATE_STORY)
                        }
                    )
                    3 -> SettingsScreen(
                        currentUser = currentUser,
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel,
                        language = language,
                        onProfileClick = { navController.navigate(Routes.PROFILE) },
                        onWalletClick = { navController.navigate(Routes.WALLET) },
                        onAiAssistantClick = { navController.navigate(Routes.AI_ASSISTANT) },
                        onAdminClick = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                        onLogout = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Story Viewer Overlay if selected
                if (activeStoryToView != null) {
                    StoryViewerScreen(
                        story = activeStoryToView!!,
                        storyViewModel = storyViewModel,
                        chatViewModel = chatViewModel,
                        language = language,
                        onClose = { activeStoryToView = null }
                    )
                }
            }
        }

        // --- CONVERSATION ROUTE ---
        composable(
            route = Routes.CONVERSATION,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ConversationScreen(
                chatId = chatId,
                chatViewModel = chatViewModel,
                callViewModel = callViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() },
                onOpenDetails = { id -> navController.navigate(Routes.conversationDetails(id)) },
                onStartCall = { otherId, name, avatar, type ->
                    callViewModel.startCall(otherId, name, avatar, type)
                    navController.navigate(Routes.ACTIVE_CALL)
                },
                onOpenMediaGallery = { id -> navController.navigate(Routes.mediaGallery(id)) }
            )
        }

        // --- CONVERSATION DETAILS ROUTE ---
        composable(
            route = Routes.CONVERSATION_DETAILS,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ConversationDetailsScreen(
                chatId = chatId,
                chatViewModel = chatViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() },
                onOpenMediaGallery = { navController.navigate(Routes.mediaGallery(chatId)) },
                onStartCall = { otherId, name, avatar, type ->
                    callViewModel.startCall(otherId, name, avatar, type)
                    navController.navigate(Routes.ACTIVE_CALL)
                },
                onReportUser = { reportedId, reportedName ->
                    adminViewModel.submitReport(
                        reporterId = "user_me",
                        reportedUserId = reportedId,
                        reportedUserName = reportedName,
                        reason = "User Report",
                        details = "Reported via conversation details"
                    )
                }
            )
        }

        // --- MEDIA GALLERY ROUTE ---
        composable(
            route = Routes.MEDIA_GALLERY,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            MediaGalleryScreen(
                chatId = chatId,
                chatViewModel = chatViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- CREATE GROUP ROUTE ---
        composable(Routes.CREATE_GROUP) {
            CreateGroupScreen(
                chatViewModel = chatViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() },
                onGroupCreated = { newChatId ->
                    navController.navigate(Routes.conversation(newChatId)) {
                        popUpTo(Routes.MAIN)
                    }
                }
            )
        }

        // --- GLOBAL SEARCH ROUTE ---
        composable(Routes.GLOBAL_SEARCH) {
            GlobalSearchScreen(
                chatViewModel = chatViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() },
                onOpenChat = { chatId ->
                    navController.navigate(Routes.conversation(chatId))
                }
            )
        }

        // --- ACTIVE CALL ROUTE ---
        composable(Routes.ACTIVE_CALL) {
            ActiveCallScreen(
                callViewModel = callViewModel,
                language = language,
                onCallEnded = { navController.popBackStack() }
            )
        }

        // --- CREATE STORY ROUTE ---
        composable(Routes.CREATE_STORY) {
            CreateStoryScreen(
                storyViewModel = storyViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- USER PROFILE ROUTE ---
        composable(Routes.PROFILE) {
            UserProfileScreen(
                authViewModel = authViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onOpenWallet = { navController.navigate(Routes.WALLET) },
                onOpenAdmin = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- EDIT PROFILE ROUTE ---
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                authViewModel = authViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- PAYMENT WALLET ROUTE ---
        composable(Routes.WALLET) {
            PaymentWalletScreen(
                currentUser = currentUser,
                paymentViewModel = paymentViewModel,
                chatViewModel = chatViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- AI ASSISTANT ROUTE ---
        composable(Routes.AI_ASSISTANT) {
            AiAssistantScreen(
                language = language,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- ADMIN DASHBOARD ROUTE ---
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                adminViewModel = adminViewModel,
                language = language,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
