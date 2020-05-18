package com.example.morsecode.sms;

import android.widget.TextView;

import com.example.morsecode.morse.MorseConverter;

public class SmsChatHandler {

    private TextView chatView;
    private MorseConverter morseConverter;

    public SmsChatHandler(TextView chatView, MorseConverter morseConverter) {
        this.chatView = chatView;
        this.morseConverter = morseConverter;
    }


    public void addSms(String from, String smsText) {
        String currentText = chatView.getText().toString();
        chatView.setText(currentText + "\n" + from + " : " + morseConverter.decodePhrase(smsText));

    }

}
