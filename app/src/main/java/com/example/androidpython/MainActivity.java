package com.example.androidpython;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.srplab.www.starcore.StarCoreFactory;
import com.srplab.www.starcore.StarCoreFactoryPath;
import com.srplab.www.starcore.StarMsgCallBackInterface;
import com.srplab.www.starcore.StarObjectClass;
import com.srplab.www.starcore.StarServiceClass;
import com.srplab.www.starcore.StarSrvGroupClass;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public StarSrvGroupClass SrvGroup;
    public StarCoreFactory starcore;

    static {
        // System.loadLibrary("star_java");
//        System.loadLibrary("starcore");
//        System.loadLibrary("star_python35");
        //System.loadLibrary("python2.7m");
        //System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final File appFile = getFilesDir();  /*-- /data/data/packageName/files --*/
        final String appLib = getApplicationInfo().nativeLibraryDir;

//        final TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {
                loadPy(appFile, appLib);
            }
        });


    }

    //public native String stringFromJNI();

    void loadPy(File appFile, String appLib) {
        //Copy Python Environment File
        File pythonLibFile = new File(appFile, "python2.7.zip");
        if (!pythonLibFile.exists()) {
            copyFile(this, "python2.7.zip");
//            copyFile(this, "_struct.cpython-37m.so");
//            copyFile(this, "binascii.cpython-37m.so");
//            copyFile(this, "time.cpython-34m.so");
//            copyFile(this, "zlib.cpython-37m.so");
        }

        // Copy Python Code File
        copyFile(this, "call_java.py");
//        copyFile(this,"libstar_java.so");
//        copyFile(this,"libstar_python35.so");
//        copyFile(this,"libstarcore.so");
//        copyFile(this,"libpython3.5m.so");
        //copyFile(this, "time.so");
        copyFile(this, "test.py");
        /*try {
            // Load Python Interpreter
            //System.load(appLib + File.separator + "libpython3.5m.so");
            //System.loadLibrary( "python3.5m");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        Log.d(TAG, "Finish interpreter load !");
        /*----init starcore----*/
        StarCoreFactoryPath.StarCoreCoreLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreShareLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreOperationPath = appFile.getPath();
        try {
            starcore = StarCoreFactory.GetFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }
        StarServiceClass Service = starcore._InitSimple("test", "123", 0, 0);
        // Message Callback Handler
        starcore._RegMsgCallBack_P(new StarMsgCallBackInterface() {
            public Object Invoke(int ServiceGroupID, int uMes, Object wParam, Object lParam) {
                if (uMes == starcore._Getint("MSG_DISPMSG") || uMes == starcore._Getint("MSG_DISPLUAMSG")) {
                    final String Str = (String) wParam;
                    Log.d(TAG, "Message:" + Str);
                }
                return null;
            }
        });
        SrvGroup = (StarSrvGroupClass) Service._Get("_ServiceGroup");
        Service._CheckPassword(false);

        // run python code
        SrvGroup._InitRaw("python", Service);
        StarObjectClass python = Service._ImportRawContext("python", "", false, "");

        // Set Python Module Path
        python._Call("import", "sys");
        StarObjectClass pythonSys = python._GetObject("sys");
        StarObjectClass pythonPath = (StarObjectClass) pythonSys._Get("path");
        pythonPath._Call("insert", 0, appFile.getPath() + File.separator + "python2.7.zip");
        pythonPath._Call("insert", 0, appLib);
        pythonPath._Call("insert", 0, appFile.getPath());

        // Execute Python Code
        Log.d(TAG, "loadPy: Start to exec py~");
        Service._DoFile("python", appFile.getPath() + "/py_code.py", "");
        long time = python._Calllong("get_time");
        python._Call("print_time");
        Log.d(TAG, "from python time=" + time + " :" + longToDate(time));

        Service._DoFile("python", appFile.getPath() + "/test.py", "");
        //int result = python._Callint("add", 5, 2);
        //Log.d(TAG, "result=" + result);
        Object res = python._Call("string_test", "String from Java");
        Log.d(TAG, "string_test:" + res);

        Log.d(TAG, "loadPy: Now call JAVA class from python.");
        python._Set("JavaClass", Log.class);
        Service._DoFile("python", appFile.getPath() + "/call_java.py", "");


        starcore._SRPLock();
        String script = "import time\n" +
                "\n" +
                "def get_time():\n" +
                "    return time.time()\n" +
                "print(get_time())";
        preCompile(script);
        Service._RunScript("python", script + "\n", "", "");
        starcore._SRPUnLock();
    }

    private void copyFile(Context c, String Name) {
        File outfile = new File(c.getFilesDir(), Name);
        BufferedOutputStream outStream = null;
        BufferedInputStream inStream = null;

        try {
            outStream = new BufferedOutputStream(new FileOutputStream(outfile));
            inStream = new BufferedInputStream(c.getAssets().open(Name));

            byte[] buffer = new byte[1024 * 10];
            int readLen = 0;
            while ((readLen = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, readLen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String longToDate(long lo) {
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(date);
    }

    public void preCompile(String script) {
        starcore._SRPLock();
        Object[] result = SrvGroup._PreCompile("python", script + "\n");
        starcore._SRPUnLock();
        if ((Boolean) result[0])
            Log.d(TAG, "preCompile: Success");
        else {
            if (((String) result[1]).length() == 0)
                Log.d(TAG, "preCompile: needed Input");
            else
                Log.d(TAG, "Compile: " + result[1]);
        }
    }

}
