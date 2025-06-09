package com.example.vision2.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.*

class BluetoothDistanceReader(private val deviceName: String = "ESP32-Ultrasonic") {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null


    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun connectAndRead(
        context: Context,
        onDistanceReceived: (Int?) -> Unit  // Int? allows null for error
    ) {
        withContext(Dispatchers.IO) {
            try {
                val permissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED

                if (!permissionGranted) {
                    onDistanceReceived(null) // null for permission error
                    return@withContext
                }

                val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.find {
                    it.name == deviceName
                }

                if (device != null) {
                    val uuid: UUID = device.uuids[0].uuid
                    socket = device.createRfcommSocketToServiceRecord(uuid)
                    bluetoothAdapter?.cancelDiscovery()
                    socket?.connect()

                    val inputStream: InputStream? = socket?.inputStream
                    val buffer = ByteArray(1024)

                    while (true) {
                        val bytes = inputStream?.read(buffer) ?: break
                        val received = buffer.decodeToString(0, bytes).trim()
                        val distance = received.toIntOrNull()
                        onDistanceReceived(distance)
                    }
                } else {
                    onDistanceReceived(null) // null for "Device not found"
                }

            } catch (e: SecurityException) {
                onDistanceReceived(null)
            } catch (e: Exception) {
                e.printStackTrace()
                onDistanceReceived(null)
            }
        }
    }

    fun closeConnection() {
        socket?.close()
    }
}