package com.ddt.attendance.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.Adapter.LogAdapter;
import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Model.LogModel;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Class_log extends Fragment {

    private RecyclerView logRecyclerview;
    private ProgressBar logProgressbar;
    private List<LogModel> modelList = new ArrayList<>();
    private LogAdapter adapter;
    private FirebaseReference firebaseReference = new FirebaseReference();

    private static final String CLASS_ID = "param1";

    private String classId;

    public Class_log() {
        // Required empty public constructor
    }

    public static Class_log newInstance(String classId) {
        Class_log fragment = new Class_log();
        Bundle args = new Bundle();
        args.putString(CLASS_ID, classId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_log, container, false);
        logRecyclerview = view.findViewById(R.id.Log_recyclerview);
        logProgressbar = view.findViewById(R.id.Log_progressbar);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
        getActivity().setTitle("Log");

        if (getArguments() != null) {
            classId = getArguments().getString(CLASS_ID);
        }
        showClassLog();
        return view;
    }

    private void showClassLog() {
        logProgressbar.setVisibility(View.VISIBLE);
        firebaseReference.collectionReference.document(classId).collection("Log")
                .orderBy("Date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                            LogModel logModel = new LogModel(
                                    doc.getString("Id"),
                                    doc.getString("Student Name"),
                                    doc.getString("Student Email"),
                                    doc.getString("Student DisplayName"),
                                    doc.getString("Date")
                            );
                            modelList.add(logModel);
                        }
                        logProgressbar.setVisibility(View.GONE);
                        adapter = new LogAdapter(Class_log.this, modelList);
                        logRecyclerview.setAdapter(adapter);
                    }
                });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                DeleteItemOnSwipe(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(logRecyclerview);
    }

    private void DeleteItemOnSwipe(final int adapterPosition) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to delete!");
        builder.setCancelable(false);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                adapter.notifyItemChanged(adapterPosition);
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteLog(adapterPosition);
            }
        });
        builder.show();
    }

    private void DeleteLog(int adapterPosition) {
        firebaseReference.collectionReference.document(classId).collection("Log")
                .document(modelList.get(adapterPosition).getId()).delete();
        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
        modelList.remove(adapterPosition);
        adapter.notifyItemRemoved(adapterPosition);
        adapter.notifyItemRangeChanged(adapterPosition,modelList.size());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.class_log_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_all_Log) {
            getAllLog();
        }
        return true;
    }

    private void getAllLog() {
        logRecyclerview.setVisibility(View.GONE);
        logProgressbar.setVisibility(View.VISIBLE);
        firebaseReference.collectionReference.document(classId).collection("Log").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty()) {
                            logRecyclerview.setVisibility(View.VISIBLE);
                            logProgressbar.setVisibility(View.GONE);
                            return;
                        }
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                if (snapshot.exists()) {
                                    deleteAllLog(snapshot.getId());
                                }
                            }
                        }
                    }
                });
    }

    private void deleteAllLog(String id) {
        firebaseReference.collectionReference.document(classId).collection("Log").document(id).delete();
        modelList.clear();
        showClassLog();
        logRecyclerview.setVisibility(View.VISIBLE);
        logProgressbar.setVisibility(View.GONE);
    }
}