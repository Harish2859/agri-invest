package com.example.agri_invest_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.agri_invest_app.data.network.RetrofitClient
import com.example.agri_invest_app.data.repository.*
import com.example.agri_invest_app.ui.auth.*
import com.example.agri_invest_app.ui.common.ProfileScreen
import com.example.agri_invest_app.ui.farmer.*
import com.example.agri_invest_app.ui.investor.InvestorDashboard
import com.example.agri_invest_app.ui.investor.InvestorPortfolioScreen
import com.example.agri_invest_app.ui.investor.InvestorViewModel
import com.example.agri_invest_app.ui.investor.ProjectDetailScreen
import com.example.agri_invest_app.ui.lead.LeadDashboardScreen
import com.example.agri_invest_app.ui.lead.LeadViewModel
import com.example.agri_invest_app.ui.theme.Agri_invest_appTheme
import com.example.agri_invest_app.util.DataStoreManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val dataStoreManager = DataStoreManager(this)
        val authRepository = AuthRepository(RetrofitClient.authService)
        val projectRepository = ProjectRepository(RetrofitClient.projectService)
        val investmentRepository = InvestmentRepository(RetrofitClient.investmentService)
        val farmerRepository = FarmerRepository(RetrofitClient.farmerService)
        val leadRepository = LeadRepository(RetrofitClient.leadService)
        
        enableEdgeToEdge()
        setContent {
            Agri_invest_appTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                var startDestination by remember { mutableStateOf<String?>(null) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Use simple state to hold ViewModels to ensure they survive recomposition but are cleared on logout
                var farmerViewModelInstance by remember { mutableStateOf<FarmerViewModel?>(null) }
                var investorViewModelInstance by remember { mutableStateOf<InvestorViewModel?>(null) }

                val forceLogout = {
                    scope.launch {
                        RetrofitClient.setToken(null)
                        dataStoreManager.clearAuthData()
                        farmerViewModelInstance = null
                        investorViewModelInstance = null
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    val token = dataStoreManager.getToken()
                    val role = dataStoreManager.getRole()
                    if (token != null && role != null) {
                        RetrofitClient.setToken(token)
                        startDestination = when (role) {
                            "FARMER" -> "farmer_root"
                            "INVESTOR" -> "investor_root"
                            "VILLAGE_LEAD" -> "lead_dashboard"
                            else -> "login"
                        }
                    } else {
                        startDestination = "login"
                    }
                }

                if (startDestination == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val loginViewModel: LoginViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return LoginViewModel(authRepository, dataStoreManager) as T
                            }
                        }
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val route = currentDestination?.route
                            val isInvestorScreen = route?.contains("investor") == true
                            val isFarmerScreen = route?.contains("farmer") == true || route == "transaction_history"
                            val isLeadScreen = route == "lead_dashboard" || route == "lead_profile"

                            val items = if (isInvestorScreen) {
                                listOf(
                                    BottomNavItemData("investor_dashboard", "Market", Icons.Default.Home),
                                    BottomNavItemData("investor_portfolio", "Portfolio", Icons.AutoMirrored.Filled.List),
                                    BottomNavItemData("investor_profile", "Profile", Icons.Default.Person)
                                )
                            } else if (isFarmerScreen) {
                                listOf(
                                    BottomNavItemData("farmer_dashboard", "Dashboard", Icons.Default.Home),
                                    BottomNavItemData("farmer_profile", "Profile", Icons.Default.Person)
                                )
                            } else if (isLeadScreen) {
                                listOf(
                                    BottomNavItemData("lead_dashboard", "Dashboard", Icons.Default.Home),
                                    BottomNavItemData("lead_profile", "Profile", Icons.Default.Person)
                                )
                            } else null
                            
                            items?.let { navItems ->
                                NavigationBar {
                                    navItems.forEach { item ->
                                        NavigationBarItem(
                                            icon = { Icon(item.icon, contentDescription = item.label) },
                                            label = { Text(item.label) },
                                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                            onClick = {
                                                // CRITICAL: Force Refresh Data on Tab Click to solve "Static UI"
                                                if (item.route == "investor_portfolio") investorViewModelInstance?.getInvestorPortfolio()
                                                if (item.route == "investor_dashboard") investorViewModelInstance?.discoverProjects()
                                                if (item.route == "farmer_dashboard") farmerViewModelInstance?.loadDashboardData()

                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController, 
                            startDestination = startDestination!!,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("login") {
                                LoginScreen(viewModel = loginViewModel, onNavigateToSignup = { navController.navigate("signup") }) { role ->
                                    scope.launch {
                                        val newToken = dataStoreManager.getToken()
                                        RetrofitClient.setToken(newToken)
                                        val destination = when (role) {
                                            "FARMER" -> "farmer_root"
                                            "INVESTOR" -> "investor_root"
                                            "VILLAGE_LEAD" -> "lead_dashboard"
                                            else -> "login"
                                        }
                                        navController.navigate(destination) { popUpTo("login") { inclusive = true } }
                                    }
                                }
                            }
                            composable("signup") {
                                val signupViewModel: SignupViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T = SignupViewModel(authRepository) as T
                                })
                                SignupScreen(signupViewModel) { navController.navigate("login") { popUpTo("signup") { inclusive = true } } }
                            }

                            // FARMER ROOT
                            navigation(startDestination = "farmer_dashboard", route = "farmer_root") {
                                composable("farmer_dashboard") {
                                    if (farmerViewModelInstance == null) {
                                        farmerViewModelInstance = FarmerViewModel(farmerRepository, projectRepository, dataStoreManager)
                                    }
                                    FarmerDashboardScreen(viewModel = farmerViewModelInstance!!, onLogout = { loginViewModel.resetState(); forceLogout() }, onCreateProject = { navController.navigate("create_project") }, onProjectClick = { navController.navigate("milestones/$it") })
                                }
                                composable("farmer_profile") {
                                    ProfileScreen(
                                        onLogout = { loginViewModel.resetState(); forceLogout() },
                                        onNavigateToTransactions = { navController.navigate("transaction_history") }
                                    )
                                }
                                composable("create_project") {
                                    FarmerCreateProjectScreen(viewModel = farmerViewModelInstance!!, onBack = { navController.popBackStack() }, onProjectCreated = { navController.popBackStack(); farmerViewModelInstance?.loadDashboardData() })
                                }
                                composable("milestones/{projectId}", arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { backStackEntry ->
                                    val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                                    MilestoneListScreen(projectId = projectId, viewModel = farmerViewModelInstance!!, onBack = { navController.popBackStack() })
                                }
                                composable("transaction_history") {
                                    TransactionHistoryScreen(viewModel = farmerViewModelInstance!!, onBack = { navController.popBackStack() })
                                }
                            }

                            // INVESTOR ROOT
                            navigation(startDestination = "investor_dashboard", route = "investor_root") {
                                composable("investor_dashboard") {
                                    if (investorViewModelInstance == null) {
                                        investorViewModelInstance = InvestorViewModel(projectRepository, investmentRepository)
                                    }
                                    InvestorDashboard(viewModel = investorViewModelInstance!!, onProjectClick = { navController.navigate("project_details/$it") })
                                }
                                composable("investor_portfolio") {
                                    InvestorPortfolioScreen(viewModel = investorViewModelInstance!!)
                                }
                                composable("investor_profile") {
                                    ProfileScreen(onLogout = { loginViewModel.resetState(); forceLogout() })
                                }
                                composable("project_details/{projectId}", arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { backStackEntry ->
                                    val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                                    ProjectDetailScreen(projectId = projectId, viewModel = investorViewModelInstance!!, onBack = { navController.popBackStack() })
                                }
                            }

                            composable("lead_dashboard") {
                                val leadViewModel: LeadViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T = LeadViewModel(leadRepository, projectRepository) as T
                                })
                                LeadDashboardScreen(viewModel = leadViewModel, onLogout = { loginViewModel.resetState(); forceLogout() })
                            }
                            composable("lead_profile") {
                                ProfileScreen(onLogout = { loginViewModel.resetState(); forceLogout() })
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItemData(val route: String, val label: String, val icon: ImageVector)
