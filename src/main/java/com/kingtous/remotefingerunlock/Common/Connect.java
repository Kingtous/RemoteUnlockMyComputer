package com.kingtous.remotefingerunlock.Common;

import com.kingtous.remotefingerunlock.BluetoothConnectTool.BluetoothConnect;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.WLANConnectTool.WLANConnect;

public class Connect {

    public static void start(RecordData data){
        if (data!=null){
            if (data.getType().equals("Bluetooth")){
                BluetoothConnect.start(data);
            }
            else
                WLANConnect.start(data);
        }
        else return;
    }
}
