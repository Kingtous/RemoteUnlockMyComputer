package com.kingtous.remotefingerunlock.BluetoothConnectTool;

public class BluetoothDeviceData {

    String name;
    String mac;

    public BluetoothDeviceData(String name, String mac) {
        if (name==null)
        {
            this.name="(未指定)";
        }
        this.name = name;
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

}
