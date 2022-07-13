package com.ddt.attendance.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ddt.attendance.Activity.MainScreenActivity;
import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.MyService.MyService;
import com.ddt.attendance.Others.ConnectionChecker;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.ddt.attendance.Activity.MainScreenActivity.fragmentManager;


public class Scan_Fragment extends Fragment implements ZXingScannerView.ResultHandler {

    private static final String TAG = "Scan";
    private Context context;

    private FirebaseReference firebaseReference = new FirebaseReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private RelativeLayout rootLayout;

    private ZXingScannerView ScannerView;

    private ImageSwitcher button;
    private ImageView searching;
    private TextView doubleTapText;
    private ImageSwitcher foundOrNot;

    private String Username;
    private CountDownTimer countDownTimer;
    private long timeLeftMilliSec = 20000;
    private boolean timerRunning;


    private ConnectionChecker conCheck;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    Date date = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    String currentDate = df.format(date);

    public Scan_Fragment() {
        // Required empty public constructor
    }

    private static final String CLASS_ID = "classId";
    private static final String CLASS_NAME = "className";

    private String ClassID;

    public static Scan_Fragment newInstance(String param1, String param2) {
        Scan_Fragment fragment = new Scan_Fragment();
        Bundle args = new Bundle();
        args.putString(CLASS_ID, param1);
        args.putString(CLASS_NAME, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        ScannerView = view.findViewById(R.id.ScannerView);
        button = view.findViewById(R.id.Flash);
        doubleTapText = view.findViewById(R.id.DoubleTap_text);
        foundOrNot = view.findViewById(R.id.found_notfound);
        rootLayout = view.findViewById(R.id.DoubleTap);
        TextView currentDateTextView = view.findViewById(R.id.CurrentDate);
        searching = view.findViewById(R.id.searching_image);

        context = container.getContext();
        conCheck = new ConnectionChecker(context);

        currentDateTextView.setText(currentDate);

        startStop();

        firebaseReference.documentReference.collection("Name").document("Name").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot snapshot = task.getResult();
                        Username = snapshot.getString("Name");
                    }
                });

        if (getArguments() != null) {
            ClassID = getArguments().getString(CLASS_ID);
            String className = getArguments().getString(CLASS_NAME);
            getActivity().setTitle(className);
        } else {
            getActivity().setTitle("Scan");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ScannerView.getFlash()) {
                    ScannerView.setFlash(true);
                    button.setBackground(context.getDrawable(R.drawable.ic_baseline_flash_on_24));
                } else {
                    ScannerView.setFlash(false);
                    button.setBackground(context.getDrawable(R.drawable.ic_baseline_flash_off_24));
                }
            }
        });
        view.findViewById(R.id.DoubleTap).setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (doubleTapText.getVisibility() == View.VISIBLE) {
                        doubleTapText.setVisibility(View.GONE);
                        ResetTimer();
                        startTimer();
                    }
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        checkCameraPermission();
        return view;
    }


    @Override
    public void handleResult(Result rawResult) {
        //first decode this result
        if (conCheck.isConnectingToInternet()) {
            final String result = String.valueOf(rawResult);
            if (result.equals("")) {
                showFalseSign();
                return;
            }
            try {
                final byte[] decode = Base64.decode(result.getBytes(), Base64.DEFAULT);
                if (ClassID != null) {
                    searching.setVisibility(View.VISIBLE);
                    firebaseReference.collectionReference.document(ClassID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            int lastIndex = snapshot.getLong("LastIndexOfStudent").intValue();
                            CheckStudentInThisClass(new String(decode), lastIndex);
                        }
                    });
                } else {
                    CheckWhichQr(new String(decode));
                }
            } catch (Exception e) {
                showFalseSign();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("No internet connection");
            builder.setIcon(R.drawable.net_error);
            builder.setCancelable(false);
            builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopTimer();
                    ResetTimer();
                    startTimer();
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }

    }

    private void CheckWhichQr(final String result) {
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        final String currentDate = df.format(date);
        try {
            if ((result.contains("@gmail.com") && result.endsWith("Share"))) {
                checkProversion(result);
            } else if (result.contains("@gmail.com") && result.endsWith("Unique") && result.contains(currentDate)) {
                String qrResult = result;
                qrResult = qrResult.replace("Unique", "");
                qrResult = qrResult.trim();
                int index = qrResult.indexOf("@gmail.com");
                final String Email = qrResult.substring(0, index);
                String email = Email.concat("@gmail.com");
                int getEmailLastIndex = qrResult.lastIndexOf("@gmail.com");
                qrResult = qrResult.replace("@gmail.com", "");
                qrResult = qrResult.replace(currentDate, "");
                qrResult = qrResult.substring(getEmailLastIndex);
                int getLength = qrResult.length() - 5;
                String time = qrResult.substring(getLength, qrResult.length());
                String classId = qrResult.substring(0, getLength);
                if (currentTime.compareTo(time) <= 0) {
                    showTrueSign();
                    getAllStudentCheckStudentAvailable(email, classId);
                } else {
                    showFalseSign();
                    Toast.makeText(context, "Qr expired", Toast.LENGTH_SHORT).show();
                }
            } else {
                showFalseSign();
            }
        } catch (Exception e) {
            showFalseSign();
        }
    }


    private void getAllStudentCheckStudentAvailable(final String email, final String classId) {
        collectionReference.document(email).collection("Classes").document(classId).collection("Students")
                .orderBy("Id", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        searching.setVisibility(View.VISIBLE);
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            final int Id = snapshot.getLong("Id").intValue();
                            final String name = snapshot.getString("Student Name");
                            if (name.equals(Username)) {
                                Intent intent = new Intent(context, MyService.class);
                                intent.putExtra("ClassId", classId);
                                intent.putExtra("Email", email);
                                context.startService(intent);
                                checkthisStudentAttendance(email, classId, Id, name);
                                return;
                            }
                            checkThisStudentAvailableInclass(email, classId, Id, name);
                        }

                    }
                });
    }

    private void setThisStudentLog(String email, String classId, String name) {
        String uid = UUID.randomUUID().toString();
        Map<String, Object> doc = new HashMap<>();
        doc.put("Id", uid);
        doc.put("Date", currentDate);
        doc.put("Student DisplayName", mAuth.getCurrentUser().getDisplayName());
        doc.put("Student Email", mAuth.getCurrentUser().getEmail());
        doc.put("Student Name", Username);
        collectionReference.document(email).collection("Classes").document(classId).collection("Log").document(uid).set(doc);
    }

    private void checkThisStudentAvailableInclass(String email, String classId, final int Id, String name) {
        //Student not in the class
        collectionReference.document(email).collection("Classes").document(classId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot.getLong("LastIndexOfStudent").intValue() == Id) {
                            searching.setVisibility(View.GONE);
                            Snackbar snackbar = Snackbar.make(rootLayout, "You are not in the class", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    }
                });

    }

    private void checkthisStudentAttendance(final String email, final String classId, final int id, final String name) {
        try {
            collectionReference.document(email).collection("Classes").document(classId).collection("Students")
                    .document(String.valueOf(id)).collection("Attendance").document(currentDate).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();
                                try {
                                    searching.setVisibility(View.GONE);
                                    snapshot.getString("Attendance").equals("P");
                                    Toast toast = Toast.makeText(context, "Your presence has been confirmed once", Toast.LENGTH_LONG);
                                    View view = toast.getView();
                                    TextView text = (TextView) view.findViewById(android.R.id.message);
                                    text.setTextColor(Color.GREEN);
                                    toast.show();

                                } catch (Exception e) {
                                    searching.setVisibility(View.GONE);
                                    Toast toast = Toast.makeText(context, name + "", Toast.LENGTH_LONG);
                                    View view = toast.getView();
                                    TextView text = (TextView) view.findViewById(android.R.id.message);
                                    text.setTextColor(Color.GREEN);
                                    toast.show();
                                    Map<String, Object> doc = new HashMap<>();
                                    doc.put("Date", currentDate);
                                    doc.put("Attendance", "P");
                                    collectionReference.document(email).collection("Classes").document(classId).collection("Students")
                                            .document(String.valueOf(id)).collection("Attendance").document(currentDate).set(doc);
                                    setThisStudentLog(email, classId, name);
                                }
                            } else {
                                searching.setVisibility(View.GONE);
                                showFalseSign();
                            }
                        }
                    });
        } catch (Exception e) {
            searching.setVisibility(View.GONE);
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkProversion(final String result) {
        firebaseReference.documentReference.collection("Version").document("Pro")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    if (snapshot.getString("version").contains("Pro")) {
                        if ((result.contains("@gmail.com") && result.endsWith("Share"))) {
                            showTrueSign();
                            String qrResult = result;
                            qrResult = qrResult.replace("Share", "");
                            qrResult = qrResult.trim();
                            int index = qrResult.indexOf("@gmail.com");
                            final String Email = qrResult.substring(0, index);
                            int emailLastindex = qrResult.lastIndexOf("@gmail.com");
                            qrResult.replace("@gmail.com", "");
                            qrResult = qrResult.substring(emailLastindex);
                            final String ClassId = qrResult.replace("@gmail.com", "");
                            getAllClassToThisMobile(Email, ClassId);
                        } else if (!result.contains("@gmail.com") || !result.endsWith("Share")) {
                            showFalseSign();
                        }
                    }
                } else {
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING, 150);
                    stopTimer();
                    ResetTimer();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    builder.setMessage("You have a basic version,Upgrade to pro version to use this feature");
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            startTimer();
                            ScannerView.startCamera();
                        }
                    });
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, new Version_Upgrade_Fragment(), null).commit();
                        }
                    });
                    ScannerView.stopCamera();
                    builder.create().show();
                }

            }
        });
    }

    private void getAllClassToThisMobile(final String Email, final String ClassId) {
        firebaseReference.collectionReference.document(ClassId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Snackbar snackbar = Snackbar.make(rootLayout, "This Class is already in your device", Snackbar.LENGTH_LONG);
                        snackbar.setAction("Update", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setAllDocmumenttothis(Email, ClassId);
                            }
                        });
                        snackbar.show();
                    } else {
                        setAllDocmumenttothis(Email, ClassId);
                    }
                } else {
                    setAllDocmumenttothis(Email, ClassId);
                }
            }
        });
    }

    private void setAllDocmumenttothis(String email, final String classId) {
        String Email = email.concat("@gmail.com");
        try {
            db.collection("Users").document(Email).collection("Classes").document(classId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                String color = documentSnapshot.getString("Color");
                                String Department = documentSnapshot.getString("Department");
                                String Division = documentSnapshot.getString("Division");
                                int FirstIndexOfStudent = documentSnapshot.getLong("FirstIndexOfStudent").intValue();
                                int LastIndexOfStudent = documentSnapshot.getLong("LastIndexOfStudent").intValue();
                                String Standard = documentSnapshot.getString("Standard");
                                String SubjectName = documentSnapshot.getString("Subject Name");
                                String SubjectType = documentSnapshot.getString("Subject Type");
                                setClassToMyMobile(classId, color, Department, Division, FirstIndexOfStudent, LastIndexOfStudent,
                                        Standard, SubjectName, SubjectType);
                            }
                        }
                    });
            db.collection("Users").document(Email).collection("Classes").document(classId).collection("Students")
                    .orderBy("Id")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            int id = documentSnapshot.getLong("Id").intValue();
                            String name = documentSnapshot.getString("Student Name");
                            setStudent(classId, id, name);
                        }
                    }
                }
            });
        } catch (Exception e) {
            //showFalseSign();
            Toast.makeText(context, "Class Not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setStudent(String classId, int id, String name) {
        String uid = UUID.randomUUID().toString();
        Map<String, Object> doc = new HashMap<>();
        doc.put("Id", id);
        doc.put("Student Name", name);
        doc.put("UniqueId", uid);
        firebaseReference.collectionReference.document(classId).collection("Students")
                .document(String.valueOf(id)).set(doc);
    }

    private void setClassToMyMobile(String classID, String color, String department, String division, int firstIndexOfStudent,
                                    int lastIndexOfStudent, String standard, String subjectName, String subjectType) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("Color", color);
        doc.put("Department", department);
        doc.put("Division", division);
        doc.put("FirstIndexOfStudent", firstIndexOfStudent);
        doc.put("LastIndexOfStudent", lastIndexOfStudent);
        doc.put("Id", classID);
        doc.put("Standard", standard);
        doc.put("Subject Name", subjectName);
        doc.put("Subject Type", subjectType);
        firebaseReference.collectionReference.document(classID).set(doc);
    }

    private void CheckStudentInThisClass(final String Id, final int lastIndex) {
        try {
            firebaseReference.collectionReference.document(ClassID).collection("Students").orderBy("Id", Query.Direction.ASCENDING)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            if (snapshot.getString("UniqueId").equals(Id)) {
                                searching.setVisibility(View.GONE);
                                showTrueSign();
                                String getId = String.valueOf(snapshot.getLong("Id").intValue());
                                MarkAttendance(getId, String.valueOf(snapshot.get("Student Name")));
                                getAllDocumentsOfClass(snapshot.getId());
                                return;
                            } else {
                                ResetTimer();
                                if (lastIndex == snapshot.getLong("Id").intValue()) {
                                    searching.setVisibility(View.GONE);
                                    showFalseSign();
                                }
                            }
                        }
                    } else {
                        showFalseSign();
                    }
                }
            });
        } catch (Exception e) {
            searching.setVisibility(View.GONE);
            Toast.makeText(context, "Qr no valid", Toast.LENGTH_SHORT).show();
            showFalseSign();
        }
    }

    private void getAllDocumentsOfClass(String Id) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(Id).collection("Attendance").document(currentDate)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.d(TAG, "onSuccess: !! Already set ");
                            firebaseReference.collectionReference.document(ClassID)
                                    .collection("Students")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                                    SetPath(documentSnapshot.getId());
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SetPath(final String id) {
        firebaseReference.collectionReference.document(ClassID).collection("Students").document(id)
                .collection("Attendance").document(df.format(date)).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getString("Attendance") == null) {
                            Map<String, Object> doc = new HashMap<>();
                            doc.put("Date", currentDate);
                            firebaseReference.collectionReference.document(ClassID).collection("Students").document(id)
                                    .collection("Attendance").document(df.format(date)).set(doc);
                        }
                    }
                });
    }

    private void MarkAttendance(final String id, final String StudentName) {
        firebaseReference.documentReference.collection("Classes").document(ClassID)
                .collection("Students")
                .document(id)
                .collection("Attendance")
                .document(currentDate)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.getString("Attendance") != null) {
                        Toast toast = Toast.makeText(context, "Your presence has been confirmed once", Toast.LENGTH_LONG);
                        View view = toast.getView();
                        TextView text = (TextView) view.findViewById(android.R.id.message);
                        text.setTextColor(Color.GREEN);
                        toast.show();
                    } else {
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("Date", currentDate);
                        doc.put("Attendance", "P");
                        firebaseReference.collectionReference.document(ClassID)
                                .collection("Students")
                                .document(id)
                                .collection("Attendance")
                                .document(currentDate)
                                .set(doc);
                        Snackbar snackbar = Snackbar.make(rootLayout, StudentName, Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                } else {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("Date", currentDate);
                    doc.put("Attendance", "P");
                    firebaseReference.collectionReference.document(ClassID)
                            .collection("Students")
                            .document(id)
                            .collection("Attendance")
                            .document(currentDate)
                            .set(doc);
                    Snackbar snackbar = Snackbar.make(rootLayout, StudentName, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });
    }

    private void showTrueSign() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING, 150);
        foundOrNot.setVisibility(View.VISIBLE);
        foundOrNot.setBackground(context.getDrawable(R.drawable.ic_found));
        foundOrNot.postDelayed(new Runnable() {
            @Override
            public void run() {
                foundOrNot.setVisibility(View.GONE);
            }
        }, 600);
        stopTimer();
        ResetTimer();
        startTimer();
    }

    private void showFalseSign() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150);
        foundOrNot.setVisibility(View.VISIBLE);
        foundOrNot.setBackground(context.getDrawable(R.drawable.ic_notfound));
        foundOrNot.postDelayed(new Runnable() {
            @Override
            public void run() {
                foundOrNot.setVisibility(View.GONE);
            }
        }, 600);
        stopTimer();
        ResetTimer();
        startTimer();
    }

    private void checkCameraPermission() {
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getActivity().recreate();
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startStop() {
        if (timerRunning) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftMilliSec, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftMilliSec = l;
            }

            @Override
            public void onFinish() {
                ScannerView.stopCamera();
                doubleTapText.setVisibility(View.VISIBLE);
                timerRunning = false;
            }
        }.start();
        if (doubleTapText.getVisibility() == View.VISIBLE) {
            doubleTapText.setVisibility(View.GONE);
        }
        ScannerView.setResultHandler(this);
        timerRunning = true;
        ScannerView.startCamera();
    }

    private void ResetTimer() {
        timeLeftMilliSec = 20000;
    }

    private void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        ScannerView.stopCamera();
    }

}