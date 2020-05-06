package com.example.morsecode;

import android.content.Context;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MorseConverter {

    private String symbolsDelimiter = "";
    private String charactersDelimiter = "  ";
    private String wordsDelimiter = "    ";
    private String phrasesDelimiter = "|";


    private String encodePrefix = "_";
    private String decodePrefix = "__";

    public MorseConverter() {
    }

    private String encodeCharacter(char character) {
        String characterStr = encodePrefix + String.valueOf(character).toUpperCase();

        Context context = MainActivity.getInitialContext();
        int chrId = context.getResources().getIdentifier(characterStr, "string", "com.example.morsecode");
        String chrFormat = context.getResources().getString(chrId);


        StringBuilder normalFormat = new StringBuilder();
        for (int index = 0; index < chrFormat.length(); index++) {
            char currentSymbol;
            if (chrFormat.charAt(index) == 'P') {
                currentSymbol = '.';
            } else {
                currentSymbol = '-';
            }
            normalFormat.append(currentSymbol);
            if (index < chrFormat.length() - 1) {
                normalFormat.append(symbolsDelimiter);
            }
        }

        return normalFormat.toString();
    }

    private String encodeWord(String word) {
        StringBuilder wordEncoded = new StringBuilder();
        for (int index = 0; index < word.length(); index++) {
            String currentLetter = encodeCharacter(word.charAt(index));
            wordEncoded.append(currentLetter);
            if (index < word.length() - 1) {
                wordEncoded.append(charactersDelimiter);
            }
        }
        return wordEncoded.toString();
    }

    public String encodePhrase(String phrase) {
        String[] words = phrase.split("[, ;:\"\'<>*|]+");

        StringBuilder encodedPhrase = new StringBuilder();
        for (int index = 0; index < words.length; index++) {
            String encodedWord = encodeWord(words[index]);
            encodedPhrase.append(encodedWord);
            if (index < words.length - 1){
                encodedPhrase.append(wordsDelimiter);
            }
        }

        return encodedPhrase.toString();
    }



    public String decodeCharacter(String character){

        StringBuilder convertFormat = new StringBuilder();
        for (int index = 0; index < character.length(); index++){
            if (character.charAt(index) == '-'){
                convertFormat.append("L");
            } else {
                convertFormat.append("P");
            }
        }

        String characterStr = decodePrefix + convertFormat.toString();

        Context context = MainActivity.getInitialContext();
        int chrId = context.getResources().getIdentifier(characterStr, "string", "com.example.morsecode");
        String characterConverted = context.getResources().getString(chrId);

        return characterConverted;
    }

    public String decodeWord(String word){
        String[] characters = word.split(charactersDelimiter);

        StringBuilder wordDecoded = new StringBuilder();
        for (int index = 0; index < characters.length; index++){
            String character = decodeCharacter(characters[index]);
            wordDecoded.append(character);
        }

        return wordDecoded.toString();
    }

    public String decodePhrase(String phrase){
        String[] words = phrase.split(wordsDelimiter);

        StringBuilder phraseDecoded = new StringBuilder();
        for (int index = 0; index < words.length; index++){
            String word = decodeWord(words[index]);
            phraseDecoded.append(word);
            if (index < words.length - 1){
                phraseDecoded.append(" ");
            }
        }

        return phraseDecoded.toString();
    }

}
