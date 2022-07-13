package com.ddt.attendance.Holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.R;

public class LogHolder extends RecyclerView.ViewHolder {

    public TextView studentName, studentDisplayName, studentEmail, date;

    public LogHolder(@NonNull View itemView) {
        super(itemView);
        studentName = itemView.findViewById(R.id.Log_which_student_attendance_present);
        studentDisplayName = itemView.findViewById(R.id.Log_student_name);
        studentEmail = itemView.findViewById(R.id.Log_student_email);
        date = itemView.findViewById(R.id.Log_date);
    }
}
