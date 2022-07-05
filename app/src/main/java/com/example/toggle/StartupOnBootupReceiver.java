package com.example.toggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupOnBootupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent myIntent = new Intent(context, MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
        }
    }
}
