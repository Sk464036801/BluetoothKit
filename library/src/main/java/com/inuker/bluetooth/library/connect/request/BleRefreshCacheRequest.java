package com.inuker.bluetooth.library.connect.request;

import com.inuker.bluetooth.library.connect.response.BluetoothResponse;

/**
 * Created by dingjikerbo on 2016/8/28.
 */
public class BleRefreshCacheRequest extends BleRequest {

    public BleRefreshCacheRequest(String mac) {
        super(mac, null);
    }

    @Override
    void processRequest() {
        refreshCache();
        onRequestFinished(REQUEST_SUCCESS, 500);
    }
}
