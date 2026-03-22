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
 * Center area fragment containing the quick mark controls.
 */
public class SceneFragment extends Fragment {


    /// Media player interface
    Player player;

    /// Change listener for persisting changes
    SceneEditListener editListener;

    EditText sceneName;
    EditText sceneNotes;

    Button begin;
    Button end;
    Button pre;
    Button inter;
    Button reps;

    Scene currentScene;



    public SceneFragment() {
        // required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MainFragment.
     */
    public static SceneFragment newInstance() {
        return new SceneFragment();
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
        View view =  inflater.inflate(R.layout.fragment_scene, container, false);

        assert view != null;
        assert getActivity() != null;

        // Set Begin button
        ImageButton setStartBtn = view.findViewById(R.id.setBeginBtn);
        setStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentScene.begin = player.getProgress();
                begin.setText(Utils.formatTime(currentScene.begin));
                afterSceneEdited();
            }
        });

        // Set End button
        ImageButton setEndBtn = view.findViewById(R.id.setEndBtn);
        setEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentScene.end = player.getProgress();
                end.setText(Utils.formatTime(currentScene.end));
                afterSceneEdited();
            }
        });

        // Name field
        sceneName = view.findViewById(R.id.sceneInnerName);
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

                currentScene.name = s.toString();
                afterSceneEdited();
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

        // Input field for the media fragment begin
        begin = view.findViewById(R.id.startTime);
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickTime(getLayoutInflater(),
                        currentScene.begin, 0, currentScene.end,
                        (int millis) -> {
                            currentScene.begin = millis;
                            begin.setText(Utils.formatTime(millis));
                            afterSceneEdited();
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
                        currentScene.end, currentScene.begin, player.getDuration(),
                        (int millis) -> {
                            currentScene.end = millis;
                            end.setText(Utils.formatTime(millis));
                            afterSceneEdited();
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
                        currentScene.pre, 0, Utils.getMilliseconds(0, 20),
                        (int millis) -> {
                            currentScene.pre = millis;
                            pre.setText(Utils.formatTime(millis));
                            afterSceneEdited();
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
                        currentScene.inter, 0, Utils.getMilliseconds(1, 0),
                        (int millis) -> {
                            currentScene.inter = millis;
                            inter.setText(Utils.formatTime(millis));
                            afterSceneEdited();
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
                        currentScene.reps, 0, 20,
                        (int n) -> {
                            currentScene.reps = n;
                            reps.setText(String.valueOf(n));
                            afterSceneEdited();
                        }
                );
            }
        });

        // notes field
        sceneNotes = view.findViewById(R.id.editSceneNotes);
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

                currentScene.notes = s.toString();
                afterSceneEdited();
            }
        });

        return view;
    }


    /**
     * Set the listener for scene edits made by this fragment
     *
     * @param listener target listening for edit notifications
     */
    public void setEditListener(SceneEditListener listener) {
        editListener = listener;
    }


    /**
     * Display the specified scene
     *
     * @param scene scene to be displayed
     */
    public void setScene(Scene scene) {
        currentScene = scene;
        sceneName.setHint(Utils.formatSpan(currentScene.begin, currentScene.end));
        Utils.setEditText(sceneName, scene.name);
        Utils.setEditText(sceneNotes, scene.notes);

        begin.setText(Utils.formatTime(scene.begin));
        end.setText(Utils.formatTime(scene.end));
        pre.setText(Utils.formatTime(scene.pre));
        inter.setText(Utils.formatTime(scene.inter));
        reps.setText(String.valueOf(scene.reps));
    }


    /**
     * Reflect any changes to the current scene in the UI, and notify the listener.
     */
    private void afterSceneEdited() {
        player.setScene(currentScene);
        if (currentScene.isAuto) {
            int index = currentScene.name.indexOf(getString(R.string.autoMarker));
            if (index >= 0) {
                currentScene.name = currentScene.name.substring(0, index);
                Utils.setEditText(sceneName, currentScene.name);
            }
        }
        currentScene.isAuto = false;
        sceneName.setHint(Utils.formatSpan(currentScene.begin, currentScene.end));

        editListener.notifySceneEdit();
    }
}
