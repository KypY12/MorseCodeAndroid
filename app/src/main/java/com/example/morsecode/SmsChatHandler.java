package com.example.morsecode;

import android.widget.TextView;

public class SmsChatHandler {

    private TextView chatView;
    private MorseConverter morseConverter;

    public SmsChatHandler(TextView chatView, MorseConverter morseConverter) {
        this.chatView = chatView;
        this.morseConverter = morseConverter;
    }


    public void addSms(String phoneNumber, String smsText) {
        String currentText = chatView.getText().toString();
        chatView.setText(currentText + "\n" + phoneNumber + " : " + morseConverter.decodePhrase(smsText));

    }

}
