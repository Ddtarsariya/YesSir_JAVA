package com.ddt.attendance.MyService;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.ddt.attendance.MyService.App.CHANNEL_ID;

public class MyService extends Service {

    private static final String TAG = "MyService";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private int lastIndex;
    Date date = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    String currentDate = df.format(date);

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MyService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        final String classId = intent.getStringExtra("ClassId");
        final String email = intent.getStringExtra("Email");
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Yes Sir")
                .setContentText("work is in progress...")
                .setSmallIcon(R.drawable.logofinal)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        collectionReference.document(email).collection("Classes").document(classId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                lastIndex = documentSnapshot.getLong("LastIndexOfStudent").intValue();
                collectionReference.document(email).collection("Classes").document(classId).collection("Students").document(lastIndex + "".trim())
                        .collection("Attendance").document(currentDate).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    StopServices();
                                } else {
                                    collectionReference.document(email).collection("Classes").document(classId).collection("Students")
                                            .orderBy("Id", Query.Direction.ASCENDING)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (DocumentSnapshot snapshot : task.getResult()) {
                                                        final int Id = snapshot.getLong("Id").intValue();
                                                        String name = snapshot.getString("Student Name");
                                                        setAllOtherStudentAttendanceThatNotScan(email, classId, Id, name);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });

        return START_NOT_STICKY;
    }

    private void setAllOtherStudentAttendanceThatNotScan(final String email, final String classId, final int id, String name) {
        try {
            collectionReference.document(email).collection("Classes").document(classId).collection("Students")
                    .document(String.valueOf(id)).collection("Attendance").document(currentDate).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    try {
                                        if (snapshot.getString("Attendance").equals("P")) {
                                            //
                                        }
                                    } catch (Exception e) {
                                    }

                                } else {
                                    Map<String, Object> doc = new HashMap<>();
                                    doc.put("Date", currentDate);
                                    collectionReference.document(email).collection("Classes").document(classId).collection("Students")
                                            .document(String.valueOf(id)).collection("Attendance").document(currentDate).set(doc);
                                }
                                if (lastIndex == id) {
                                    StopServices();
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "setAllOtherStudentAttendanceThatNotScan: catch exception");
        }

    }

    private void StopServices() {
        Intent StopService = new Intent(this, MyService.class);
        stopService(StopService);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
