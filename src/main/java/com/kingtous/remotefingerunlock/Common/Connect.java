package com.kingtous.remotefingerunlock.Common;

import android.content.Context;

import com.kingtous.remotefingerunlock.BluetoothConnectTool.BluetoothConnect;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.WLANConnectTool.WLANConnect;

public class Connect {

    public static void start(Context context, RecordData data){
        if (data!=null){
            if (data.getType().equals("Bluetooth")){
                BluetoothConnect connection=new BluetoothConnect();
                connection.start(context,data);
            }
            else{
                WLANConnect connection=new WLANConnect();
                connection.start(context,data);
            }
        }
        else return;
    }
}
