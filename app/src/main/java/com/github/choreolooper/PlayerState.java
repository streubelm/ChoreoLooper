package com.github.choreolooper;

/**
 * States of the Player's internal state machine.
 */
public enum PlayerState {
    /// No media loaded
    UNINITIALIZED,
    /// Media playback active
    PLAYING,
    /// Inactive
    STOPPED,
    /// Waiting in pre-playback delay
    WAITING_PRE,
    /// Waiting in inter-repetition delay
    WAITING_INTER
}

