package com.kingtous.remotefingerunlock.WLANConnectTool;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.kingtous.remotefingerunlock.BluetoothConnectTool.BluetoothConnectActivity;
import com.kingtous.remotefingerunlock.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pub.devrel.easypermissions.EasyPermissions;

public class WLANConnectActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    static int Text=0;
    static int Info=1;

    WifiManager manager;
    ArrayList<WLANDeviceData> deviceDatalist=new ArrayList<>();
    RecyclerView lst_wlan;
    WLANRecyclerAdapter adapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {  //这个是发送过来的消息
            // 处理从子线程发送过来的消息
            int arg1 = msg.arg1;  //获取消息携带的属性值
            Object result = msg.obj;
            switch (arg1){
                case 0:
                    log((String) result);
                    break;
                case 1:
                    Bundle bundle=msg.getData();
                    String mac=bundle.getString("Mac");
                    for (WLANDeviceData data:deviceDatalist){
                        if (data.getMac()==mac){
                            return;
                        }
                    }
                    WLANDeviceData data=new WLANDeviceData(null,mac);
                    deviceDatalist.add(data);
                    adapter.notifyDataSetChanged();
                    //WIFI信息

            }

        };
    };

    int MAX_DEVICES=10;
    SearchThread thread=new SearchThread(handler,MAX_DEVICES);

    //权限
    int WIFI_REQUEST_CODE=2;
    String[] permission={
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list);
        //设置服务和控件
        lst_wlan=findViewById(R.id.lst_WLAN);
        manager=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        adapter=new WLANRecyclerAdapter(deviceDatalist);

        //Wifi三连
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        lst_wlan.setLayoutManager(linearLayoutManager);
        lst_wlan.setAdapter(adapter);

        //广播
        IntentFilter filter=new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver,filter);
        //检查wlan状况
        checkWLAN(this,manager);
    }

    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            int extra;
            assert action != null;
            switch (action){
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    extra=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
                    switch (extra){
                        case WifiManager.WIFI_STATE_ENABLED:
                            //检测wifi是否连接无线局域网
                            if (isWifiConnected()){
                                //wifi已开启，开始向局域网广播
                                if (thread.isInterrupted())
                                    thread=new SearchThread(handler,MAX_DEVICES);
                                thread.startSearch();
                            }
                            else {
                                Toast.makeText(WLANConnectActivity.this,"WLAN还未连接",Toast.LENGTH_LONG).show();
                            }
                            break;

                        case WifiManager.WIFI_STATE_DISABLING:
                            log("Wifi关闭,停止扫描");
                            if (!thread.isInterrupted())
                                thread.interrupt();
                            break;
                        default:
                            break;
                    }
                    break;

                case ConnectivityManager.CONNECTIVITY_ACTION:
                    extra=intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE,-1);
                    switch (extra)
                    {
                        case ConnectivityManager.TYPE_WIFI:
                            //wifi已开启，开始向局域网广播
                            if (thread.isInterrupted() || !thread.isAlive()){
                                thread=new SearchThread(handler,MAX_DEVICES);
                                thread.startSearch();
                            }
                            break;
                        default:
                            if (!thread.isInterrupted()){
                                thread.interrupt();
                            }
                            break;
                    }
                    break;
                    default:
                    break;
            }
        }
    };

    private boolean isWifiConnected() {
        /*
        判断是否为Wifi连接
         */
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        int state=networkInfo.getType();
        switch (state) {
            case ConnectivityManager.TYPE_WIFI:
                return true;
            default:
                return false;
        }


    }


    private void checkPermission(Context context){
        if (!EasyPermissions.hasPermissions(context,permission)){
            EasyPermissions.requestPermissions(this,"此功能需要WLAN相关权限",WIFI_REQUEST_CODE,permission);
        }
        return;
    }


    public void checkWLAN(final Context context, final WifiManager manager){
        checkPermission(context);
        if (manager!=null && !manager.isWifiEnabled()){

            final NiftyDialogBuilder builder=NiftyDialogBuilder.getInstance(WLANConnectActivity.this);
            builder.withEffect(Effectstype.Shake)
                    .withDialogColor(R.color.dodgerblue)
                    .withTitle("WLAN检测")
                    .withMessage("未打开WLAN，请问是否开启?")
                    .withButton1Text("打开")
                    .withButton2Text("取消")
                    .setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manager.setWifiEnabled(true);
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
        else return;

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode==WIFI_REQUEST_CODE){
            finish();
        }
    }

    static class SearchThread extends Thread {
        private boolean flag = true;
        private byte[] recvDate = null;
        private byte[] sendDate = null;
        private DatagramPacket recvDP = null;
        private DatagramSocket recvDS = null;
        private DatagramSocket sendDS = null;
        private Handler mHandler;
        private StateChangeListener onStateChangeListener;
        private int state;
        private int maxDevices;//防止广播攻击，设置最大搜素数量
        public static final int STATE_INIT_FINISH = 0;
        public static final int STATE_SEND_BROADCAST = 1;
        public static final int STATE_WAITE_RESPONSE = 2;
        public static final int STATE_HANDLE_RESPONSE = 3;

        public SearchThread(Handler handler, int max) {
            recvDate = new byte[256];
            recvDP = new DatagramPacket(recvDate, 0, recvDate.length);
            mHandler = handler;
            maxDevices = max;

        }

        public void setOnStateChangeListener(StateChangeListener onStateChangeListener) {
            this.onStateChangeListener = onStateChangeListener;
        }

        public void run() {
            try {
                recvDS = new DatagramSocket(2085);//接收响应套接口
                sendDS = new DatagramSocket();//广播发送套接口

                changeState(STATE_INIT_FINISH);//更新线程状态
                //发送一次广播:广播地址255.255.255.255和组播地址224.0.1.140 --  为了防止丢包，理应多次发送
                sendDate = "query".getBytes();//设置发送数据
                DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("255.255.255.255"), 53000);//广播UDP数据包
                sendDS.send(sendDP);//发送数据包
                changeState(STATE_SEND_BROADCAST);//更新线程状态
//                sendMsg("等待接收-----");//日志打印
                int curDevices = 0;//当前搜索到的设备数量
                while (flag) {
                    changeState(STATE_WAITE_RESPONSE);
                    recvDS.receive(recvDP);//阻塞等待接收响应
                    changeState(STATE_HANDLE_RESPONSE);
                    String recvContent = new String(recvDP.getData());
                    //判断是不是本机发起的结束搜索请求--处理响应内容
                    if (recvContent.contains("stop_search")) {
                        sendMsg(WLANConnectActivity.Text,"停止搜索：" + flag);
                    } else {
                        if (curDevices >= maxDevices) {
                            break;
                        }
                        sendMsg(WLANConnectActivity.Text,"收到：" + recvDP.getAddress() + ":" + recvDP.getPort() + " 发来：" + recvContent);
                        Bundle bundle=new Bundle();
                        bundle.putString("Mac",recvDP.getAddress().toString());
                        Message message=new Message();
                        message.arg1=Info;
                        message.setData(bundle);
                        mHandler.handleMessage(message);
                        //回应
                        sendDate = "name:服务器:msg:你好啊:type:response".getBytes();//回应内容
                        DatagramPacket responseDP = new DatagramPacket(sendDate, sendDate.length, recvDP.getAddress(), 53000);//回应数据包
                        sendDS.send(responseDP);//发送回应
                        curDevices++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (recvDS != null)
                    recvDS.close();
                if (sendDS != null)
                    sendDS.close();
            }
        }

        private void sendMsg(int type,String string) {
            Message msg = Message.obtain(mHandler);
            msg.arg1=type;
            msg.obj = string;
            mHandler.sendMessage(msg);
        }

        public void stopSearch() {
            flag = false;
            //由于在等待接收数据包时阻塞，无法达到关闭线程效果，因此给本机发送一个消息取消阻塞状态
            //为了避免用户在UI线程调用，所以新建一个线程
            new Thread() {
                @Override
                public void run() {
                    if (sendDS != null) {
                        sendDate = "msg:stop_search:type:stop".getBytes();
                        try {
                            DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("localhost"), 54000);
                            sendDS.send(sendDP);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        public void startSearch() {
            flag = true;
            start();
            sendMsg(Text,"开始搜索");
        }

        private void changeState(int state) {
            this.state = state;
            if (onStateChangeListener != null) {
                onStateChangeListener.onStateChanged(this.state);
            }
        }
        //搜索状态更新回调
        public interface StateChangeListener {
            void onStateChanged(int state);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void log(String text){
        Toast.makeText(WLANConnectActivity.this,text,Toast.LENGTH_LONG).show();
    }

}
