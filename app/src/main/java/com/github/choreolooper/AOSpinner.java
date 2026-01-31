package com.github.choreolooper;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Always On Spinner, a spinner widget derivate sending a change notification
 * even if the user selects the already selected element.
 *
 * @link <a href="https://stackoverflow.com/a/25478205">StackOverflow</a>
 */
public class AOSpinner extends androidx.appcompat.widget.AppCompatSpinner {

    public AOSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public void
    setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (getSelectedView() == null) return;
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected,
            // so do it manually now
            if (getOnItemSelectedListener() != null) {
                getOnItemSelectedListener().onItemSelected(
                        this, getSelectedView(), position, getSelectedItemId()
                );
            }
        }
    }
}
