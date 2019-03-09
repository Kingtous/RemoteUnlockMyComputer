package com.kingtous.remotefingerunlock.ToolFragment;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kingtous.remotefingerunlock.Common.Connect;
import com.kingtous.remotefingerunlock.DataStoreTool.DataQueryHelper;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordSQLTool;
import com.kingtous.remotefingerunlock.R;

import java.util.Objects;


public class UnlockFragment extends Fragment {

    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;
    CancellationSignal cancellationSignal;
    FingerprintManager.AuthenticationCallback authenticationCallback;
    SQLiteOpenHelper helper;

    Context context;
    public UnlockFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.unlock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper=new DataQueryHelper(context,getString(R.string.sqlDBName),null,1);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintManager = (FingerprintManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.FINGERPRINT_SERVICE);
        keyguardManager = (KeyguardManager) getActivity().getSystemService(Activity.KEYGUARD_SERVICE);
        cancellationSignal = new CancellationSignal();
        context=getContext();
        //检测是否有硬件
        if (!fingerprintManager.isHardwareDetected()) {
            Toast.makeText(getContext(), "没检测到相关指纹硬件，指纹解锁可能不生效", Toast.LENGTH_LONG).show();
        }
        initCallBack();
        startFingerListening();

    }

    private void initCallBack() {
        authenticationCallback = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //Toast.makeText(context, errString, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                Toast.makeText(context, helpString, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                RecordData data=RecordSQLTool.getDefaultRecordData(helper.getReadableDatabase());
                if (data!=null)
                {
                    Toast.makeText(context,"验证成功，连接中...",Toast.LENGTH_SHORT).show();
                    Connect.start(data);
                }
                else Toast.makeText(context,"还未配置默认连接，请到设置中配置或在扫描时添加默认配置",Toast.LENGTH_LONG).show();
                startFingerListening();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(context, "指纹认证失败", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void startFingerListening() {
        fingerprintManager.authenticate(null,
                cancellationSignal,
                0,
                authenticationCallback,
                null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancellationSignal.cancel();
    }


}
