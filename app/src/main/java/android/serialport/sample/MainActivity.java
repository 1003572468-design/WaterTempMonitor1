package com.example.floatingweather;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private static final int REQUEST_CODE_OVERLAY = 1001;
    private static final int REQUEST_CODE_NOTIFICATION = 1002;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 检查并申请必要权限
        checkAndRequestPermissions();
    }
    
    private void checkAndRequestPermissions() {
        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                showPermissionDialog();
                return;
            }
        }
        
        // 检查通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                    REQUEST_CODE_NOTIFICATION);
                return;
            }
        }
        
        // 所有权限都已授予，启动服务
        startFloatingService();
        finish();
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("需要悬浮窗权限")
            .setMessage("悬浮窗权限用于在桌面上显示时间和天气信息。\n\n点击确定后，请在设置页面允许悬浮窗权限。")
            .setPositiveButton("去设置", (dialog, which) -> {
                requestOverlayPermission();
            })
            .setNegativeButton("退出", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // 权限已授予，继续检查其他权限
                    checkAndRequestPermissions();
                } else {
                    Toast.makeText(this, "需要悬浮窗权限才能正常工作", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
    
    private void startFloatingService() {
        Intent serviceIntent = new Intent(this, FloatingService.class);
        
        // Android 8.0+ 需要使用 startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show();
        finish();
    }
}
