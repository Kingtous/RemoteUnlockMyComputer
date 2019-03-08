package com.kingtous.remotefingerunlock.ConnectTool;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kingtous.remotefingerunlock.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import pub.devrel.easypermissions.EasyPermissions;

public class bluetoothConnectActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    //蓝牙配置
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    //
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView lst_view;
    bluetooth_Adapter adapter;
    Set<BluetoothDevice> pairedDevices;//已配对设备
    ArrayList<bluetooth_device> device_list;
    BluetoothDevice deviceSelected;
    IntentFilter filter;//广播
    TextView BluetoothStatusView;

    Button btn_search;
    Button btn_connect;
    Button btn_back;

    //请求码
    int Request_position=1;
    String MY_UUID="4E5877C0-8297-4AAE-B7BD-73A8CBC1EDAF";

    //
    String name;
    String passwd;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_list);

        btn_search=findViewById(R.id.btn_BLUETOOTH_search);
        btn_connect=findViewById(R.id.btn_BLUETOOTH_connect);
        btn_back=findViewById(R.id.btn_BLUETOOTH_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        BluetoothStatusView=findViewById(R.id.title_bluetooth_status);

        //下拉搜索
        swipeRefreshLayout=findViewById(R.id.lst_BLUETOOTH_swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getDeviceList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceList();
            }
        });


        bluetoothManager=(BluetoothManager) getSystemService(Activity.BLUETOOTH_SERVICE);

        bluetoothAdapter=bluetoothManager.getAdapter();
        if (bluetoothAdapter==null){
            Toast.makeText(this,"设备不支持蓝牙",Toast.LENGTH_SHORT).show();
            finish();
        }

        lst_view=findViewById(R.id.lst_BLUETOOTH);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        lst_view.setLayoutManager(linearLayoutManager);

        device_list=new ArrayList<bluetooth_device>();

        adapter=new bluetooth_Adapter(device_list);
        adapter.setOnItemClickListener(new bluetooth_Adapter.OnItemClickListener() {
            @Override
            public void OnClick(View view, int Position) {
                //DEBUG: String name=((TextView)view.findViewById(R.id.name_bluetooth_device_name)).getText().toString();
                String mac=((TextView)view.findViewById(R.id.name_bluetooth_device_mac)).getText().toString();
                deviceSelected=bluetoothAdapter.getRemoteDevice(mac);
                if (deviceSelected.getBondState()==BluetoothDevice.BOND_NONE)
                {
                    Toast.makeText(bluetoothConnectActivity.this,"还没配对，正在配对中...",Toast.LENGTH_SHORT).show();
                    deviceSelected.createBond();
                }
                else {
                    Query();
                }

            }
        });

        lst_view.setAdapter(adapter);
//        setFooterButtons(lst_view);

        //注册
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);


        //已配对
        getDeviceList();

    }

    private void startConnect()
    {
        connect();
    }

    public void Query(){

        final View et=LayoutInflater.from(this).inflate(R.layout.dialog_user_passwd,null,false);

        new AlertDialog.Builder(this).setTitle("请输入设备的账户名，密码")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        name=((EditText)et.findViewById(R.id.edit_username)).getText().toString();
                        passwd=((EditText)et.findViewById(R.id.edit_passwd)).getText().toString();
                        startConnect();
                    }
                })
                .setNegativeButton("取消",null).show();
    }


    private void setFooterButtons(RecyclerView view)
    {
        View header= LayoutInflater.from(this).inflate(R.layout.bluetooth_list_footer,view,false);
        adapter.setFooter(header);
    }

    private void checkBluetooth()
    {
        if (!bluetoothAdapter.isEnabled())
        {
            new AlertDialog.Builder(this)
                    .setMessage("蓝牙未打开")
                    .setMessage("请打开蓝牙")
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bluetoothAdapter.enable();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
        //检查权限
        String[] strings={Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
        if (!EasyPermissions.hasPermissions(this,strings))
        {
            EasyPermissions.requestPermissions(this,"蓝牙搜索还需要位置服务.",Request_position,strings);
        }

    }

    private void getBondDevices(){
        pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                device_list.add(new bluetooth_device(device.getName()+" (已配对)",device.getAddress()));
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void clearDeviceList()
    {
        device_list.clear();
    }

    private void getDeviceList()
    {
        checkBluetooth();
        clearDeviceList();
        getBondDevices();
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                if (!(pairedDevices.contains(device))) {
                    for (bluetooth_device list_Dev : device_list) {
                        if (list_Dev.getMac().equals(device.getAddress()))
                            return;
                    }
                    device_list.add(new bluetooth_device(device.getName(), device.getAddress()));
                    adapter.notifyDataSetChanged();
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                BluetoothStatusView.setText("正在搜索");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                BluetoothStatusView.setText("搜索完成");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (deviceSelected.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Query();
                } else if (deviceSelected.getBondState() == BluetoothDevice.BOND_NONE) {
                    //Toast.makeText(bluetoothConnectActivity.this, "配对取消", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };


    private void connect()
    {
        bluetoothAdapter.cancelDiscovery();
            if (deviceSelected.getBondState() == BluetoothDevice.BOND_NONE) {
                deviceSelected.createBond();
            } else {
                Toast.makeText(this,"正在连接",Toast.LENGTH_SHORT).show();
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
                            if (name != null && passwd != null) {
                                try {
                                    object.put("username", name);
                                    object.put("passwd", passwd);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (stream!= null)
                                {
                                    stream.write(object.toString().getBytes(StandardCharsets.UTF_8));
                                    log("发送成功\n内容:"+object.toString());
                                }
                                else {
                                    log("未打开输出流，请检查设备是否开启服务端");
                                }
                            }
                            if(stream!=null)
                            {
                                stream.close();
                            }
                        } catch (IOException e) {
                            log("相关设备未准备好，请检查设备是否开启服务端");
                            //e.printStackTrace();
                        }
                        finally {
                            Looper.loop();
                        }
                    }
                }).start();


            }
        }



    private void testDeviceList()
    {
        for (int i=0;i<10;i++)
        {
            bluetooth_device device=new bluetooth_device(String.valueOf(i),String.valueOf(i));
            device_list.add(device);
        }
    }

    private void log(String text)
    {
        Toast.makeText(bluetoothConnectActivity.this,text,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this,"相关权限未打开，蓝牙发现功能可能不运作",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

    }
}
