package com.chm.bledemo.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.chm.bledemo.R;
import com.chm.bledemo.adspter.ListViewAdspter;
import com.chm.bledemo.bleutils.BleController;
import com.chm.bledemo.bleutils.callback.ConnectCallback;
import com.chm.bledemo.bleutils.callback.ScanCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mlistview)
    ListView mListviev;
    @BindView(R.id.btn_new)
    ImageButton btnNew;

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private ProgressDialog progressDialog;
    private BleController mBleController;//蓝牙工具类
    private String mDeviceAddress;//当前连接的mac地址
    private ListViewAdspter mListAdspter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initview();
    }

    private void initview() {
        setToolbar();

        mBleController = BleController.getInstance().initble(this);

        initListviev();

        scanDevices(true);
    }

    /**
     * 设置标题栏
     */
    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//设计隐藏标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置显示返回键
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish();
                Toast.makeText(MainActivity.this, "别乱点我啊!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化listview
     */
    private void initListviev() {
        mListAdspter = new ListViewAdspter(MainActivity.this);
        mListviev.setAdapter(mListAdspter);
        mListviev.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showProgressDialog("请稍候!", "正在连接...");
                final BluetoothDevice device = mListAdspter.getDevice(position);
                if (device == null)
                    return;
                mDeviceAddress = device.getAddress();
                mBleController.Connect(mDeviceAddress, new ConnectCallback() {
                    @Override
                    public void onConnSuccess() {
                        hideProgressDialog();
                        startActivity(MainActivity.this, TestActivity.class);
                    }

                    @Override
                    public void onConnFailed() {
                        hideProgressDialog();
                        Toast.makeText(MainActivity.this, "连接超时，请重试", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }


    @OnClick({R.id.btn_new})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_new:
                mListAdspter.clear();
                scanDevices(true);
                break;
        }
    }

    /**
     * 扫描
     *
     * @param enable
     */
    private void scanDevices(final boolean enable) {
        mBleController.ScanBle(enable, new ScanCallback() {
            @Override
            public void onSuccess() {
                if (mListAdspter.mBleDevices.size() < 0) {
                    Toast.makeText(MainActivity.this, "未搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                mListAdspter.addDevice(device, getDistance(rssi));
            }
        });
    }


    private static final double A_Value = 60; // A - 发射端和接收端相隔1米时的信号强度
    private static final double n_Value = 2.0; //  n - 环境衰减因子

    public static double getDistance(int rssi) { //根据Rssi获得返回的距离,返回数据单位为m
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }

    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, title, message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }

    /**
     * 开启位置权限
     */
    private void checkGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanDevices(true);
                Toast.makeText(this, "位置权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未开启位置权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
