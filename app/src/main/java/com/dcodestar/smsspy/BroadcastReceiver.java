package com.dcodestar.smsspy;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent1) {
        Intent intent=new Intent(context,BroadcastReceiverService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
