package com.github.choreolooper;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Center area fragment containing the user settings menu.
 */
public class SettingsFragment extends Fragment {

    private String filename;

    private SwitchCompat sleepInhibit;

    public SettingsFragment() {
        // required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        assert view != null;
        assert getActivity() != null;

        filename = "UserSettings.json";

        // Sleep inhibit switch
        sleepInhibit = view.findViewById(R.id.sleep_inhibit);
        sleepInhibit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (getActivity() != null) {
                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }
                storeSettings();
            }
        });

        loadSettings();

        return view;
    }

    private void storeSettings() {
        Context context = getContext();
        assert context != null;

        JSONObject json = new JSONObject();
        String content;
        try {
            json.put("sleepInhibit", sleepInhibit.isChecked());

            content = json.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        try (FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(file, StandardCharsets.UTF_8));
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSettings() {
        Context context = getContext();
        assert context != null;

        JSONObject json = new JSONObject();
        String content = "";

        try (FileInputStream file = context.openFileInput(filename)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            content = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!content.isEmpty()) {
            try {
                json = new JSONObject(content);

                if (json.has("sleepInhibit")) {
                    sleepInhibit.setChecked(json.getBoolean("sleepInhibit"));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
