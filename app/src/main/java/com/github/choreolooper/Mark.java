package com.github.choreolooper;

import androidx.annotation.NonNull;


/**
 * Class representation of a mark, annotating a specific point in time of the media.
 */
public class Mark {

    /// User-defined name of the mark
    String name;
    /// User-defined text associated with the mark
    String notes;
    /// Time of the mark, in milliseconds
    int time;


    public Mark(String name, int time) {
        this.name = name;
        this.time = time;
        this.notes = "";
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other.getClass() != getClass()) return false;

        Mark otherMark = (Mark) other;
        return time == otherMark.time;
    }

}
