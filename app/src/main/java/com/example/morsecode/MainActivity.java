package com.example.morsecode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int CAMERA_PERMISSION_CODE = 0;
    private final int SEND_SMS_PERMISSION_CODE = 1;
    private final int RECEIVE_SMS_PERMISSION_CODE = 2;

    private static Context initialContext;

    public static Context getInitialContext() {
        return initialContext;
    }

    private MorseConverter morseConverter;

    private TextView livePreview;
    private TextView chatView;
    private EditText phraseInput;

    private EditText phoneNumberSend;
    private Button startFlashButton;
    private Button stopFlashButton;
    private Button sendSmsButton;

    private BroadcastReceiver smsReceiver;


    private void enableFlashMode(){
        findViewById(R.id.livePreviewTableRow).setVisibility(View.VISIBLE);
        findViewById(R.id.start_flash_button).setVisibility(View.VISIBLE);


        findViewById(R.id.sendToPhoneNumberTableRow).setVisibility(View.GONE);
        findViewById(R.id.chatViewTableRow).setVisibility(View.GONE);
        findViewById(R.id.send_sms_button).setVisibility(View.GONE);
    }

    private void enableSmsMode(){
        findViewById(R.id.livePreviewTableRow).setVisibility(View.GONE);
        findViewById(R.id.start_flash_button).setVisibility(View.GONE);


        findViewById(R.id.sendToPhoneNumberTableRow).setVisibility(View.VISIBLE);
        findViewById(R.id.chatViewTableRow).setVisibility(View.VISIBLE);
        findViewById(R.id.send_sms_button).setVisibility(View.VISIBLE);
    }

    private void setModeButtonsListeners(){

        enableFlashMode();

        Button flashModeButton = (Button)findViewById(R.id.flashModeButton);
        flashModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableFlashMode();
            }
        });

        Button smsModeButton = (Button)findViewById(R.id.smsModeButton);
        smsModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableSmsMode();
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        morseConverter = new MorseConverter();

        livePreview = (TextView) findViewById(R.id.livePreview);
        chatView = (TextView) findViewById(R.id.chatView);
        phraseInput = (EditText) findViewById(R.id.phraseInput);

        smsReceiver = new SmsHandler(chatView, morseConverter);

        phoneNumberSend = (EditText) findViewById(R.id.sendToPhoneNumber);
        startFlashButton = (Button) findViewById(R.id.start_flash_button);
        stopFlashButton = (Button) findViewById(R.id.stop_flash_button);
        sendSmsButton = (Button) findViewById(R.id.send_sms_button);


        stopFlashButton.setVisibility(View.GONE);
        setModeButtonsListeners();


//        final boolean hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, RECEIVE_SMS_PERMISSION_CODE);
        } else {
            IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
            registerReceiver(smsReceiver, intentFilter);
        }

        startFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phrase = String.valueOf(phraseInput.getText());

                if (phrase.length() > 0) {
                    String encoded = morseConverter.encodePhrase(phrase);
                    String decoded = morseConverter.decodePhrase(encoded);
                    livePreview.setText(encoded + "\n\n\n" + decoded);
                    flash(encoded);
                }

            }
        });

        stopFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFlashButton.setVisibility(View.GONE);
                startFlashButton.setVisibility(View.VISIBLE);
            }
        });

        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phrase = String.valueOf(phraseInput.getText());
                String encoded = morseConverter.encodePhrase(phrase);
                SmsHandler.sendSms(phoneNumberSend.getText().toString(), encoded);

                String currentText = chatView.getText().toString();
                chatView.setText(currentText + "\n" + "Eu : " + phraseInput.getText().toString());
            }
        });

        initialContext = getApplicationContext();
    }

    private void flash(String morsePhrase) {
        findViewById(R.id.start_flash_button).setVisibility(View.GONE);
        findViewById(R.id.stop_flash_button).setVisibility(View.VISIBLE);

        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        new Thread(new FlashLighter(this, morsePhrase, cameraManager, startFlashButton, stopFlashButton, 300, 600)).start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case SEND_SMS_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Send sms permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case RECEIVE_SMS_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Receive sms permission denied!", Toast.LENGTH_SHORT).show();
                }
//                else {
//                    IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
//                    registerReceiver(smsReceiver, intentFilter);
//                }
                break;
            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (smsReceiver != null){
                IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
//                intentFilter.addAction(android.provider.Telephony.SMS_RECEIVED);
                registerReceiver(smsReceiver, intentFilter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            if (smsReceiver != null){
                unregisterReceiver(smsReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }
}
