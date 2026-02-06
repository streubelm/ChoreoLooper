package com.github.choreolooper;

import android.content.Context;
import android.media.Image;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FileArrayAdapter extends ArrayAdapter<String> {

    FileActionInterface actionInterface;

    private Context context;
    private List<String> files;

    public FileArrayAdapter(@NonNull Context context, @NonNull List<String> fileNames, FileActionInterface actionInterface) {
        super(context, -1, fileNames);

        this.actionInterface = actionInterface;

        this.context = context;
        this.files = fileNames;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemView = inflater.inflate(R.layout.nav_file_item, parent, false);
        TextView name = itemView.findViewById(R.id.file_item_name);
        name.setText(files.get(position));

        name.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        name.setSelected(true);
        name.setSingleLine(true);

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionInterface.open(files.get(position));
            }
        });

        ImageButton rename = itemView.findViewById(R.id.file_item_rename);
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickString(inflater, files.get(position), new StringPickerTargetInterface() {
                    @Override
                    public void setString(String string) {
                        actionInterface.rename(files.get(position), string);
                        files.set(position, string);
                        notifyDataSetChanged();
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
}
