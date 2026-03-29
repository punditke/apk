package com.xtremeiptv.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xtremeiptv.data.database.entity.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAddEditScreen(
    profileId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val existingProfile by viewModel.getProfile(profileId).collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var protocolType by remember { mutableStateOf("m3u") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }
    
    LaunchedEffect(existingProfile) {
        existingProfile?.let {
            name = it.name
            protocolType = it.protocolType
            serverUrl = it.serverUrl
            username = it.username ?: ""
            password = it.password ?: ""
            macAddress = it.macAddress ?: ""
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileId == null) "Add Profile" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = macAddress,
                onValueChange = { macAddress = it },
                label = { Text("MAC Address (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("00:1A:79:00:00:00") }
            )
            
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    viewModel.saveProfile(
                        id = profileId,
                        name = name,
                        protocolType = protocolType,
                        serverUrl = serverUrl,
                        username = username.takeIf { it.isNotBlank() },
                        password = password.takeIf { it.isNotBlank() },
                        macAddress = macAddress.takeIf { it.isNotBlank() }
                    ) { success ->
                        if (success) onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && serverUrl.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save")
                }
            }
        }
    }
}
