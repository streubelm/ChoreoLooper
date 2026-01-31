package com.github.choreolooper;

import androidx.annotation.NonNull;


/**
 * Class representation of a sequence.
 * <p/>
 * A sequence consists of a media segment, a number of repetitions, the
 * delay before and between these repetitions, and an associated user-defined
 * name and free text.
 */
public class Scene {

    /// User-defined name of this scene
    String name;
    /// User-defined text associated with this scene
    String notes;

    /// Start time of the scene, in milliseconds
    int begin;
    /// End time of the scene, in milliseconds
    int end;
    /// Number of repetitions of the media segment. A value of 0 indicates infinite repetitions.
    int reps;
    /// Pre-playback delay, in milliseconds
    int pre;
    /// Delay between two repetitions, in milliseconds
    int inter;

    /// Flag indicating whether this scene was generated automatically
    boolean isAuto;


    public Scene(String name, int begin, int end, int pre, int inter, int reps) {
        this.name = name;
        this.notes = "";
        this.begin = begin;
        this.end = end;
        this.pre = pre;
        this.inter = inter;
        this.reps = reps;
        this.isAuto = false;
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

        Scene otherScene = (Scene) other;
        return (begin == otherScene.begin) && (end == otherScene.end);
    }

}
