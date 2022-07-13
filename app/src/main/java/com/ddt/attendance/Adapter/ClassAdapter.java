package com.ddt.attendance.Adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Fragment.Show_Class;
import com.ddt.attendance.Holder.ClassHolder;
import com.ddt.attendance.Model.ClassModel;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassHolder> {

    List<ClassModel> modelList;
    Show_Class show_class;
    private FirebaseReference firebaseReference = new FirebaseReference();

    public ClassAdapter(Show_Class show_class, List<ClassModel> modelList) {
        this.show_class = show_class;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ClassHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_class_layout, parent, false);
        ClassHolder classHolder = new ClassHolder(view);
        classHolder.setOnClickListener(new ClassHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
                show_class.onLongClickShowLog(position);
            }

            @Override
            public void onScanStudentClick(View view, int position) {
                show_class.ScanStudentClick(position);
            }

            @Override
            public void onReportStudentClick(View view, int position) {
                show_class.ShowStudentReport(position);
            }

            @Override
            public void onAddStudentClick(View view, int position) {
                show_class.AddShowStudentInClass(position);
            }

            @Override
            public void onClassEditClick(View view, int position) {
                show_class.ClassUpdate(position);
            }

            @Override
            public void onClassDeleteClick(View view, int position) {
                show_class.ClassDelete(position);
            }

            @Override
            public void onClassColorClick(View view, int position) {
                show_class.ClassColor(position);
            }

            @Override
            public void onClassQrClick(View view, int position) {
                show_class.ClassQr(position);
            }

        });
        return classHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ClassHolder holder, int position) {
        holder.SubjectName.setText(modelList.get(position).getSubjectName().toUpperCase());
        holder.SubjectType.setText(modelList.get(position).getSubjectType());
        holder.Standard.setText(modelList.get(position).getStandard());
        holder.Department.setText(modelList.get(position).getDepartment());
        holder.Division.setText(modelList.get(position).getDivision());
        try {
            firebaseReference.collectionReference.document(modelList.get(position).getId()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                try {
                                    String color = documentSnapshot.getString("Color");
                                    int colorr = Integer.parseInt(color);
                                    holder.classCard.setCardBackgroundColor(colorr);
                                } catch (Exception e) {
                                    holder.classCard.setCardBackgroundColor(Color.BLACK);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            holder.classCard.setCardBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
