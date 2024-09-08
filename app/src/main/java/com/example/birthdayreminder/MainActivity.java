package com.example.birthdayreminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * This is the main activity of the app.
 */
public class MainActivity extends AppCompatActivity {


    // Request code for Google Sign-In
    private static final int RC_SIGN_IN = 9001;
    // Google Sign-In client
    private GoogleSignInClient mGoogleSignInClient;
    // Notification permission code
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Google Sign-In client
        //This creates GoogleSignInOptions, configuring it to request email and contacts read-only scope, then creates a GoogleSignInClient.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/contacts.readonly"))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up the FAB to trigger Google Sign-In
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionAndScheduleWork();
            }
        });
        //Schedule the birthday check alarm to run periodically.
        setRepeatingAlarmEveryMinute(new View(this));
    }

    /**
     * This method checks for the notification permission and, if not granted, requests it.
     * Called by the onCreate method, with a setOnClickListener for a button, after the Google Sign-In client is initialized.
     */
    private void checkPermissionAndScheduleWork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionCheck", "Permission already granted");
                signIn();
            } else {
                Log.d("PermissionCheck", "Requesting permission");
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.POST_NOTIFICATIONS"}, NOTIFICATION_PERMISSION_CODE);
            }
        } else {
            Log.d("PermissionCheck", "Permission not required for this Android version");
            signIn();
        }
    }
    /**
     * This method is called when the user grants or denies the notification permission.
     * @param requestCode The request code passed in {@link #(android.app.Activity, String[], int)}
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            Log.d("PermissionResult", "Request Code: " + requestCode);
            Log.d("PermissionResult", "Permissions: " + Arrays.toString(permissions));
            Log.d("PermissionResult", "Grant Results: " + Arrays.toString(grantResults));

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionResult", "Permission Granted");
                signIn();
            } else {
                Log.d("PermissionResult", "Permission Denied");
                // Handle the permission denied case
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.POST_NOTIFICATIONS")) {
                    // Show an explanation to the user
                    Toast.makeText(this, "Notification permission is needed for certain features", Toast.LENGTH_LONG).show();
                } else {
                    // User has permanently denied the permission, guide them to app settings
                    Toast.makeText(this, "Please enable notification permission from app settings", Toast.LENGTH_LONG).show();
                }
                // Proceed with sign-in anyway
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
//                signIn();
            }
        }
    }

    /**
     * This method is called by the checkPermissionAndScheduleWork.
     * Or called by onRequestPermissionsResult if the permission wasn't granted.
     */
    private void signIn() {
        Log.d("SignIn", "Initiating sign-in process");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * This method is called when the Google Sign-In activity finishes.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    /**
     * This method handles the result of the Google Sign-In task.
     * And if successful, it schedules the birthday check worker.
     * @param completedTask The result of the Google Sign-In task.
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult();
            // Sign-in successful, you can now use the account to access the People API
            Log.d("handleSignInResult", "Sign-in successful");
            Toast.makeText(this, "Sign-in successful", Toast.LENGTH_SHORT).show();
            scheduleBirthdayChecks();
        } catch (Exception e) {
            Log.e("handleSignInResult", "Sign-in failed", e); // e is the exception object
            Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * This method schedules the birthday check worker to run periodically.
     */
    private void scheduleBirthdayChecks() {
        // Schedule the birthday check worker to run periodically
        Constraints constraints = null;
        try {
            constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
        } catch (Exception e) {
            Log.e("scheduleBirthdayChecks", "constraints NetworkType failed", e); // e is the exception object
            throw new RuntimeException(e);
        }

        //Schedule the birthday check worker to run periodically
        PeriodicWorkRequest birthdayCheckRequest =
                new PeriodicWorkRequest.Builder(BirthdayCheckWorker.class, 1, TimeUnit.SECONDS)
                        .setConstraints(constraints)
                        .build();

        //Schedule the birthday check worker to run periodically
        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(//consider changing this to getApplicationContext())
                "BirthdayCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                birthdayCheckRequest);
        Log.d("scheduleBirthdayChecks", "periodic worker started");
        Toast.makeText(this, "periodic worker started", Toast.LENGTH_SHORT).show();

        //Schedule the birthday check alarm to run periodically.
        //setRepeatingAlarmEveryMinute(this);
    }

    /**
     * This method calls the scheduler of the birthday check alarm to run periodically.
     * @param view The view that triggered the notification.
     */
    public void setRepeatingAlarmEveryMinute(View view) {
        Toast.makeText(this, "set Repeating Alarm Every Minute started", Toast.LENGTH_SHORT).show();
        PeriodicBirthdayCheckSetupper.setupPeriodicBirthdayCheck(this);
    }
    /**
     * This method creates a notification channel for the notification.
     * Only needed for the test notification button
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Channel for hello notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * This method cancels the setRepeatingAlarmEveryMinute and the birthday check worker.
     * @param view The view that triggered the notification.
     */
    public void cancelAlarms(View view) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("BirthdayCheck");
        Toast.makeText(this, "Alarms and worker cancelled", Toast.LENGTH_SHORT).show();
    }
    /**
     * This method makes a notification.
     * This method checks for the notification permission and, if not granted, requests it.
     * Called by the test notification button.
     * @param view The view that triggered the notification.
     */
    public void makeNotification(View view) {
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.POST_NOTIFICATIONS"}, NOTIFICATION_PERMISSION_CODE);
                return;
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.notification_icon) // Replace with your icon
                .setContentTitle("Hello Notification")
                .setContentText("Hello form MainActivity\nThis is a test notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = 1; // You can use any unique integer
        notificationManager.notify(notificationId, builder.build());
    }


}