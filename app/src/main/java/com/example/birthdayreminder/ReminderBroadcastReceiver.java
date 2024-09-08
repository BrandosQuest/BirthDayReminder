package com.example.birthdayreminder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Person;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * This class is the broadcast receiver, it's called when the alarm goes off.
 * it is set up by the PeriodicBirthdayCheckSetupper
 */
public class ReminderBroadcastReceiver extends BroadcastReceiver {

private final String BIRTHDAY_CHANNEL_ID ="birthday_channel";

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderBroadcastReceiver", "onReceive triggered");
        startNotificationCheck(context);
    }
    /**
     * This method starts the notification check.
     * @param context The Context in which the receiver is running.
     */
    private void startNotificationCheck(Context context) {
        // Create a notification channel (required for Android 8.0+)
        createNotificationChannel(context);

        //This retrieves the last signed-in Google account.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            //This creates a GoogleAccountCredential and sets it to use the "https://www.googleapis.com/auth/contacts.readonly" scope.
            //This creates a GoogleAccountCredential object with the necessary OAuth2 scope for reading contacts, and sets the selected account.
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton("https://www.googleapis.com/auth/contacts.readonly")
            );
            credential.setSelectedAccount(account.getAccount());

            //This creates a PeopleApiHelper object with the GoogleAccountCredential.
            PeopleApiHelper apiHelper = new PeopleApiHelper(credential);
            //This tries to get the contacts using the PeopleApiHelper, in a thread because of network tasks.
            // It then loops through each contact. The commented-out code suggests where you
            // would check for birthdays and create notifications.
            // The catch block handles any IOException that might occur.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Perform long-running operation here
                    // For example, network request or database access
                    List<Person> contacts = null;
                    try {
                        contacts = apiHelper.getContacts();

                        //debug code
                        Log.d("ReminderBroadcastReceiver", "Number of contacts retrieved: " + contacts.size());
                        String names = "ciao";
                        for (Person person : contacts) {
                            names=names+person.getNames().get(0).getDisplayName()+","+person.getBirthdays().get(0).getDate()+"    ";
                        }
                        Log.d("ReminderBroadcastReceiver", "Contacts retrieved: " + contacts);
                        Log.d("ReminderBroadcastReceiver", "names Contacts retrieved: " + names);

                        int count = 0;
                        for (Person person : contacts) {
                            List<Birthday> birthdays = person.getBirthdays();
                            for (Birthday birthday : birthdays) {

                                Date date = birthday.getDate();
                                if (date != null) {
                                    int day = date.getDay();
                                    int month = date.getMonth();// Get today's date
                                    int year = date.getYear();
                                    Calendar today = Calendar.getInstance();
                                    int todayDay = today.get(Calendar.DAY_OF_MONTH);
                                    int todayMonth = today.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed
                                    int todayYear = today.get(Calendar.YEAR);
                                    int daysTillBirthday = calculateDaysTillBirthday(date);
                                    //int daysTillBirthday = (year - todayYear) * 365 + (month - todayMonth) * 30 + (day - todayDay);

                                    /*Calendar birthdayy = Calendar.getInstance();
                                    birthdayy.set(Calendar.MONTH, date.getMonth());
                                    birthdayy.set(Calendar.DAY_OF_MONTH, date.getDay());
                                    birthdayy.set(Calendar.YEAR, today.get(Calendar.YEAR));
                                    // If birthday has already passed this year, set it to next year
                                    long diffInMillis = birthdayy.getTimeInMillis() - today.getTimeInMillis();
                                    long diffInDays= TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
                                    Log.d("ReminderBroadcastReceiver", person.getNames().get(0).getDisplayName()+ " Days till birthday: " + diffInDays);
                                    if(diffInDays<0){
                                        diffInDays+=365;
                                    }
                                    Log.d("ReminderBroadcastReceiver", person.getNames().get(0).getDisplayName()+ " Days till birthday, after eventual correction: " + diffInDays);

                                    if (diffInDays<=7) {
                                        // Today is the contact's birthday!
                                        // ... do something special ...
                                        buildNotification(context, person.getNames().get(0).getDisplayName(), Integer.toString(todayYear-year));
                                        count++;
                                    }*/
                                    Log.d("ReminderBroadcastReceiver", person.getNames().get(0).getDisplayName()+ " Days till birthday, after eventual correction: " + daysTillBirthday);

                                    if (daysTillBirthday<=7) {
                                        // Today is the contact's birthday!
                                        // ... do something special ...
                                        buildNotification(context, person.getNames().get(0).getDisplayName(), Integer.toString(todayYear-year), daysTillBirthday);
                                        count++;
                                    }
                                    /*if (day == todayDay && month == todayMonth) {
                                        // Today is the contact's birthday!
                                        // ... do something special ...
                                        buildNotification(context, person.getNames().get(0).getDisplayName(), Integer.toString(todayYear-year));
                                        count++;
                                    }*/
                                }
                            }
                        }
                        if(count==0){
                            buildNotification(context, "No one", "X", null);
                        }
                    } catch (IOException e) {
                        buildNotification(context, null, null, null);
                    }
                }
            }).start();
        }
    }

    /**
     * This method calculates the number of days till the contact's birthday.
     * @param birthdate The contact's birthday.
     * @return The number of days till the contact's birthday.
     */
    private int calculateDaysTillBirthday(Date birthdate) {
        Date today = new Date();
        today.setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        today.setMonth(Calendar.getInstance().get(Calendar.MONTH)+1);
        today.setYear(Calendar.getInstance().get(Calendar.YEAR));

        int daysTillBirthday = 0;
        assert birthdate != null;
        assert today != null;
        if (birthdate.getMonth().equals(today.getMonth())) {
            if(birthdate.getDay()>=today.getDay()){
                daysTillBirthday=birthdate.getDay()-today.getDay();
            } else if (birthdate.getDay()<today.getDay()) {
                daysTillBirthday=daysOfMonth(today.getMonth(), today.getYear())-today.getDay();
                daysTillBirthday+=birthdate.getDay();
                for(int i=today.getMonth()+1; i<=12; i++){
                    daysTillBirthday+=daysOfMonth(i, today.getYear());
                }
                for(int i=1; i<birthdate.getMonth(); i++){
                    daysTillBirthday+=daysOfMonth(i, today.getYear()+1);
                }
            }
        }
        //Log.d("ReminderBroadcastReceiver", "birthdate.getMonth(): " + birthdate.getMonth()+ "\ntoday.getMonth(): "+today.getMonth());
        //Log.d("ReminderBroadcastReceiver", "birthdate: " + birthdate+ "\ntoday: "+today);
        if (birthdate.getMonth() > today.getMonth()) {
            daysTillBirthday=daysOfMonth(today.getMonth(), today.getYear())-today.getDay();
            daysTillBirthday+=birthdate.getDay();
            for(int i=today.getMonth()+1; i<birthdate.getMonth(); i++){
                daysTillBirthday+=daysOfMonth(i, today.getYear());
            }
        }
        if (birthdate.getMonth() < today.getMonth()) {
            daysTillBirthday=daysOfMonth(today.getMonth(), today.getYear())-today.getDay();
            daysTillBirthday+=birthdate.getDay();
            for(int i=today.getMonth()+1; i<=12; i++){
                daysTillBirthday+=daysOfMonth(i, today.getYear());
            }
            for(int i=1; i<birthdate.getMonth(); i++){
                daysTillBirthday+=daysOfMonth(i, today.getYear()+1);
            }
        }

        return daysTillBirthday;
    }

    /**
     * This method calculates the number of days in a month.
     * @param month The month.
     * @param year The year.
     * @return The number of days in the month.
     */
    private int daysOfMonth(int month, int year){
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 ==0) {
            // The year is a leap year
            switch (month) {
                case 4:return 30;
                case 6:return 30;
                case 9:return 30;
                case 11:return 30;
                case 2:return 29;
                default:return 31;
            }
        }else {
            switch (month) {
                case 4:return 30;
                case 6:return 30;
                case 9:return 30;
                case 11:return 30;
                case 2:return 28;
                default:return 31;
            }
        }

    }

    /**
     * This method builds the notification.
     * @param context The Context in which the receiver is running.
     * @param nameOfCelebrated The name of the person whose birthday it is.
     * @param ageOfCelebrated The age of the person whose birthday it is.
     */
    private void buildNotification(Context context, String nameOfCelebrated, String ageOfCelebrated, Integer daysTillBirthday) {
        // Check if we have permission to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, you can log, notify the user, or return early
                Log.e("ReminderBroadcastReceiver", "Notification permission not granted");
                return;
            }
        }

        // Create a notification
        String NOTIFICATION_TITLE = "Birthday Reminder";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BIRTHDAY_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(makeNotificationContent(nameOfCelebrated, ageOfCelebrated, daysTillBirthday))//giacomo will be 15 years old in 3 days  Their 15th birthday is in a week.
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = nameOfCelebrated.hashCode();
        notificationManager.notify(notificationId, builder.build());


        String notificationLogData = "Notification{ " +
                "NotificationId: " + notificationId +
                ", NotificationTime: " + Calendar.getInstance().getTime()+
                ", NotificationTitle: " + NOTIFICATION_TITLE +
                ", NotificationText: " +makeNotificationContent(nameOfCelebrated, ageOfCelebrated, daysTillBirthday) +
                ", NotificationChannelId: " + BIRTHDAY_CHANNEL_ID +
                ", NotificationIcon: " + R.drawable.notification_icon +
                " }\n";


        writeToFile(notificationLogData, context);
        Log.d("ReminderBroadcastReceiver", notificationLogData);
    }
    private String makeNotificationContent(String nameOfCelebrated, String ageOfCelebrated, Integer daysTillBirthday){
        if(nameOfCelebrated==null && ageOfCelebrated==null){
            return "Problems with retrieving contacts.\nPlease check your internet connection.";
        }
        assert nameOfCelebrated != null;
        if(nameOfCelebrated.equals("No one")&&ageOfCelebrated.equals("X")){
            return "It's no one's birthday today!";
        }
        if(daysTillBirthday==0){
            int age=Integer.parseInt(ageOfCelebrated);
            if (age>9 && age<14){
                return "It's " + nameOfCelebrated + "'s birthday today! Their " + ageOfCelebrated + "th birthday is today!";
            }
            int ageLastDigit=age%10;
            switch (ageLastDigit){
                case 1:
                    return "It's " + nameOfCelebrated + "'s birthday today! It's their " + ageOfCelebrated + "st birthday.";
                case 2:
                    return "It's " + nameOfCelebrated + "'s birthday today! It's their " + ageOfCelebrated + "nd birthday.";
                case 3:
                    return "It's " + nameOfCelebrated + "'s birthday today! It's their " + ageOfCelebrated + "rd birthday.";
                default:
                    return "It's " + nameOfCelebrated + "'s birthday today! It's their " + ageOfCelebrated + "th birthday.";
            }
        }else {
            return nameOfCelebrated + " turns " + ageOfCelebrated + " in "+daysTillBirthday+" days!";
            /*int age=Integer.parseInt(ageOfCelebrated);
            if (age>9 && age<14){
                return nameOfCelebrated + "'s birthday is coming soon!\nTheir " + ageOfCelebrated + "th birthday is in "+daysTillBirthday+" days.";
            }
            int ageLastDigit=age%10;
            switch (ageLastDigit){
                case 1:
                    return nameOfCelebrated + "'s birthday is coming soon!\nTheir " + ageOfCelebrated + "st birthday is in "+daysTillBirthday+" days.";
                case 2:
                    return nameOfCelebrated + "'s birthday is coming soon!\nTheir " + ageOfCelebrated + "nd birthday is in "+daysTillBirthday+" days.";
                case 3:
                    return nameOfCelebrated + "'s birthday is coming soon!\nTheir " + ageOfCelebrated + "rd birthday is in "+daysTillBirthday+" days.";
                default:
                    return nameOfCelebrated + "'s birthday is coming soon!\nTheir " + ageOfCelebrated + "th birthday is in "+daysTillBirthday+" days.";
            }*/
        }

    }

    /**
     * This method writes the notification log to a file.
     * @param data The data to be written to the file.
     * @param context The Context in which the receiver is running.
     */
    private void writeToFile(String data, Context context) {
        try {
            File logFile = new File(context.getExternalFilesDir(null), "notification_log.txt");
            FileWriter writer = new FileWriter(logFile, true);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("ReminderBroadcastReceiver", "Error writing to file", e);
        }
    }
    /**
     * This method creates the notification channel.
     * @param context The Context in which the receiver is running.
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Birthday Reminders";
            String description = "Channel for birthday reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(BIRTHDAY_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
