import 'dart:async';
import 'dart:convert';
import 'dart:developer';

import 'package:bluetooth_connect/bluetooth_device.dart';
import 'package:flutter/services.dart';

class BluetoothConnect {
  static const MethodChannel _channel = MethodChannel('bluetooth_connect');

  static Future<void> listen(
      {Function(double)? weightCall, Function(BluetoothDevice)? deviceCall}) async {
    _channel.setMethodCallHandler((call) async {
      switch (call.method) {
        case "SOCKET_READ_WEIGHT_LISTEN":
          if (weightCall != null) {
            double data = call.arguments;
            weightCall(data);
          }
          break;
        case "DEVICE_LISTEN":
          if (deviceCall != null) {
              var data = BluetoothDevice.fromJson(call.arguments);
              deviceCall(data);
          }
          break;
        default:
          log('没有此方法：${call.method}');
          break;
      }
    });
  }

  static Future<bool> register() async {
    try {
      bool result = await _channel.invokeMethod('REGISTER');
      return result;
    } on PlatformException catch (ex) {
      print('错误码:${ex.code}\t错误消息:${ex.message}');
      return false;
    }
  }

  static Future<bool> unregister() async {
    try {
      bool result = await _channel.invokeMethod('UNREGISTER');
      return result;
    } on PlatformException catch (ex) {
      print('错误码:${ex.code}\t错误消息:${ex.message}');
      return false;
    }
  }

  static Future<bool> startScan() async {
    try {
      bool result = await _channel.invokeMethod('START_SCAN');
      return result;
    } on PlatformException catch (ex) {
      print('错误码:${ex.code}\t错误消息:${ex.message}');
      return false;
    }
  }

  static Future<bool> stopScan() async {
    try {
      bool result = await _channel.invokeMethod('STOP_SCAN');
      return result;
    } on PlatformException catch (ex) {
      print('错误码:${ex.code}\t错误消息:${ex.message}');
      return false;
    }
  }

  static Future<void> startWeight(String mac) async {
    try {
      await _channel.invokeMethod('CONNECT_AND_READ_WEIGHT', mac);
    } on PlatformException catch (ex) {
      print('错误码:${ex.code}\t错误消息:${ex.message}');
    }
  }
}
