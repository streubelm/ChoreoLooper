package com.github.choreolooper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;


public class Utils {

    /**
     * Format a duration given in milliseconds as a human-readable String in seconds.
     * Hereby, the duration is rounded to the nearest full second.
     *
     * @param milliseconds duration to display
     *
     * @return a String representation of the duration
     */
    public static String formatTime(int milliseconds) {
        int s = (int) Math.round((double)milliseconds/1000.0);

        int min = s / 60;
        int sec = s % 60;
        return formatTime(min, sec);
    }


    /**
     * Format a duration given in minutes and seconds as a human-readable String.
     *
     * @param min minutes part in the duration
     * @param sec seconds part of the duration
     *
     * @return a String representation of the duration
     */
    @SuppressLint("DefaultLocale")
    public static String formatTime(int min, int sec) {
        return String.format("%02d:%02d", min, sec);
    }


    /**
     * Extract the number of full minutes from a duration given in milliseconds,
     * rounded to the nearest full second.
     *
     * @param milliseconds duration in milliseconds
     *
     * @return number of full minutes in the duration
     */
    public static int getMinutes(int milliseconds) {
        return (int) Math.round((double)milliseconds / 1000.0) / 60;
    }


    /**
     * Extract the number of seconds from a duration in milliseconds,
     * after subtracting the full minutes. Rounded to the nearest full second.
     *
     * @param milliseconds duration in milliseconds
     *
     * @return number of full seconds after subtracting the full minutes
     */
    public static int getSeconds(int milliseconds) {
        return (int) Math.round((double)milliseconds / 1000.0) % 60;
    }


    /**
     * Convert a duration given in minutes and seconds to milliseconds.
     *
     * @param min number of minutes
     * @param sec number of seconds
     *
     * @return the total duration in milliseconds
     */
    public static int getMilliseconds(int min, int sec) {
        return ((min*60) + sec) * 1000;
    }


    /**
     * Wrapper function for setting the contents of an EditText.
     * <p/>
     * Sets and resets the Tag of the EditText before changing the content,
     * allowing the EditText's change listener to recognize that this change was made
     * programmatically.
     *
     * @param field EditText to edit
     * @param text new text to display in the EditText
     */
    public static void setEditText(EditText field, String text) {
        field.setTag("ignore");
        field.setText(text);
        field.setTag(null);
    }


    /**
     * Let the user select a time via an alert dialog.
     *
     * @param inflater LayoutInflater used to display the dialog
     * @param current current time used as initial value, in milliseconds
     * @param min minimum value permitted, in milliseconds
     * @param max maximum value permitted, in milliseconds
     * @param target callback function for receiving the result
     */
    public static void pickTime(LayoutInflater inflater, int current, int min, int max,
                                TimePickerTargetInterface target) {

        View dialogView = inflater.inflate(R.layout.time_picker_dialog, null);
        AlertDialog.Builder d = new AlertDialog.Builder(dialogView.getContext());
        d.setView(dialogView);

        // picker for the minutes component
        NumberPicker minPicker = dialogView.findViewById(R.id.dialog_min_picker);
        minPicker.setMinValue(getMinutes(min));
        minPicker.setMaxValue(getMinutes(max));
        minPicker.setValue(getMinutes(current));

        // picker for the seconds component
        NumberPicker secPicker = dialogView.findViewById(R.id.dialog_sec_picker);
        // set the initial data ranges for the seconds picker
        if (getMinutes(current) == getMinutes(min)) {
            secPicker.setMinValue(getSeconds(min));
        } else {
            secPicker.setMinValue(0);
        }
        if (getMinutes(current) == getMinutes(max)) {
            secPicker.setMaxValue(getSeconds(max));
        } else {
            secPicker.setMaxValue(59);
        }
        secPicker.setValue(getSeconds(current));

        // adapt available seconds values to the permitted range, depending on the minutes value
        minPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == getMinutes(min)) {
                    secPicker.setMinValue(getSeconds(min));
                } else {
                    secPicker.setMinValue(0);
                }
                if (newVal == getMinutes(max)) {
                    secPicker.setMaxValue(getSeconds(max));
                } else {
                    secPicker.setMaxValue(59);
                }
            }
        });

        d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                target.setTime(getMilliseconds(minPicker.getValue(), secPicker.getValue()));
            }
        });
        d.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }


    /**
     * Allow the user to pick a single number via an alert dialog.
     *
     * @param inflater LayoutInflater used to display the dialog
     * @param current current number used as initial value
     * @param min minimum permitted number
     * @param max maximum permitted number
     * @param target callback function for receiving the result
     */
    public static void pickNumber(LayoutInflater inflater, int current, int min, int max,
                                  NumberPickerTargetInterface target) {

        View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);
        AlertDialog.Builder d = new AlertDialog.Builder(dialogView.getContext());
        d.setView(dialogView);

        NumberPicker picker = dialogView.findViewById(R.id.dialog_number_picker);
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(current);

        d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                target.setNumber(picker.getValue());
            }
        });
        d.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }


    /**
     * Allow the user to enter a string via an alert dialog.
     *
     * @param inflater LayoutInflater used to display the dialog
     * @param current current string used as initial value
     * @param target callback function for receiving the result
     */
    public static void pickString(LayoutInflater inflater, String current,
                                  StringPickerTargetInterface target) {

        View dialogView = inflater.inflate(R.layout.string_picker_dialog, null);
        AlertDialog.Builder d = new AlertDialog.Builder(dialogView.getContext());
        d.setView(dialogView);

        EditText picker = dialogView.findViewById(R.id.dialog_string_picker);
        picker.setText(current);
        picker.setSingleLine(true);

        d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                target.setString(picker.getText().toString());
            }
        });
        d.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

}


