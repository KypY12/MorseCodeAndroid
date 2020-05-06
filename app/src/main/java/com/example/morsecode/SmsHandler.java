package com.example.morsecode;

import android.telephony.SmsManager;

public class SmsHandler {


    public SmsHandler(){


    }


    public void sendSms(String phoneNumber, String message){

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);

    }

}
