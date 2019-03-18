package com.kingtous.remotefingerunlock.WLANConnectTool;

public class WLANDeviceData  {

    static int port=2085;


    String name;
    String mac;
    String currentIP;

    public WLANDeviceData(String name, String mac) {
        if (name==null ||name.equals(""))
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


    public String getCurrentIP() {
        return currentIP;
    }

    public void setCurrentIP(String currentIP) {
        this.currentIP = currentIP;
    }
}
