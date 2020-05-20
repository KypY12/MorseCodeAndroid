package com.example.morsecode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.morsecode.flash.FlashLighter;
import com.example.morsecode.handlers.FiltersHandler;
import com.example.morsecode.handlers.UiHandler;
import com.example.morsecode.morse.MorseConverter;
import com.example.morsecode.sms.SmsHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_PERMISSION_CODE = 0;
    public static final int SEND_SMS_PERMISSION_CODE = 1;
    public static final int RECEIVE_SMS_PERMISSION_CODE = 2;
    public static final int READ_CONTACTS_PERMISSION_CODE = 3;
    public static final int CONTACT_SELECTED_CODE = 4;


    private MorseConverter morseConverter;
    private BroadcastReceiver smsReceiver;

    private UiHandler uiHandler;
    private FiltersHandler filtersHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.uiHandler = new UiHandler(this);
        this.filtersHandler = new FiltersHandler(this);

        // Forteaza utilizatorul sa utilizeze UPPERCASE doarece peste tot se foloseste UPPERCASE (iar codul morse e case insensitive)
        filtersHandler.addAllCapsFilter(uiHandler.getPhraseInput());
        // De asemenea, alfabetul morse are doar caractere alfanumerice (si eventual spatiu)
        filtersHandler.addSpaceLetterDigitsOnlyFilter(uiHandler.getPhraseInput());


        morseConverter = new MorseConverter(this);
        smsReceiver = new SmsHandler(this, uiHandler.getChatView(), morseConverter);

        // Gestionez permisiunile
        handlePermissions();


        // Ascunde butonul de stop flash
        uiHandler.hideStopButton();
        // Setez listeners pentru butoanele de selectie a modului aplicatiei
        uiHandler.setModeButtonsListeners();
        // Setez listeners pentru celelalte butoane
        setSecondButtonsListeners();

    }


    // =============================================================================================
    // HELPER FUNCTIONS
    // =============================================================================================


    private void showToast(String msg, int length) {
        Toast.makeText(getApplicationContext(), msg, length).show();
    }


    private boolean isPhoneNumberValid(String phoneNumber) {
        return (phoneNumber.length() == 10 && phoneNumber.matches("[0-9]+")) ||
                (phoneNumber.length() == 12 && phoneNumber.substring(1).matches("[0-9]+"));
    }

    // =============================================================================================


    private void setSecondButtonsListeners() {

        // Pentru butonul START FLASH
        uiHandler.getStartFlashButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

                } else {
                    String phrase = uiHandler.getPhraseInputText();

                    if (phrase.length() > 0) {
                        uiHandler.hideStartButton();
                        uiHandler.showStopButton();

                        String encoded = morseConverter.encodePhrase(phrase);

                        uiHandler.setLivePreviewText(encoded);

                        flash(encoded);
                    }
                }

            }
        });


        // Pentru butonul STOP FLASH
        uiHandler.getStopFlashButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uiHandler.hideStopButton();
                uiHandler.showStartButton();
            }
        });


        // Pentru butonul SEND SMS
        uiHandler.getSendSmsButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_CODE);
                } else {
                    String phoneNumber = uiHandler.getSendPhoneNumber();

                    if (!isPhoneNumberValid(phoneNumber)) {
                        showToast("Please add a valid phone number.", Toast.LENGTH_SHORT);
                    } else {
                        String phrase = uiHandler.getPhraseInputText();

                        if (phrase.length() > 0) {

                            String encoded = morseConverter.encodePhrase(phrase);
                            SmsHandler.sendSms(phoneNumber, encoded);

                            uiHandler.setChatViewText(uiHandler.getChatViewText() + "\n" + "Me : " + phrase);

                            final ScrollView scrollView1 = findViewById(R.id.localScrollView1);
                            scrollView1.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollView1.fullScroll(View.FOCUS_DOWN);
                                }
                            });

                            // Clear phraseInput
                            uiHandler.setPhraseInputText("");
                        }
                    }

                }

            }
        });


        // Pentru butonul AGENDA
        uiHandler.getAgendaButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
                } else {
                    Intent contactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(contactsIntent, CONTACT_SELECTED_CODE);
                }
            }
        });
    }


    private void flash(String morsePhrase) {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        new Thread(new FlashLighter(this, uiHandler, morsePhrase, cameraManager, 300, 600)).start();

    }

    // =============================================================================================
    //  PERMISSION FUNCTIONS
    // =============================================================================================

    private void handlePermissions() {
        final boolean hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (hasFlash) {
            String[] neededPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS};
            ActivityCompat.requestPermissions(MainActivity.this, neededPermissions, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Camera permission denied!", Toast.LENGTH_SHORT);
                } else {
                    showToast("Press START FLASH again", Toast.LENGTH_SHORT);
                }
                break;
            case SEND_SMS_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Send sms permission denied!", Toast.LENGTH_SHORT);
                } else {
                    showToast("Press SEND SMS again", Toast.LENGTH_SHORT);
                }
                break;
            case RECEIVE_SMS_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Receive sms permission denied!", Toast.LENGTH_SHORT);
                } else {
                    IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                    registerReceiver(smsReceiver, intentFilter);
                }
                break;
            case READ_CONTACTS_PERMISSION_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Read contacts permission denied!", Toast.LENGTH_SHORT);
                } else {
                    showToast("Press AGENDA again", Toast.LENGTH_SHORT);
                }
                break;
            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // =============================================================================================


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_SELECTED_CODE) {

            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);

                if (cursor.moveToFirst()) {
                    // Daca persoana din agenda are macar un numar de telefon adaugat
                    if (!cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("0")) {
                        String currentSendID = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        // Cautam numarul de telefon de la contactul cu id-ul pe care l-a a ales utilizatorul
                        Cursor secondCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{currentSendID},
                                null);

                        if (secondCursor.moveToFirst()) {
                            String currentSendNumber = secondCursor.getString(secondCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            uiHandler.setPhoneNumberSendText(currentSendNumber.replace(" ", "").trim());
                        }

                    } else {
                        showToast("The contact you selected doesn't have a phone number in Contacts.", Toast.LENGTH_SHORT);
                    }
                }
                cursor.close();
            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (smsReceiver != null) {
                IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                registerReceiver(smsReceiver, intentFilter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            if (smsReceiver != null) {
                unregisterReceiver(smsReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }


}
