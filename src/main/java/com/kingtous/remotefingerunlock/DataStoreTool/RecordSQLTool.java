package com.kingtous.remotefingerunlock.DataStoreTool;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RecordSQLTool {

    public static RecordData toRecordData(Cursor cursor){
        //返回cursor下的数据，需要配合cursor.movetoNext使用
        RecordData recordData =new RecordData();
        int cnt=cursor.getColumnCount();
        for (int colomn=0;colomn<cnt;++colomn)
        {
            String colomnName=cursor.getColumnName(colomn);
            switch (colomnName)
            {
                case "Type":
                    recordData.setType(cursor.getString(cursor.getColumnIndex(colomnName)));
                    break;
                case "Mac":
                    recordData.setMac(cursor.getString(cursor.getColumnIndex(colomnName)));
                    break;
                case "User":
                    recordData.setUser(cursor.getString(cursor.getColumnIndex(colomnName)));
                    break;
                case "Passwd":
                    recordData.setPasswd(cursor.getString(cursor.getColumnIndex(colomnName)));
                    break;
                case "isDefault":
                    recordData.setIsDefault(cursor.getInt(cursor.getColumnIndex(colomnName)));
                    break;
                default:
                    break;
            }
        }
        return recordData;
    }

    public static boolean updatetoSQL(SQLiteDatabase writableDatabase, RecordData data){
        if (writableDatabase!=null && data!=null) {

            ContentValues values = new ContentValues();
            values.put("Type", data.getType());
            values.put("Mac", data.getMac());
            values.put("User", data.getUser());
            values.put("Passwd", data.getPasswd());
            values.put("isDefault",data.getIsDefault());

            boolean result = false;
            //先在数据库中查找是否有Mac相同的值
            String[] cond = new String[]{data.getMac()};
            int cnt=writableDatabase.update("data",values,"Mac:?",cond);
            if (cnt>0){
                result=true;
            }
            return result;

        }
        else return false;
    }


    public static boolean deleteRecordFromSQL(SQLiteDatabase writableDatabase, RecordData data){
        if (writableDatabase!=null && data!=null) {

            boolean result = false;
            //先在数据库中查找是否有Mac相同的值
            String[] cond = new String[]{data.getMac()};
            int cnt=writableDatabase.delete("data","Mac=?",cond);
            if (cnt>0){
                result=true;
            }
            return result;

        }
        else return false;
    }

    public static boolean addtoSQL(SQLiteDatabase writableDatabase, RecordData data){
        if (writableDatabase!=null && data!=null) {

            ContentValues values = new ContentValues();
            values.put("Type", data.getType());
            values.put("Mac", data.getMac());
            values.put("User", data.getUser());
            values.put("Passwd", data.getPasswd());
            values.put("isDefault",data.getIsDefault());

            boolean result = false;
            //先在数据库中查找是否有Mac相同的值
            String[] cond = new String[]{data.getMac()};
            Cursor cursor = writableDatabase.query("data", null, "Mac=?", cond, null, null, null);
            if (cursor.moveToNext()) {
                //有值，放弃插入
                return result;
            }
            try {
                writableDatabase.insert("data", null, values);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = false;
            } finally {
                cursor.close();
                return result;
            }
        }
        else return false;
    }


    public static RecordData getDefaultRecordData(SQLiteDatabase readableDatabase){
        String[] cond=new String[]{String.valueOf(RecordData.TRUE)};
        Cursor cursor=readableDatabase.query("data",null,"isDefault=?",cond,null,null,null);
        if(cursor.moveToNext())
        {
            return toRecordData(cursor);
        }
        return null;
    }

    public static void updateDefaultRecord(SQLiteDatabase writableDatabase){
        //寻找
    }

}
