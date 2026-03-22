package com.github.choreolooper;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Adapter used for the list of available choreography files in internal storage.
 */
public class FileArrayAdapter extends ArrayAdapter<String> {

    /// Entity managing operations on the files on this list.
    FileActionInterface actionInterface;

    /// Application context used for rendering
    private final Context context;
    /// Names of the listed files
    private final List<String> files;

    /// Name of the currently loaded file
    private String activeItem;


    /**
     * Create a new FileArrayAdapter for a list of filenames.
     *
     * @param context Application context used for drawing.
     * @param fileNames List used as the source of available filenames.
     * @param actionInterface Interface to use for triggering actions on the contained files.
     */
    public FileArrayAdapter(@NonNull Context context, @NonNull List<String> fileNames, FileActionInterface actionInterface) {
        super(context, -1, fileNames);

        this.actionInterface = actionInterface;

        this.context = context;
        this.files = fileNames;
        activeItem = "";
    }


    /**
     * Overwritten method returning a custom View for display.
     * <p/>
     * This View object contains the complete list of files, each
     * including the file name, rename, and delete buttons with their
     * associated action callbacks.
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     *
     * @return A View object containing the file list and action buttons.
     */
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemView = inflater.inflate(R.layout.nav_file_item, parent, false);
        TextView name = itemView.findViewById(R.id.file_item_name);
        name.setText(files.get(position));

        name.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        name.setSelected(true);
        name.setSingleLine(true);

        if (files.get(position).equals(activeItem)) {
            itemView.setBackgroundResource(R.drawable.pressed);
        }

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeItem = files.get(position);
                actionInterface.open(files.get(position));
            }
        });

        ImageButton rename = itemView.findViewById(R.id.file_item_rename);
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickUniqueString(inflater, files.get(position), files, new StringPickerTargetInterface() {
                    @Override
                    public void setString(String string) {
                        actionInterface.rename(files.get(position), string);
                    }
                });
            }
        });

        ImageButton delete = itemView.findViewById(R.id.file_item_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionInterface.delete(files.get(position));
            }
        });

        return itemView;
    }

    public void setActiveItem(String item) {
        activeItem = item;
    }
}
