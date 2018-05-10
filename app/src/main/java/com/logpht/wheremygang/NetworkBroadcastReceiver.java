package com.logpht.wheremygang;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn =  (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            // no connection
            AlertDialog.Builder lostNetworkDialog = new AlertDialog.Builder(context);
            lostNetworkDialog.setTitle(R.string.network_lost_text)
                    .setMessage(R.string.network_lost_message)
                    .setPositiveButton(R.string.ok_text, null)
                    .setCancelable(false)
                    .show();
        }
    }
}
