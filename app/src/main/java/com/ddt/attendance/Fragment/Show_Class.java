package com.ddt.attendance.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ddt.attendance.Adapter.ClassAdapter;
import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Fragment.Class.ClassQrGenerator;
import com.ddt.attendance.Model.ClassModel;
import com.ddt.attendance.Others.ConnectionChecker;
import com.ddt.attendance.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import petrov.kristiyan.colorpicker.ColorPicker;

import static com.ddt.attendance.Activity.MainScreenActivity.fragmentManager;

public class Show_Class extends Fragment {

    private FloatingActionButton floatingActionButton;
    private RecyclerView classRecyclerview;
    private ProgressBar classProgressBar;
    private CircleImageView navImageView;
    private final FirebaseReference firebaseReference = new FirebaseReference();
    private final List<ClassModel> modelList = new ArrayList<>();
    private ClassAdapter classAdapter;
    private Context context;
    private int size;
    private ConnectionChecker checker;
    private boolean versionSet;
    RecyclerView.LayoutManager layoutManager;
    TextView emailTextview;
    TextView usernameTextview;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String ARG_PARAM1 = "param1";

    private String mParam1;

    public static Show_Class newInstance(String param1) {
        Show_Class fragment = new Show_Class();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public Show_Class() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("TAG", "onAttach: Class");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("TAG", "onActivityCreated: Class");
        showClassData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_class, container, false);
        context = container.getContext();
        checker = new ConnectionChecker(context);
        Log.d("TAG", "onCreateView: Class");
        getActivity().setTitle("Class");
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        classProgressBar = view.findViewById(R.id.class_progressbar);
        classRecyclerview = view.findViewById(R.id.class_recyclerview);
        floatingActionButton = view.findViewById(R.id.class_add_fab);

        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        layoutManager = new LinearLayoutManager(context);
        classRecyclerview.setLayoutManager(layoutManager);

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        emailTextview = headerView.findViewById(R.id.nav_email);
        usernameTextview = headerView.findViewById(R.id.nav_username);
        navImageView = headerView.findViewById(R.id.nav_image);



        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (size == 4 && !versionSet) {
                    Toast.makeText(context, "You can add up to 4 class in basic version", Toast.LENGTH_SHORT).show();
                    return;
                }
                fragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.fragment_container, new Add_Class(), null).commit();
            }
        });
        classRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    floatingActionButton.hide();
                } else {
                    floatingActionButton.show();
                }
            }
        });
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checker.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("No internet connection");
            builder.setIcon(R.drawable.net_error);
            builder.setCancelable(false);

            builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.exit(0);
                }
            });
            builder.show();
        }
    }

    @Override
    public void onStart() {
        Log.d("TAG", "onStart: ");
        super.onStart();
        ((Login.DrawerLocker) getActivity()).setDrawerEnabled(true);
        if (mAuth.getCurrentUser() != null) {
                usernameTextview.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
                Uri uri = Uri.parse(String.valueOf(mAuth.getCurrentUser().getPhotoUrl()));
                Picasso.get().load(uri).into(navImageView);
                firebaseReference.documentReference.collection("Name").document("Name").get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot snapshot = task.getResult();
                                try {
                                    if (snapshot.getString("Name") == null) {
                                        Map<String, Object> doc = new HashMap<>();
                                        doc.put("Name", mAuth.getCurrentUser().getDisplayName());
                                        firebaseReference.documentReference.collection("Name").document("Name").set(doc);
                                    }
                                } catch (Exception e) {
                                    //
                                }
                            }
                        });

                firebaseReference.documentReference.collection("Version").document("Pro")
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                String version = snapshot.getString("version");
                                if (version.contains("Pro")) {
                                    versionSet = true;
                                    emailTextview.setText(mAuth.getCurrentUser().getEmail() + " " + "(Pro)");
                                } else {
                                    versionSet = false;
                                    emailTextview.setText(mAuth.getCurrentUser().getEmail() + " " + "(Basic)");
                                }
                            } else {
                                versionSet = false;
                                emailTextview.setText(mAuth.getCurrentUser().getEmail() + " " + "(Basic)");
                            }
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                });

        } else {
            fragmentManager.beginTransaction().add(R.id.fragment_container, new Login(), null).commit();
        }

    }

    private void showClassData() {
        modelList.clear();
        classProgressBar.setVisibility(View.VISIBLE);
        firebaseReference.collectionReference.orderBy("Subject Name", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                            ClassModel classModel = new ClassModel(
                                    doc.getString("Id"),
                                    doc.getLong("FirstIndexOfStudent").intValue(),
                                    doc.getLong("LastIndexOfStudent").intValue(),
                                    doc.getString("Subject Name"),
                                    doc.getString("Department"),
                                    doc.getString("Standard"),
                                    doc.getString("Division"),
                                    doc.getString("Subject Type")
                            );
                            modelList.add(classModel);
                        }
                        classProgressBar.setVisibility(View.GONE);
                        classAdapter = new ClassAdapter(Show_Class.this, modelList);
                        classRecyclerview.setAdapter(classAdapter);
                        size = modelList.size();
                        Animation logoMoveAnimation3 = AnimationUtils.loadAnimation(context, R.anim.pull_in_left);
                        classRecyclerview.startAnimation(logoMoveAnimation3);
                    }
                });
    }

    public void ShowStudentReport(final int position) {
        int i = modelList.get(position).getLastIndexOfStudent();
        String SubjectName = modelList.get(position).getSubjectName();
        Report_Fragment report_fragment = Report_Fragment.newInstance(i, modelList.get(position).getId(),
                SubjectName);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, report_fragment, null)
                .addToBackStack(null)
                .commit();
    }

    public void AddShowStudentInClass(int position) {
        Show_Student show_student = Show_Student.newInstance(modelList.get(position).getId(),
                modelList.get(position).getSubjectName());
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, show_student, null)
                .addToBackStack(null)
                .commit();
    }

    public void ScanStudentClick(int position) {
        Scan_Fragment scan_fragment = Scan_Fragment.newInstance(modelList.get(position).getId(),
                modelList.get(position).getSubjectName());
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, scan_fragment, null)
                .addToBackStack(null)
                .commit();
    }

    public void ClassUpdate(int position) {
        final String classId = modelList.get(position).getId();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Edit this subject ?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UpdateClass(classId);
            }
        });
        builder.create().show();
    }

    private void UpdateClass(final String classId) {
        Add_Class add_class = Add_Class.newInstance(classId);

        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, add_class, null)
                .commit();
    }

    public void ClassDelete(final int position) {
        final String classId = modelList.get(position).getId();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.logocirclewhite);
        builder.setCancelable(true);
        builder.setTitle("Sure to delete this subject?");
        builder.setMessage("All attendance for this subject will be removed too. Once deleted, can't be recovered back!");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteClass(classId, position);
            }
        });
        builder.create().show();
    }

    private void DeleteClass(String classId, int pos) {
        firebaseReference.collectionReference.document(classId).delete();
        modelList.remove(pos);
        classAdapter.notifyItemRemoved(pos);
        classAdapter.notifyItemRangeChanged(pos, modelList.size());
    }

    public void ClassColor(int position) {
        String classId = modelList.get(position).getId();
        openColorPicker(classId);

    }

    private void openColorPicker(final String classId) {
        final ColorPicker colorPicker = new ColorPicker(getActivity());
        ArrayList<String> colors = new ArrayList<>();
        colors.add("#ffa931"); //yellow
        colors.add("#2c786c"); //green
        colors.add("#e65f05"); // orange
        colors.add("#ed0c0c"); //red
        colors.add("#1302d4"); // dark blur
        colors.add("#6a2c70"); //dark purple
        colors.add("#fa26a0"); // pink
        colors.add("#02d459"); // light green
        colors.add("#f09ae9"); //light purple
        colors.add("#05dfd7"); //light blue
        colors.add("#aa05e6"); // light purple
        colors.add("#979797"); // light gray
        colors.add("#ab72c0"); // light
        colors.add("#000000"); // Black
        colors.add("#616f39"); //


        colorPicker
                .setColors(colors)
                .setColumns(5)
                .setRoundColorButton(true)
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        if (color == 0) {
                            setClassColoronfirebase(classId, Color.parseColor("#000000"));
                        } else {
                            setClassColoronfirebase(classId, color);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                }).show();
    }

    private void setClassColoronfirebase(String classId, int color) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("Color", String.valueOf(color));
        firebaseReference.collectionReference.document(classId).update(doc);
        classAdapter.notifyDataSetChanged();
    }

    public void ClassQr(final int position) {
        firebaseReference.documentReference.collection("Version").document("Pro")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String version = snapshot.getString("version");
                    if (version.contains("Pro")) {
                        //open ClassQr and share qr fragment
                        ClassQrGenerator classQrGenerator = ClassQrGenerator.newInstance(
                                modelList.get(position).getId(), modelList.get(position).getSubjectName());
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, classQrGenerator, null).addToBackStack(null).commit();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("You have a basic version,Upgrade to pro");
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, new Version_Upgrade_Fragment(), null).commit();
                            }
                        });
                        builder.create().show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Upgrade to Pro version to use this feature");
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, new Version_Upgrade_Fragment(), null).commit();
                        }
                    });
                    builder.create().show();
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Attendance Reminder";
            String desc = "This Subject name";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyAttendance", name, importance);
            channel.setDescription(desc);

            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void onLongClickShowLog(int position) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(30);
        Class_log class_log = Class_log.newInstance(modelList.get(position).getId());
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, class_log, null)
                .addToBackStack(null)
                .commit();
    }

}