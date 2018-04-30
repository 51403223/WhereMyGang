package com.logpht.wheremygang;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class InputEditTextErrorInformer implements View.OnTouchListener {
    private Drawable defaultBackground;
    private EditText owner;

    public InputEditTextErrorInformer(EditText owner) {
        this.owner = owner;
        this.defaultBackground = owner.getBackground();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.owner.setBackground(this.defaultBackground);
        return false;
    }
}
