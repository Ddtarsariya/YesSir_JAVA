package com.ddt.attendance.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.Adapter.StudentAdapter;
import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Model.StudentModel;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

import static com.ddt.attendance.Activity.MainScreenActivity.fragmentManager;


public class Show_Student extends Fragment {

    private FloatingActionButton AddStudent;
    private ProgressBar showStudentProgress;
    private RecyclerView ShowStudentRecyclerview;
    private FirebaseReference firebaseReference = new FirebaseReference();
    public List<StudentModel> modelList = new ArrayList<>();
    private StudentAdapter studentAdapter;
    private int temp = 0;
    private LinearLayoutManager linearLayoutManager;
    private int StudentLastIndex = 0;
    private int StudentFirstIndex = 0;
    private static final String CLASS_ID = "param1";
    private static final String CLASS_NAME = "param2";
    private Context context;

    private String ClassID;
    private String ClassName;
    private Fragment fragment;

    public Show_Student() {
        // Required empty public constructor
    }

    public static Show_Student newInstance(String param1, String param2) {
        Show_Student fragment = new Show_Student();
        Bundle args = new Bundle();
        args.putString(CLASS_ID, param1);
        args.putString(CLASS_NAME, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_student, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
        fragment = this;
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        AddStudent = view.findViewById(R.id.show_students_Add);
        showStudentProgress = view.findViewById(R.id.show_students_Progressbar);
        ShowStudentRecyclerview = view.findViewById(R.id.show_students_Recyclerview);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.supportsPredictiveItemAnimations();
        ShowStudentRecyclerview.setLayoutManager(linearLayoutManager);
        ShowStudentRecyclerview.setHasFixedSize(true);
        studentAdapter = new StudentAdapter(Show_Student.this, modelList);
        ShowStudentRecyclerview.setAdapter(studentAdapter);
        context = container.getContext();
        ShowStudentRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && AddStudent.getVisibility() == View.VISIBLE) {
                    AddStudent.hide();
                } else if (dy < 0 && AddStudent.getVisibility() != View.VISIBLE) {
                    AddStudent.show();
                }
            }
        });
        if (getArguments() != null) {
            ClassID = getArguments().getString(CLASS_ID);
            ClassName = getArguments().getString(CLASS_NAME);
            getActivity().setTitle("Subject : " + ClassName);
        }
        showClassStudentsData();

        checkStudentEmptyOrnot();
        StudentLastIndex = getStudentLastIndex();
        AddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getStudentLastIndex() >= 250){
                    Toast.makeText(context,"Maximum limit is 250",Toast.LENGTH_LONG).show();
                    return;
                }
                linearLayoutManager.removeAllViews();
                modelList.clear();
                AddStudent.setEnabled(false);
                AddnewStudent();
                temp = 1;
            }
        });
        return view;
    }

    private void AddnewStudent() {
        checkStudentEmptyOrnot();
        String StudentUniqueId = UUID.randomUUID().toString();
        int LastIndex = getStudentLastIndex() + 1;
        Map<String, Object> doc = new HashMap<>();
        doc.put("Id", LastIndex);
        doc.put("Student Name", "Student " + LastIndex);
        doc.put("UniqueId", StudentUniqueId);
        firebaseReference.documentReference.collection("Classes").document((ClassID))
                .collection("Students").document(String.valueOf(LastIndex)).set(doc);

        setAllDateToNewStudent(getStudentLastIndex());
        firebaseReference.documentReference.collection("Classes").document((ClassID))
                .update("LastIndexOfStudent", LastIndex);
        studentAdapter.notifyDataSetChanged();
        showClassStudentsData();
    }

    private void setAllDateToNewStudent(final int studentLastIndex) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(String.valueOf(studentLastIndex))
                .collection("Attendance").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                            String date = snapshot.getString("Date");
                            Map<String, Object> doc = new HashMap<>();
                            doc.put("Date", date);
                            firebaseReference.documentReference.collection("Classes").document((ClassID))
                                    .collection("Students").document(String.valueOf(studentLastIndex + 1)).collection("Attendance")
                                    .document(date).set(doc);
                        }
                    }
                });
    }

    private void showClassStudentsData() {
        showStudentProgress.setVisibility(View.VISIBLE);
        firebaseReference.collectionReference.document((ClassID))
                .collection("Students").orderBy("Id", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        showStudentProgress.setVisibility(View.GONE);
                        for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                            StudentModel studentModel = new StudentModel(
                                    doc.getLong("Id").intValue(),
                                    doc.getString("UniqueId"),
                                    doc.getString("Student Name"),
                                    doc.getString("Number"),
                                    doc.getString("Note")
                            );
                            modelList.add(studentModel);
                        }
                        studentAdapter = new StudentAdapter(Show_Student.this, modelList);
                        ShowStudentRecyclerview.setAdapter(studentAdapter);
                        Animation logoMoveAnimation3 = AnimationUtils.loadAnimation(context, R.anim.push_out_left);
                        ShowStudentRecyclerview.startAnimation(logoMoveAnimation3);
                        if (temp == 1) {
                            linearLayoutManager.smoothScrollToPosition(
                                    ShowStudentRecyclerview, null, studentAdapter.getItemCount());
                        }
                    }
                });
        AddStudent.setEnabled(true);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                DeleteItemOnSwipe(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(ShowStudentRecyclerview);
    }

    private void checkStudentEmptyOrnot() {
        firebaseReference.documentReference.collection("Classes").document((ClassID)).collection("Students")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty()) {
                            StudentFirstIndex = 1;
                            StudentLastIndex = 0;
                            setStudentLastIndex(0);
                        }
                    }
                });
    }

    private int getStudentLastIndex() {
        firebaseReference.documentReference.collection("Classes").document((ClassID))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            StudentLastIndex = document.getLong("LastIndexOfStudent").intValue();
                        }
                    }
                });
        return StudentLastIndex;
    }

    private void setStudentLastIndex(int lastIndex) {
        firebaseReference.collectionReference.document((ClassID))
                .update("LastIndexOfStudent", lastIndex);
    }

    public void saveAddedStudenta(int position, CharSequence charSequence) {
        firebaseReference.documentReference.collection("Classes").document((ClassID)).collection("Students")
                .document(String.valueOf(modelList.get(position).getId()))
                .update("Student Name", String.valueOf(charSequence));
    }

    private void DeleteItemOnSwipe(final int adapterPosition) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to delete!");
        builder.setCancelable(false);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                studentAdapter.notifyItemChanged(adapterPosition);
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteStudent(adapterPosition);
            }
        });
        builder.show();
    }


    private void DeleteStudent(int adapterPosition) {
        int StudentID = modelList.get(adapterPosition).getId();
        int lastIndex = getStudentLastIndex();
        firebaseReference.collectionReference.document(ClassID).collection("Students")
                .document(String.valueOf(StudentID)).delete();
        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
        if (StudentID == lastIndex) {
            setStudentLastIndex(lastIndex - 1);
        }
        modelList.remove(adapterPosition);
        studentAdapter.notifyItemRemoved(adapterPosition);
        studentAdapter.notifyItemRangeChanged(adapterPosition,modelList.size());
        checkStudentEmptyOrnot();
    }

    public void showQrCode(int position) {
        final String savePath = Environment.getExternalStorageDirectory().getPath() + "/Attendance" + "/" + ClassName  + "/QrCode/" ;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.single_qr_view);
        final ImageView qrImageView = dialog.findViewById(R.id.show_Student_QR);
        final TextView studentName = dialog.findViewById(R.id.show_qr_Student_name);
        final TextView studentId = dialog.findViewById(R.id.show_qr_Student_id);
        Button download = dialog.findViewById(R.id.show_qr_download);

        studentName.setText(modelList.get(position).getStudentName());
        studentId.setText(String.valueOf(modelList.get(position).getId()));
        final String u = modelList.get(position).getUniqueId();
        final byte[] encodeValue = Base64.encode(u.getBytes(), Base64.DEFAULT);
        QRGEncoder qrgEncoder = new QRGEncoder(new String(encodeValue), null, QRGContents.Type.TEXT, 650);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        // Getting QR-Code as Bitmap
        final Bitmap bitmap = qrgEncoder.getBitmap();
        // Setting Bitmap to ImageView
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        File file = new File(savePath);
                        file.mkdirs();
                        dialog.dismiss();
                        boolean save = new QRGSaver().save(savePath, studentName.getText().toString(), bitmap, QRGContents.ImageType.IMAGE_PNG);
                        String result = save ? savePath : "Image Not Saved";
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        });
        qrImageView.setImageBitmap(bitmap);

        dialog.create();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ResetPointer:
                resetStudentLastIndex();
                break;
            case R.id.AddStud:
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.add_student_dialogue);
                Button cancel = dialog.findViewById(R.id.Cancel_new_student);
                Button save = dialog.findViewById(R.id.Save_new_student);
                final TextInputEditText id = dialog.findViewById(R.id.Add_Student_Id);
                final TextInputEditText Name = dialog.findViewById(R.id.Add_Student_Name);
                final TextView error = dialog.findViewById(R.id.Add_student_error);
                id.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        error.setText("");
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                Name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        error.setText("");
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (id.getEditableText().toString().trim().equals("") || Name.getEditableText().toString().equals("")) {
                            error.setText("Field can't be empty");
                        } else if (Integer.valueOf(id.getEditableText().toString().trim()) > 250){
                            error.setText("You can add upto 250 students");
                        } else{
                            CompareStudent(id.getEditableText().toString().trim(), Name.getEditableText().toString().trim(), error, dialog);
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.create();
                Window window = dialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();
                break;
            case R.id.student_search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setQueryHint("Type here to search");
                searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        studentAdapter.getFilter().filter(newText);
                        return true;
                    }
                });
                searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        showClassStudentsData();
                        return false;
                    }
                });
                break;
        }
        return true;
    }

    private void resetStudentLastIndex() {
        firebaseReference.collectionReference.document(ClassID).collection("Students").orderBy("Id", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int reseter = 0;
                    for (DocumentSnapshot snapshot : task.getResult()) {
                        int id = Integer.parseInt(snapshot.getId());
                        reseter = id;
                    }
                    setStudentLastIndex(reseter);
                    modelList.clear();
                    fragmentManager.beginTransaction().detach(fragment).attach(fragment).commit();
                    Toast.makeText(context, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void CompareStudent(final String id, final String name, final TextView error, final Dialog dialog) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            if (snapshot.exists()) {
                                if (snapshot.getId().equals(id)) {
                                    error.setText("This student already in class");
                                    return;
                                } else if (snapshot.getId().equals(String.valueOf(getStudentLastIndex()))) {
                                    setNewStudent(id, name, dialog);
                                }
                            }
                        }
                    }
                });
    }

    private void setNewStudent(final String id, String name, Dialog dialog) {
        String uid = UUID.randomUUID().toString();
        Map<String, Object> doc = new HashMap<>();
        doc.put("Id", Integer.valueOf(id));
        doc.put("UniqueId", uid);
        doc.put("Student Name", name);
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(id)
                .set(doc);
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(String.valueOf(getStudentLastIndex()))
                .collection("Attendance").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                            String date = snapshot.getString("Date");
                            Map<String, Object> doc = new HashMap<>();
                            doc.put("Date", date);
                            firebaseReference.documentReference.collection("Classes").document((ClassID))
                                    .collection("Students").document(id).collection("Attendance")
                                    .document(date).set(doc);
                        }
                    }
                });
        if (Integer.parseInt(id) > StudentLastIndex) {
            setStudentLastIndex(Integer.parseInt(id));
        }
        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        modelList.clear();
        showClassStudentsData();
    }

    public void setNumberOfStudent(int position, Editable editable) {
        Map<String,Object> doc = new HashMap<>();
        doc.put("Number",editable.toString());
        firebaseReference.documentReference.collection("Classes").document((ClassID)).collection("Students")
                .document(String.valueOf(modelList.get(position).getId())).update(doc);
    }

    public void setNotesOfStudent(int position, Editable editable) {
        Map<String,Object> doc = new HashMap<>();
        doc.put("Note",editable.toString());
        firebaseReference.documentReference.collection("Classes").document((ClassID)).collection("Students")
                .document(String.valueOf(modelList.get(position).getId())).update(doc);
    }
}