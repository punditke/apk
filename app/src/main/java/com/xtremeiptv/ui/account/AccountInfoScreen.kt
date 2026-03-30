package com.xtremeiptv.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val accountInfo by viewModel.accountInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (accountInfo == null) {
                Text(
                    "No account info available.\nSelect a profile first.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Portal URL
                    InfoRow(
                        icon = "🌐",
                        title = "PORTAL",
                        value = accountInfo!!.portalUrl
                    )
                    
                    // Server IP
                    InfoRow(
                        icon = "🌍",
                        title = "SERVER",
                        value = accountInfo!!.serverIp
                    )
                    
                    // MAC Address
                    accountInfo!!.macAddress?.let {
                        InfoRow(
                            icon = "♏️",
                            title = "MAC",
                            value = it
                        )
                    }
                    
                    // Username
                    accountInfo!!.username?.let {
                        InfoRow(
                            icon = "👤",
                            title = "USERNAME",
                            value = it
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Created Date
                    accountInfo!!.createdDate?.let {
                        InfoRow(
                            icon = "🗓",
                            title = "CREATED",
                            value = it
                        )
                    }
                    
                    // Expiry Date
                    accountInfo!!.expiryDate?.let {
                        InfoRow(
                            icon = "📆",
                            title = "EXPIRE",
                            value = it
                        )
                    }
                    
                    // Remaining Days
                    accountInfo!!.remainingDays?.let {
                        InfoRow(
                            icon = "⏰",
                            title = "REMAINING",
                            value = it
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Tariff Plan
                    accountInfo!!.tariffPlan?.let {
                        InfoRow(
                            icon = "🛰",
                            title = "TARIFF PLAN",
                            value = it
                        )
                    }
                    
                    // Max Connections
                    accountInfo!!.maxConnections?.let {
                        InfoRow(
                            icon = "👪",
                            title = "MAX CONN",
                            value = it.toString()
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Protocol
                    InfoRow(
                        icon = "📡",
                        title = "PROTOCOL",
                        value = accountInfo!!.protocol.uppercase()
                    )
                    
                    // Connection Status
                    InfoRow(
                        icon = "📶",
                        title = "STATUS",
                        value = accountInfo!!.status,
                        valueColor = if (accountInfo!!.status == "Connected") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: String,
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}