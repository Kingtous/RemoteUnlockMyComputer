package com.kingtous.remotefingerunlock.ToolFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kingtous.remotefingerunlock.DataStoreTool.DataQueryHelper;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordData;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordAdapter;
import com.kingtous.remotefingerunlock.DataStoreTool.RecordSQLTool;
import com.kingtous.remotefingerunlock.DataStoreTool.SecurityTransform;
import com.kingtous.remotefingerunlock.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pub.devrel.easypermissions.EasyPermissions;

public class DataManagementFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    int EDIT_BUTTON=0;
    int DELETE_BUTTON=1;

    int WRITE_PERMISSION=2;

    String macRegex="[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}";
    Pattern pattern=Pattern.compile(macRegex);


    private ArrayList<RecordData> recordData;

    private RecyclerView data_view;
    private RecordAdapter adapter;
    private FloatingActionButton floatingActionButton;
    private DataQueryHelper helper;
    private RelativeLayout app_empty;

    private SQLiteDatabase readSQL;
    private SQLiteDatabase writeSQL;

    public DataManagementFragment(){

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkPermission();
        getRecord();
    }

    private void checkPermission()
    {
        String[] WritePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!EasyPermissions.hasPermissions(Objects.requireNonNull(getContext()),WritePermission))
        {
            EasyPermissions.requestPermissions(this,"存储数据需要写入权限",WRITE_PERMISSION,WritePermission);
        }
    }

    private void update()
    {
        if (recordData.size()==0)
        {
            app_empty.setVisibility(View.VISIBLE);
        }
        else app_empty.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void deleteRecord(RecordData recordData)
    {
        if (writeSQL!=null){
            String mac="'"+ recordData.getMac()+"'";
            try {
                //删除SQL中的元素
                writeSQL.execSQL("delete from data where Mac=" + mac);
                //删除List中的
                this.recordData.remove(recordData);
            }
            catch (Exception e)
            {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateRecord(RecordData recordData, RecordData newlyRecordData){
        if (writeSQL!=null)
        {
            String mac="'"+ recordData.getMac()+"'";

            ContentValues values=new ContentValues();
            values.put("Mac", newlyRecordData.getMac());
            values.put("User", newlyRecordData.getUser());
            values.put("Type", newlyRecordData.getType());
            values.put("Passwd", newlyRecordData.getPasswd());
            String[] cond=new String[]{mac};
            try {
                writeSQL.update("data",values,"Mac=?",cond);
            }
            catch (Exception e)
            {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }

        }

    }

    private void getRecord()
    {
        if (readSQL!=null)
        {
            Cursor cursor=readSQL.query
                    ("data",null,null,null,null,null,"Type");
            while (cursor.moveToNext()){
                RecordData recordData =RecordSQLTool.toRecordData(cursor);
                this.recordData.add(recordData);
            }
            update();
            cursor.close();
        }
    }


    private void addRecord(String Type,String user,String passwd,String mac)
    {
        if (user.equals("") || passwd.equals("") || mac.equals(""))
        {
            Toast.makeText(getContext(),"输入数据不合法",Toast.LENGTH_SHORT).show();
        }
        else {

            ContentValues values=new ContentValues();
            values.put("Type",Type);
            values.put("Mac",mac);
            values.put("User",user);
            values.put("Passwd",passwd);
            try {
                writeSQL.insert("data",null,values);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            RecordData recordDataTobeAdd =new RecordData();
            recordDataTobeAdd.setType(Type);
            recordDataTobeAdd.setUser(user);
            recordDataTobeAdd.setPasswd(SecurityTransform.encrypt(passwd));
            recordDataTobeAdd.setMac(mac);
            recordData.add(recordDataTobeAdd);

            update();

            Toast.makeText(getContext(),"存储成功",Toast.LENGTH_LONG).show();
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.data_management,container,false);
        recordData =new ArrayList<>();
        data_view= view.findViewById(R.id.data_list);
        floatingActionButton=view.findViewById(R.id.data_floatButton);
        app_empty=view.findViewById(R.id.app_empty);
        app_empty.setVisibility(View.GONE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View diaView=LayoutInflater.from(getContext()).inflate(R.layout.dialog_manual_add,null,false);
                final RadioGroup radioGroup=diaView.findViewById(R.id.manual_type_selected);
                new AlertDialog.Builder(getContext())
                        .setView(diaView)
                        .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                            @Override
                                public void onClick(DialogInterface dialog, int which) {
                                int id=radioGroup.getCheckedRadioButtonId();
                                    String type;
                                    if (id==R.id.manual_type_wlan)
                                        type="WLAN";
                                    else if (id==R.id.manual_type_bluetooth)
                                        type="Bluetooth";
                                    else {
                                        Toast.makeText(getContext(),"请选择连接方式！",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    final EditText mac=diaView.findViewById(R.id.manual_mac_edit);
                                    Matcher matcher=pattern.matcher(mac.getText().toString());
                                    final EditText user=diaView.findViewById(R.id.manual_user_edit);
                                    final EditText passwd=diaView.findViewById(R.id.manual_passwd_edit);
                                    //先测试
                                    if (!matcher.matches())
                                    {
                                        Toast.makeText(getContext(),"MAC地址不合法",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                addRecord(type,user.getText().toString(),passwd.getText().toString()
                                        ,mac.getText().toString());
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();
            }
        });
        helper=new DataQueryHelper(getContext(),getString(R.string.sqlDBName),null,1);
        readSQL=helper.getReadableDatabase();
        writeSQL=helper.getWritableDatabase();

        //动画
        DefaultItemAnimator animator=new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setRemoveDuration(300);
        animator.setChangeDuration(300);
        data_view.setItemAnimator(animator);

        //LayoutManager
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        data_view.setLayoutManager(layoutManager);

        //适配器
        adapter=new RecordAdapter(recordData);
        adapter.setOnItemClickListener(new RecordAdapter.OnItemClickListener() {

            @Override
            public void OnClick(int type, final RecordData recordData) {
                if (type==EDIT_BUTTON)
                {
                    //编辑
                    View view1= LayoutInflater.from(getContext()).inflate(R.layout.dialog_manual_add,null,false);

                    String recordType= recordData.getType();
                    String mac= recordData.getMac();
                    String user= recordData.getUser();
                    String passwd= recordData.getPasswd();

                    final RadioGroup group=((RadioGroup)view1.findViewById(R.id.manual_type_selected));
                    final EditText macEdit=((EditText)view1.findViewById(R.id.manual_mac_edit));
                    final EditText userEdit= ((EditText)view1.findViewById(R.id.manual_user_edit));
                    final EditText passwdEdit= ((EditText)view1.findViewById(R.id.manual_passwd_edit));

                    //填入数据
                    //Type
                    if (recordType.equals("Bluetooth")){
                        ((RadioButton)view1.findViewById(R.id.manual_type_bluetooth)).setChecked(true);
                    }
                    else ((RadioButton)view1.findViewById(R.id.manual_type_wlan)).setChecked(true);
                    macEdit.setText(mac);
                    userEdit.setText(user);
                    passwdEdit.setText(passwd);

                    new AlertDialog.Builder(getContext())
                            .setView(view1)
                            .setPositiveButton("提交", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    RecordData newRecordData =new RecordData();

                                    if (group.getCheckedRadioButtonId()==R.id.manual_type_bluetooth){
                                        newRecordData.setType("Bluetooth");
                                    }
                                    else newRecordData.setType("WLAN");
                                    newRecordData.setMac(macEdit.getText().toString());
                                    newRecordData.setUser(userEdit.getText().toString());
                                    newRecordData.setPasswd(passwdEdit.getText().toString());

                                    updateRecord(recordData, newRecordData);
                                    update();
                                }
                            })
                            .setNegativeButton("取消",null)
                            .show();

                }
                else if (type==DELETE_BUTTON)
                {
                    new AlertDialog.Builder(getContext())
                            .setMessage("是否要删除所选记录?")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //删除
                                    deleteRecord(recordData);
                                    update();
                                }
                            })
                            .setNegativeButton("取消",null)
                            .show();

                }
            }
        });

        data_view.setAdapter(adapter);

        return view;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode==WRITE_PERMISSION)
        {
            String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            EasyPermissions.requestPermissions(this,"读取需要获取读写权限",WRITE_PERMISSION,permissions);
        }
    }
}
