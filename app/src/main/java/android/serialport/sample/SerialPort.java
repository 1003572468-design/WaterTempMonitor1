package android.serialport.sample;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    private static final String TAG = "SerialPort";
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    
    public SerialPort(File device, int baudrate, int flags) throws Exception {
        grantRootPermission(device);
        mFileInputStream = new FileInputStream(device);
        mFileOutputStream = new FileOutputStream(device);
        Log.d(TAG, "串口打开成功: " + device.getAbsolutePath());
    }
    
    private void grantRootPermission(File device) {
        String[] suPaths = {"/system/bin/su", "/system/xbin/su", "/su/bin/su", "/sbin/su"};
        
        for (String suPath : suPaths) {
            File suFile = new File(suPath);
            if (suFile.exists()) {
                try {
                    Log.d(TAG, "使用 su: " + suPath);
                    Process process = Runtime.getRuntime().exec(suPath);
                    String cmd = "chmod 666 " + device.getAbsolutePath() + "\n exit\n";
                    process.getOutputStream().write(cmd.getBytes());
                    process.waitFor();
                    Log.d(TAG, "权限修改成功");
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "su 失败: " + suPath, e);
                }
            }
        }
    }
    
    public InputStream getInputStream() {
        return mFileInputStream;
    }
    
    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }
    
    public void close() {
        try {
            if (mFileInputStream != null) mFileInputStream.close();
            if (mFileOutputStream != null) mFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
