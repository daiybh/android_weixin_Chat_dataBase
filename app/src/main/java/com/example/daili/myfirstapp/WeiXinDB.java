package com.example.daili.myfirstapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DaiLi on 12/6/2017.
 */

public class WeiXinDB {
    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";
    private static final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";
    private List<File> mWxDbPathList = new ArrayList<>();
    private static final String WX_DB_FILE_NAME = "EnMicroMsg.db";


    private String mCurrApkPATH ="/data/data/";
    private static final String COPY_WX_DATA_DB = "wx_data.db";
    private Context context =null;
    private String mDbPassword = null;
    public void SetContext(Context _context,String dbPassword){
    context = _context;
    mDbPassword = dbPassword;
        mCurrApkPATH = context.getFilesDir()+"/";
        File wxDataDir = new File(WX_DB_DIR_PATH);
        mWxDbPathList.clear();
        searchFile(wxDataDir, WX_DB_FILE_NAME);
    }
    /**
     * 递归查询微信本地数据库文件
     *
     * @param file     目录
     * @param fileName 需要查找的文件名称
     */
    private void searchFile(File file, String fileName) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    searchFile(childFile, fileName);
                }
            }
        } else {
            if (fileName.equals(file.getName())) {
                mWxDbPathList.add(file);
                Log.i(MainActivity.LOG_TAG,"dbList=>"+file.getAbsolutePath());
            }
        }
    }
    void openWxDb(String sFilePath){
//处理多账号登陆情况
        for (int i = 0; i < mWxDbPathList.size(); i++) {
            File file = mWxDbPathList.get(i);
            String copyFilePath = mCurrApkPATH + COPY_WX_DATA_DB;
            //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
            copyFile(file.getAbsolutePath(), copyFilePath);
            File copyWxDataDb = new File(copyFilePath);
            openWxDb(copyWxDataDb);
        }
        Log.i(MainActivity.LOG_TAG,"OPenWxDB Over...");
    }
    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int byteRead = 0;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }
    /**
     * 连接数据库
     *
     * @param dbFile
     */
    private void openWxDb(File dbFile) {

        if(!dbFile.exists())
        {Log.i(MainActivity.LOG_TAG,"exists=>"+dbFile.getAbsolutePath());
        return;}



        SQLiteDatabase.loadLibs(context);
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };

        try {

            Log.i(MainActivity.LOG_TAG,"start 读取数据库信息=>"+dbFile.getAbsolutePath());
            //打开数据库连接
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);
            //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
            /*
            Cursor c1 = db.rawQuery("select * from rcontact where verifyFlag = 0 and type != 4 and type != 2 and nickname != '' limit 20, 9999", null);


            while (c1.moveToNext()) {
                String userName = c1.getString(c1.getColumnIndex("username"));
                String alias = c1.getString(c1.getColumnIndex("alias"));
                String nickName = c1.getString(c1.getColumnIndex("nickname"));
                Log.i(MainActivity.LOG_TAG,"DB=userName>"+userName+" alias=>"+alias+" nickName=>"+nickName);
            }/**/
//*
            Cursor c1 = db.query("message", null, null, null, null, null, null);
            while (c1.moveToNext()) {
                int _id = c1.getInt(c1.getColumnIndex("msgId"));
                String name = c1.getString(c1.getColumnIndex("content"));
                Log.i(MainActivity.LOG_TAG,"DB=>--_id=>" + _id + ", content=>" + name);
            }/**/
            c1.close();
            db.close();
            Log.i(MainActivity.LOG_TAG,"End11111 读取数据库信息=>"+dbFile.getAbsolutePath());
        } catch (Exception e) {
            Log.i(MainActivity.LOG_TAG,"读取数据库信息失败" + e.toString());
//            e.printStackTrace();
            return;
        }
        Log.i(MainActivity.LOG_TAG,"End 读取数据库信息=>"+dbFile.getAbsolutePath());
    }
}
