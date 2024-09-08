package com.example.birthdayreminder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

public class BirthdayNotificationMaker {
    public static void checkForNotifications(Context context){


        class NetworkTask extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... params) {// Perform network operation here
//
//                //This retrieves the last signed-in Google account.
//                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
//
//                List<Person> contacts = Collections.emptyList();
//                if (account != null) {
//                    //This creates a GoogleAccountCredential and sets it to use the "https://www.googleapis.com/auth/contacts.readonly" scope.
//                    //This creates a GoogleAccountCredential object with the necessary OAuth2 scope for reading contacts, and sets the selected account.
//                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
//                            context,
//                            Collections.singleton("https://www.googleapis.com/auth/contacts.readonly")
//                    );
//                    credential.setSelectedAccount(account.getAccount());
//
//                    //This creates a PeopleApiHelper object with the GoogleAccountCredential.
//                    PeopleApiHelper apiHelper = new PeopleApiHelper(credential);
//                    //This tries to get the contacts using the PeopleApiHelper.
//                    // It then loops through each contact. The commented-out code suggests where you
//                    // would check for birthdays and create notifications.
//                    // The catch block handles any IOException that might occur.
//                    try {
//                        contacts = apiHelper.getContacts();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                //String result = contacts.toString(); // Replace with your actual network call and result
                String result = null; // Replace with your actual network call and result
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                // Update UI elements with the result here
            }
        }

        NetworkTask networkTask = new NetworkTask();
        networkTask.execute();

//        for (Person person : contacts) {
//            if (person.getBirthdays() != null && !person.getBirthdays().isEmpty()) {
//                // Check if today is the person's birthday
//                List<Birthday> birthdays =person.getBirthdays();
//                for (Birthday birthday : birthdays) {
//                    Date date = birthday.getDate();
//                    if (date != null) {
//                        int day = date.getDay();
//                        int month = date.getMonth();// Get today's date
//                        Calendar today = Calendar.getInstance();
//                        int todayDay = today.get(Calendar.DAY_OF_MONTH);
//                        int todayMonth = today.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed
//
//                        if (day == todayDay && month == todayMonth) {
//                            // Today is the contact's birthday!
//                            // ... do something special ...
////                            Toast.makeText(context, "Birthday today of " + person.getNames().get(0).getDisplayName(), Toast.LENGTH_LONG).show();
//                            Log.d("BirthdayNotificationMaker", "birthdayyd");
//                        }
//                    }
//                }
//                // If so, create a notification
//            }
//        }
    }

}
