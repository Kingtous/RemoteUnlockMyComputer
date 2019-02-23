package com.kingtous.remotefingerunlock.ToolFragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class bluetoothConnect extends Activity {

    String name="Kingtous";
    String passwd="password";

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothManager=(BluetoothManager) getSystemService(Activity.BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();



        send();
    }

    public void send()
    {

    }



}
