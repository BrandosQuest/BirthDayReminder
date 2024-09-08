package com.example.birthdayreminder;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a worker that checks for birthdays and creates notifications.
 */
public class BirthdayCheckWorker extends Worker {

    /**
     * Creates an instance of the worker.
     * @param context The application context.
     * @param workerParams The worker parameters.
     */
    public BirthdayCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Does the work of the worker.
     * is overrides the doWork() method from the Worker class.
     * It calls createNotificationChannel() showNotification() checkBirthdays() and returns a success result.
     * @return The result of the work.
     */
    @NonNull
    @Override
    public Result doWork() {
        Log.d("YourWorkerClass", "doWork() started");
        createNotificationChannel();
        showNotification();
        checkBirthdays();
        return Result.success();
    }

    //This is the start of the checkBirthdays() method, which does the main work of checking for birthdays.

    /**
     * This method checks for birthdays and creates notifications.
     */
    private void checkBirthdays() {
        //This retrieves the last signed-in Google account.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            //This creates a GoogleAccountCredential and sets it to use the "https://www.googleapis.com/auth/contacts.readonly" scope.
            //This creates a GoogleAccountCredential object with the necessary OAuth2 scope for reading contacts, and sets the selected account.
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(),
                    Collections.singleton("https://www.googleapis.com/auth/contacts.readonly")
            );
            credential.setSelectedAccount(account.getAccount());

            //This creates a PeopleApiHelper object with the GoogleAccountCredential.
            PeopleApiHelper apiHelper = new PeopleApiHelper(credential);
            //This tries to get the contacts using the PeopleApiHelper.
            // It then loops through each contact. The commented-out code suggests where you
            // would check for birthdays and create notifications.
            // The catch block handles any IOException that might occur.
            try {
                List<Person> contacts = apiHelper.getContacts();
                //BirthdayNotificationMaker.checkForNotifications(contacts);
                for (Person person : contacts) {
                    if (person.getBirthdays() != null && !person.getBirthdays().isEmpty()) {
                        // If so, create a notification
                        // Check if today is the person's birthday
                        List<Birthday> birthdays = person.getBirthdays();
                        for (Birthday birthday : birthdays) {
                            Date date = birthday.getDate();
                            if (date != null) {
                                int day = date.getDay();
                                int month = date.getMonth();// Get today's date
                                Calendar today = Calendar.getInstance();
                                int todayDay = today.get(Calendar.DAY_OF_MONTH);
                                int todayMonth = today.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed

                                if (day == todayDay && month == todayMonth) {
                                    // Today is the contact's birthday!
                                    // ... do something special ...
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("checkBirthdays", "IOException in apiHelper.getContacts()", e);
            }
        }
    }

    /**
     * This method creates a notification channel for the notification.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Channel for hello notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * This method shows a notification.
     */
    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Hello Notification")
                .setContentText("Hello from Worker!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        int notificationId = 1;
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }
}