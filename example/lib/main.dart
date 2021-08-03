import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:bluetooth_connect/bluetooth_connect.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  double weight = 0.0;

  @override
  void initState() {
    super.initState();
    BluetoothConnect.listen(deviceCall: (device) {
      log(jsonEncode(device));
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('实时重量:${this.weight}'),
        ),
        floatingActionButton: IconButton(
            onPressed: () {
              Permission.location.request().isGranted.then((value) async {
                if (value) {
                  BluetoothConnect.register();
                  BluetoothConnect.startScan();
                  BluetoothConnect.startWeight('06:63:18:02:00:07');
                } else {
                  log('你没有权限');
                }
              });
            },
            icon: Icon(Icons.add)),
      ),
    );
  }

  @override
  void dispose() {
    BluetoothConnect.unregister();
    log('销毁广播');
    super.dispose();
  }
}
