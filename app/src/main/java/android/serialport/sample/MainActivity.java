package android.serialport.sample;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends SerialPortActivity {
    
    private TextView temperatureText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        temperatureText = findViewById(R.id.temperatureText);
        
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyMT1"), 19200, 0);
            temperatureText.setText("串口已打开，等待数据...");
        } catch (Exception e) {
            e.printStackTrace();
            temperatureText.setText("错误：无法打开串口\n" + e.getMessage());
            Toast.makeText(MainActivity.this, "串口打开失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data = new String(buffer, 0, size);
                temperatureText.setText("水温: " + data.trim() + " ℃");
            }
        });
    }
}
