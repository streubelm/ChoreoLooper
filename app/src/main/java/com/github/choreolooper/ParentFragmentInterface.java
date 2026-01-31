package com.github.choreolooper;

/**
 * Interface for communication between a fragment and its parent.
 */
public interface ParentFragmentInterface {

    /**
     * Request the parent to return to the default child fragment,
     * sent by any other child fragment.
     */
    void leaveSubFragment();
}
