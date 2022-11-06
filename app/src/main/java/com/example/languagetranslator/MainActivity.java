package com.example.languagetranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fSpiner, secondSpiner;
    private TextInputEditText sourceEdt;
    private ImageView imgmic;
    private MaterialButton btntranslate;
    private TextView translatedto;
    private ImageButton btnspeak, btncopy, btnshare;
    TextToSpeech tts;
    /*String[] collang = {"EN", "HE", "ZH", "HI", "ES", "AR", "RU","UR","JA"}; */
    String[] fromlanguage = {"English","Hebrew","Chinese","Hindi","Spanish","Arabic","Russian","Urdu","Japanese"};
    String[] tolanguage = {"English","Hebrew","Chinese","Hindi","Spanish","Arabic","Russian","Urdu","Japanese"};

    private static final int REQUEST_PREMISSION_CODE = 1;
    int lanCode,fromLanguageCode,toLanguagesCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fSpiner = findViewById(R.id.idFromSpinner);
        secondSpiner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idtoSource);
        imgmic = findViewById(R.id.idmic);
        btntranslate = findViewById(R.id.idtransate);
        translatedto = findViewById(R.id.idTVTranslatedTV);
        btncopy = findViewById(R.id.idcopy);
        btnshare = findViewById(R.id.idshare);
        btnspeak = findViewById(R.id.idspeak);
        fSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromlanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //creating adapter for
        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromlanguage);
                fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                fSpiner.setAdapter(fromAdapter);


                secondSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        toLanguagesCode = getLanguageCode(tolanguage[position]);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item, tolanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondSpiner.setAdapter(toAdapter);

        btntranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedto.setText("");
                if(sourceEdt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter Your Language", Toast.LENGTH_SHORT).show();

                } else if(fromLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please Enter Source Language", Toast.LENGTH_SHORT).show();
                } else if(toLanguagesCode==0){
                    Toast.makeText(MainActivity.this,"Please enter the translating Language", Toast.LENGTH_SHORT).show();

                } else{
                    translateText(fromLanguageCode,toLanguagesCode,sourceEdt.getText().toString());

                }
            }
        });

        //mic button for speak to convert

        imgmic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert");
                try{
                    startActivityForResult(i,REQUEST_PREMISSION_CODE);

                } catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PREMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }
    //initilazing firebase for language

    private void translateText(int fromLanguageCode, int toLanguagesCode, String source)
    {
        translatedto.setText("Downloading....");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguagesCode)
                .build();
        FirebaseTranslator translator;
        translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

     translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
         @Override
         public void onSuccess(Void unused) {
             translatedto.setText("Translating...");
             translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                 @Override
                 public void onSuccess(String s) {
                     translatedto.setText(s);
                 }
             }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     Toast.makeText(MainActivity.this,"Fail to Translate!!!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                 }
             });
         }
         //error when downloading the model
     }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception e) {
             Toast.makeText(MainActivity.this,"Fail to Download Language!!"+e.getMessage(), Toast.LENGTH_SHORT).show();
         }
     });

     // button onclick acticity
     btnspeak.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             String tospeak = translatedto.getText().toString();
             if(tospeak !="" & Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                 Toast.makeText(MainActivity.this,tospeak,Toast.LENGTH_SHORT).show();
                 tts.speak(tospeak, TextToSpeech.QUEUE_FLUSH,null,null);
             }
             else{
                 Toast.makeText(MainActivity.this,"Nothing To Speak",Toast.LENGTH_SHORT).show();
             }

         }
     });
     // setting text from textview to speech

     tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
         @Override
         public void onInit(int status) {
             if(status !=TextToSpeech.ERROR){
                 tts.setLanguage(Locale.UK);
             }
             else{
                 Toast.makeText(MainActivity.this,"Empty Content",Toast.LENGTH_SHORT).show();
             }
         }
     });

    }

    public int getLanguageCode(String language)
    {
        int languageCode = 0;
        switch(language){

            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Hebrew":
                languageCode = FirebaseTranslateLanguage.HE;
                break;
            case "Chinese":
                languageCode = FirebaseTranslateLanguage.ZH;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "URDU":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "JAPANESE":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
                default:
            languageCode = 0;

        }

        return languageCode;
    }



}