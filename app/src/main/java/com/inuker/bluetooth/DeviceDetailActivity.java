package com.inuker.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.BluetoothUtils;

import java.util.UUID;

/**
 * Created by liwentian on 2016/9/2.
 */
public class DeviceDetailActivity extends Activity implements BleConnectStatusListener {

    private TextView mTvTitle;
    private ProgressBar mPbar;

    private ListView mListView;
    private DeviceDetailAdapter mAdapter;

    private BluetoothDevice mDevice;

    private boolean mConnected;

    private boolean mPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail_activity);

        Intent intent = getIntent();
        String mac = intent.getStringExtra("mac");

        mDevice = BluetoothUtils.getRemoteDevice(mac);

        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setText(mDevice.getAddress());

        mPbar = (ProgressBar) findViewById(R.id.pbar);

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new DeviceDetailAdapter(this, mDevice);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mConnected) {
                    return;
                }
                DetailItem item = (DetailItem) mAdapter.getItem(position);
                if (item.type == DetailItem.TYPE_CHARACTER) {
                    BluetoothLog.v(String.format("click service = %s, character = %s", item.service, item.uuid));
                    startCharacterActivity(item.service, item.uuid);
                }
            }
        });

        connectDeviceIfNeeded();
    }

    private void startCharacterActivity(UUID service, UUID character) {
        Intent intent = new Intent(this, CharacterActivity.class);
        intent.putExtra("mac", mDevice.getAddress());
        intent.putExtra("service", service);
        intent.putExtra("character", character);
        startActivity(intent);
    }

    private void connectDevice() {
        BluetoothLog.v("connectDevice");

        mTvTitle.setText(String.format("%s%s", getString(R.string.connecting), mDevice.getAddress()));
        mPbar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);

        ClientManager.getClient().connect(mDevice.getAddress(), new BleConnectResponse() {
            @Override
            public void onResponse(int code, Bundle data) {
                BluetoothLog.v(String.format("onResponse code = %d", code));

                mTvTitle.setText(String.format("%s", mDevice.getAddress()));
                mPbar.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);


                if (code == REQUEST_SUCCESS) {
                    BleGattProfile profile = data.getParcelable(EXTRA_GATT_PROFILE);
//                    BluetoothLog.v(String.format("Profiles: \n%s", profile));
                    mAdapter.setGattProfile(profile);
                }
            }
        }, this);
    }

    @Override
    public void onConnectStatusChanged(int status) {
        if (mPause) {
            return;
        }

        BluetoothLog.v(String.format("Activity onConnectStatusChanged %d in %s",
                status, Thread.currentThread().getName()));

        mConnected = (status == STATUS_CONNECTED);

        Intent intent = new Intent(Constants.ACTION_CONNECT_STATUS_CHANGE);
        intent.putExtra("status", status);
        sendBroadcast(intent);

        connectDeviceIfNeeded();
    }

    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    @Override
    protected void onDestroy() {
        ClientManager.getClient().disconnect(mDevice.getAddress());
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPause = true;
    }
}
