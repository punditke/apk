package com.xtremeiptv.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAddEditScreen(
    profileId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val existingProfile by viewModel.getProfile(profileId).collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var protocolType by remember { mutableStateOf("stalker") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }
    var m3uFilePath by remember { mutableStateOf<String?>(null) }
    var m3uFileName by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var resultChannels by remember { mutableStateOf(0) }
    var resultMovies by remember { mutableStateOf(0) }
    var resultSeries by remember { mutableStateOf(0) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = it.lastPathSegment ?: "playlist.m3u"
                val cacheFile = File(context.cacheDir, fileName)
                inputStream?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
                m3uFilePath = cacheFile.absolutePath
                m3uFileName = fileName
            } catch (e: Exception) { }
        }
    }
    
    LaunchedEffect(existingProfile) {
        existingProfile?.let {
            name = it.name
            protocolType = it.protocolType
            serverUrl = it.serverUrl
            username = it.username ?: ""
            password = it.password ?: ""
            macAddress = it.macAddress ?: ""
            if (it.protocolType == "m3u" && it.serverUrl.startsWith("/")) {
                m3uFilePath = it.serverUrl
                m3uFileName = it.serverUrl.substringAfterLast("/")
            }
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Protocol Selection - Radio Buttons
            Text("Protocol", style = MaterialTheme.typography.titleSmall)
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = protocolType == "stalker",
                        onClick = {
                            protocolType = "stalker"
                            username = ""
                            password = ""
                            macAddress = ""
                        }
                    )
                    Text("Stalker Portal (URL + Username + Password + MAC)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = protocolType == "mac",
                        onClick = {
                            protocolType = "mac"
                            username = ""
                            password = ""
                        }
                    )
                    Text("MAC Portal (URL + MAC)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = protocolType == "xtream",
                        onClick = {
                            protocolType = "xtream"
                            macAddress = ""
                        }
                    )
                    Text("XTream Codes (URL + Username + Password)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = protocolType == "m3u",
                        onClick = {
                            protocolType = "m3u"
                            username = ""
                            password = ""
                            macAddress = ""
                        }
                    )
                    Text("M3U (URL or File Upload)")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Server URL (all protocols except M3U with file)
            if (!(protocolType == "m3u" && m3uFilePath != null)) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }
            
            // M3U File Upload
            if (protocolType == "m3u") {
                if (m3uFilePath == null) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select M3U File")
                    }
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Or Enter M3U URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = m3uFileName ?: "",
                        onValueChange = {},
                        label = { Text("Selected File") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                m3uFilePath = null
                                m3uFileName = null
                            }) {
                                Text("✕")
                            }
                        }
                    )
                }
            }
            
            // Username (Stalker and XTream)
            if (protocolType == "stalker" || protocolType == "xtream") {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // Password (Stalker and XTream)
            if (protocolType == "stalker" || protocolType == "xtream") {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // MAC Address (Stalker and MAC)
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
            
            // Error message
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            Button(
                onClick = {
                    val finalServerUrl = when {
                        protocolType == "m3u" && m3uFilePath != null -> m3uFilePath!!
                        else -> serverUrl
                    }
                    viewModel.saveProfile(
                        id = profileId,
                        name = name,
                        protocolType = protocolType,
                        serverUrl = finalServerUrl,
                        username = username.takeIf { it.isNotBlank() },
                        password = password.takeIf { it.isNotBlank() },
                        macAddress = macAddress.takeIf { it.isNotBlank() }
                    ) { success, message, channels, movies, series ->
                        resultMessage = message
                        resultChannels = channels
                        resultMovies = movies
                        resultSeries = series
                        showResultDialog = true
                        if (success) onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && (
                    (protocolType == "m3u" && (serverUrl.isNotBlank() || m3uFilePath != null)) ||
                    (protocolType == "stalker" && serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank() && macAddress.isNotBlank()) ||
                    (protocolType == "xtream" && serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()) ||
                    (protocolType == "mac" && serverUrl.isNotBlank() && macAddress.isNotBlank())
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save")
                }
            }
        }
    }
    
    // Result Dialog
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text(if (resultChannels > 0 || resultMovies > 0 || resultSeries > 0) "Success" else "Warning") },
            text = {
                Column {
                    Text(resultMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (resultChannels > 0 || resultMovies > 0 || resultSeries > 0) {
                        Text("Profile saved successfully!")
                    } else {
                        Text("No content found. Check your credentials.")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
