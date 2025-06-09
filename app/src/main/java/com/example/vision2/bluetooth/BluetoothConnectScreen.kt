package com.example.vision2.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vision2.ViewModels.DomainLayer
import com.example.vision2.camera.StartNavigationScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BluetoothConnectionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val viewModel: DomainLayer = hiltViewModel()

    var isConnected by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Connecting to ESP32...") }
    var receivedData by remember { mutableStateOf(0) }
    var availableDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                status = "Bluetooth permission not granted"
                return@launch
            }

            val bondedDevices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()
            availableDevices = bondedDevices

            val espDevice = bondedDevices.find { it.name == "ESP32-Ultrasonic" }

            if (espDevice != null) {
                val reader = BluetoothDistanceReader()
                reader.connectAndRead(context) { data ->
                    if (data != null) {
                        isConnected = true
                        receivedData = data
                        viewModel.updateReceivedData(receivedData)
                    } else {
                        status = "Connection Failed"
                    }
                }
            } else {
                status = "ESP32-Ultrasonic not found. Available devices:"
            }
        }
    }

    if (isConnected) {
        StartNavigationScreen(modifier = modifier)
    } else {
        Surface(modifier = modifier.padding(16.dp)) {
            Column {
                Text(text = status, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(availableDevices) { device ->
                        Text(text = "${device.name} (${device.address})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

//package com.example.vision2.bluetooth
//
//import android.Manifest
//import android.os.Build
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.example.vision2.camera.StartNavigationScreen
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import androidx.core.content.ContextCompat
//import android.content.pm.PackageManager
//
//@RequiresApi(Build.VERSION_CODES.S)
//@Composable
//fun BluetoothConnectionScreen(modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//    var isConnected by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("Connecting to ESP32...") }
//    var availableDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
//
//    LaunchedEffect(Unit) {
//        scope.launch(Dispatchers.IO) {
//            val permissionGranted = ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) == PackageManager.PERMISSION_GRANTED
//
//            if (!permissionGranted) {
//                status = "Bluetooth permission not granted"
//                return@launch
//            }
//
//            val bondedDevices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()
//            availableDevices = bondedDevices
//
//            val espDevice = bondedDevices.find { it.name == "ESP32-Ultrasonic" }
//
//            if (espDevice != null) {
//                val reader = BluetoothDistanceReader()
//                reader.connectAndRead(context) {
//                    if (!it.contains("Error", ignoreCase = true) && it != "Device not found") {
//                        isConnected = true
//                    } else {
//                        status = it
//                    }
//                }
//            } else {
//                status = "ESP32-Ultrasonic not found. Available devices:"
//            }
//        }
//    }
//
//    if (isConnected) {
//        StartNavigationScreen(modifier = modifier)
//    } else {
//        Surface(modifier = modifier.padding(16.dp)) {
//            Column {
//                Text(text = status, style = MaterialTheme.typography.bodyMedium)
//                Spacer(modifier = Modifier.height(16.dp))
//                LazyColumn {
//                    items(availableDevices) { device ->
//                        Text(text = "${device.name} (${device.address})", style = MaterialTheme.typography.bodySmall)
//                    }
//                }
//            }
//        }
//    }
//}
