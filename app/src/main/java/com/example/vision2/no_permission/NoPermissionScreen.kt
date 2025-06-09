package com.example.vision2.no_permission

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NoPermissionScreen(
    onRequestCameraPermission: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    onRequestNearbyDevicesPermission: () -> Unit,
) {
    NoPermissionContent(
        onRequestCameraPermission = onRequestCameraPermission,
        onRequestBluetoothPermission = onRequestBluetoothPermission,
        onRequestNearbyDevicesPermission = onRequestNearbyDevicesPermission
    )
}

@Composable
fun NoPermissionContent(
    onRequestCameraPermission: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    onRequestNearbyDevicesPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = "Please grant Camera, Bluetooth and Nearby Devices permissions to use the full functionality of this app."
            )
            Button(
                onClick = {
                    onRequestCameraPermission()
                    onRequestBluetoothPermission()
                    onRequestNearbyDevicesPermission()
                }
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Permissions")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Grant Permissions")
            }
        }
    }
}

@Preview
@Composable
private fun Preview_NoPermissionContent() {
    NoPermissionContent(
        onRequestCameraPermission = {},
        onRequestBluetoothPermission = {},
        onRequestNearbyDevicesPermission = {}
    )
}
