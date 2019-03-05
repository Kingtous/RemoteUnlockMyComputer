package com.kingtous.remotefingerunlock.ConnectTool;

public class bluetooth_device {

    String name;
    String mac;
    String uuid;

    public bluetooth_device(String name, String mac, String uuid) {
        this.name = name;
        this.mac = mac;
        this.uuid = uuid;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
