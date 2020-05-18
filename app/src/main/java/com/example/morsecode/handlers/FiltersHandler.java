package com.example.morsecode.handlers;

import android.app.Activity;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.Toast;

public class FiltersHandler {

    private Activity activity;

    public FiltersHandler(Activity activity){
        this.activity = activity;

    }

    // =============================================================================================
    //  FILTERS FUNCTIONS
    // =============================================================================================

    public void addAllCapsFilter(EditText inputView) {
        InputFilter[] inputFilters = inputView.getFilters();
        InputFilter[] newInputFilters = new InputFilter[inputFilters.length + 1];
        System.arraycopy(inputFilters, 0, newInputFilters, 0, inputFilters.length);

        newInputFilters[inputFilters.length] = new InputFilter.AllCaps();
        inputView.setFilters(newInputFilters);
    }

    public void addSpaceLetterDigitsOnlyFilter(EditText inputView) {
        InputFilter[] inputFilters = inputView.getFilters();
        InputFilter[] newInputFilters = new InputFilter[inputFilters.length + 1];
        System.arraycopy(inputFilters, 0, newInputFilters, 0, inputFilters.length);

        newInputFilters[inputFilters.length] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int index = start; index < end; index++) {
                    if (!Character.isLetterOrDigit(source.charAt(index)) && source.charAt(index) != ' ') {
                        Toast.makeText(activity, "You can use only letters or digits.", Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                return null;
            }
        };
        inputView.setFilters(newInputFilters);
    }


}
