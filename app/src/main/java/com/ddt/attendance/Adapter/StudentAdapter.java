package com.ddt.attendance.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Fragment.Show_Student;
import com.ddt.attendance.Holder.StudentHolder;
import com.ddt.attendance.Model.StudentModel;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentAdapter extends RecyclerView.Adapter<StudentHolder> implements Filterable {

    List<StudentModel> modelList;
    List<StudentModel> modelListIsFull;
    Show_Student show_student;
    private FirebaseReference firebaseReference = new FirebaseReference();

    public StudentAdapter(Show_Student show_student, List<StudentModel> modelList) {
        this.show_student = show_student;
        this.modelList = modelList;
        modelListIsFull = new ArrayList<>(modelList);
    }

    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_student_list, parent, false);

        StudentHolder studentHolder = new StudentHolder(view);
        studentHolder.setOnTextChangeListener(new StudentHolder.TextChangeListener() {
            @Override
            public void onTextChange(int position, CharSequence charSequence) {
                show_student.saveAddedStudenta(position, charSequence);
            }

            @Override
            public void onQrClick(int position) {
                show_student.showQrCode(position);
            }

            @Override
            public void onExpandClick(final int position) {
                firebaseReference.documentReference.collection("Version").document("Pro")
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                String version = snapshot.getString("version");
                                if (version.contains("Pro")) {
                                    StudentModel model = modelList.get(position);
                                    model.setExpanded(!model.isExpanded());
                                    notifyItemChanged(position);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                });
            }

            @Override
            public void onNumberTextChange(int position, Editable editable) {
                show_student.setNumberOfStudent(position,editable);
            }

            @Override
            public void onNotesTextChange(int position, Editable editable) {
                show_student.setNotesOfStudent(position,editable);
            }
        });
        return studentHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StudentHolder holder, int position) {
        holder.studentName.setText(modelList.get(position).getStudentName());
        holder.counter.setText(String.valueOf(modelList.get(position).getId()));
        if ("".equals(modelList.get(position).getNumber())){
            holder.studentNumber.setText("");
        } else {
            holder.studentNumber.setText(String.valueOf(modelList.get(position).getNumber()));
        }
        if ("".equals(modelList.get(position).getNumber())){
            holder.studentNotes.setText("");
        } else {
            holder.studentNotes.setText(String.valueOf(modelList.get(position).getNotes()));
        }
        boolean isExpanded = modelList.get(position).isExpanded();
        holder.ExpandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    @Override
    public Filter getFilter() {
        return modelFilter;
    }

    private Filter modelFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<StudentModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(modelListIsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (StudentModel item : modelListIsFull) {
                    if (item.getStudentName().toLowerCase().contains(filterPattern) || String.valueOf(item.getId()).contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            modelList.clear();
            modelList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
