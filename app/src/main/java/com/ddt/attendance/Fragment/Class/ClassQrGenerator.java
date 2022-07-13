package com.ddt.attendance.Fragment.Class;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ddt.attendance.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class ClassQrGenerator extends Fragment {

    private TextView classSubject_name;
    private TextView classQrWhich;
    private ImageView class_qr_image;
    private Button shareButton;
    private Button uniqueButton;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;
    Context context;
    private static final int MY_STORAGE_REQUEST_CODE = 100;
    private int mHour, mMinute;
    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;
    private  String expiredTime;

    private static final String CLASS_ID = "param1";
    private static final String SUBJECT_NAME = "param2";

    private String ClassId;
    private String SubjectName;

    public ClassQrGenerator() {
        // Required empty public constructor
    }


    public static ClassQrGenerator newInstance(String classId, String subjectName) {
        ClassQrGenerator fragment = new ClassQrGenerator();
        Bundle args = new Bundle();
        args.putString(CLASS_ID, classId);
        args.putString(SUBJECT_NAME, subjectName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_qr_generator, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle("Class QR");
        email = mAuth.getCurrentUser().getEmail();
        classSubject_name = view.findViewById(R.id.calssQr_SubjectName);
        classQrWhich = view.findViewById(R.id.classQr_Which);
        class_qr_image = view.findViewById(R.id.calssQr_Qrview);
        class_qr_image = view.findViewById(R.id.calssQr_Qrview);
        shareButton = view.findViewById(R.id.calssQr_Share);
        uniqueButton = view.findViewById(R.id.calssQr_Unique);
        context = container.getContext();
        if (getArguments() != null) {
            ClassId = getArguments().getString(CLASS_ID);
            SubjectName = getArguments().getString(SUBJECT_NAME);
            classSubject_name.setText(SubjectName);
        }
        generateShareClassQr();
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classQrWhich.setText("Share");
                generateShareClassQr();
                shareButton.setEnabled(false);
                uniqueButton.setEnabled(true);
            }
        });
        uniqueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                final String currentDate = df.format(date);
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                shareButton.setEnabled(true);
                                uniqueButton.setEnabled(false);
                                String time = hourOfDay + ":" + minute;
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                                    Date date = sdf.parse(time);
                                    expiredTime = new SimpleDateFormat("K:mm a").format(date);
                                    classQrWhich.setText("Unique" + "\n" + "Expired time" + "(" + expiredTime + ")" + "\n" + "Current Date :- " + currentDate);
                                } catch (Exception e) {
                                    classQrWhich.setText("Unique" + "\n" + "Expired time :- " + time);
                                }
                                String hr = String.valueOf(hourOfDay);
                                String min = String.valueOf(minute);
                                if (hr.length() == 1) {
                                    hr = "0" + hr;
                                }
                                if (min.length() == 1) {
                                    min = "0" + min;
                                }
                                generateUniqueClassQr(hr, min);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        uniqueButton.setEnabled(true);
                        shareButton.setEnabled(false);
                        classQrWhich.setText("Share");
                    }
                });
                timePickerDialog.show();
            }
        });
        return view;
    }

    private void generateShareClassQr() {
        String placer = email + "" + ClassId + "Share";
        byte[] encodeValue = Base64.encode(placer.getBytes(), Base64.DEFAULT);
        qrgEncoder = new QRGEncoder(new String(encodeValue), null, QRGContents.Type.TEXT, 700);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        // Getting QR-Code as Bitmap
        bitmap = qrgEncoder.getBitmap();
        // Setting Bitmap to ImageView
        shareButton.setEnabled(false);
        uniqueButton.setEnabled(true);
        class_qr_image.setImageBitmap(bitmap);
    }

    private void generateUniqueClassQr(String hrs, String min) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        final String currentDate = df.format(date);
        String placer = email + ClassId + hrs + ":" + min + currentDate + "Unique";
        byte[] encodeValue = Base64.encode(placer.getBytes(), Base64.DEFAULT);
        qrgEncoder = new QRGEncoder(new String(encodeValue), null, QRGContents.Type.TEXT, 700);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        // Getting QR-Code as Bitmap
        bitmap = qrgEncoder.getBitmap();
        Log.d("TAG", "generateUniqueClassQr: "+bitmap);
        // Setting Bitmap to ImageView
        class_qr_image.setImageBitmap(bitmap);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.class_qr_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save_Qr) {
            final String savePath = Environment.getExternalStorageDirectory().getPath() + "/Attendance" + "/Class" + "/" + SubjectName + "/";
            File file = new File(savePath);
            file.mkdir();
            if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    String imageName = (SubjectName + "( Expired At : " + expiredTime + ")");
                    boolean save = new QRGSaver().save(savePath, imageName, bitmap, QRGContents.ImageType.IMAGE_PNG);
                    String result = save ? savePath : "Image Not Saved";
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                    Log.d("TAG", "onOptionsItemSelected: "+save);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_REQUEST_CODE);
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "storage permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}