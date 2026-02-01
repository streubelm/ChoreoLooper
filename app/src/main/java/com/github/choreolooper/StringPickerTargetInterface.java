package com.github.choreolooper;


/**
 * Callback interface for receiving the result of a number picker dialog.
 */
public interface StringPickerTargetInterface {

    /**
     * Called by the picker when a number was chosen.
     *
     * @param string number returned by the picker
     */
    void setString(String string);
}
