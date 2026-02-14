package com.github.choreolooper;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment containing the main app functionality.
 * <p/>
 * Manages scene and mark selection, and the property fragments in the center area.
 */
public class MainFragment extends Fragment
        implements PositionControlInterface, MarkEditListener, SceneEditListener {


    /// Media player interface
    Player player;

    /// Fragment containing the quick scene controls
    SceneFragment sceneFragment;
    /// Fragment containing the quick mark controls
    MarkFragment markFragment;

    /// Change listener for persisting changes
    EditListener editListener;

    Fragment currentFragment;

    /// switch to edit scene view
    ImageButton editSceneBtn;
    ImageButton deleteSceneBtn;

    /// switch to edit mark view
    ImageButton editMarkBtn;


    ArrayList<Scene> sceneList;
    ArrayAdapter<Scene> sceneAdapter;
    Spinner sceneSpinner;

    Scene currentScene;

    ArrayList<Mark> markList;
    ArrayAdapter<Mark> markAdapter;
    Spinner markSpinner;

    Mark currentMark;



    public MainFragment() {
        // required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance() {
        return new MainFragment();
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
        View view =  inflater.inflate(R.layout.fragment_main, container, false);

        assert view != null;
        assert getActivity() != null;

        // initialize player, without loading a media file yet.
        player = new Player(getActivity().getApplicationContext(), view.findViewById(R.id.main), this);

        // Inflate all fragments used for the center area for later use.
        sceneFragment = SceneFragment.newInstance();
        sceneFragment.player = player;
        sceneFragment.setEditListener(this);

        markFragment = MarkFragment.newInstance();
        markFragment.player = player;
        markFragment.setEditListener(this);

        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_inner_fragment, markFragment)
                .add(R.id.main_inner_fragment, sceneFragment)
                .hide(markFragment)
                .commit();
        currentFragment = sceneFragment;


        /*
         * Scene bar buttons
         */

        // Edit button
        editSceneBtn = view.findViewById(R.id.edit);
        editSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSceneFragment();
            }
        });

        // delete scene button
        deleteSceneBtn = view.findViewById(R.id.delete);
        deleteSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sceneSpinner.getSelectedItemPosition() < 0) return;

                sceneList.remove(sceneSpinner.getSelectedItemPosition());

                if (sceneList.isEmpty()) {
                    sceneList.add(new Scene(getString(R.string.fullScene) + getString(R.string.autoMarker), 0, player.getDuration(), 5000, 5000, 0));
                    sceneList.get(0).isAuto = true;
                }

                sceneAdapter.notifyDataSetChanged();
                sceneSpinner.setSelection(0);
                editListener.notifyChange();
            }
        });

        // new scene button
        ImageButton addSceneBtn = view.findViewById(R.id.saveNew);
        addSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scene newScene = new Scene(getString(R.string.seqBasename) + (sceneList.size()+1),
                        player.getProgress(), player.getDuration(),
                        5000, 5000, 0);
                sceneList.add(newScene);
                sceneAdapter.notifyDataSetChanged();
                sceneSpinner.setSelection(sceneList.size()-1);
                editListener.notifyChange();

                showSceneFragment();
            }
        });

        // Scene spinner
        sceneList = new ArrayList<>();
        sceneList.add(new Scene(getString(R.string.fullScene) + getString(R.string.autoMarker), 0, 0, 5000, 5000, 0));
        sceneList.get(0).isAuto = true;
        sceneAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, sceneList);
        sceneSpinner = view.findViewById(R.id.sceneSpinner);
        sceneSpinner.setAdapter(sceneAdapter);
        sceneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentScene = (Scene) sceneSpinner.getItemAtPosition(position);
                selectScene(currentScene);
                if (sceneList.size() < 2 && currentScene.isAuto) {
                    // Forbid deleting autogenerated scenes, if no other exists
                    disableButton(deleteSceneBtn);
                } else if (!deleteSceneBtn.isClickable()) {
                    enableButton(deleteSceneBtn);
                }

                showSceneFragment();

                if (!player.isPlaying()) {
                    player.seekTo(currentScene.begin);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                disableButton(deleteSceneBtn);
            }
        });


        /*
         * Mark bar buttons
         */

        // Edit button
        editMarkBtn = view.findViewById(R.id.editMark);
        editMarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMarkFragment();
            }
        });

        // initially disabled, as there is no default mark
        disableButton(editMarkBtn);

        // delete mark button
        ImageButton deleteMarkBtn = view.findViewById(R.id.markDelete);
        deleteMarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markList.isEmpty()) return;

                if (currentMark == null) return;

                player.removeMark(currentMark);
                markList.remove(currentMark);

                markAdapter.notifyDataSetChanged();

                if (!markList.isEmpty()) {
                    markSpinner.setSelection(0);
                    currentMark = markList.get(0);
                } else {
                    showSceneFragment();
                }

                editListener.notifyChange();
            }
        });

        // initially disabled, as there is no default mark
        disableButton(deleteMarkBtn);

        // new mark button
        ImageButton addMarkBtn = view.findViewById(R.id.saveMarkNew);
        addMarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mark newMark = new Mark(getString(R.string.markBasename) + (markList.size()+1), player.getProgress());
                selectMark(newMark);

                markList.add(newMark);
                player.addMark(newMark);
                currentMark = newMark;

                notifyMarkEdit();
                showMarkFragment();
            }
        });

        // Mark spinner
        markList = new ArrayList<>();
        markAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, markList);
        markSpinner = view.findViewById(R.id.markSpinner);
        markSpinner.setAdapter(markAdapter);
        markSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Mark newMark = (Mark) markSpinner.getItemAtPosition(position);

                enableButton(deleteMarkBtn);
                enableButton(editMarkBtn);

                if (!newMark.equals(currentMark)) {
                    selectMark(newMark);
                    showMarkFragment();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showSceneFragment();
                currentMark = null;

                disableButton(deleteMarkBtn);
                disableButton(editMarkBtn);
            }
        });


        /*
         * Main view buttons
         */

        // Sequence switch
        SwitchMaterial seqSwitch = view.findViewById(R.id.modeSwitch);
        seqSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (seqSwitch.isChecked()) {
                    // Activate Sequence mode
                    seqSwitch.setChecked(player.setSequence(true));
                } else {
                    // Stop the sequence in the player
                    seqSwitch.setChecked(player.setSequence(false));
                }
            }
        });


        return view;
    }


    /**
     * Register an edit callback triggered on any change to a scene.
     *
     * @param listener listener subscribed to scene edits
     */
    public void setEditListener(EditListener listener) {
        editListener = listener;
    }


    /**
     * Replace the main content with the specified fragment.
     * <p/>
     * The specified fragment needs to be already inflated and added to the fragment manager.
     *
     * @param fragment Fragment to display in the main area.
     */
    private void showFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .hide(currentFragment)
                .show(fragment)
                .commit();
        currentFragment = fragment;
    }


    /**
     * Make an image button clickable.
     *
     * @param btn button that should be activated
     */
    private void enableButton(ImageButton btn) {
        btn.setColorFilter(null);
        btn.setEnabled(true);
        btn.setClickable(true);
    }


    /**
     * Make a button not clickable
     *
     * @param btn button that should be deactivated
     */
    public void disableButton(ImageButton btn) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnSecondary,
                typedValue, true
        );
        int color = ContextCompat.getColor(requireContext(), typedValue.resourceId);
        btn.setColorFilter(color);
        btn.setEnabled(false);
        btn.setClickable(false);
    }


    /**
     * Display the quick scene control fragment
     */
    public void showSceneFragment() {
        sceneFragment.setScene(currentScene);
        showFragment(sceneFragment);

        editSceneBtn.setBackgroundResource(R.drawable.pressed);
        editMarkBtn.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Display the quick mark control fragment
     */
    private void showMarkFragment() {
        markFragment.setMark(currentMark);
        showFragment(markFragment);

        editMarkBtn.setBackgroundResource(R.drawable.pressed);
        editSceneBtn.setBackgroundColor(Color.TRANSPARENT);
    }


    /**
     * Display the specified scene and load it into the player.
     * <p/>
     * Only to be used from the scene spinner callback, use ext_selectScene() otherwise.
     *
     * @param scene scene to be loaded
     */
    private void selectScene(Scene scene) {
        currentScene = scene;
        player.setScene(scene);
        sceneFragment.setScene(currentScene);
    }

    /**
     * Display the specified mark and load it into the player
     * <p/>
     * Only to be used from the mark spinner callback, use ext_selectMark() otherwise.
     *
     * @param mark mark to be loaded
     */
    private void selectMark(Mark mark) {
        currentMark = mark;
        player.setMark(mark);
    }


    /**
     * Display the specified scene and load it into the player.
     * <p/>
     * Selects the scene within the scene spinner, which in turn handles the
     * actual loading. <br/>
     * If the scene is not present in the spinner, no changes are made.
     *
     * @param scene scene to be activated
     */
    @Override
    public void ext_selectScene(Scene scene) {
        for (int i = 0; i < sceneList.size(); i++) {
            if (scene.equals(sceneList.get(i))) {
                sceneSpinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Display the specified mark and load it into the player.
     * <p/>
     * Selects the mark within the mark spinner, which in turn handles the
     * actual loading. <br/>
     * If the mark is not present in the spinner, no changes are made.
     *
     * @param mark mark to be activated
     */
    @Override
    public void ext_selectMark(Mark mark) {
        for (int i = 0; i < markList.size(); i++) {
            if (mark.equals(markList.get(i))) {
                markSpinner.setSelection(i);
                break;
            }
        }
    }


    /**
     * Listener target for external changes to any mark
     */
    @Override
    public void notifyMarkEdit() {
        Collections.sort(markList, (Mark a, Mark b) -> (a.time - b.time));
        markAdapter.notifyDataSetChanged();
        markSpinner.setSelection(markList.indexOf(currentMark));
        editListener.notifyChange();
    }

    /**
     * Listener target for external changes to any scene
     */
    @Override
    public void notifySceneEdit() {
        sceneAdapter.notifyDataSetChanged();

        if (!deleteSceneBtn.isEnabled() &&
            !(currentScene.isAuto && sceneList.size() == 1)) {
            enableButton(deleteSceneBtn);
        }

        editListener.notifyChange();
    }
}
