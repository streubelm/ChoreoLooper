package com.github.choreolooper;

/**
 * Listener interface for observing all changes made to a choreography project.
 */
public interface EditListener {

    /**
     * Notify the listener of changes to the current choreography.
     * <p/>
     * To be called by the editing fragment after any change to sequences,
     * marks, etc.
     * Used by the main activity for updating the internal save files.
     */
    void notifyChange();
}
