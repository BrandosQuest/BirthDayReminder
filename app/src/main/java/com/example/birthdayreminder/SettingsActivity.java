package com.example.birthdayreminder;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



    }

    //lettura delle impostazioni selezionate attraverso i textView della velocità e della mappa
    public void saveSettings() {
        String s = (String) speedDisplayed.getText();
        switch(s) {
            case "normal":
                Constants.frameRate=300;
                break;
            case "easy":
                Constants.frameRate=600;
                break;
            case "brando":
                Constants.frameRate=1000;
                break;
            case "hard":
                Constants.frameRate=200;
                break;
            case "impossible":
                Constants.frameRate=150;
                break;
            default:
        }


        s = (String) mapDisplayed.getText();

        switch (s){
            case "grass":
                Constants.mapType=MapType.GRASS;
                break;
            case "lava":
                Constants.mapType=MapType.LAVA;
                break;
            case "original":
                Constants.mapType=MapType.ORIGINAL;
                break;
            case "artic":
                Constants.mapType=MapType.ARTIC;
                break;
            default:
        }

    }


    //quando esco dalla schermata salvo le impostazioni sul file di testo nel caso non le riaggiorni successivamente
    public void writeSettings(){
        FileWriter fOUT= null;
        try {
            fOUT = new FileWriter(this.getFilesDir()+"fileSettings.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedWriter bOUT = new BufferedWriter(fOUT);

        try {
            bOUT.write(Integer.toString(Constants.frameRate)+" ");
            switch(Constants.mapType) {
                case GRASS:
                    bOUT.write("grass"+" ");
                    break;
                case LAVA:
                    bOUT.write("lava"+" ");
                    break;
                case ARTIC:
                    bOUT.write("artic"+" ");
                    break;
                case ORIGINAL:
                    bOUT.write("original"+" ");
                    break;
                default:
            }
            bOUT.write(Boolean.toString(Constants.sound)+" ");
            bOUT.write(Boolean.toString(Constants.vibration)+" ");
            bOUT.close();
            fOUT.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //se premo overview(tasto tendina app), home o back salvo i dati e chiudo l'activity
    @Override
    public void onPause() {
        super.onPause();
        this.saveSettings();
        this.writeSettings();
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
        finish();
    }

    //Esco dalla activity e eseguo quella della homepage
    public void finished(View v){
        this.saveSettings();
        this.writeSettings();
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
        finish();
    }

}

