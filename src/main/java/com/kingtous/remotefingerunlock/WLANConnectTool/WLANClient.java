package com.kingtous.remotefingerunlock.WLANConnectTool;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class WLANClient extends AsyncTask<Void, String, String> {

    String host;
    int port;
    RecordData data;
    private Context context;

    WLANClient(Context context,String host, int port, RecordData data) {
        this.context=context;
        this.host = host;
        this.port = port;
        this.data = data;
    }

    @Override
    protected String doInBackground(Void... arg0) {
        Socket socket = null;
        try {
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);
            Log.d("async-client","connected to: " + host + ":" + port);
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            Gson gson=new Gson();
            bw.write(gson.toJson(data));
            bw.flush();

            Log.d("async-client","sent message: " + gson.toJson(data));

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            Log.d("async-client","recieved message: " + response);
            return response;
        } catch (IOException exception) {
            Toast.makeText(context,"连接失败，请检查服务器设置",Toast.LENGTH_LONG).show();
            Log.d("async-client", "the server is offline?");
        }finally {
            Toast.makeText(context,"发送",Toast.LENGTH_LONG).show();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

}
