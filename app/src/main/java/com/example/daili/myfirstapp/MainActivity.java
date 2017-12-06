package com.example.daili.myfirstapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG="firstAPP";
public static final String EXTRA_MESSAGE="com.example.myfirstapp.MESSAGE";
public static final int REQUEST_READ_PHONE_STATE = 111;
private TextView tv_imei=null;
private TextView tv_password=null;
private String uin ="394743435";
public String mCurrWxUin=null;
public String mDbPassword = "";
public String mPhoneIMEI = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_imei = (TextView)findViewById(R.id.editText_IMEI);
        tv_password = (TextView)findViewById(R.id.textView_password);

    }
    public static final String WX_ROOT_PATH ="/data/data/com.tencent.mm/";
    public void execRootCmd(String paramString)
    {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream((OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            localObject = localProcess.exitValue();
        } catch (Exception localException) {
            localException.printStackTrace();
            Log.i(LOG_TAG,"Exception=>"+localException.getMessage());
            return;
        }
        Log.i(LOG_TAG,"execRootCmd=>"+paramString);
    }
    private static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";
    /**
     * 获取微信的uid
     * 微信的uid存储在SharedPreferences里面
     * 存储位置\data\data\com.tencent.mm\shared_prefs\auth_info_key_prefs.xml
     */
    private void initCurrWxUin() {
        mCurrWxUin = null;
        File file = new File(WX_SP_UIN_PATH);
        try {
            FileInputStream in = new FileInputStream(file);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                if ("_auth_uin".equals(element.attributeValue("name"))) {
                    mCurrWxUin = element.attributeValue("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(LOG_TAG,"获取微信uid失败，请检查auth_info_key_prefs文件权限");
            return;
        }
    Log.i(LOG_TAG,"initCurrWxUin=>"+mCurrWxUin);
    }
    /**
     * 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
     *
     * @param imei
     * @param uin
     * @return
     */
    private void initDbPassword(String imei, String uin) {
        if (imei.isEmpty() || (uin.isEmpty())) {
            Log.i(LOG_TAG,"初始化数据库密码失败：imei或uid为空");
            return;
        }
        String md5 = md5(imei + uin);
        String password = md5.substring(0, 7).toLowerCase();
        mDbPassword = password;
        tv_password.setText(mDbPassword);
    }
    /**
     * md5加密
     *
     * @param content
     * @return
     */
    private String md5(String content) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes("UTF-8"));
            byte[] encryption = md5.digest();//加密
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    sb.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    sb.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取手机的imei码
     *
     * @return
     */
    private void initPhoneIMEI() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        try {
            mPhoneIMEI = tm.getDeviceId();
        }
        catch (SecurityException  se)
        {
            Log.i(LOG_TAG,"initPhoneIMEI==SecuntyException>"+se.getMessage());
        }
        catch (Exception e)
        {
            Log.i(LOG_TAG,"initPhoneIMEI==eXCEPTION>"+e.getMessage());
        }
    }

    public void onClick_IMEI(View view){
        execRootCmd("chmod 777 -R " + WX_ROOT_PATH);
        initPhoneIMEI();
        initCurrWxUin();
        tv_imei.setText(mPhoneIMEI);
        initDbPassword(mPhoneIMEI,mCurrWxUin);
       // getDeviceID11();
        WeiXinDB wxDb = new WeiXinDB();
        wxDb.SetContext(this,mDbPassword);


        wxDb.openWxDb("");

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Log.i("ffff","permission was granted, yay! ");
                    getDeviceID(this);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i("ffff","permission denied, boo! Disable the");
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void getDeviceID11(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_PHONE_STATE)){

            }   else       {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        REQUEST_READ_PHONE_STATE);
            }
            return;
        }
       getDeviceID(this);

    }
    public String getDeviceID(Context context){
        if(context==null)return "getDeviceID2222==>null";
        try{
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if(telephonyManager==null)
            {
                Log.i("ffff","telephonyManager==null");
                return "telephonyManager==null";
            }
            String deviceId = telephonyManager.getDeviceId();
            Log.i("ffff","deviceID=>"+deviceId);

            tv_imei.setText(deviceId);

            return deviceId;
        }catch(SecurityException e) {
            Log.i("ffff","SecurityException =>"+e.getMessage());
            return "SecurityException =>"+e.getMessage();
        }catch(Exception ex)
        {
            Log.i("ffff","exception =>"+ex.getMessage());
            return "exception =>"+ex.getMessage();
        }
    }
    public void SQLCipher(View view){
        Intent intent = new Intent(this,HelloSQLCipherActivity.class);
        intent.putExtra(EXTRA_MESSAGE,mDbPassword);
        startActivity(intent);
    }
}
