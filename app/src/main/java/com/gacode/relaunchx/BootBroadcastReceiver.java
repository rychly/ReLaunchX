package com.gacode.relaunchx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    public BootBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LockScreen.class));
    }
}
