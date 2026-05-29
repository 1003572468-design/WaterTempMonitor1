package android.serialport.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class SerialPortActivity extends Activity {
    
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    protected Handler mHandler;
    
    protected abstract void onDataReceived(byte[] buffer, int size);
    
    private class ReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int size;
            try {
                while (true) {
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        final byte[] data = new byte[size];
                        System.arraycopy(buffer, 0, data, 0, size);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                onDataReceived(data, data.length);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }
    
    public void setSerialPort(SerialPort serialPort) throws Exception {
        mSerialPort = serialPort;
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        mReadThread = new ReadThread();
        mReadThread.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSerialPort != null) mSerialPort.close();
        if (mReadThread != null) mReadThread.interrupt();
    }
}
