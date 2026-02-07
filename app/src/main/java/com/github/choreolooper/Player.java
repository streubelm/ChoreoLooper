package com.github.choreolooper;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Manages the media player, including the corresponding UI controls.
 * <p/>
 * This class contains all functionality directly accessing the media player,
 * including UI elements directly interacting with the player, information
 * on the current playback state, controlling  the playback/wait sequence.
 */
public class Player {

    /// media player widget
    private MediaPlayer player;

    /// state of the internal state machine
    private PlayerState state;

    /// URI of the current media file, or null if none is loaded
    private Uri uri;

    /// Application context required for resolving the URI
    private final Context context;

    /// Play/Pause button
    ImageButton playBtn;

    /// Seek bar element of the UI
    private final MarkedSeekBar seeker;
    /// Timer synchronizing the seeker with the media position asynchronously
    private CountDownTimer seekerUpdater;


    ///  Progress bar shown during pauses
    private final ProgressBar pauseProgress;


    /// Container for all sequence-state-specific display elements
    private final ViewGroup sequenceDetails;

    /// Shows current state of the player in the UI, e.g. Paused/Playing/Waiting
    private final TextView stateDisplay;
    /// Shows the current position in the UI
    private final TextView timeDisplay;
    /// Shows the total duration in the UI
    private final TextView durationDisplay;

    /// Shows the number of the current repetition in the UI
    private final TextView repDisplay;
    /// Shows the total number of repetitions in the UI
    private final TextView repCountDisplay;

    /// Begin timestamp of the sequence loop, in ms
    private Scene currentScene;

    /// Timer starting the playback on complete
    private CountDownTimer pauseTimer;

    /// Switches the playback mode from normal to the sequence loop
    private boolean playSequence = false;
    /// Current number of repetitions within the current sequence loop
    private int current_count = 1;

    /// list of marks displayed on the seekbar
    ArrayList<Integer> bookmarks = new ArrayList<>();

    /// External interface for changing the displayed element from the seekbar
    PositionControlInterface posControl;


    /**
     * Initialize the player and connect it to the current activity.
     *
     * @param context Application context used for resolving the media file URI
     * @param view Activity ViewGroup containing all player-related UI elements
     */
    public Player(Context context, ViewGroup view, PositionControlInterface posControl) {
        this.context = context;
        this.posControl = posControl;

        // media player widget
        player = new MediaPlayer();
        state = PlayerState.UNINITIALIZED;

        // seekbar UI element
        seeker = view.findViewById(R.id.seekBar);
        seeker.setProgress(0);
        seeker.setMax(1);
        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // on seek actions, snap to POIs within a 5s distance.
                    boolean snapped = false;
                    for (int mark : seeker.markers) {
                        if (Math.abs(progress - mark) <= 5000) {
                            seekTo(mark);
                            snapped = true;
                            posControl.ext_selectMark(new Mark("", mark));
                            break;
                        }
                    }
                    if (Math.abs(progress - currentScene.begin) <= 5000) {
                        seekTo(currentScene.begin);
                        snapped = true;
                        posControl.ext_selectScene(currentScene);
                    }
                    if (Math.abs(progress - currentScene.end) <= 5000) {
                        seekTo(currentScene.end);
                        snapped = true;
                    }
                    if (!snapped) {
                        seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Stop the seeker synchronization while the user is seeking
                if (state == PlayerState.PLAYING) {
                    seekerUpdater.cancel();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Re-initialize the seeker synchronization when the user stops seeking
                if (state == PlayerState.PLAYING) {
                    startSeekerUpdater();
                }
            }
        });

        // progress bar shown during pauses
        pauseProgress = view.findViewById(R.id.pauseProgress);

        // Play/Pause button
        playBtn = view.findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        // -5s button
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now = player.getCurrentPosition();
                seekTo(now-5000);
            }
        });

        // +5s button
        ImageButton fwdBtn = view.findViewById(R.id.fwdBtn);
        fwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now = player.getCurrentPosition();
                seekTo(now+5000);
            }
        });

        // seek to previous mark
        ImageButton prevBtn = view.findViewById(R.id.prevBtn);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int target = 0;
                boolean toMark = false;
                boolean toScene = false;
                for (int mark : bookmarks) {
                    if (mark < getProgress() && mark > target) {
                        target = mark;
                        toMark = true;
                    }
                }

                if (currentScene.end < getProgress() && currentScene.end > target) {
                    target = currentScene.end;
                } else if (currentScene.begin < getProgress() && currentScene.begin > target) {
                    target = currentScene.begin;
                    toScene = true;
                }

                if (toScene) {
                    posControl.ext_selectScene(currentScene);
                } else if (toMark) {
                    posControl.ext_selectMark(new Mark("", target));
                }

                seekTo(target);
            }
        });

        // seek to next mark
        ImageButton nextBtn = view.findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int target = getDuration();
                boolean toMark = false;
                boolean toScene = false;
                for (int mark : bookmarks) {
                    if (mark > getProgress() && mark < target) {
                        target = mark;
                        toMark = true;
                    }
                }

                if (currentScene.begin > getProgress() && currentScene.begin < target) {
                    target = currentScene.begin;
                    toScene = true;
                } else if (currentScene.end > getProgress() && currentScene.end < target) {
                    target = currentScene.end;
                }

                if (toScene) {
                    posControl.ext_selectScene(currentScene);
                } else if (toMark) {
                    posControl.ext_selectMark(new Mark("", target));
                }

                seekTo(target);
            }
        });

        // seek to begin button
        ImageButton beginBtn = view.findViewById(R.id.beginBtn);
        beginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playSequence) {
                    seekTo(currentScene.begin);
                } else {
                    seekTo(0);
                }
            }
        });

        // seek to end button
        ImageButton endBtn = view.findViewById(R.id.endBtn);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playSequence) {
                    seekTo(currentScene.end);
                } else {
                    seekTo(player.getDuration());
                }
            }
        });


        sequenceDetails = view.findViewById(R.id.sequenceDetails);

        // player state display
        stateDisplay = view.findViewById(R.id.currentState);
        stateDisplay.setText(R.string.stateUninit);

        // current count display
        repDisplay = view.findViewById(R.id.stateRep);
        repDisplay.setText(R.string.empty);
        // total count display
        repCountDisplay = view.findViewById(R.id.stateRepCount);
        repCountDisplay.setText(R.string.empty);

        // current time display
        timeDisplay = view.findViewById(R.id.playerTime);
        // total duration display
        durationDisplay = view.findViewById(R.id.playerDuration);

    }


    /**
     * Load a file into the media player, and prepare for playback.
     *
     * @param uri Path to the media file to be loaded
     * @return whether the file could be loaded successfully
     */
    public boolean loadFile(Uri uri) {
        setSequence(false);

        if (uri == null) {
            // explicit unload
            state = PlayerState.UNINITIALIZED;
            player.reset();

            this.uri = null;

            seeker.setMax(0);
            timeDisplay.setText(Utils.formatTime(0));
            durationDisplay.setText(Utils.formatTime(0));

            playBtn.setImageResource(R.drawable.play);
            return true;
        }

        // Try to load file in new player, so the old one still exists on failure
        MediaPlayer tmp = new MediaPlayer();
        try {
            tmp.setDataSource(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // prepare for playback, includes buffering
        try {
            tmp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // delete old player, replace with new one
        player.release();
        player = tmp;

        this.uri = uri;

        seeker.setMax(player.getDuration());
        timeDisplay.setText(Utils.formatTime(0));
        durationDisplay.setText(Utils.formatTime(player.getDuration()));

        playBtn.setImageResource(R.drawable.play);

        state=PlayerState.STOPPED;

        return true;
    }


    /**
     * Toggle playback.
     * <p/>
     * This function can be used regardless of the playback mode.
     * <p/>
     * If the media is currently paused, it will immediately start playing
     * from the current position, ignoring possible delays.
     * <p/>
     * If the media is currently playing or waiting for a delay, it is paused
     * and all delay timers are cancelled.
     */
    public void playPause() {
        if (state == PlayerState.UNINITIALIZED) {
            return;
        }

        if (seekerUpdater != null) {
            seekerUpdater.cancel();
        }
        if (pauseTimer != null) {
            pauseTimer.cancel();
        }

        if (!playSequence) {
            if (state == PlayerState.STOPPED) {
                state = PlayerState.PLAYING;

                if (player.getCurrentPosition() == player.getDuration()) {
                    player.seekTo(0);
                }
                player.start();

                stateDisplay.setText(R.string.statePlaying);
                playBtn.setImageResource(R.drawable.pause);

                startSeekerUpdater();

            } else if (state == PlayerState.PLAYING) {
                state = PlayerState.STOPPED;

                player.pause();

                stateDisplay.setText(R.string.stateStopped);
                playBtn.setImageResource(R.drawable.play);
            }
        } else {
            switch (state) {
                case STOPPED:
                    state = PlayerState.WAITING_PRE;

                    // Jump to begin
                    seekTo(currentScene.begin);
                    current_count = 1;

                    // Set up display
                    stateDisplay.setText(R.string.statePre);
                    repDisplay.setText("1");
                    if (currentScene.reps > 0)
                        repCountDisplay.setText(String.valueOf(currentScene.reps));
                    else
                        repCountDisplay.setText("∞");

                    playBtn.setImageResource(R.drawable.stop);

                    showPauseProgress(currentScene.pre);

                    // Start pre-sequence delay
                    startPauseUpdater(currentScene.pre);
                    break;

                case PLAYING:
                    state = PlayerState.STOPPED;
                    player.pause();

                    // Reset to begin
                    seekTo(currentScene.begin);
                    current_count = 1;

                    // Update display
                    stateDisplay.setText(R.string.stateStopped);
                    repDisplay.setText("1");
                    playBtn.setImageResource(R.drawable.play);
                    break;

                case WAITING_PRE:
                case WAITING_INTER:
                    state = PlayerState.STOPPED;

                    // Reset to begin
                    current_count = 1;

                    // set up display
                    stateDisplay.setText(R.string.stateStopped);
                    repDisplay.setText("1");
                    showSeeker();

                    playBtn.setImageResource(R.drawable.play);
                    break;
            }
        }
    }


    /**
     * Jump to the given position in the playback, and update the seekbar.
     *
     * @param milliseconds listener position in the media
     */
    public void seekTo(int milliseconds) {
        if (state == PlayerState.UNINITIALIZED) {
            return;
        }

        if (seeker.getVisibility() == View.GONE) {
            return;
        }

        if (milliseconds < 0) {
            milliseconds = 0;
        } else if (milliseconds > player.getDuration()) {
            milliseconds = player.getDuration();
        }
        player.seekTo(milliseconds);

        timeDisplay.setText(Utils.formatTime(milliseconds));

        seeker.setProgress(milliseconds);
        if (state == PlayerState.PLAYING) {
            seekerUpdater.cancel();
            startSeekerUpdater();
        }
    }


    /**
     * Called after the current sequence step has been completed.
     * <p/>
     * This includes the end of the media or sequence, or the end of a pause.
     * <p/>
     * This function progresses the internal state machine for time-controlled actions.
     */
    private void end() {
        if (state == PlayerState.UNINITIALIZED) {
            return;
        }

        if (seekerUpdater != null) {
            seekerUpdater.cancel();
        }
        if (pauseTimer != null) {
            pauseTimer.cancel();
        }

        if (!playSequence) {
            if (state == PlayerState.PLAYING) {
                state = PlayerState.STOPPED;

                player.pause();
                seekTo(0);

                stateDisplay.setText(R.string.stateStopped);
                playBtn.setImageResource(R.drawable.play);
            }
        } else {
            switch (state) {
                case PLAYING:
                    if (currentScene.reps > 0 && current_count >= currentScene.reps) {
                        current_count = 1;
                        state = PlayerState.STOPPED;

                        player.pause();
                        seekTo(currentScene.begin);

                        stateDisplay.setText(R.string.stateStopped);
                        playBtn.setImageResource(R.drawable.play);

                    } else {
                        current_count++;
                        state = PlayerState.WAITING_INTER;

                        player.pause();
                        seekTo(currentScene.begin);

                        stateDisplay.setText(R.string.stateInter);
                        playBtn.setImageResource(R.drawable.stop);

                        showPauseProgress(currentScene.inter);
                        startPauseUpdater(currentScene.inter);
                    }

                    repDisplay.setText(String.valueOf(current_count));
                    break;

                case WAITING_PRE:
                    state = PlayerState.PLAYING;

                    player.start();

                    stateDisplay.setText(R.string.statePlaying);
                    playBtn.setImageResource(R.drawable.stop);

                    showSeeker();
                    startSeekerUpdater();

                case WAITING_INTER:
                    state = PlayerState.PLAYING;

                    player.start();

                    stateDisplay.setText(R.string.statePlaying);
                    playBtn.setImageResource(R.drawable.stop);

                    showSeeker();
                    startSeekerUpdater();
            }
        }
    }


    /**
     * Activate or deactivate sequence mode.
     * <p/>
     * If activated, playback will play the active sequence including
     * pauses and repetitions. Otherwise, it will behave like a normal media player.
     *
     * @param sequence whether to activate sequence mode
     *
     * @return true if setting was applied successfully
     */
    public boolean setSequence(boolean sequence) {
        if (state == PlayerState.UNINITIALIZED) {
            return false;
        }

        playSequence = sequence;

        if (!playSequence) {
            if (state != PlayerState.STOPPED) {
                player.pause();
                if (seekerUpdater != null) {
                    seekerUpdater.cancel();
                }
                if (pauseTimer != null) {
                    pauseTimer.cancel();
                }

                stateDisplay.setText(R.string.stateStopped);
                playBtn.setImageResource(R.drawable.play);

                seeker.setEnabled(true);
                state = PlayerState.STOPPED;
            }


            sequenceDetails.setVisibility(View.GONE);
            showSeeker();

        } else {

            showSeeker();

            if (state != PlayerState.STOPPED) {
                player.pause();
                if (seekerUpdater != null) {
                    seekerUpdater.cancel();
                }
            }

            seekTo(currentScene.begin);

            stateDisplay.setText(R.string.stateStopped);
            repDisplay.setText("1");
            if (currentScene.reps > 0)
                repCountDisplay.setText(String.valueOf(currentScene.reps));
            else
                repCountDisplay.setText("∞");
            playBtn.setImageResource(R.drawable.play);

            state = PlayerState.STOPPED;

            sequenceDetails.setVisibility(View.VISIBLE);
        }

        return playSequence;
    }


    /**
     * Show the seek bar instead of the pause progress bar.
     */
    private void showSeeker() {
        seeker.setVisibility(View.VISIBLE);
        pauseProgress.setVisibility(View.GONE);

        timeDisplay.setText(Utils.formatTime(player.getCurrentPosition()));
        durationDisplay.setText(Utils.formatTime(player.getDuration()));
    }

    /**
     * Show a pause progress bar of the specified duration instead of the seek bar.
     *
     * @param duration total duration of the pause, in milliseconds
     */
    private void showPauseProgress(int duration) {
        seeker.setVisibility(View.GONE);
        pauseProgress.setVisibility(View.VISIBLE);
        pauseProgress.setMax(duration);

        timeDisplay.setText(R.string.time0);
        durationDisplay.setText(Utils.formatTime(duration));
    }


    /**
     * Load the specified scene.
     * <p/>
     * Loads the scene data into the playback settings, and updates the UI.
     *
     * @param scene scene to be loaded
     */
    public void setScene(Scene scene) {
        currentScene = scene;
        if (scene.reps > 0)
            repCountDisplay.setText(String.valueOf(scene.reps));
        else
            repCountDisplay.setText("∞");
        seeker.setScene(scene);

        if (playSequence && state != PlayerState.STOPPED) {
            player.pause();
            if (seekerUpdater != null) {
                seekerUpdater.cancel();
            }
            if (pauseTimer != null) {
                pauseTimer.cancel();
            }

            stateDisplay.setText(R.string.stateStopped);
            playBtn.setImageResource(R.drawable.play);

            seeker.setEnabled(true);
            state = PlayerState.STOPPED;
        }
    }


    /**
     * Load the specified mark.
     * <p/>
     * If there is no active playback, this seeks to the position of the mark.
     *
     * @param mark mark to be activated
     */
    public void setMark(Mark mark) {
        if (!player.isPlaying())
            seekTo(mark.time);
    }


    /**
     * Add a mark to the seek bar.
     * <p/>
     * Visualizes the mark in the seek bar, and allows snapping to the mark position
     *
     * @param mark new mark to be added
     */
    public void addMark(Mark mark) {
        bookmarks.add(mark.time);
        seeker.addMarker(mark.time);
    }


    /**
     * Remove a mark from the seek bar.
     * <p/>
     * Removes the mark from display and snapping.
     * If no mark with the given time exists, no changes are made.
     *
     * @param mark mark to be removed
     */
    public void removeMark(Mark mark) {
        bookmarks.remove(Integer.valueOf(mark.time));
        seeker.removeMarker(mark.time);
    }


    /// URI of the currently loaded media file, or null if none is loaded
    public Uri getUri() {
        return uri;
    }

    /// Current position within the media, in milliseconds
    public int getProgress() {
        return player.getCurrentPosition();
    }

    /// Total duration of the media, in milliseconds
    public int getDuration() {
        return player.getDuration();
    }

    /// Whether the player is currently playing media
    public boolean isPlaying() {
        return state == PlayerState.PLAYING;
    }


    /**
     * Initialize the timer synchronizing the player with the seekbar.
     * <p/>
     * Apart from synchronizing the seekbar in the UI, the seekerUpdater timer
     * also updates the textual time display and triggers repetitions in the
     * sequence loop
     */
    private void startSeekerUpdater() {
        // Create new timer ticking every 100ms for the remaining duration of the media
        if (seekerUpdater != null) {
            seekerUpdater.cancel();
        }
        seekerUpdater = new CountDownTimer(
                player.getDuration()-player.getCurrentPosition(), 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                // On every tick, check if we need to loop back to the beginning of the sequence
                if (playSequence && player.getCurrentPosition() > currentScene.end) {
                    end();
                } else {

                    // On every tick, update the seekbar UI element
                    seeker.setProgress(player.getCurrentPosition());

                    // ... and update the detailed time info
                    timeDisplay.setText(Utils.formatTime(player.getCurrentPosition()));
                }
            }

            @Override
            public void onFinish() {
                end();
            }
        };
        seekerUpdater.start();
    }


    /**
     * Initialize the pause progress bar updater.
     * <p/>
     * Apart from updating the indicator in the UI, this also progresses the state machine
     * after the time has elapsed.
     *
     * @param duration duration of the pause, in milliseconds
     */
    private void startPauseUpdater(int duration) {
        if (pauseTimer != null) {
            pauseTimer.cancel();
        }
        pauseProgress.setMax(duration);
        pauseProgress.setProgress(0);

        pauseTimer = new CountDownTimer(duration, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                int newTime = (int)(duration - millisUntilFinished);
                pauseProgress.setProgress(newTime);
                timeDisplay.setText(Utils.formatTime(newTime));
            }

            @Override
            public void onFinish() {
                // Start media playback after the delay
                end();
            }
        };
        pauseTimer.start();
    }
}