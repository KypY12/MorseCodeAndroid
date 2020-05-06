package com.example.morsecode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int CAMERA_PERMISSION_CODE = 0;
    private final int SEND_SMS_PERMISSION_CODE = 1;
    private final int READ_SMS_PERMISSION_CODE = 2;

    private static Context initialContext;

    public static Context getInitialContext() {
        return initialContext;
    }

    private MorseConverter morseConverter;
    private TextView livePreview;
    private EditText phraseInput;
    private Button convertButton;
    private Button sendSmsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        morseConverter = new MorseConverter();
        livePreview = (TextView) findViewById(R.id.live_preview);
        phraseInput = (EditText) findViewById(R.id.phrase_input);
        convertButton = (Button) findViewById(R.id.convert_button);
        sendSmsButton = (Button) findViewById(R.id.send_sms_button);

        final String test = this.getResources().getString(R.string._A);
        livePreview.setText(test);


        final boolean hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSION_CODE);
        }

        convertButton.setOnClickListener(new View.OnClickListener() {
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

        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phrase = String.valueOf(phraseInput.getText());
                String encoded = morseConverter.encodePhrase(phrase);
                new SmsHandler().sendSms("0753328634", encoded);
            }
        });

        initialContext = getApplicationContext();
    }

    private void flash(String morsePhrase) {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        new Thread(new FlashLighter(morsePhrase, cameraManager, convertButton, 300, 600)).start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Camera permission denied!", Toast.LENGTH_SHORT);
                }
                break;
            case READ_SMS_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Read sms permission denied!", Toast.LENGTH_SHORT);
                }
                break;
            case SEND_SMS_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Send sms permission denied!", Toast.LENGTH_SHORT);
                }
                break;
            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
