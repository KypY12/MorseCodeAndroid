package com.example.morsecode;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import java.util.logging.Handler;

public class FlashLighter implements Runnable {

    private Activity context;

    private String morsePhrase;
    private CameraManager cameraManager;
    private String cameraId;

    private int dotTime;
    private int dashTime;

    private Button startFlashButton;
    private Button stopFlashButton;


    public FlashLighter(Activity context, String morsePhrase, CameraManager cameraManager, Button startFlashButton, Button stopFlashButton, int dotTime, int dashTime) {
        this.context = context;

        this.morsePhrase = morsePhrase;
        this.cameraManager = cameraManager;
        try {
            this.cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        this.startFlashButton = startFlashButton;
        this.stopFlashButton = stopFlashButton;
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

    @Override
    public void run() {

        String[] words = morsePhrase.split("    ");

        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            String[] chars = words[wordIndex].split(" ");

            for (int charIndex = 0; charIndex < chars.length; charIndex++) {
                String character = chars[charIndex];

                for (int symbolIndex = 0; symbolIndex < character.length(); symbolIndex++) {
                    if (startFlashButton.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    flashOnOff(true);
                    // Tine aprins pentru punct sau pentru linie
                    if (character.charAt(symbolIndex) == '.') {
                        waitTime(dotTime);
                    } else {
                        waitTime(dashTime);
                    }
                    flashOnOff(false);

                    // Intre simboluri se asteapta un timp echivalent cu cel al unui punct
                    // iar la final nu se mai asteapta nimic
                    if (symbolIndex < character.length() - 1) {
                        waitTime(dotTime);
                    }
                }

                // Intre caractere se asteapta un timp echivalent cu cel al unei linii
                // iar la final nu se mai asteapta nimic
                if (charIndex < chars.length - 1) {
                    waitTime(dashTime);
                }
            }

            // Intre cuvinte se asteapta un timp echivalent cu doua linii
            // iar la final nu se mai asteapta nimic
            if (wordIndex < words.length - 1) {
                waitTime(2 * dashTime);
            }
        }

        context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startFlashButton.setVisibility(View.VISIBLE);
                    stopFlashButton.setVisibility(View.GONE);
                }
            });
//        Handler mainHandler = new Handler(context.getMainLooper());
//
//
//        try {
//            Activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    startFlashButton.setVisibility(View.VISIBLE);
//                    stopFlashButton.setVisibility(View.GONE);
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }
}