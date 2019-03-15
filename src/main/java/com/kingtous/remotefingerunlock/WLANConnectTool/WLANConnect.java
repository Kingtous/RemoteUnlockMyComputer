package com.kingtous.remotefingerunlock.WLANConnectTool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;

import androidx.appcompat.app.AlertDialog;

public class WLANConnect {


    WifiManager manager;

    Context context;
    RecordData data;

    public void start(Context context, RecordData data){
        manager=(WifiManager) context.getApplicationContext().getSystemService(Activity.WIFI_SERVICE);
        this.context=context;
        this.data=data;
        if (manager!=null){
            IntentFilter filter=new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
            context.registerReceiver(receiver,filter);

        }
        else return;
    }


    public static void checkWLAN(final Context context, final WifiManager manager){
        if (!manager.isWifiEnabled()){
            new AlertDialog.Builder(context)
                    .setMessage("未打开WLAN，请问是否开启?")
                    .setPositiveButton("开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            manager.setWifiEnabled(true);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();

        }
        else return;

    }

    private BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            assert action != null;
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                int state=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
                switch (state){
                    case WifiManager.WIFI_STATE_ENABLED:
                        startConnect(context,data);
                        break;
                    default:
                        break;
                }
            }
        }
    };


    private void startConnect(Context context,RecordData data){

    }

}
