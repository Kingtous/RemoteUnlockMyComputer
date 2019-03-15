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
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.kingtous.remotefingerunlock.DataStoreTool.DataQueryHelper;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordSQLTool;
import com.kingtous.remotefingerunlock.MainActivity;
import com.kingtous.remotefingerunlock.R;

import org.json.JSONException;
import org.json.JSONObject;

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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import pub.devrel.easypermissions.EasyPermissions;

public class BluetoothConnectActivity extends SwipeBackActivity implements EasyPermissions.PermissionCallbacks {

    //蓝牙配置
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    //
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView lst_view;
    BluetoothRecyclerAdapter adapter;
    Set<BluetoothDevice> pairedDevices;//已配对设备
    ArrayList<BluetoothDeviceData> device_list;
    BluetoothDevice deviceSelected;
    IntentFilter filter;//广播
    TextView BluetoothStatusView;

    Button btn_search;
    Button btn_connect;
    Button btn_back;

    //请求码
    static int Request_position=1;
    static String MY_UUID="4E5877C0-8297-4AAE-B7BD-73A8CBC1EDAF";

    //
    String name;
    String passwd;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_list);

        //注册
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);

        btn_search=(Button) findViewById(R.id.btn_BLUETOOTH_search);
        btn_connect=(Button)findViewById(R.id.btn_BLUETOOTH_connect);
        btn_back=(Button)findViewById(R.id.btn_BLUETOOTH_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        BluetoothStatusView=(TextView)findViewById(R.id.title_bluetooth_status);

        //下拉搜索
        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.lst_BLUETOOTH_swipe);

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

        lst_view=(RecyclerView) findViewById(R.id.lst_BLUETOOTH);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        lst_view.setLayoutManager(linearLayoutManager);

        device_list=new ArrayList<BluetoothDeviceData>();

        adapter=new BluetoothRecyclerAdapter(device_list);
        adapter.setOnItemClickListener(new BluetoothRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View view, int Position) {
                //DEBUG: String name=((TextView)view.findViewById(R.id.name_bluetooth_device_name)).getText().toString();
                String mac=((TextView)view.findViewById(R.id.name_bluetooth_device_mac)).getText().toString();
                deviceSelected=bluetoothAdapter.getRemoteDevice(mac);
                if (deviceSelected.getBondState()==BluetoothDevice.BOND_NONE)
                {
                    Toast.makeText(BluetoothConnectActivity.this,"还没配对，正在配对中...",Toast.LENGTH_SHORT).show();
                    deviceSelected.createBond();
                }
                else {
                    Query();
                }
            }
        });

        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(300);
        defaultItemAnimator.setRemoveDuration(300);
        lst_view.setItemAnimator(defaultItemAnimator);

        lst_view.setAdapter(adapter);
//        setFooterButtons(lst_view);



        //已配对
        if (bluetoothAdapter!=null)
            getDeviceList();

    }

    private void startConnect()
    {
        connect();
    }

    public void Query(){

        final View view=LayoutInflater.from(this).inflate(R.layout.dialog_user_passwd,null,false);

        //设置CheckBox关系
        final CheckBox box_store=view.findViewById(R.id.dialog_checkbox_storeConnection);
        final CheckBox box_default=view.findViewById(R.id.dialog_checkbox_setDefault);

        box_default.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    box_store.setChecked(true);
                }
            }
        });

        new AlertDialog.Builder(this).setTitle("请输入设备的账户名，密码")
                .setView(view)
                .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        name=((EditText)view.findViewById(R.id.edit_username)).getText().toString();
                        passwd=((EditText)view.findViewById(R.id.edit_passwd)).getText().toString();
                        //检查checkbox

                        SQLiteOpenHelper helper=new DataQueryHelper(BluetoothConnectActivity.this,getString(R.string.sqlDBName),null,1);
                        if (box_store.isChecked()){
                            //保存
                            boolean result=RecordSQLTool.addtoSQL(helper,new RecordData("Bluetooth",name,passwd,deviceSelected.getAddress()));
                            if (!result)
                                log("保存失败，存在同MAC地址的记录或者数据库异常");
                        }
                        if (box_default.isChecked()){
                            //设置为指纹默认
                            RecordSQLTool.updateDefaultRecord(helper,deviceSelected.getAddress());
                        }
                        helper=null;
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

            final NiftyDialogBuilder builder=NiftyDialogBuilder.getInstance(BluetoothConnectActivity.this);
            builder.withEffect(Effectstype.Shake)
                    .withDialogColor(R.color.dodgerblue)
                    .withMessage("蓝牙未打开，是否打开蓝牙？")
                    .withButton1Text("打开")
                    .withButton2Text("取消")
                    .setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bluetoothAdapter.enable();
                            builder.dismiss();
                        }
                    })
                    .setButton2Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                device_list.add(new BluetoothDeviceData(device.getName()+" (已配对)",device.getAddress()));
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
                    for (BluetoothDeviceData list_Dev : device_list) {
                        if (list_Dev.getMac().equals(device.getAddress()))
                            return;
                    }
                    device_list.add(new BluetoothDeviceData(device.getName(), device.getAddress()));
                    adapter.notifyDataSetChanged();
                }
            }
            else if (android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                BluetoothStatusView.setText("正在搜索");
            }
            else if (android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                BluetoothStatusView.setText("搜索完成");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (deviceSelected.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Query();
                } else if (deviceSelected.getBondState() == BluetoothDevice.BOND_NONE) {
                    //Toast.makeText(BluetoothConnectActivity.this, "配对取消", Toast.LENGTH_SHORT).show();
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
            BluetoothDeviceData device=new BluetoothDeviceData(String.valueOf(i),String.valueOf(i));
            device_list.add(device);
        }
    }

    private void log(String text)
    {
        Toast.makeText(BluetoothConnectActivity.this,text,Toast.LENGTH_SHORT).show();
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
