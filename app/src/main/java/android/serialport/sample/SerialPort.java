package android.serialport.sample;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class SerialPort {
    private static final String TAG = "SerialPort";
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
        
        mFileInputStream = new FileInputStream(device);
        mFileOutputStream = new FileOutputStream(device);
    }
    
    public InputStream getInputStream() {
        return mFileInputStream;
    }
    
    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }
    
    public void close() {
        try {
            if (mFileInputStream != null) {
                mFileInputStream.close();
            }
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
