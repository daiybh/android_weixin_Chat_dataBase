package com.example.daili.myfirstapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.io.IOException;

public class HelloSQLCipherActivity extends AppCompatActivity {
String password ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_sqlcipher);
      //  InitializeSQLCipher();
        EditText et = (EditText)findViewById(R.id.textView2);
        et.setText("/data/user/0/com.tencent.mm/MicroMsg/cc2e313400cbe3b7eefea293dc21f795/EnMicroMsg.db");
        Intent intent = getIntent();
        password = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        addText("Password:=>"+password);

/*
        addText( "getRootDirectory(): "
                + Environment.getRootDirectory().toString());
        addText("getDataDirectory(): "
                + Environment.getDataDirectory().toString());
        addText("context.getFilesDir(): "
                + this.getFilesDir().toString());
        addText("context.getCacheDir(): "
                + this.getCacheDir().toString());
/**/
        try{
            File databaseFile = getDatabasePath("//myfile.txt");
            addText("absolutepath=>"+databaseFile.getAbsolutePath());

            if(!databaseFile.exists())
            {
                try {
                    databaseFile.createNewFile();

                }catch (IOException e){
                    addText("IOException=>"+e.getMessage());
                    return;
                }
                catch (Exception e)
                {
                    addText("Exception------=>"+e.getMessage());
                    return;
                }
            }
        }catch (Exception e)
        {
            addText("----Exception=>"+e.getMessage());
        }

    }
    private  void addText(String s){
        Log.i("DDBBB",s);
        EditText tv = (EditText)findViewById(R.id.editText2);

        String oldText = tv.getText().toString()+"\r\n"+s;
        tv.setText(oldText);
        Toast.makeText(getApplicationContext(),oldText,Toast.LENGTH_SHORT).show();
    }
    private void InitializeSQLCipher() {
        File databaseFile = getDatabasePath("demo.db");
        addText("InitializeSQLCipher"+databaseFile.getAbsolutePath());
        SQLiteDatabase.loadLibs(this);
        databaseFile.mkdirs();
        databaseFile.delete();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, "test123", null);
        database.execSQL("create table t1(a, b)");
        database.execSQL("insert into t1(a, b) values(?, ?)", new Object[]{"one for the money",
                "two for the show"});
        addText("init over");
    }
    public  void readDataBase(){
        File databaseFile = getDatabasePath("demo.db");
        SQLiteDatabase.loadLibs(this);
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook(){
            public void preKey(SQLiteDatabase database){
            }
            public void postKey(SQLiteDatabase database){
                database.rawExecSQL("PRAGMA cipher_migrate;");  //最关键的一句！！！
            }
        };
        try {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, "test123", null, hook);
            Cursor c = db.query("t1", null, null, null, null, null, null);
            while (c.moveToNext()) {
                String a = c.getString(c.getColumnIndex("a"));
                String b = c.getString(c.getColumnIndex("b"));
               // Log.i("db", "_id=>" + _id + ", content=>" + name);
                addText("a=>"+a+" b=>"+b);
            }
            c.close();
            db.close();
        } catch (Exception e) {

            addText("ReadDataBase Exception=>"+e.getMessage());
        }

    }
    public void onClick(View view){
        readWeChatDatabase();
    }

    public void readWeChatDatabase() {

        SQLiteDatabase.loadLibs(this);
        EditText et = (EditText)findViewById(R.id.textView2);
        addText("DB=>"+et.getText().toString());
        File databaseFile = getDatabasePath(et.getText().toString());
        //File databaseFile = getDatabasePath("EnMicroMsg.db");
        //eventsData = new myDataHelper(this);

        SQLiteDatabaseHook hook = new SQLiteDatabaseHook(){
            public void preKey(SQLiteDatabase database){
            }
            public void postKey(SQLiteDatabase database){
                database.rawExecSQL("PRAGMA cipher_migrate;");  //最关键的一句！！！
            }
        };

        try {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, password, null, hook);
            Cursor c = db.query("message", null, null, null, null, null, null);
            while (c.moveToNext()) {
                int _id = c.getInt(c.getColumnIndex("msgId"));
                String name = c.getString(c.getColumnIndex("content"));
                addText("DB=>--_id=>" + _id + ", content=>" + name);
            }
            c.close();
            db.close();
        } catch (Exception e) {
            addText("DB exception=>"+e.getMessage());
        }
    }
}
