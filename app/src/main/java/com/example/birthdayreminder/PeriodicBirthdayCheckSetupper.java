package com.example.birthdayreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

/**
 * This class is responsible for setting up the periodic birthday check.
 */
public class PeriodicBirthdayCheckSetupper {
    /**
     * Sets up the periodic birthday check.
     * @param context The context of the application.
     */
    public static void setupPeriodicBirthdayCheck(Context context) {
        // Check if we can schedule exact alarms
        AlarmManager alarmManager = null;//split line
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required because this is happening in a BroadcastReceiver
                context.startActivity(intent); // Ask user for permission to schedule exact alarms
                return;
            }
        }
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);

        // Set the alarm to start now and repeat every minute
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),  // Start now
                    60 * 1000,                   // Repeat every minute
                    pendingIntent);
        }
    }
}
