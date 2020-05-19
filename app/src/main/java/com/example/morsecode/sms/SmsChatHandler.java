package com.example.morsecode.sms;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.morsecode.R;
import com.example.morsecode.morse.MorseConverter;

public class SmsChatHandler {

    private TextView chatView;
    private MorseConverter morseConverter;
    private Activity activity;

    public SmsChatHandler(Activity activity, TextView chatView, MorseConverter morseConverter) {
        this.chatView = chatView;
        this.morseConverter = morseConverter;
        this.activity = activity;
    }


    public void addSms(String from, String smsText) {
        String currentText = chatView.getText().toString();

        chatView.setText(currentText + "\n" + from + " : " + morseConverter.decodePhrase(smsText));

        final ScrollView scrollView1 = activity.findViewById(R.id.localScrollView1);
        scrollView1.post(new Runnable() {
            @Override
            public void run() {
                scrollView1.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

}
