package com.example.morsecode.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.TextView;


import com.example.morsecode.morse.MorseConverter;


public class SmsHandler extends BroadcastReceiver {

    private static String smsPrefix = "\n";
    private static String smsSuffix = "\n";

    private SmsChatHandler smsChatHandler;

    public SmsHandler(TextView chatView, MorseConverter morseConverter) {
        this.smsChatHandler = new SmsChatHandler(chatView, morseConverter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {

            Object[] smsExtras = (Object[]) intent.getExtras().get("pdus");
            for (int index = 0; index < smsExtras.length; index++) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) smsExtras[index]);

                String smsText = smsMessage.getMessageBody().toString();

                if (smsText.startsWith(smsPrefix) && smsText.endsWith(smsSuffix)) {

                    String senderPhoneNumber = smsMessage.getOriginatingAddress();

                    // Verificam daca se afla in agenda (in contacts)
                    Uri contactsSearchUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(senderPhoneNumber));
                    Cursor cursor = context.getContentResolver().query(contactsSearchUri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null, null);
                    if (cursor.moveToFirst()) {
                        // Daca il avem printre contacte vom afisa numele
                        String personName = cursor.getString(0);
                        smsChatHandler.addSms(personName, smsText.substring(smsPrefix.length(), smsText.length() - smsSuffix.length()));
                    } else {
                        // Daca nu il avem printre contacte atunci vom afisa numarul de telefon in stanga mesajului
                        smsChatHandler.addSms(senderPhoneNumber, smsText.substring(smsPrefix.length(), smsText.length() - smsSuffix.length()));
                    }
                    cursor.close();
                }
            }

        }
    }


    public static void sendSms(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, smsPrefix + message + smsSuffix, null, null);
    }

}
