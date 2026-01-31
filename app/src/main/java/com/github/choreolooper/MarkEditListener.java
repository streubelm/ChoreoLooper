package com.github.choreolooper;

/**
 * Listener interface for observing changes made to a mark.
 */
public interface MarkEditListener {

    /**
     * Signal indicating an edit of a mark to a MarkEditListener.
     */
    void notifyMarkEdit();
}
