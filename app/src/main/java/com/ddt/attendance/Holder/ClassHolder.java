package com.ddt.attendance.Holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.R;
import com.google.android.material.card.MaterialCardView;

public class ClassHolder extends RecyclerView.ViewHolder {

    public TextView SubjectName;
    public TextView SubjectType;
    public TextView Standard;
    public TextView Department;
    public TextView Division;
    public ImageView Scan;
    public ImageView Report;
    public ImageView StudentAdd;
    public ImageView ClassDelete;
    public ImageView ClasssEdit;
    public ImageView ClassColor;
    public ImageView ClassQr;
    public MaterialCardView classCard;

    public ClassHolder(@NonNull View itemView) {
        super(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mClickListener.onItemLongClick(view, getAdapterPosition());
                return true;
            }
        });

        SubjectName = itemView.findViewById(R.id.class_subject_name);
        SubjectType = itemView.findViewById(R.id.class_subject_type);
        Standard = itemView.findViewById(R.id.class_standard);
        Department = itemView.findViewById(R.id.class_department);
        Division = itemView.findViewById(R.id.class_division);
        Scan = itemView.findViewById(R.id.class_scan);
        Report = itemView.findViewById(R.id.class_report);
        StudentAdd = itemView.findViewById(R.id.class_student_add);
        ClasssEdit = itemView.findViewById(R.id.class_edit);
        ClassDelete = itemView.findViewById(R.id.class_delete);
        ClassColor = itemView.findViewById(R.id.class_color);
        ClassQr = itemView.findViewById(R.id.class_qr);
        classCard = itemView.findViewById(R.id.Class_Single_Card);

        Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onScanStudentClick(view, getAdapterPosition());
            }
        });
        Report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onReportStudentClick(view, getAdapterPosition());
            }
        });
        StudentAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onAddStudentClick(view, getAdapterPosition());
            }
        });
        ClasssEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClassEditClick(view, getAdapterPosition());
            }
        });
        ClassDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClassDeleteClick(view, getAdapterPosition());
            }
        });
        ClassColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClassColorClick(view, getAdapterPosition());
            }
        });
        ClassQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClassQrClick(view, getAdapterPosition());
            }
        });
    }

    private ClassHolder.ClickListener mClickListener;

    public interface ClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onScanStudentClick(View view, int position);

        void onReportStudentClick(View view, int position);

        void onAddStudentClick(View view, int position);

        void onClassEditClick(View view, int position);

        void onClassDeleteClick(View view, int position);

        void onClassColorClick(View view, int position);

        void onClassQrClick(View view, int position);
    }

    public void setOnClickListener(ClassHolder.ClickListener clickListener) {
        mClickListener = clickListener;
    }
}
