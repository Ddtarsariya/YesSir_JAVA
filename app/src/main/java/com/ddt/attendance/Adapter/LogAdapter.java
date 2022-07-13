package com.ddt.attendance.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.Fragment.Class_log;
import com.ddt.attendance.Holder.LogHolder;
import com.ddt.attendance.Model.LogModel;
import com.ddt.attendance.R;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogHolder> {

    Class_log class_log;
    List<LogModel> modelList;

    public LogAdapter(Class_log class_log, List<LogModel> modelList) {
        this.class_log = class_log;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_student_log, parent, false);
        LogHolder logHolder = new LogHolder(view);

        return logHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LogHolder holder, int position) {
        holder.studentName.setText(modelList.get(position).getStudentName());
        holder.studentDisplayName.setText(modelList.get(position).getStudentDisplayName());
        holder.studentEmail.setText(modelList.get(position).getStudentEmail());
        holder.date.setText(modelList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
