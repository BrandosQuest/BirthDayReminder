package com.example.birthdayreminder;

import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for interacting with the Google People API.
 * Provides methods to retrieve contact information, specifically names and birthdays.
 */
public class PeopleApiHelper {
    private final PeopleService peopleService;

    /*** Constructor for PeopleApiHelper.
     * Initializes the PeopleService using the provided GoogleAccountCredential.
     *
     * @param credential The GoogleAccountCredential object for authentication.
     */
    public PeopleApiHelper(GoogleAccountCredential credential) {
        peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("BirthdayChecker")
                .build();
    }

    /**
     * Retrieves a list of contacts with their names and birthdays only if they have birthdays.
     *
     * @return A list of Person objects representing the contacts.
     * @throws IOException If there is an error communicating with the People API.
     */
    public List<Person> getContacts() throws IOException {
        List<Person> contactsWithBirthdays = new ArrayList<>();
        String pageToken = null;

        int numContactsRetrieved = 0;
        do {
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,birthdays")
                    .setPageSize(100)//set to 1000
                    .setPageToken(pageToken)
                    .execute();

            List<Person> connections = response.getConnections();

            //Log.d("getContacts", "Contacts retrieved: " + connections);
            if (connections != null) {
                numContactsRetrieved += connections.size();
                for (Person person : connections) {
                    if (person.getBirthdays() != null && !person.getBirthdays().isEmpty()) {
                        contactsWithBirthdays.add(person);
                    }
                }
            }

            pageToken = response.getNextPageToken();
        } while (pageToken != null);
        Log.d("getContacts", "Number of contacts retrieved: " + numContactsRetrieved);
        return contactsWithBirthdays;
    }
}