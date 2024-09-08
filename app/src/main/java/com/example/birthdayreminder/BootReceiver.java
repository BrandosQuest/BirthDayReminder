package com.example.birthdayreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

/**
 * This class starts the periodic birthday check in the event that the device is rebooted.
 */
public class BootReceiver extends BroadcastReceiver {
    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Re-schedule all alarms here
            PeriodicBirthdayCheckSetupper.setupPeriodicBirthdayCheck(context);
            Toast.makeText(context, "Periodic birthday check re-scheduled after reboot", Toast.LENGTH_LONG).show();
        }
    }
}
