package com.xtremeiptv.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
    var protocolType by remember { mutableStateOf("stalker") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
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
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = when (protocolType) {
                        "stalker" -> "Stalker Portal (MAC + URL)"
                        "mac" -> "MAC Portal (MAC + URL)"
                        "xtream" -> "XTream Codes (URL + User + Pass)"
                        "m3u" -> "M3U (URL or File)"
                        else -> "Select Protocol"
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Protocol") }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Stalker Portal (MAC + URL)") },
                        onClick = {
                            protocolType = "stalker"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("MAC Portal (MAC + URL)") },
                        onClick = {
                            protocolType = "mac"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("XTream Codes (URL + User + Pass)") },
                        onClick = {
                            protocolType = "xtream"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("M3U (URL or File)") },
                        onClick = {
                            protocolType = "m3u"
                            expanded = false
                        }
                    )
                }
            }
            
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                placeholder = { Text("http://tv.push4k.tv/stalker_portal/c/") }
            )
            
            if (protocolType == "xtream") {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            if (protocolType == "stalker" || protocolType == "mac") {
                OutlinedTextField(
                    value = macAddress,
                    onValueChange = { macAddress = it },
                    label = { Text("MAC Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("00:1A:79:00:07:A9") }
                )
            }
            
            if (protocolType == "stalker") {
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
            }
            
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
                enabled = name.isNotBlank() && serverUrl.isNotBlank() &&
                        (protocolType != "xtream" || (username.isNotBlank() && password.isNotBlank())) &&
                        (protocolType != "mac" || macAddress.isNotBlank())
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