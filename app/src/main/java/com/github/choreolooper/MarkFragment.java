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
public class MarkFragment extends Fragment {

    /// Media player interface
    Player player;

    /// Change listener for persisting changes
    MarkEditListener editListener;

    EditText markName;
    EditText markNotes;
    Button markTime;

    Mark currentMark;



    public MarkFragment() {
        // required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MainFragment.
     */
    public static MarkFragment newInstance() {
        return new MarkFragment();
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
        View view =  inflater.inflate(R.layout.fragment_mark, container, false);

        assert view != null;
        assert getActivity() != null;


        // Set Position button
        ImageButton setPosButton = view.findViewById(R.id.setPosBtn);
        setPosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.removeMark(currentMark);
                currentMark.time = player.getProgress();
                player.addMark(currentMark);

                afterMarkEdited();
            }
        });

        // Time picker button
        markTime = view.findViewById(R.id.markInnerEdit);
        markTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.pickTime(getLayoutInflater(),
                        currentMark.time, 0, player.getDuration(),
                        (int millis) -> {
                            player.removeMark(currentMark);
                            currentMark.time = millis;
                            player.addMark(currentMark);

                            afterMarkEdited();
                        }
                );
            }
        });

        // Name field
        markName = view.findViewById(R.id.markInnerName);
        markName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (markName.getTag() != null) return;

                currentMark.name = s.toString();
                afterMarkEdited();
            }
        });
        markName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    markName.clearFocus();
                }
                return true;
            }
        });

        // notes field
        markNotes = view.findViewById(R.id.editMarkNotes);
        markNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (markNotes.getTag() != null) return;

                currentMark.notes = s.toString();
                afterMarkEdited();
            }
        });
        markNotes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    markNotes.clearFocus();
                }
                return true;
            }
        });

        return view;
    }


    /**
     * Set the listener for mark edits made by this fragment
     *
     * @param listener target listening for edit notifications
     */
    public void setEditListener(MarkEditListener listener) {
        editListener = listener;
    }


    /**
     * Display the specified mark.
     *
     * @param mark mark to be displayed.
     */
    public void setMark(Mark mark) {
        currentMark = mark;
        Utils.setEditText(markName, mark.name);
        Utils.setEditText(markNotes, mark.notes);
        markTime.setText(Utils.formatTime(mark.time));
    }


    /**
     * Reflect mark changes in the UI, and notify the edit listener.
     */
    private void afterMarkEdited() {
        markTime.setText(Utils.formatTime(currentMark.time));
        editListener.notifyMarkEdit();
    }
}
