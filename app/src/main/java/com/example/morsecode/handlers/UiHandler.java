package com.example.morsecode.handlers;

import android.app.Activity;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.morsecode.R;

public class UiHandler {

    private Activity activity;

    private TextView livePreview;
    private TextView livePreviewLetter;
    private TextView livePreviewSymbols;
    private TextView chatView;
    private EditText phraseInput;

    private EditText phoneNumberSend;
    private Button startFlashButton;
    private Button stopFlashButton;
    private Button sendSmsButton;
    private Button agendaButton;

    // 0 = flash mode ; 1 = sms mode
    private int currentMode = 0;


    public UiHandler(Activity activity) {
        initViews(activity);
    }


    public void initViews(Activity activity) {
        this.activity = activity;

        livePreview = (TextView) activity.findViewById(R.id.livePreview);
        livePreviewLetter = (TextView) activity.findViewById(R.id.livePreviewLetter);
        livePreviewSymbols = (TextView) activity.findViewById(R.id.livePreviewSymbols);

        chatView = (TextView) activity.findViewById(R.id.chatView);
        phraseInput = (EditText) activity.findViewById(R.id.phraseInput);

        phoneNumberSend = (EditText) activity.findViewById(R.id.sendToPhoneNumber);
        startFlashButton = (Button) activity.findViewById(R.id.start_flash_button);
        stopFlashButton = (Button) activity.findViewById(R.id.stop_flash_button);
        sendSmsButton = (Button) activity.findViewById(R.id.send_sms_button);
        agendaButton = (Button) activity.findViewById(R.id.agendaButton);
    }

    // =============================================================================================
    // GETTERS
    // =============================================================================================


    public int getCurrentMode() {
        return currentMode;
    }

    public Button getAgendaButton() {
        return agendaButton;
    }

    public TextView getChatView() {
        return chatView;
    }

    public EditText getPhraseInput() {
        return phraseInput;
    }

    public Button getStartFlashButton() {
        return startFlashButton;
    }

    public Button getStopFlashButton() {
        return stopFlashButton;
    }

    public Button getSendSmsButton() {
        return sendSmsButton;
    }

    // SECONDARY GETTERS ===================================

    public String getPhraseInputText() {
        return phraseInput.getText().toString();
    }

    public String getSendPhoneNumber() {
        return phoneNumberSend.getText().toString();
    }

    public String getChatViewText() {
        return chatView.getText().toString();
    }

    public String getLivePreviewText() {
        return livePreview.getText().toString();
    }


    // =============================================================================================


    // =============================================================================================
    // SETTERS
    // =============================================================================================

    // SECONDARY SETTERS ===================================

    public void setPhraseInputText(String text) {
        phraseInput.setText(text);
    }

    public void setPhraseInputText(SpannableString text) {
        phraseInput.setText(text);
    }

    public void setLivePreviewText(String text) {
        livePreview.setText(text);
    }

    public void setLivePreviewText(SpannableString text) {
        livePreview.setText(text);
    }

    public void setChatViewText(String text) {
        chatView.setText(text);
    }

    public void setPhoneNumberSendText(String text) {
        phoneNumberSend.setText(text);
    }

    public void setLivePreviewLetterText(String text) {
        livePreviewLetter.setText(text);
    }

    public void setLivePreviewSymbolsText(String text) {
        livePreviewSymbols.setText(text);
    }

    public void setLivePreviewSymbolsText(SpannableString text) {
        livePreviewSymbols.setText(text);
    }

    public void setMode(int mode) {
        if (mode == 0) {
            enableFlashMode();
        } else if (mode == 1) {
            enableSmsMode();
        }

    }

    // =============================================================================================
    // MODE FUNCTIONS
    // =============================================================================================


    public void enableFlashMode() {
        activity.findViewById(R.id.livePreviewTableRow).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.start_flash_button).setVisibility(View.VISIBLE);


        activity.findViewById(R.id.sendToPhoneNumberTableRow).setVisibility(View.GONE);
        activity.findViewById(R.id.chatViewTableRow).setVisibility(View.GONE);
        activity.findViewById(R.id.send_sms_button).setVisibility(View.GONE);

        currentMode = 0;
    }

    public void enableSmsMode() {
        activity.findViewById(R.id.livePreviewTableRow).setVisibility(View.GONE);
        activity.findViewById(R.id.start_flash_button).setVisibility(View.GONE);


        activity.findViewById(R.id.sendToPhoneNumberTableRow).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.chatViewTableRow).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.send_sms_button).setVisibility(View.VISIBLE);

        currentMode = 1;
    }

    public void setModeButtonsListeners() {

        enableFlashMode();

        Button flashModeButton = (Button) activity.findViewById(R.id.flashModeButton);
        flashModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableFlashMode();
            }
        });

        Button smsModeButton = (Button) activity.findViewById(R.id.smsModeButton);
        smsModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableSmsMode();
            }
        });

    }

    // =============================================================================================


    public void showStartButton() {
        startFlashButton.setVisibility(View.VISIBLE);
    }

    public void hideStartButton() {
        startFlashButton.setVisibility(View.GONE);
    }

    public void showStopButton() {
        stopFlashButton.setVisibility(View.VISIBLE);
    }

    public void hideStopButton() {
        stopFlashButton.setVisibility(View.GONE);
    }


    public boolean isStartButtonVisible() {
        return startFlashButton.getVisibility() == View.VISIBLE;
    }

}
