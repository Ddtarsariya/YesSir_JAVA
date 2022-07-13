package com.ddt.attendance.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.ddt.attendance.Activity.MainScreenActivity.fragmentManager;

public class Report_Fragment extends Fragment {

    private RadioButton studentAttendanceRadio;
    private RadioGroup change_student_RadioGroup;

    private final FirebaseReference firebaseReference = new FirebaseReference();
    private Context context;
    private Fragment fragment;

    private LinearLayout linearLayout;
    private TableLayout tableLayout;

    private TableRow topRow;

    private TextView idTextView;
    private TextView totalPresent;
    private TextView totalAbsent;
    private TextView totalPercent;

    private TextView dateTextView;
    private TextView NameTextView;

    private TextView AttendanceTextView;
    private TextView DataPresentTextView;
    private TextView DataAbsentTextView;
    private TextView TotalPercent;

    private static final String CLASS_ID = "classid";
    private static final String LAST_INDEX = "last";
    private static final String CLASS_NAME = "classname";

    private String ClassID;
    private String ClassName;
    private String LastIndex;
    private ProgressBar progressBar;

    StringBuilder data = new StringBuilder();
    private int StudentLastIndex;

    public Report_Fragment() {
        // Required empty public constructor
    }


    public static Report_Fragment newInstance(int lastIndex, String classID, String className) {
        Report_Fragment fragment = new Report_Fragment();
        Bundle args = new Bundle();
        args.putInt(LAST_INDEX, lastIndex);
        args.putString(CLASS_ID, classID);
        args.putString(CLASS_NAME, className);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        context = container.getContext();
        Objects.requireNonNull(getActivity()).setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        getActivity().setTitle("Report");
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        fragment = this;

        linearLayout = view.findViewById(R.id.layout_lin);
        TextView subjectName = view.findViewById(R.id.Student_Report_Subject_Name);
        FloatingActionButton exportData = view.findViewById(R.id.Export_Report);
        progressBar = view.findViewById(R.id.report_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        exportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save();
            }
        });

        tableLayout = new TableLayout(getContext());

        topRow = new TableRow(context);

        idTextView = new TextView(context);
        NameTextView = new TextView(context);
        totalPresent = new TextView(context);
        totalAbsent = new TextView(context);
        totalPercent = new TextView(context);


        if (getArguments() != null) {
            LastIndex = String.valueOf(getArguments().getInt(LAST_INDEX));
            ClassID = getArguments().getString(CLASS_ID);
            ClassName = getArguments().getString(CLASS_NAME);
            tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
        subjectName.setText(ClassName);
        getTopRow();
        return view;
    }

    private void getTopRow() {
        idTextView.setText("No.");
        NameTextView.setText("Name");
        data.append("No,Name");

        idTextView.setBackground(context.getDrawable(R.drawable.table_menu));
        idTextView.setTextColor(context.getColor(R.color.colorwhite));

        NameTextView.setBackground(context.getDrawable(R.drawable.table_menu));
        NameTextView.setTextColor(context.getColor(R.color.colorwhite));

        idTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        NameTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        topRow.addView(idTextView);
        topRow.addView(NameTextView);
        totalPresent.setText("Total Present");
        totalPresent.setBackground(context.getDrawable(R.drawable.table_menu));
        totalPresent.setTextColor(context.getColor(R.color.colorwhite));
        totalPresent.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        totalAbsent.setText("Total Absent");
        totalAbsent.setBackground(context.getDrawable(R.drawable.table_menu));
        totalAbsent.setTextColor(context.getColor(R.color.colorwhite));
        totalAbsent.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        totalPercent.setText("Attendance %");
        totalPercent.setBackground(context.getDrawable(R.drawable.table_menu));
        totalPercent.setTextColor(context.getColor(R.color.colorwhite));
        totalPercent.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        firebaseReference.collectionReference.document(ClassID).collection("Students")
                .document(LastIndex).collection("Attendance").orderBy("Date")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                data.append(",").append(document.getString("Date"));

                                dateTextView = new TextView(context);

                                dateTextView.setText(document.getString("Date"));

                                dateTextView.setBackground(context.getDrawable(R.drawable.table_menu));
                                dateTextView.setTextColor(context.getColor(R.color.colorwhite));

                                dateTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                                topRow.addView(dateTextView);
                            }
                            topRow.addView(totalPresent);
                            topRow.addView(totalAbsent);
                            topRow.addView(totalPercent);
                            tableLayout.addView(topRow);
                            displayData(ClassID);
                        }

                    }
                });

    }

    private void displayData(final String classId) {
        data.append(",").append("Total Present,Total Absent,Attendance(%)").append("\n");
        firebaseReference.documentReference.collection("Classes").document(classId).collection("Students")
                .orderBy("Id", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                final String id = String.valueOf(document.getLong("Id").intValue());
                                showData(document.getId(), document.getString("Student Name"));
                                setData(id, document.getString("Student Name"));
                            }
                            linearLayout.addView(tableLayout);
                            /*Animation logoMoveAnimation3 = AnimationUtils.loadAnimation(context, R.anim.push_out_right);
                            linearLayout.startAnimation(logoMoveAnimation3);*/
                        }

                    }
                });
    }

    private void setData(final String id, final String name) {
        firebaseReference.collectionReference.document(ClassID).collection("Students")
                .document(id).collection("Attendance")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        data.append(id).append(",").append(name);
                        int counterPresent = 0;
                        int counterAbsent = 0;

                        for (DocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {

                            if (snapshot.getString("Attendance") != null) {
                                data.append(",").append("P");
                                counterPresent++;
                            } else {
                                data.append(",").append("A");
                                counterAbsent++;
                            }
                        }
                        data.append(",").append(counterPresent);

                        data.append(",").append(counterAbsent);

                        int total = counterPresent + counterAbsent;
                        float percent = (float) (counterPresent * 100) / total;
                        data.append(",").append(percent).append("%").append("\n");
                    }
                });

    }

    private void showData(final Object id, Object student_name) {
        String StudentID = String.valueOf(id);
        String StudentName = String.valueOf(student_name);

        TableRow Datarow = new TableRow(context);
        TextView NoDataTextView = new TextView(context);
        TextView DataNameTextView = new TextView(context);

        NoDataTextView.setText(StudentID);
        DataNameTextView.setText(StudentName);

        NoDataTextView.setBackground(context.getDrawable(R.drawable.table_row));
        NoDataTextView.setTextColor(context.getColor(R.color.colorwhite));

        DataNameTextView.setBackground(context.getDrawable(R.drawable.table_row));
        DataNameTextView.setTextColor(context.getColor(R.color.colorwhite));

        NoDataTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        DataNameTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        Datarow.addView(NoDataTextView);

        Datarow.addView(DataNameTextView);
        setAttendance(StudentID, Datarow);
        tableLayout.addView(Datarow);
    }

    private void setAttendance(final String StudentID, final TableRow Datarow) {
        try {
            firebaseReference.collectionReference.document(ClassID).collection("Students")
                    .document(StudentID).collection("Attendance")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int counterPresent = 0;
                                int counterAbsent = 0;

                                for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {

                                    AttendanceTextView = new TextView(context);

                                    if (snapshot.getString("Attendance") != null) {
                                        AttendanceTextView.setText(snapshot.getString("Attendance"));
                                        AttendanceTextView.setBackground(context.getDrawable(R.drawable.table_row));
                                        AttendanceTextView.setTextColor(context.getColor(R.color.colorwhite));
                                        counterPresent++;
                                    } else {
                                        AttendanceTextView.setText("A");
                                        AttendanceTextView.setBackground(context.getDrawable(R.drawable.table_rowabsent));
                                        AttendanceTextView.setTextColor(context.getColor(R.color.colorwhite));
                                        counterAbsent++;
                                    }
                                    AttendanceTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                    Datarow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                    Datarow.addView(AttendanceTextView);
                                }

                                DataPresentTextView = new TextView(context);
                                DataPresentTextView.setText(String.valueOf(counterPresent));
                                DataPresentTextView.setBackground(context.getDrawable(R.drawable.table_row));
                                DataPresentTextView.setTextColor(context.getColor(R.color.colorwhite));
                                DataPresentTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                Datarow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                Datarow.addView(DataPresentTextView);

                                DataAbsentTextView = new TextView(context);
                                DataAbsentTextView.setText(String.valueOf(counterAbsent));
                                DataAbsentTextView.setBackground(context.getDrawable(R.drawable.table_row));
                                DataAbsentTextView.setTextColor(context.getColor(R.color.colorwhite));
                                DataAbsentTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                Datarow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                Datarow.addView(DataAbsentTextView);

                                int total = counterPresent + counterAbsent;
                                float percent = (float) (counterPresent * 100) / total;
                                TotalPercent = new TextView(context);
                                try {
                                    float finalPercent = BigDecimal.valueOf(percent).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                                    TotalPercent.setText(finalPercent + "%");
                                } catch (Exception e) {
                                    TotalPercent.setText(percent + "%");
                                }

                                TotalPercent.setBackground(context.getDrawable(R.drawable.table_row));
                                TotalPercent.setTextColor(context.getColor(R.color.colorwhite));
                                TotalPercent.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                Datarow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                Datarow.addView(TotalPercent);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressBar.setVisibility(View.GONE);
    }

    private void Save() {
        try {
            //String savePath = Environment.getExternalStorageDirectory().getPath() + "/Attendance/Class/";
            final String savePath = Environment.getExternalStorageDirectory().getPath() + "/Attendance" + "/Class" + "/" + ClassName + "/";
            File file = new File(savePath);
            if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    file.mkdirs();
                    File path = new File(file, ClassName + ".csv");
                    FileOutputStream outputStream = new FileOutputStream(path);

                    outputStream.write(data.toString().getBytes());
                    System.out.println(data);
                    outputStream.close();
                    Toast.makeText(context, "Saved " + savePath, Toast.LENGTH_LONG).show();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Not saved", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.report_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_Attendance:
                openChangeAttendanceDialogue();
                break;
            case R.id.delete_Attendance:
                deleteDatabyDate();
                break;
        }
        return true;
    }

    private void deleteDatabyDate() {
        final SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        final Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                alertDialogueOfDeleteDataByDate(df.format(newDate.getTime()));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void alertDialogueOfDeleteDataByDate(final String date) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.logocirclewhite);
        builder.setTitle("Sure to Delete ?");
        builder.setCancelable(false);
        builder.setMessage("All attendance of this date will be deleted. Once deleted, can't be recovered back!");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkDateIsAvailable(date);
            }
        });
        builder.create().show();
    }

    private int getLastIndexOfStudent() {
        firebaseReference.documentReference.collection("Classes").document(ClassID)
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

    private void checkDateIsAvailable(final String date) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(String.valueOf(getLastIndexOfStudent()))
                .collection("Attendance").document(date).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            getAllStudentWithDate(date);
                        } else {
                            Toast.makeText(context, "Date not found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getAllStudentWithDate(final String date) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                firebaseReference.collectionReference.document(ClassID).collection("Students").document(snapshot.getId())
                                        .collection("Attendance").document(date).delete();
                            }
                        }
                        fragmentManager.beginTransaction().detach(fragment).attach(fragment).commit();
                        Toast.makeText(context, "Deleted..", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openChangeAttendanceDialogue() {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.change_attendance);
        final TextInputEditText id = dialog.findViewById(R.id.change_Student_attendance_Id);
        final Button pickDate, cancel, change;
        final TextView error, showPickedDate;
        pickDate = dialog.findViewById(R.id.change_student_attendance_datePicker);
        cancel = dialog.findViewById(R.id.Cancel_change);
        change_student_RadioGroup = dialog.findViewById(R.id.change_student_attendance_radioGroup);

        change = dialog.findViewById(R.id.change_student_attendance);
        error = dialog.findViewById(R.id.change_student_attendance_error);
        showPickedDate = dialog.findViewById(R.id.change_student_attendance_showDate);

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                final Calendar newCalendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        showPickedDate.setText(df.format(newDate.getTime()));
                    }
                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radioId = change_student_RadioGroup.getCheckedRadioButtonId();
                studentAttendanceRadio = dialog.findViewById(radioId);
                if (id.getEditableText().toString().trim().isEmpty() || showPickedDate.equals("")) {
                    error.setText("Id is empty or Date has not chosen!");
                } else {
                    checkThisStudentAvailable(error, id.getEditableText().toString().trim(), showPickedDate.getText().toString(), studentAttendanceRadio.getText(), dialog);
                }
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.create();
        dialog.show();
    }

    private void checkThisStudentAvailable(final TextView error, final String studentId, final String date, final CharSequence attendance, final Dialog dialog) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(studentId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            changeNewStudentAttendance(studentId, date, attendance, dialog);
                        } else {
                            error.setText("This student not available in class!");
                        }
                    }
                });
    }

    private void changeNewStudentAttendance(final String studentId, final String date, final CharSequence attendance, final Dialog dialog) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(studentId)
                .collection("Attendance").document(date).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (attendance.equals("A")) {
                                Map<String, Object> doc = new HashMap<>();
                                doc.put("Date", date);
                                firebaseReference.collectionReference.document(ClassID).collection("Students").document(studentId)
                                        .collection("Attendance").document(date).set(doc);
                            } else {
                                Map<String, Object> doc = new HashMap<>();
                                doc.put("Date", date);
                                doc.put("Attendance", "P");
                                firebaseReference.collectionReference.document(ClassID).collection("Students").document(studentId)
                                        .collection("Attendance").document(date).set(doc);
                            }
                            Toast.makeText(context, "Attendance changed successfully.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            fragmentManager.beginTransaction().detach(fragment).attach(fragment).commit();
                        } else {
                            Toast.makeText(context, "Date not found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}