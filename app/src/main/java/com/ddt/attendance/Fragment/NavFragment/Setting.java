package com.ddt.attendance.Fragment.NavFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import petrov.kristiyan.colorpicker.ColorPicker;

public class Setting extends Fragment {

    private Context context;
    private ImageView themeColorChoicer;
    private GradientDrawable gradientDrawable;
    private static final String THEME_COLOR = "themeColor";
    private static final String SHARED_PREFERENCE = "sharedPref";
    private TextInputEditText changeName;
    private TextInputLayout saveChangeName;
    private FirebaseReference firebaseReference = new FirebaseReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private int themeColor;

    public Setting() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().setTitle("Setting");
        themeColorChoicer = view.findViewById(R.id.color_choice_shower);
        changeName = view.findViewById(R.id.Setting_change_name);
        saveChangeName = view.findViewById(R.id.Setting_change_name_save_btn);
        context = container.getContext();
        gradientDrawable = (GradientDrawable) themeColorChoicer.getBackground().mutate();
        themeColorChoicer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorChoicePallete();
            }
        });
        context = container.getContext();
        loadThemeColor();
        updateTheme();
        firebaseReference.documentReference.collection("Name").document("Name").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            changeName.setText(documentSnapshot.getString("Name"));
                        } else {
                            changeName.setText(mAuth.getCurrentUser().getDisplayName());
                        }
                    }
                });
        changeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                saveChangeName.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        saveChangeName.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changeName.getEditableText().toString().trim().isEmpty()) {
                    saveChangeName.setError("Enter Name");
                } else {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("Name", changeName.getEditableText().toString().trim());
                    firebaseReference.documentReference.collection("Name").document("Name").set(doc);
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                }
                View viewKey = getActivity().getCurrentFocus();
                if (viewKey != null){
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
            }
        });
        return view;
    }

    private void showColorChoicePallete() {
        final ColorPicker colorPicker = new ColorPicker(getActivity());
        ArrayList<String> colors = new ArrayList<>();
        colors.add("#ffa931"); //yellow
        colors.add("#2c786c"); //green
        colors.add("#e65f05"); // orange
        colors.add("#ed0c0c"); //red
        colors.add("#000000"); // Black

        colorPicker
                .setColors(colors)
                .setColumns(5)
                .setRoundColorButton(true)
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        if (color == 0) {

                        } else {
                            saveThemeColor(color);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                }).show();
    }

    public void saveThemeColor(int color) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THEME_COLOR, color);
        editor.apply();
        gradientDrawable.setColor(color);
        Objects.requireNonNull(getActivity()).recreate();
    }

    public void loadThemeColor() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        themeColor = sharedPreferences.getInt(THEME_COLOR, 0);
    }

    public void updateTheme() {
        if (themeColor != 0) {
            gradientDrawable.setColor(themeColor);
        }
    }
}