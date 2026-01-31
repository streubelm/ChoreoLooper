package com.github.choreolooper;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


/**
 * Center area fragment containing the detailed scene controls.
 */
public class EditSceneFragment extends Fragment {

    /// Signal completion of edit
    SceneEditListener listener;

    /// Containing fragment
    ParentFragmentInterface parent;

    /// target scene to be edited
    Scene scene;

    /// total duration of the media used for range checks
    int mediaDuration;


    EditText sceneName;
    EditText sceneNotes;

    Button begin;
    Button end;
    Button pre;
    Button inter;
    Button reps;



    public EditSceneFragment() {
        super(R.layout.fragment_edit_scene);
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment
     *
     * @return A new instance of fragment ManualFragment.
     */
    public static EditSceneFragment newInstance() {
        return new EditSceneFragment();
    }


    /**
     * Called by android on initial creation, before the View is inflated.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getContext()!= null;

    }


    /**
     * Called by android on initialization, after the View was inflated.
     * <p/>
     * Contains the main initialization code for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the View object constituting this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_scene, container, false);

        assert view != null;

        // scene name
        sceneName = view.findViewById(R.id.edit_name);
        sceneName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (sceneName.getTag() != null) return;

                scene.name = s.toString();
                apply();
            }
        });
        sceneName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    sceneName.clearFocus();
                }
                return true;
            }
        });

        // scene notes
        sceneNotes = view.findViewById(R.id.edit_notes);
        sceneNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (sceneNotes.getTag() != null) return;

                scene.notes = s.toString();
                apply();
            }
        });

        // Input field for the media fragment begin
        begin = view.findViewById(R.id.startTime);
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickTime(getLayoutInflater(),
                        scene.begin, 0, scene.end,
                        (int millis) -> {
                                scene.begin = millis;
                                begin.setText(Utils.formatTime(millis));
                                apply();
                        }
                );
            }
        });

        // Input field for the media fragment end
        end = view.findViewById(R.id.endTime);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickTime(getLayoutInflater(),
                        scene.end, scene.begin, mediaDuration,
                        (int millis) -> {
                            scene.end = millis;
                            end.setText(Utils.formatTime(millis));
                            apply();
                        }
                );
            }
        });

        // Input field for the pre-sequence delay
        pre = view.findViewById(R.id.prePause);
        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickTime(getLayoutInflater(),
                        scene.pre, 0, Utils.getMilliseconds(0, 20),
                        (int millis) -> {
                            scene.pre = millis;
                            pre.setText(Utils.formatTime(millis));
                            apply();
                        }
                );
            }
        });

        // Input field for the inter-repetition delay
        inter = view.findViewById(R.id.interPause);
        inter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickTime(getLayoutInflater(),
                        scene.inter, 0, Utils.getMilliseconds(1, 0),
                        (int millis) -> {
                            scene.inter = millis;
                            inter.setText(Utils.formatTime(millis));
                            apply();
                        }
                );
            }
        });

        // Input field for the number of repetitions in the sequence
        reps = view.findViewById(R.id.reps);
        reps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickNumber(getLayoutInflater(),
                        scene.reps, 0, 20,
                        (int n) -> {
                            scene.reps = n;
                            reps.setText(String.valueOf(n));
                            apply();
                        }
                );
            }
        });


        // return button
        ImageButton abort = view.findViewById(R.id.edit_back);
        abort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.leaveSubFragment();
            }
        });

        return view;
    }


    /**
     * Post-creation initialization for listeners and content data.
     *
     * @param listener listener to any scene edits
     * @param parent fragment controlling the display of the current one
     * @param scene scene to display initially. May be null
     * @param mediaDuration total duration of the media to be used for range checks
     */
    public void initialize(SceneEditListener listener, ParentFragmentInterface parent,
                           Scene scene, int mediaDuration) {
        this.listener = listener;
        this.parent = parent;
        this.mediaDuration = mediaDuration;

        if (scene != null)
            setScene(scene);
    }


    /**
     * Set the scene to be edited.
     * <p/>
     * Updates the UI and contained data.
     * Must always be called before displaying this fragment.
     *
     * @param scene scene data to load
     */
    public void setScene(Scene scene) {
        this.scene = scene;

        Utils.setEditText(sceneName, scene.name);
        Utils.setEditText(sceneNotes, scene.notes);

        begin.setText(Utils.formatTime(scene.begin));
        end.setText(Utils.formatTime(scene.end));
        pre.setText(Utils.formatTime(scene.pre));
        inter.setText(Utils.formatTime(scene.inter));
        reps.setText(String.valueOf(scene.reps));
    }


    /**
     * Set the total media duration.
     * <p/>
     * Updates the upper limit for selectable times.
     * Must always be called before displaying this fragment
     * if the media might have changed.
     *
     * @param mediaDuration total duration of the media
     */
    public void setMediaDuration(int mediaDuration) {
        this.mediaDuration = mediaDuration;
    }


    /**
     * Apply a change made to the current scene by notifying the edit listener.
     */
    public void apply() {
        listener.notifySceneEdit();
    }
}