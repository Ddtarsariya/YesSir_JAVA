package com.ddt.attendance.Others;

import android.text.Editable;
import android.text.TextWatcher;

import com.ddt.attendance.R;
import com.google.android.material.textfield.TextInputLayout;

public class GenericTextWatcher implements TextWatcher {

    public TextInputLayout view;

    public GenericTextWatcher(TextInputLayout view) {
        this.view = view;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        switch (view.getId()) {
            case R.id.Add_class_Standard:
            case R.id.Add_class_SubjectName:
            case R.id.Add_class_Department:
            case R.id.Add_class_Division:
            case R.id.Add_class_Roll_from:
            case R.id.Add_class_Roll_end:
                view.setErrorEnabled(false);

        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

}