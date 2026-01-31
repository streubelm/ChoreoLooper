package com.github.choreolooper;


/**
 * Callback interface for receiving the result of a time picker dialog.
 */
public interface TimePickerTargetInterface {

    /**
     * Called by the picker when a time was chosen.
     *
     * @param milliseconds time returned by the picker, in milliseconds
     */
    void setTime(int milliseconds);
}
