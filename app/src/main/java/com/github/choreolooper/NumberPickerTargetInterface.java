package com.github.choreolooper;


/**
 * Callback interface for receiving the result of a number picker dialog.
 */
public interface NumberPickerTargetInterface {

    /**
     * Called by the picker when a number was chosen.
     *
     * @param n number returned by the picker
     */
    void setNumber(int n);
}
