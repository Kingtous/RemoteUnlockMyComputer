package com.kingtous.remotefingerunlock.ToolFragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.kingtous.remotefingerunlock.BluetoothConnectTool.BluetoothConnectActivity;
import com.kingtous.remotefingerunlock.R;
import com.kingtous.remotefingerunlock.WLANConnectTool.WLANConnectActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScanFragment extends Fragment {

    public ScanFragment(){

    }

    Button btn_WL;
    Button btn_BT;

    int BT_RequestCode=1;
    int WL_RequestCode=2;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.scan_list,container,false);
        btn_WL=view.findViewById(R.id.btn_WLAN);
        btn_BT=view.findViewById(R.id.btn_BLUETOOTH);

        btn_WL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Snackbar.make(v, "正在开发中...", Snackbar.LENGTH_LONG)
//                        .setAction("OK", null).show();
                Intent intent=new Intent(getContext(), WLANConnectActivity.class);
                startActivityForResult(intent,WL_RequestCode);
            }
        });


        btn_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), BluetoothConnectActivity.class);
                startActivityForResult(intent,BT_RequestCode);
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==BT_RequestCode)
        {

        }
        else if (requestCode==WL_RequestCode)
        {

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
