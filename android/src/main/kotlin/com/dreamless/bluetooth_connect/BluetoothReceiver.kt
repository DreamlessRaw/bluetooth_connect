package com.dreamless.bluetooth_connect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.flutter.plugin.common.MethodChannel

class BluetoothReceiver(private val channel: MethodChannel) : BroadcastReceiver() {

    companion object {
        const val TAG = "BluetoothReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> when (intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                0
            )) {
                BluetoothAdapter.STATE_TURNING_ON -> Log.i(TAG, "蓝牙正在打开")
                BluetoothAdapter.STATE_ON -> Log.i(TAG, "蓝牙已经打开")
                BluetoothAdapter.STATE_TURNING_OFF -> Log.i(TAG, "蓝牙正在关闭")
                BluetoothAdapter.STATE_OFF -> Log.i(TAG, "蓝牙已经关闭")
            }
            BluetoothDevice.ACTION_FOUND -> {
                val bluetoothDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, 0.toShort())
                bluetoothDevice?.run {
                    val map = mutableMapOf<String, Any?>()
                    map["address"] = this.address
                    map["name"] = if (this.name.isNullOrEmpty()) this.address else this.name
                    map["bond_state"] = this.bondState == BluetoothDevice.BOND_BONDED
                    map["rssi"] = rssi
                    channel.invokeMethod(BluetoothConnectPlugin.DEVICE_LISTEN, map)
                }
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                Log.i(TAG, "搜索开始")
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                Log.i(TAG, "搜索完成")
            }
        }
    }
}