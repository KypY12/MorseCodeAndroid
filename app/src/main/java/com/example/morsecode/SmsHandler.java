package com.example.morsecode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;


public class SmsHandler extends BroadcastReceiver {

    private static String smsPrefix = "\n";
    private static String smsSuffix = "\n";

    private SmsChatHandler smsChatHandler;

    public SmsHandler(TextView chatView, MorseConverter morseConverter){
        this.smsChatHandler = new SmsChatHandler(chatView, morseConverter);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null){

            Object[] smsExtras = (Object[]) intent.getExtras().get("pdus");
            for (int index = 0; index < smsExtras.length; index++){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[])smsExtras[index]);

                String smsText = smsMessage.getMessageBody().toString();
                String senderPhoneNumber = smsMessage.getOriginatingAddress();

                if (smsText.startsWith(smsPrefix) && smsText.endsWith(smsSuffix)){
//                    Toast.makeText(context, "MESAJ PRIMIT", Toast.LENGTH_LONG);
                    smsChatHandler.addSms(senderPhoneNumber, smsText.substring(smsPrefix.length(), smsText.length() - smsSuffix.length()));
                }
            }

        }
    }


    public static void sendSms(String phoneNumber, String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, smsPrefix + message + smsSuffix, null, null);
    }

}
