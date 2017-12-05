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

public class MainActivity extends AppCompatActivity {
public static final String EXTRA_MESSAGE="com.example.myfirstapp.MESSAGE";
public static final int REQUEST_READ_PHONE_STATE = 111;
private TextView tv_imei=null;
private TextView tv_password=null;
private String uin ="394743435";
public String sPassword = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_imei = (TextView)findViewById(R.id.editText_IMEI);
        tv_password = (TextView)findViewById(R.id.textView_password);

    }
    public void onClick_IMEI(View view){
        getDeviceID11();
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
            String ss = tv_imei.getText()+uin;
            Log.i("ffff","tv_imei+uin==>"+ss);
            String md5Str = WeixinMD5.n(ss.getBytes());
            Log.i("ffff",md5Str);
            sPassword = md5Str.substring(0,7);
            Log.i("ffff",sPassword);
            tv_password.setText(sPassword);
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
        intent.putExtra(EXTRA_MESSAGE,sPassword);
        startActivity(intent);
    }
}
