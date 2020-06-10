package com.example.locationbasedshoppingservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // View
    private Button btn_start;
    private Button btn_stop;
    private TextView scan_result;

    // 스캔 결과
    private String str_scan_result = "";
    private int scan_cnt = 0;

    // 블루투스 관련
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private BluetoothAdapter bluetoothAdapter;
    private List<ScanFilter> filterList;

    // 비콘 맥 주소 저장을 위한 변수
    private static Map<String, Integer> beaconMacAddress = new HashMap<String, Integer>();

    // 변수에 비콘 맥 주소 설정하는 함수
    private static void setBeaconMacAddress() {
        beaconMacAddress.put("A0:00:00:00:00:00", 1);
        beaconMacAddress.put("B1:11:11:11:11:11", 2);
        beaconMacAddress.put("C2:22:22:22:22:22", 3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        scan_result = findViewById(R.id.scan_result);

        btn_start.setOnClickListener(onClickListener);
        btn_stop.setOnClickListener(onClickListener);

        setBeaconMacAddress();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        setUpBLE();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        else {
            // API level 21 이상
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            filterList = new ArrayList<ScanFilter>();
        }
    }

    private Button.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start:
                    Toast.makeText(MainActivity.this, "Start Scanning", Toast.LENGTH_SHORT).show();
                    str_scan_result = "";
                    scan_cnt = 0;
                    scanLeDevice(true);
                    break;
                case R.id.btn_stop:
                    Toast.makeText(MainActivity.this, "Stop Scanning", Toast.LENGTH_SHORT).show();
                    scanLeDevice(false);
                    break;
            }
        }
    };

    private void setUpBLE() {
        // 디바이스에서 BLE 지원하는지 확인
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // BluetoothAdapter 초기화
        // API level 18 이상
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // 블루투스 지원 확인
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // BLE 신호 스캔 여부 설정 함수
    private boolean scanLeDevice(final boolean enable) {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (enable) {
                bluetoothLeScanner.startScan(filterList, scanSettings, scanCallback);
            }
            else {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }
        return enable;
    }

    // BLE 신호 수신 시 호출되는 함수
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final BluetoothDevice device = result.getDevice();
            // device 이름이 null이 아닐 때, 스캔 결과 출력
            if (device.getName() != null) {
                scan_cnt++;
                str_scan_result += ("  " + scan_cnt + "\n");
                str_scan_result += ("  Device Name: " + device.getName() + "\n");
                str_scan_result += ("  Device Address: " + device.getAddress() + "\n");
                str_scan_result += ("  Device RSSI: " + result.getRssi() + "\n\n");
                scan_result.setText(str_scan_result);
            }
        }
    };
}
