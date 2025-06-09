package com.example.vision2

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vision2.bluetooth.BluetoothConnectionScreen
import com.example.vision2.dataClass.Destination
import com.example.vision2.no_permission.NoPermissionScreen
import com.example.vision2.screens.EmailScreen
import com.example.vision2.screens.Home_Layout
import com.example.vision2.screens.LanguageTranslateScreen
import com.example.vision2.screens.LaunchAssistantButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun navGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    val cameraPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.CAMERA)
    val bluetoothPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.BLUETOOTH)

    val nearbyDevicesPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.BLUETOOTH_CONNECT)
    NavHost(
        navController = navController,
        startDestination = Destination.HOME.route,
        modifier = modifier
    ) {
        composable(Destination.HOME.route) {
            Home_Layout(
                modifier = modifier,
                navController = navController
            )
        }
        composable(Destination.NAVIGATION.route) {
            Navigation_Screen(
                modifier = modifier,
                hasCameraPermission = cameraPermissionState.status.isGranted,
                hasBluetoothPermission = bluetoothPermissionState.status.isGranted,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                onRequestBluetoothPermission = { bluetoothPermissionState.launchPermissionRequest() },
                onRequestNearbyDevicesPermission = { nearbyDevicesPermissionState.launchPermissionRequest() }
            )
        }
        composable(Destination.EMAIL.route) { EmailScreen() }
        composable(Destination.PHONE_CALL.route) { LaunchAssistantButton() }
        composable(Destination.TRANSLATE.route) { LanguageTranslateScreen() }
    }

}

@Composable
private fun Navigation_Screen(
    modifier: Modifier,
    hasCameraPermission: Boolean,
    hasBluetoothPermission: Boolean,
    onRequestPermission: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    onRequestNearbyDevicesPermission: () -> Unit
) {
    if (hasCameraPermission && hasBluetoothPermission) {
        BluetoothConnectionScreen(
            modifier = modifier
        )
    } else {
        NoPermissionScreen(
            onRequestCameraPermission = onRequestPermission,
            onRequestBluetoothPermission = onRequestBluetoothPermission,
            onRequestNearbyDevicesPermission = onRequestNearbyDevicesPermission
        )
    }


//    else {
//        StartNavigationScreen(modifier = modifier)
//    }
}
