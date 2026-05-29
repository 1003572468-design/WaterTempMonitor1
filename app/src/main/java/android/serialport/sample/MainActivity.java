package android.serialport.sample;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.OutputStream;

public class MainActivity extends SerialPortActivity {
    
    private TextView temperatureText;
    private TextView obdStatusText;
    private Handler handler = new Handler();
    private OutputStream outputStream;
    private boolean isConnected = false;
    
    // OBD 命令（十六进制字节数组）
    private static final byte[] OBD_GET_COOLANT_TEMP = new byte[]{(byte)0x01, (byte)0x05};
    private static final byte[] OBD_GET_RPM = new byte[]{(byte)0x01, (byte)0x0C};
    private static final byte[] OBD_GET_SPEED = new byte[]{(byte)0x01, (byte)0x0D};
    
    // AT 指令（字符串）
    private static final String AT_RESET = "ATZ\r\n";           // 复位 ELM327
    private static final String AT_ECHO_OFF = "ATE0\r\n";       // 关闭回显
    private static final String AT_PROTOCOL_AUTO = "ATSP0\r\n"; // 自动检测协议
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        temperatureText = findViewById(R.id.temperatureText);
        obdStatusText = findViewById(R.id.obdStatusText);
        
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyMT1"), 19200, 0);
            outputStream = mSerialPort.getOutputStream();
            temperatureText.setText("等待数据...");
            obdStatusText.setText("初始化 OBD...");
            
            // 初始化 OBD 连接
            initOBDConnection();
        } catch (Exception e) {
            e.printStackTrace();
            temperatureText.setText("错误：" + e.getMessage());
            obdStatusText.setText("串口打开失败");
        }
    }
    
    private void initOBDConnection() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendObdAtCommand(AT_RESET);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendObdAtCommand(AT_ECHO_OFF);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendObdAtCommand(AT_PROTOCOL_AUTO);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        isConnected = true;
                                        obdStatusText.setText("OBD 已连接");
                                        startObdQuery(); // 开始循环查询
                                    }
                                }, 500);
                            }
                        }, 500);
                    }
                }, 500);
            }
        }, 1000);
    }
    
    private void sendObdAtCommand(String command) {
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendObdPidCommand(byte[] pid) {
        try {
            // OBD 命令格式：PID 请求需要加 0x01 前缀
            outputStream.write(pid);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startObdQuery() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    // 请求冷却液温度 (PID 0x05)
                    sendObdPidCommand(OBD_GET_COOLANT_TEMP);
                    // 每 2 秒查询一次
                    handler.postDelayed(this, 2000);
                }
            }
        });
    }
    
    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String hexData = bytesToHex(buffer, size);
                String textData = new String(buffer, 0, size);
                
                // 解析 OBD 响应
                String temperature = parseObdTemperature(hexData);
                if (temperature != null) {
                    temperatureText.setText(temperature + " ℃");
                } else {
                    // 显示原始数据用于调试
                    temperatureText.setText(textData.trim());
                }
                
                // 调试信息显示在状态栏
                obdStatusText.setText("收到: " + hexData);
            }
        });
    }
    
    // 解析 OBD 冷却液温度响应
    // 期望格式: 41 05 XX（XX 是温度值，单位摄氏度，减去40）
    private String parseObdTemperature(String hexData) {
        // 查找 "4105" 模式（响应帧头）
        int index = hexData.indexOf("41 05");
        if (index >= 0 && hexData.length() >= index + 8) {
            try {
                String tempHex = hexData.substring(index + 5, index + 7);
                int tempValue = Integer.parseInt(tempHex, 16);
                int temperature = tempValue - 40;  // OBD 标准转换公式
                return String.valueOf(temperature);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    // 字节数组转十六进制字符串
    private String bytesToHex(byte[] bytes, int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isConnected = false;
        handler.removeCallbacksAndMessages(null);
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
