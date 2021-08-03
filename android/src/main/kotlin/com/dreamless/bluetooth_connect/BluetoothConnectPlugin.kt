package com.dreamless.bluetooth_connect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*
import kotlin.concurrent.thread

class BluetoothConnectPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    companion object {
        private const val TAG: String = "BluetoothConnectPlugin"
        private const val START_SCAN: String = "START_SCAN"
        private const val STOP_SCAN: String = "STOP_SCAN"
        private const val REGISTER: String = "REGISTER"
        private const val UNREGISTER: String = "UNREGISTER"
        private const val CONNECT_AND_READ: String = "CONNECT_AND_READ"
        private const val CONNECT_AND_READ_WEIGHT: String = "CONNECT_AND_READ_WEIGHT"
        public const val DEVICE_LISTEN: String = "DEVICE_LISTEN"
        private const val SOCKET_READ_LISTEN: String = "SOCKET_READ_LISTEN"
        private const val SOCKET_READ_WEIGHT_LISTEN: String = "SOCKET_READ_WEIGHT_LISTEN"
    }

    private lateinit var activityBinding: ActivityPluginBinding
    private lateinit var channel: MethodChannel
    private lateinit var receiver: BluetoothReceiver
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "bluetooth_connect")
        channel.setMethodCallHandler(this)
        receiver = BluetoothReceiver(channel)

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            REGISTER -> register(result)
            UNREGISTER -> unregister(result)
            START_SCAN -> startScan(result)
            STOP_SCAN -> stopScan(result)
            CONNECT_AND_READ -> connectAndRead(call.arguments.toString())
            CONNECT_AND_READ_WEIGHT -> connectAndReadWeight(call.arguments.toString())
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    /**
     * 注册广播
     */
    private fun register(result: Result) {
        try {
            val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            activityBinding.activity.registerReceiver(receiver, intentFilter)
            result.success(true)
        } catch (ex: Exception) {
            result.error("10001", ex.message, ex)
        }
    }

    /**
     * 停止广播
     */
    private fun unregister(result: Result) {
        try {
            activityBinding.activity.unregisterReceiver(receiver)
            result.success(true)
        } catch (ex: Exception) {
            ex.printStackTrace()
            result.error("10001", ex.message, ex)
        }
    }


    /**
     * 开始扫描
     */
    private fun startScan(result: Result) {
        when {
            !bluetoothAdapter.isEnabled -> result.error("10002", "蓝牙未开启", "")
            bluetoothAdapter.isDiscovering -> result.error("10002", "蓝牙正在扫描", "")
            (bluetoothAdapter.isEnabled && !bluetoothAdapter.isDiscovering) -> {
                bluetoothAdapter.startDiscovery()
                result.success(true)
            }
            else -> result.error("10002", "状态未知", "")
        }
    }

    /**
     * 停止扫描
     */
    private fun stopScan(result: Result) {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
            result.success(true)
        }
    }

    /**
     * 连接设备
     */
    private fun connectAndRead(mac: String) {
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac)
        when (bluetoothDevice.bondState) {
            BluetoothDevice.BOND_NONE -> {
                Toast.makeText(activityBinding.activity, "请先配对蓝牙", Toast.LENGTH_LONG).show()
            }
            BluetoothDevice.BOND_BONDED -> {
                val socket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                thread {
                    // 连接socket
                    socket.connect()
                    if (socket.isConnected) {
                        channel.invokeMethod(SOCKET_READ_LISTEN, socket.inputStream)
                    } else {
                        channel.invokeMethod(SOCKET_READ_LISTEN, "设备已断开连接")
                    }
                }
            }
            else -> {
                Toast.makeText(activityBinding.activity, "状态未知", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 直接获取重量，通用的请使用connectAndRead
     */
    private fun connectAndReadWeight(mac: String) {
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac)
        when (bluetoothDevice.bondState) {
            BluetoothDevice.BOND_NONE -> {
                Toast.makeText(activityBinding.activity, "请先配对蓝牙", Toast.LENGTH_LONG).show()
            }
            BluetoothDevice.BOND_BONDED -> {
                val socket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                thread {
                    // 连接socket
                    if (!socket.isConnected) {
                        socket.connect()
                    }
                    val tempSb: StringBuilder = StringBuilder()
                    val tt = ByteArray(1024)
                    while (true) {
                        try {
                            val iss = socket.inputStream
                            val len = iss.read(tt, 0, tt.size);
                            //原始字符串
                            val str = String(tt.take(len).toByteArray(), Charsets.UTF_8);

                            tempSb.append(str);

                            //蓝牙结束符 /r/n
                            if (str.contains("\r\n")) {
                                val pattern = "\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$";
                                val re = Regex(pattern)

                                val r: StringBuilder = java.lang.StringBuilder()
                                re.findAll(tempSb).forEach { r.append(it.value) }
                                tempSb.clear()
                                activityBinding.activity.runOnUiThread {
                                    channel.invokeMethod(
                                        SOCKET_READ_WEIGHT_LISTEN,
                                        r.toString()
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // 关闭连接
                            socket.close()
                            Log.i(TAG, e.message.toString())
                            e.printStackTrace()
                        }
                    }
                }
            }
            else -> {
                Toast.makeText(activityBinding.activity, "状态未知", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activityBinding = binding
    }

    override fun onDetachedFromActivity() {
    }


}
