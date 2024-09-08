package com.example.birthdayreminder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.birthdayreminder.databinding.FragmentFirstBinding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * This is the first fragment of the application.
 */
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private TextView notificationLog;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    /**
     * onViewCreated
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );*/
        binding.LoadNotificationLogButton.setOnClickListener(v ->
                loadNotificationLog(view)
        );
        binding.ClearNotificationLogButton.setOnClickListener(v ->
                clearNotificationLog(view)
        );
        notificationLog = view.findViewById(R.id.notificationLogTextView);
    }

    /**
     * A method to clear the notification log file.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     */
    private void clearNotificationLog(View view) {
        File logFile = new File(requireContext().getExternalFilesDir(null), "notification_log.txt");
        if (logFile.exists()) {
            try {
                FileWriter writer = new FileWriter(logFile);
                writer.write("");
                writer.close();
                loadNotificationLog(view);
                Log.d("ClearNotificationLog", "File cleared successfully");
            } catch (IOException e) {
                Log.e("ClearNotificationLog", "Error clearing file: " + e.getMessage());
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * A method to load the notification log file.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     */
    public void loadNotificationLog(View view) {
        notificationLog.setText("");
        File logFile = new File(requireContext().getExternalFilesDir(null), "notification_log.txt");
        if (logFile.exists()) {
            try {
                List<String> lines = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    lines = Files.readAllLines(logFile.toPath());
                }
                StringBuilder fileContent = new StringBuilder();for (String line : lines) {
                    fileContent.append(line).append("\n");
                }
                // Do something with the fileContent, e.g., display it in a TextView
                for (String line : lines) {
                    notificationLog.append("- "+line + "\n");
                }
            } catch (IOException e) {
                // Handle exception, e.g., log the error or display a message
                Log.e("LoadNotificationLog", "Error reading file: " + e.getMessage());
            }
        } else {
            // Handle case where the file doesn't exist
            Log.e("LoadNotificationLog", "File not found");
        }
    }
}