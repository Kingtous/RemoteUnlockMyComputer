package com.kingtous.remotefingerunlock.DataStoreTool;

public class RecordData {

    private String type;
    private String user;
    private String passwd;
    private String mac;
    private int isDefault;

    public static int FALSE=0;
    public static int TRUE=1;


    public RecordData(){

    }

    public RecordData(String Type,String User,String Passwd,String Mac)
    {
        this.type=Type;
        this.user=User;
        this.passwd=Passwd;
        this.mac=Mac;
        this.isDefault=FALSE;
    }

    public RecordData(String Type,String User,String Passwd,String Mac,int isDefault)
    {
        this.type=Type;
        this.user=User;
        this.passwd=Passwd;
        this.mac=Mac;
        this.isDefault=isDefault;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswd() {
        return SecurityTransform.decrypt(passwd);
    }

    public void setPasswd(String passwd) {
        this.passwd = SecurityTransform.encrypt(passwd);
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }
}
