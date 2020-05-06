package com.example.morsecode;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.widget.Button;

public class FlashLighter implements Runnable {

    private String morsePhrase;
    private CameraManager cameraManager;
    private String cameraId;

    private int dotTime;
    private int dashTime;

    private Button flashButton;


    public FlashLighter(String morsePhrase, CameraManager cameraManager, Button flashButton, int dotTime, int dashTime) {
        this.morsePhrase = morsePhrase;
        this.cameraManager = cameraManager;
        try {
            this.cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        this.flashButton = flashButton;
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

        //flashButton.setEnabled(false);
        String[] words = morsePhrase.split("    ");

        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            String[] chars = words[wordIndex].split(" ");

            for (int charIndex = 0; charIndex < chars.length; charIndex++) {
                String character = chars[charIndex];

                for (int symbolIndex = 0; symbolIndex < character.length(); symbolIndex++) {
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
        //flashButton.setEnabled(true);
    }
}