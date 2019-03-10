package com.kingtous.remotefingerunlock.BluetoothConnectTool;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.widget.Toast;

import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import androidx.annotation.NonNull;

import static com.kingtous.remotefingerunlock.BluetoothConnectTool.BluetoothConnectActivity.MY_UUID;


public class BluetoothConnect {


    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private Context context;
    private RecordData record;

    public void start(@NonNull Context context,@NonNull RecordData data){
        this.context=context;
        record=data;

        manager=(BluetoothManager) context.getSystemService(Activity.BLUETOOTH_SERVICE);

        adapter=manager.getAdapter();

        checkBluetooth(context,adapter);

        IntentFilter filter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(receiver,filter);


    }

    private void checkBluetooth(Context context, final BluetoothAdapter bluetoothAdapter)
    {
        if (!bluetoothAdapter.isEnabled())
        {
            new AlertDialog.Builder(context)
                    .setMessage("蓝牙未打开")
                    .setMessage("请打开蓝牙")
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bluetoothAdapter.enable();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
        else startConnect();
    }


    private void startConnect(){
        final BluetoothDevice deviceSelected=adapter.getRemoteDevice(record.getMac());
        if (deviceSelected.getBondState() == BluetoothDevice.BOND_NONE) {
            deviceSelected.createBond();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Looper.prepare();
                        BluetoothSocket socket;
                        socket = deviceSelected.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                        socket.connect();
                        OutputStream stream = socket.getOutputStream();
                        JSONObject object = new JSONObject();
                        if (record.getUser() != null && record.getPasswd() != null) {
                            try {
                                object.put("username", record.getUser());
                                object.put("passwd", record.getPasswd());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (stream != null) {
                                stream.write(object.toString().getBytes(StandardCharsets.UTF_8));
                                log("发送成功\n内容:" + object.toString());
                            } else {
                                log("未打开输出流，请检查设备是否开启服务端");
                            }
                        }
                        if (stream != null) {
                            stream.close();
                        }
                    } catch (IOException e) {
                        log("相关设备未准备好，请检查设备是否开启服务端");
                        //e.printStackTrace();
                    } finally {
                        Looper.loop();
                    }
                }
            }).start();
        }


    }

    private BroadcastReceiver receiver= new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
                switch (state){
                    case (BluetoothAdapter.STATE_ON):
                        startConnect();
                        break;
                    default:
                        break;
                }

            }
        }
    };


    private void log(String text){
        if (context!=null){
            Toast.makeText(context,text,Toast.LENGTH_LONG).show();
        }
    }


}
