package com.github.choreolooper;

/*
TODO Don't crash on color scheme change
TODO Confirm before deleting scenes/marks
TODO MediaSession
TODO MediaControl notification
TODO Ton- und Blinksignale bei Marken
TODO Farben / Tags für Sequenzen und Marken
TODO Marken nach Tag filtern
TODO Store / sort by file history
 */


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Main class of this app.
 * <p/>
 * This class manages file operations,
 * the navigation bar including all fragments accessed from it,
 * and the Player object.
 */
public class MainActivity extends AppCompatActivity
        implements EditListener, FileActionInterface {

    /// Select an audio file to replace the current one
    private static final int PICK_AUDIO_FILE = 2;
    ///  Select an audio or project file to load
    private static final int PICK_PROJECT_FILE = 3;
    /// Select or create a project file to write to
    private static final int PICK_SAVE_FILE = 4;

    /// File name prefix added to internal save files
    String internalFilePrefix = "ChoreoFile-";

    /// currently opened project file
    TextView currentFile;

    /// List of internal save files
    List<String> internalFiles;
    FileArrayAdapter navFilesAdapter;

    /// navigation menu drawer
    DrawerLayout drawerLayout;
    /// navigation menu contents
    NavigationView navigationView;

    ///  Fragment containing the main app logic
    MainFragment mainFragment;
    /// Fragment containing the user manual and about pages
    HTMLFragment htmlFragment;

    /// Fragment containing usersettings
    SettingsFragment settingsFragment;

    /// Currently shown fragment
    Fragment currentFragment;


    /**
     * Called by Android on startup of this app.
     * <p/>
     * Initialize the app and the UI, including all child fragments.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainFragment = MainFragment.newInstance();
        mainFragment.setEditListener(this);

        htmlFragment = HTMLFragment.newInstance();

        settingsFragment = SettingsFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.mainContainer, mainFragment)
                .add(R.id.mainContainer, htmlFragment)
                .add(R.id.mainContainer, settingsFragment)
                .hide(htmlFragment)
                .hide(settingsFragment)
                .commit();

        currentFragment = mainFragment;

        // start file name display
        currentFile = findViewById(R.id.currentFile);
        currentFile.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        currentFile.setSelected(true);
        currentFile.setSingleLine(true);

        // Display quick guide
        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.reference_dialog, null);
                AlertDialog.Builder d = new AlertDialog.Builder(dialogView.getContext());
                d.setView(dialogView);

                d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                d.create();
                d.show();
            }
        });


        /*
         * Navigation drawer
         */

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.actionOpen, R.string.cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View homeMenu = findViewById(R.id.nav_home);
        homeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(mainFragment);
            }
        });

        View manualMenu = findViewById(R.id.nav_manual);
        manualMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                htmlFragment.loadPage("file:///android_asset/manual.html");
                showFragment(htmlFragment);
            }
        });

        View aboutMenu = findViewById(R.id.nav_about);
        aboutMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                htmlFragment.loadPage("file:///android_asset/about.html");
                showFragment(htmlFragment);
            }
        });

        View settingsMenu = findViewById(R.id.nav_settings);
        settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(settingsFragment);
            }
        });


        // list of internal save files
        internalFiles = new ArrayList<>();
        ListView fileItems = findViewById(R.id.nav_list_view);
        navFilesAdapter = new FileArrayAdapter(MainActivity.this, internalFiles, this);
        fileItems.setAdapter(navFilesAdapter);


        /*
         * File action buttons
         */

        // Open File button
        ImageButton openButton = findViewById(R.id.openButton);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInternal();
                openFile(false);
                showFragment(mainFragment);
            }
        });

        // Save button
        ImageButton saveBtn = findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInternal();
                saveFile();
                showFragment(mainFragment);
            }
        });

    }


    /**
     * Called by android on application start or resume, after all other initialization.
     * <p/>
     * Reload list of choreos in internal app memory
     */
    @Override
    protected void onStart() {
        super.onStart();
        updateFileList();
    }


    /**
     * Listener callback updating the internal save file on any edit
     */
    @Override
    public void notifyChange() {
        saveInternal();
    }


    /**
     * Catch back button pressed
     * <p/>
     * Prevent app from closing when back button is pressed from a HTML view.
     * Uses deprecated feature as the modern replacement always calls the super method.
     */
    @Override
    public void onBackPressed() {
        if (currentFragment == mainFragment) {
            super.onBackPressed();
        } else {
            showFragment(mainFragment);
        }
    }


    /**
     * Replace the main content with the specified fragment.
     * <p/>
     * The specified fragment needs to be already inflated and added to the fragment manager.
     *
     * @param fragment Fragment to display in the main area.
     */
    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(currentFragment)
                .show(fragment)
                .commit();
        currentFragment = fragment;

        drawerLayout.closeDrawer(GravityCompat.START);
    }


    /**
     * Initiate writing the current configuration to a file.
     * <p/>
     * Opens or creates a JSON file, and fills it from within the
     * activity result callback.
     */
    private void saveFile() {
        // do not save if no media is opened
        if (mainFragment.player.getUri() == null) {
            return;
        }

        // Start file choosing intent, actual write is done in Activity callback
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, currentFile.getText());

        startActivityForResult(intent, PICK_SAVE_FILE);
    }


    /**
     * Load a new input file.
     * <p/>
     * This file can either be a media file to load into the player,
     * of a project file created with saveFile. <br/>
     * If it is explicitly requested to load only an audio file, the current choreography
     * data is not removed when loading the new file. Use clearChoreo() if necessary.
     *
     * @param mediaOnly if true, filter only for audio files
     */
    private void openFile(boolean mediaOnly) {
        // Start file creation intent, actual file read is done in Activity callback
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] types;
        if (mediaOnly) {
            types = new String[] {"audio/*"};
        } else {
            types = new String[] {"audio/*", "application/json"};
        }
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, types);

        if (!mediaOnly) {
            startActivityForResult(intent, PICK_PROJECT_FILE);
        } else {
            startActivityForResult(intent, PICK_AUDIO_FILE);
        }
    }


    /**
     * Clear all currently loaded sequence and mark data.
     * <p/>
     * Must be called before loading a new file; otherwise the new data will be
     * added to the existing ones instead of replacing them.
     */
    private void clearChoreo() {

        mainFragment.sceneList.clear();
        mainFragment.sceneAdapter.notifyDataSetChanged();

        for (Mark m : mainFragment.markList) {
            mainFragment.player.removeMark(m);
        }
        mainFragment.markList.clear();
        mainFragment.disableButton(mainFragment.editMarkBtn);

        mainFragment.markAdapter.notifyDataSetChanged();
    }


    /**
     * Load a new audio file into the player without modifying the chorography data.
     *
     * @param uri URI of the audio file. Assumed to be valid.
     */
    private void loadAudioFile(Uri uri) {
        getApplicationContext().getContentResolver().takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (!mainFragment.player.loadFile(uri)) {
            // Request loading new audio file
            alertLoaderError(getResources().getString(R.string.mediaError));
        }
    }


    /**
     * Load a new choreography project.
     * <p/>
     * This replaces the currently displayed choreography data.
     * The opened file can be either a project JSON or an audio file.
     *
     * @param uri            URI of the file to be opened.
     * @param name           Name to be used for the project.
     * @param forceOverwrite If true, overwrite existing files without asking.
     */
    private void loadProjectFile(Uri uri, String name, boolean forceOverwrite) {

        if (internalFiles.contains(name) && !forceOverwrite) {
            Utils.warnOverwrite(getLayoutInflater(), name, internalFiles, new StringPickerTargetInterface() {
                @Override
                public void setString(String string) {
                    loadProjectFile(uri, string, true);
                }
            });
            return;
        }

        // we're replacing all content
        clearChoreo();

        if (getContentResolver().getType(uri) != null &&
                Objects.requireNonNull(getContentResolver().getType(uri)).contains("audio")) {
            // This is an audio file. Store access permission and load the file
            loadAudioFile(uri);
        } else {
            // This is a project file
            StringBuilder string = new StringBuilder();
            try {
                // Read file
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    string.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                alertLoaderError(e.getLocalizedMessage());
                return;
            }

            // Parse and load the choreography data
            if (!parseJSON(string.toString()))
                return;
        }

        // Update the display with the new choreo data
        finalizeFileLoad(name);
    }


    /**
     * Post-processing operations after loading new data.
     * <p/>
     * Updates the file name display, spinners, and handles empty scene lists.
     *
     * @param name File name to be displayed in the app header
     */
    private void finalizeFileLoad(String name) {

        if (mainFragment.sceneList.isEmpty()) {
            mainFragment.sceneList.add(
                    new Scene(getString(R.string.fullScene) + getString(R.string.autoMarker), "",
                            0, mainFragment.player.getDuration(), 5000, 5000, 0)
            );
            mainFragment.sceneList.get(0).isAuto = true;
            mainFragment.sceneAdapter.notifyDataSetChanged();
        }

        if (name != null) {
            currentFile.setText(name);
        }

        if (!mainFragment.markList.isEmpty()) {
            mainFragment.markSpinner.setSelection(0);
        }
        mainFragment.sceneSpinner.setSelection(0);
        saveInternal();
    }


    /**
     * Export the current choreography data as a JSON string.
     * <p/>
     * Used internally by the export function.
     */
    private String exportJSON() {
        if (mainFragment.player.getUri() == null)
            // no file loaded
            return "";

        JSONObject json = new JSONObject();
        String result;
        try {
            // magic number to recognize choreography files
            json.put("MAGIC", 60559);

            // write scene data
            JSONArray scenes = new JSONArray();
            for (int i = 0; i < mainFragment.sceneList.size(); i++) {
                JSONObject scene = new JSONObject();
                scene.put("name", mainFragment.sceneList.get(i).name);
                scene.put("begin", mainFragment.sceneList.get(i).begin);
                scene.put("end", mainFragment.sceneList.get(i).end);
                scene.put("pre", mainFragment.sceneList.get(i).pre);
                scene.put("inter", mainFragment.sceneList.get(i).inter);
                scene.put("reps", mainFragment.sceneList.get(i).reps);
                scene.put("notes", mainFragment.sceneList.get(i).notes);

                scenes.put(scene);
            }
            json.put("Scenes", scenes);

            // write mark data
            JSONArray marks = new JSONArray();
            for (int i = 0; i < mainFragment.markList.size(); i++) {
                JSONObject mark = new JSONObject();
                mark.put("name", mainFragment.markList.get(i).name);
                mark.put("time", mainFragment.markList.get(i).time);
                mark.put("notes", mainFragment.markList.get(i).notes);

                marks.put(mark);
            }
            json.put("Marks", marks);

            // write media data
            json.put("uri", mainFragment.player.getUri().toString());

            result = json.toString(4);
        } catch (JSONException e) {
            alertSaveError(e.getLocalizedMessage());
            e.printStackTrace();
            result = "";
        }

        return result;
    }


    /**
     * Load choreography data from a JSON string.
     * <p/>
     * The data from the JSON string is directly loaded into the application.
     * Use finalizeFileLoad() to update the display afterwards.
     *
     * @param jsonString JSON-encoded choreography data, as returned by exportJSON()
     *
     * @return true if the data was loaded successfully
     */
    private boolean parseJSON(String jsonString) {

        Uri media;

        try {
            JSONObject json = new JSONObject(jsonString);

            // magic number to ensure this is a choreography file
            if (json.getInt("MAGIC") != 60559) {
                alertLoaderError(getString(R.string.noChoreoMsg));
                return false;
            }

            // read mark data
            JSONArray marks = json.getJSONArray("Marks");
            for (int i = 0; i < marks.length(); i++) {
                JSONObject elem = marks.getJSONObject(i);
                Mark mark = new Mark(elem.getString("name"),
                        elem.getString("notes"),
                        elem.getInt("time"));
                mainFragment.markList.add(mark);
                mainFragment.player.addMark(mark);
            }
            Collections.sort(mainFragment.markList, (Mark a, Mark b) -> (a.time - b.time));

            mainFragment.markAdapter.notifyDataSetChanged();


            // read scene data
            JSONArray scenes = json.getJSONArray("Scenes");
            for (int i = 0; i < scenes.length(); i++) {
                JSONObject elem = scenes.getJSONObject(i);
                Scene scene = new Scene(
                        elem.getString("name"),
                        elem.getString("notes"),
                        elem.getInt("begin"),
                        elem.getInt("end"),
                        elem.getInt("pre"),
                        elem.getInt("inter"),
                        elem.getInt("reps"));
                mainFragment.sceneList.add(scene);
            }

            mainFragment.sceneAdapter.notifyDataSetChanged();


            // read media data
            media = Uri.parse(json.getString("uri"));

        } catch (JSONException e) {
            e.printStackTrace();
            alertLoaderError(e.getLocalizedMessage());
            return false;
        }


        // if loading the media failed, ask for a new one.
        // Usually happens when read permissions are not present.
        if (!mainFragment.player.loadFile(media)) {
            requestNewMedia();
        }

        return true;
    }


    /**
     * Update the list of files in the internal storage.
     * <p/>
     * Project files are identified by the the file name prefix defined in
     * internalFilePrefix.
     */
    private void updateFileList() {
        internalFiles.clear();

        File[] allFiles = getApplicationContext().getFilesDir().listFiles();
        assert (allFiles != null);
        for (File file : allFiles) {
            if (!file.isFile() || !file.getName().startsWith(internalFilePrefix))
                continue;
            internalFiles.add(file.getName().substring(internalFilePrefix.length()));
        }
        navFilesAdapter.setActiveItem(currentFile.getText().toString());
        navFilesAdapter.notifyDataSetChanged();
    }


    /**
     * Write configuration to a file in local storage.
     * <p/>
     * The target file is determined by the current file name and extended
     * by the internal file name prefix.
     */
    private void saveInternal() {
        if (mainFragment.player.getUri() == null) return;

        String json = exportJSON();
        if (json.isEmpty())
            return;
        try (FileOutputStream file = openFileOutput(
                internalFilePrefix + currentFile.getText().toString(),
                Context.MODE_PRIVATE)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(file, StandardCharsets.UTF_8));
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFileList();
    }


    /**
     * Read configuration from a file in local storage.
     *
     * @param name name of the file to be loaded, excluding the internal file name prefix.
     */
    private void readInternal(String name) {
        String json = "";
        try (FileInputStream file = openFileInput(internalFilePrefix + name)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8));
            String line;
            StringBuilder tmp = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                tmp.append(line);
            }
            reader.close();
            json = tmp.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        clearChoreo();
        if (!parseJSON(json))
            return;
        finalizeFileLoad(name);
    }


    /**
     * Ask the user for a replacement media file.
     * <p/>
     * This is necessary if the original file was deleted, moved, or renamed,
     * or if file permissions are not present when loading a JSON file for the first time. <br/>
     *
     * Caution: This performs a "dumb" replacement of the media, without checking
     * time ranges of the current choreo data.
     */
    private void requestNewMedia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mediaError);
        builder.setMessage(R.string.replaceMedia);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                openFile(true);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // Create the AlertDialog object and return it.
        builder.show();
    }


    /**
     * Report an error that occurred while loading a file.
     *
     * @param message detailed error description
     */
    private void alertLoaderError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.loaderError);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // Create the AlertDialog object and return it.
        builder.show();
    }


    /**
     * Report an error that occurred when exporting to a file.
     *
     * @param message detailed error description
     */
    private void alertSaveError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.saveError);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // Create the AlertDialog object and return it.
        builder.show();
    }


    /**
     * Ask for confirmation before deleting a file.
     * <p/>
     * If the confirmation is positive, this function will delete the specified file.
     *
     * @param filename Name of the file to be deleted
     */
    private void warnDelete(String filename) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warnung");
        builder.setMessage(filename + " löschen? Dies kann nicht rückgängig gemacht werden.");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteFile(internalFilePrefix + filename);
                updateFileList();

                if (filename.equals(currentFile.getText().toString())) {
                    // unload file we just deleted
                    clearChoreo();
                    mainFragment.player.loadFile(null);
                    finalizeFileLoad(getString(R.string.stateUninit));
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Create the AlertDialog object and return it.
        builder.show();
    }


    /**
     * Rename an internal save file.
     * <p/>
     * Caution: Will overwrite other save files without warning.
     *
     * @param targetFile Current name of the file to be renamed.
     * @param newName New name to use for the file.
     */
    @Override
    public void rename(String targetFile, String newName) {

        try (InputStream in = openFileInput(internalFilePrefix + targetFile)) {
            try (OutputStream out = openFileOutput(internalFilePrefix + newName, Context.MODE_PRIVATE)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                writer.write(builder.toString());
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            return;
        }

        deleteFile(internalFilePrefix + targetFile);
        updateFileList();
    }


    /**
     * Delete an internal save file.
     * <p/>
     * Will ask for user confirmation before deleting.
     *
     * @param targetFile Name of the file to be deleted.
     */
    @Override
    public void delete(String targetFile) {
        warnDelete(targetFile);
    }


    /**
     * Load a file from internal storage.
     * <p/>
     * This will replace the current content of the UI.
     *
     * @param targetFile Name of the file to load.
     */
    @Override
    public void open(String targetFile) {
        readInternal(targetFile);
        showFragment(mainFragment);
    }


    /**
     * Called by android when an Activity returns.
     * <p/>
     * Contains the main control logic for loading and exporting files.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param resultData An Intent, which can return result data to the caller
     *                   (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);

        if (resultCode != Activity.RESULT_OK) return;

        // Open File: file chooser returned an URI
        if (requestCode == PICK_AUDIO_FILE || requestCode == PICK_PROJECT_FILE) {
            Uri uri = null;
            if (resultData == null)
                return;

            uri = resultData.getData();
            if (uri == null || uri.getPath() == null)
                return;

            if (requestCode == PICK_PROJECT_FILE) {
                // Replacing the whole project, update filename display
                String name;
                if (uri.getScheme().equals("content")) {
                    try (android.database.Cursor cursor = getContentResolver().query(uri, null,
                            null, null, null)) {

                        if (cursor == null) return;
                        cursor.moveToFirst();
                        int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                        if (index < 0) return;
                        name = cursor.getString(index);
                    }
                } else {
                    String[] path = uri.getPath().split("/");
                    name = path[path.length - 1].split("\\.")[0];
                }
                // load internally calls finalizeFileLoad after all possible
                // user interactions are done.
                loadProjectFile(uri, name, false);
            } else {
                loadAudioFile(uri);
                finalizeFileLoad(null);
            }

        // Save File: File creation dialog returned an URI
        } else if (requestCode == PICK_SAVE_FILE) {

            if (resultData == null)
                return;

            Uri uri = resultData.getData();
            if (uri == null)
                return;

            String content = exportJSON();
            if (content.isEmpty())
                return;

            try {
                // open file and write json
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                writer.write(content);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                alertSaveError(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}