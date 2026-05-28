package android.serialport.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import java.io.IOException;
import java.security.InvalidParameterException;

public abstract class SerialPortActivity extends Activity {
    protected SerialPort mSerialPort;
    
    protected abstract void onDataReceived(byte[] buffer, int size);
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSerialPort != null) {
            mSerialPort.close();
        }
    }
}
