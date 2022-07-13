package com.ddt.attendance.Holder;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.Model.StudentModel;
import com.ddt.attendance.R;
import com.google.android.material.textfield.TextInputEditText;

public class StudentHolder extends RecyclerView.ViewHolder {

    public TextView counter;
    public EditText studentName;
    public EditText studentNumber;
    public EditText studentNotes;
    public ImageView qrView;
    private StudentHolder.TextChangeListener textChangeListener;
    public LinearLayout ExpandedLayout;

    public StudentHolder(@NonNull View itemView) {
        super(itemView);
        counter = itemView.findViewById(R.id.Student_List_Counter);
        studentName = itemView.findViewById(R.id.Student_Name);
        studentNumber = itemView.findViewById(R.id.Student_Number);
        studentNotes = itemView.findViewById(R.id.Student_Notes);
        qrView = itemView.findViewById(R.id.Student_Qr);
        ExpandedLayout = itemView.findViewById(R.id.Expanded_Layout);

        counter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textChangeListener.onExpandClick(getAdapterPosition());
            }
        });

        studentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textChangeListener.onTextChange(getAdapterPosition(), charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        studentNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                textChangeListener.onNumberTextChange(getAdapterPosition(),editable);
            }
        });
        studentNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                textChangeListener.onNotesTextChange(getAdapterPosition(),editable);
            }
        });

        qrView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textChangeListener.onQrClick(getAdapterPosition());
            }
        });
    }

    public interface TextChangeListener {
        void onTextChange(int position, CharSequence charSequence);
        void onQrClick(int position);
        void onExpandClick(int position);
        void onNumberTextChange(int position,Editable editable);
        void onNotesTextChange(int position,Editable editable);
    }

    public void setOnTextChangeListener(StudentHolder.TextChangeListener listener) {
        textChangeListener = listener;
    }
}
