package com.ddt.attendance.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ddt.attendance.Activity.MainScreenActivity;
import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Others.GenericTextWatcher;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.ddt.attendance.Activity.MainScreenActivity.fragmentManager;

public class Add_Class extends Fragment {

    private TextInputLayout Add_class_Standard, Add_class_SubjectName, Add_class_Department, Add_class_Division;
    private EditText Add_class_StandardE, Add_class_SubjectNameE, Add_class_DepartmentE, Add_class_DivisionE;
    private EditText rollFromE, rollEndE;
    private TextInputLayout rollFrom, rollEnd;
    private TextView rollError;
    private RadioGroup Add_class_SelectType;
    private RadioButton Add_class_RadioButton;
    private FloatingActionButton save;
    private FirebaseReference firebaseReference = new FirebaseReference();
    private Show_Class show_class = new Show_Class();
    private View view;
    String SubjectType;

    private static final String CLASS_ID = "param1";

    private String ClassId;

    public Add_Class() {
    }

    public static Add_Class newInstance(String classId) {
        Add_Class fragment = new Add_Class();
        Bundle args = new Bundle();
        args.putString(CLASS_ID, classId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_class, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        Add_class_Standard = view.findViewById(R.id.Add_class_Standard);
        Add_class_SubjectName = view.findViewById(R.id.Add_class_SubjectName);
        Add_class_Department = view.findViewById(R.id.Add_class_Department);
        Add_class_Division = view.findViewById(R.id.Add_class_Division);
        Add_class_SelectType = view.findViewById(R.id.Add_class_SelectType);
        rollFrom = view.findViewById(R.id.Add_class_Roll_from);
        rollEnd = view.findViewById(R.id.Add_class_Roll_end);
        rollError = view.findViewById(R.id.rollError);

        save = view.findViewById(R.id.Add_class_save);

        Add_class_StandardE = view.findViewById(R.id.Add_class_StandardE);
        Add_class_SubjectNameE = view.findViewById(R.id.Add_class_SubjectNameE);
        Add_class_DepartmentE = view.findViewById(R.id.Add_class_DepartmentE);
        Add_class_DivisionE = view.findViewById(R.id.Add_class_DivisionE);
        rollFromE = view.findViewById(R.id.Add_class_Roll_fromE);
        rollEndE = view.findViewById(R.id.Add_class_Roll_endE);

        Add_class_StandardE.addTextChangedListener(new GenericTextWatcher(Add_class_Standard));
        Add_class_SubjectNameE.addTextChangedListener(new GenericTextWatcher(Add_class_SubjectName));
        Add_class_DepartmentE.addTextChangedListener(new GenericTextWatcher(Add_class_Department));
        Add_class_DivisionE.addTextChangedListener(new GenericTextWatcher(Add_class_Division));
        rollFromE.addTextChangedListener(new GenericTextWatcher(rollFrom));
        rollEndE.addTextChangedListener(new GenericTextWatcher(rollEnd));


        if (getArguments() != null) {
            getActivity().setTitle("Update Class");
            rollFrom.setEnabled(false);
            rollEnd.setEnabled(false);
            ClassId = getArguments().getString(CLASS_ID);
            update();
        } else {
            getActivity().setTitle("Add Class");
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ErrorCheck();
            }
        });

        return view;
    }


    private void update() {
        firebaseReference.collectionReference.document(ClassId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Add_class_Department.getEditText().setText(documentSnapshot.getString("Department"));
                        Add_class_SubjectName.getEditText().setText(documentSnapshot.getString("Subject Name"));
                        Add_class_Division.getEditText().setText(documentSnapshot.getString("Division"));
                        Add_class_Standard.getEditText().setText(documentSnapshot.getString("Standard"));
                        SubjectType = documentSnapshot.getString("Subject Type");
                        rollFrom.getEditText().setText(String.valueOf(documentSnapshot.getLong("FirstIndexOfStudent").intValue()));
                        rollEnd.getEditText().setText(String.valueOf(documentSnapshot.getLong("LastIndexOfStudent").intValue()));
                        switch (Objects.requireNonNull(SubjectType)) {
                            case "Lecture":
                                Add_class_SelectType.check(R.id.Add_class_radio_Lecture);
                                break;
                            case "Lab":
                                Add_class_SelectType.check(R.id.Add_class_radio_Lab);
                                break;
                            case "Exam":
                                Add_class_SelectType.check(R.id.Add_class_radio_Exam);
                                break;
                        }
                    }
                });
    }


    private void ErrorCheck() {
        String id = UUID.randomUUID().toString();
        String std = Add_class_Standard.getEditText().getText().toString().trim();
        String sub_name = Add_class_SubjectName.getEditText().getText().toString().trim();
        String department = Add_class_Department.getEditText().getText().toString().trim();
        String division = Add_class_Division.getEditText().getText().toString().trim();
        String classRollFrom = rollFrom.getEditText().getText().toString().trim();
        String classRollEnd = rollEnd.getEditText().getText().toString().trim();
        int radiioId = Add_class_SelectType.getCheckedRadioButtonId();
        Add_class_RadioButton = view.findViewById(radiioId);

        if (std.isEmpty()) {
            Add_class_Standard.setError("Enter Standard");
            Add_class_Standard.setFocusable(true);
        } else if (sub_name.isEmpty()) {
            Add_class_SubjectName.setError("Enter Subject Name");
        } else if (department.isEmpty()) {
            Add_class_Department.setError("Enter Department Name");
        } else if (division.isEmpty()) {
            Add_class_Division.setError("Enter Division");
        } else if (classRollFrom.isEmpty()) {
            rollFrom.setError("Enter Roll number");
        } else if (classRollEnd.isEmpty()) {
            rollEnd.setError("Enter Roll number");
        } else if (Integer.parseInt(rollFrom.getEditText().getText().toString().trim()) < 1) {
            rollFrom.setError("Enter Valid number");
        } else if (Integer.parseInt(rollFrom.getEditText().getText().toString().trim()) > 250) {
            rollError.setText("You can add upto 1 to 250 students!");
        } else if (Integer.parseInt(rollFrom.getEditText().getText().toString().trim()) > Integer.parseInt(rollEnd.getEditText().getText().toString().trim())) {
            rollFrom.setError("Please check number");
        } else {
            int rollf = Integer.parseInt(rollFrom.getEditText().getText().toString().trim());
            int rolle = Integer.parseInt(rollEnd.getEditText().getText().toString().trim());
            if (ClassId != null) {
                saveClass(ClassId, std, sub_name, department, division, 0, 0);
            } else {
                if (Integer.parseInt(rollEnd.getEditText().getText().toString().trim()) > 250) {
                    rollError.setText("You can add upto 1 to 250 students!");
                    return;
                }
                saveClass(id, std, sub_name, department, division, rollf, rolle);
            }

        }
    }

    private void saveClass(String id, String std, String sub_name, String department, String division, int rollF, int rollE) {
        View viewKey = getActivity().getCurrentFocus();
        if (viewKey != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
        if (rollF == 0 && rollE == 0) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("Id", id);
            doc.put("Standard", std);
            doc.put("Subject Name", sub_name);
            doc.put("Department", department);
            doc.put("Division", division);
            doc.put("Subject Type", Add_class_RadioButton.getText());
            firebaseReference.collectionReference.document(id).update(doc);
            Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "saveClass: 00");
            getActivity().getSupportFragmentManager().popBackStack();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new Show_Class(), null).commit();
            return;
        }
        Map<String, Object> docStudent = new HashMap<>();
        for (int i = rollF; i <= rollE; i++) {
            String uniueId = UUID.randomUUID().toString();
            docStudent.put("Id", i);
            docStudent.put("UniqueId", uniueId);
            docStudent.put("Student Name", "Student " + i);
            firebaseReference.documentReference.collection("Classes")
                    .document(id).collection("Students").document(String.valueOf(i))
                    .set(docStudent);
        }

        Map<String, Object> doc = new HashMap<>();
        doc.put("Id", id);
        doc.put("Standard", std);
        doc.put("Subject Name", sub_name);
        doc.put("Department", department);
        doc.put("Division", division);
        doc.put("Subject Type", Add_class_RadioButton.getText());
        doc.put("FirstIndexOfStudent", (long) rollF);
        doc.put("LastIndexOfStudent", (long) rollE);
        firebaseReference.collectionReference.document(id).set(doc);
        Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
        getActivity().getSupportFragmentManager().popBackStack();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new Show_Class(), null).commit();
    }


}