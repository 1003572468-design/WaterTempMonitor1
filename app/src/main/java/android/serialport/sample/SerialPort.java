package android.serialport.sample;

import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;   // 添加这一行

public class SerialPort {
    private static final String TAG = "SerialPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    
    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException, InvalidParameterException {
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n exit\n";
                su.getOutputStream().write(cmd.getBytes());
                su.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        
        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }
    
    private native static FileDescriptor open(String path, int baudrate, int flags);
    public native void close();
    
    public FileInputStream getInputStream() {
        return mFileInputStream;
    }
    
    public FileOutputStream getOutputStream() {
        return mFileOutputStream;
    }
    
    static {
        System.loadLibrary("serial_port");
    }
}
