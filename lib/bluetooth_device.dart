import 'dart:developer';

class BluetoothDevice {
  String name = '';
  String mac = '';
  int rssi = 0;
  bool bondState = false;

  BluetoothDevice(
      {this.name = '', this.mac = '', this.rssi = 0, this.bondState = false});

  BluetoothDevice.fromJson(dynamic json) {
    this.name = json['name'];
    this.mac = json['address'];
    this.rssi = json['rssi'];
    this.bondState = json['bond_state'];
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'name': name,
        'mac': mac,
        'rssi': rssi,
        'state': bondState
      };
}
