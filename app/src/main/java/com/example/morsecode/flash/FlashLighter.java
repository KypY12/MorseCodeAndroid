package com.example.morsecode.flash;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.morsecode.R;
import com.example.morsecode.handlers.UiHandler;

import java.util.logging.Handler;

public class FlashLighter implements Runnable {

    private Activity activity;
    private UiHandler uiHandler;

    private String morsePhrase;
    private CameraManager cameraManager;
    private String cameraId;

    private int dotTime;
    private int dashTime;

//    private Button startFlashButton;
//    private Button stopFlashButton;

//    EditText inputPreview;
//    TextView livePreview;
//    TextView letterTextView;


    public FlashLighter(Activity activity, UiHandler uiHandler, String morsePhrase, CameraManager cameraManager, int dotTime, int dashTime) {

        this.activity = activity;
        this.uiHandler = uiHandler;

//        this.inputPreview = inputPreview;
//        this.livePreview = livePreview;
//        this.letterTextView = letterTextView;

        this.morsePhrase = morsePhrase;
        this.cameraManager = cameraManager;
        try {
            this.cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

//        this.startFlashButton = startFlashButton;
//        this.stopFlashButton = stopFlashButton;
        this.dotTime = dotTime;
        this.dashTime = dashTime;
    }


    private void flashOnOff(boolean on) {
        try {
            cameraManager.setTorchMode(cameraId, on);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void waitTime(int timeVal) {
        try {
            Thread.sleep(timeVal);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void uiThreadUnmarkSymbols(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                String currentText = livePreview.getText().toString();
//                livePreview.setText(currentText);
                uiHandler.setLivePreviewText(uiHandler.getLivePreviewText());
            }
        });
    }

    private void uiThreadUnmarkLetters(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                String currentText = inputPreview.getText().toString();
//                inputPreview.setText(currentText);
//                letterTextView.setText(R.string.null_live_letter);

                uiHandler.setPhraseInputText(uiHandler.getPhraseInputText());

                int resId = activity.getResources().getIdentifier("null_live_letter", "string", "com.example.morsecode");
                uiHandler.setLivePreviewLetterText(activity.getResources().getString(resId));
            }
        });
    }

    private void uiThreadMarkSymbol(int symbolIndex){
        final int index = symbolIndex;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpannableString temp = new SpannableString(uiHandler.getLivePreviewText());

                ForegroundColorSpan highlighted = new ForegroundColorSpan(Color.RED);
                //StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);

                temp.setSpan(highlighted, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //temp.setSpan(boldStyle,  index, index+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                uiHandler.setLivePreviewText(temp);
            }
        });
    }

    private void uiThreadMarkLetters(int letterIndex){
        final int index = letterIndex;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpannableString temp = new SpannableString(uiHandler.getPhraseInputText());

                ForegroundColorSpan highlighted = new ForegroundColorSpan(Color.RED);
                //StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);

                temp.setSpan(highlighted, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //temp.setSpan(boldStyle,  index, index+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                uiHandler.setPhraseInputText(temp);
//                letterTextView.setText(String.valueOf(inputPreview.getText().charAt(index)).toUpperCase());
                uiHandler.setLivePreviewLetterText(String.valueOf(uiHandler.getPhraseInputText().charAt(index)).toUpperCase());
            }
        });
    }



    private void flashSymbol(char symbol){
        flashOnOff(true);
        // Tine aprins pentru punct sau pentru linie
        if (symbol == '.') {
            waitTime(dotTime);
        } else {
            waitTime(dashTime);
        }
        flashOnOff(false);
    }

    private int flashCharacter(String character, int currentSymbol){
        for (int symbolIndex = 0; symbolIndex < character.length(); symbolIndex++) {
            // Daca s-a apasat butonul de stop, atunci start este vizibil si oprim thread-ul
            if (!uiHandler.isStartButtonVisible()) {
                return -1;
            }

            // Marcam in timp real simbolul care este marcat in prezent
            uiThreadMarkSymbol(currentSymbol);

            flashSymbol(character.charAt(symbolIndex));

            // Intre simboluri se asteapta un timp echivalent cu cel al unui punct
            // iar la final nu se mai asteapta nimic
            if (symbolIndex < character.length() - 1) {
                waitTime(dotTime);
            }

            currentSymbol++;
        }

        return currentSymbol;
    }

    private int[] flashWord(String[] chars, int currentSymbol, int currentLetter){
        for (int charIndex = 0; charIndex < chars.length; charIndex++) {
            String character = chars[charIndex];

            // Marcam in timp real litera care este luminata in prezent
            uiThreadMarkLetters(currentLetter);

            // Luminam simbolurile caracterului curent
            currentSymbol = flashCharacter(character, currentSymbol);
            if (currentSymbol == -1){
                return new int[]{-1, -1};
            }

            // Intre caractere se asteapta un timp echivalent cu cel al unei linii
            // iar la final nu se mai asteapta nimic
            if (charIndex < chars.length - 1) {
                waitTime(dashTime);
            }

            currentLetter++;
            // dupa fiecare litera sunt 2 spatii (pe care le ignoram)
            currentSymbol += 2;
        }

        return new int[]{currentSymbol, currentLetter};
    }

    private void flashPhrase(String[] words){
        int currentSymbol = 0;
        int currentLetter = 0;

        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            String[] chars = words[wordIndex].split("  ");

            // Luminam simbolurile caracterelor cuvantului curent
            int[] symbolAndLetter = flashWord(chars, currentSymbol, currentLetter);

            if (symbolAndLetter[0] == -1){
                return;
            }
            currentSymbol = symbolAndLetter[0];
            currentLetter = symbolAndLetter[1];

            // Intre cuvinte se asteapta un timp echivalent cu doua linii
            // iar la final nu se mai asteapta nimic
            if (wordIndex < words.length - 1) {
                waitTime(2 * dashTime);
            }

            // dupa fiecare cuvant sunt 4 spatii pentru simboluri (pe care le ignoram; 2 de mai sus si inca 2)
            currentSymbol += 2;

            // dupa fiecare cuvant este un spatiu (pe care il ignoram)
            currentLetter++;
        }
    }

    @Override
    public void run() {

        String[] words = morsePhrase.split("    ");

        flashPhrase(words);

        // Demarcam simbolurile si caracterele ramase marcate
        uiThreadUnmarkSymbols();
        uiThreadUnmarkLetters();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiHandler.showStartButton();
                uiHandler.hideStopButton();
            }
        });

    }
}