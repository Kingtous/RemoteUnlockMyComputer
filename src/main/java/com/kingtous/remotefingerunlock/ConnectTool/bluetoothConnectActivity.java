package com.kingtous.remotefingerunlock.ConnectTool;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kingtous.remotefingerunlock.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class bluetoothConnectActivity extends AppCompatActivity {

    //蓝牙配置
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    //
    RecyclerView lst_view;
    bluetooth_Adapter adapter;
    ArrayList<bluetooth_device> device_list;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_list);

        bluetoothManager=(BluetoothManager) getSystemService(Activity.BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();

        lst_view=findViewById(R.id.lst_BLUETOOTH);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        lst_view.setLayoutManager(linearLayoutManager);

        device_list=new ArrayList<bluetooth_device>();
        testDeviceList();
        adapter=new bluetooth_Adapter(device_list);
        adapter.setOnItemClickListener(new bluetooth_Adapter.OnItemClickListener() {
            @Override
            public void OnClick(View view, int Position) {
                String name=((TextView)view.findViewById(R.id.name_bluetooth_device_name)).getText().toString();
                String mac=((TextView)view.findViewById(R.id.name_bluetooth_device_mac)).getText().toString();
                Toast.makeText(bluetoothConnectActivity.this,"你点击了：\n"+name+'\n'+mac,Toast.LENGTH_LONG).show();
            }
        });

        lst_view.setAdapter(adapter);
//        setFooterButtons(lst_view);


        send();
    }

    public void send()
    {

    }


    private void setFooterButtons(RecyclerView view)
    {
        View header= LayoutInflater.from(this).inflate(R.layout.bluetooth_list_footer,view,false);
        adapter.setFooter(header);
    }

    private void getDeviceList()
    {
        //蓝牙操作
    }

    private void testDeviceList()
    {
        for (int i=0;i<10;i++)
        {
            bluetooth_device device=new bluetooth_device(String.valueOf(i),String.valueOf(i),String.valueOf(i));
            device_list.add(device);
        }
    }


}
