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


    public Mark(String name, String notes, int time) {
        this.name = name;
        this.notes = notes;
        this.time = time;
    }

    @NonNull
    @Override
    public String toString() {
        if (!name.isEmpty()) return name;

        return Utils.formatTime(time);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other.getClass() != getClass()) return false;

        Mark otherMark = (Mark) other;
        return time == otherMark.time;
    }

}
