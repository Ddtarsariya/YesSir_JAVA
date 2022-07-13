package com.ddt.attendance.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Fragment.Login;
import com.ddt.attendance.Fragment.NavFragment.About_us;
import com.ddt.attendance.Fragment.NavFragment.Guidence;
import com.ddt.attendance.Fragment.NavFragment.Report_us;
import com.ddt.attendance.Fragment.NavFragment.Setting;
import com.ddt.attendance.Fragment.Scan_Fragment;
import com.ddt.attendance.Fragment.Show_Class;
import com.ddt.attendance.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Login.DrawerLocker {

    private static final String TAG = "MainScreenActivity";
    public static FragmentManager fragmentManager;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle toggle;
    private static final String THEME_COLOR = "themeColor";
    private static final String SHARED_PREFERENCE = "sharedPref";
    TextView emailTextview;
    TextView usernameTextview;
    private CircleImageView navImageView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseReference firebaseReference;
    private Show_Class show_class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt(THEME_COLOR, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);

        drawerLayout = findViewById(R.id.draw_layout);
        setSupportActionBar(toolbar);

        Window window = getWindow();

        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        emailTextview = headerView.findViewById(R.id.nav_email);
        usernameTextview = headerView.findViewById(R.id.nav_username);
        navImageView = headerView.findViewById(R.id.nav_image);
        if (themeColor != 0) {
            headerView.setBackgroundColor(themeColor);
            toolbar.setBackgroundColor(themeColor);
            switch (themeColor) {
                case -22223: //yellow
                    setTheme(R.style.AppTheme_Yellow);
                    window.setStatusBarColor(Color.parseColor("#FDA01E"));
                    break;
                case -13862804: //green
                    window.setStatusBarColor(Color.parseColor("#2c786c"));
                    setTheme(R.style.AppTheme_Green);
                    break;
                case -1679611: //orange
                    window.setStatusBarColor(Color.parseColor("#DF5B04"));
                    setTheme(R.style.AppTheme_Orange);
                    break;
                case -1242100: //red
                    window.setStatusBarColor(Color.parseColor("#EA0505"));
                    setTheme(R.style.AppTheme_Red);
                    break;
                default:
                    window.setStatusBarColor(Color.parseColor("#000000"));
                    setTheme(R.style.AppTheme);
                    break;
            }
        }
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigatio_drawer_open, R.string.navigatio_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        fragmentManager = getSupportFragmentManager();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Login login = new Login();
            fragmentTransaction.add(R.id.fragment_container, login, null);
            fragmentTransaction.commit();
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else if (getSupportFragmentManager().getBackStackEntryCount() >= 2) {
            Log.d(TAG, "onBackPressed: 1"+getSupportFragmentManager().getBackStackEntryCount());
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            Log.d(TAG, "onBackPressed: 1");
            super.onBackPressed();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            firebaseReference = new FirebaseReference();
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
                                emailTextview.setText(mAuth.getCurrentUser().getEmail() + " " + "(Pro)");
                            } else {
                                emailTextview.setText(mAuth.getCurrentUser().getEmail() + " " + "(Basic)");
                            }
                        } else {
                            emailTextview.setText(mAuth.getCurrentUser().getEmail() + " " + "(Basic)");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onComplete: "+e);
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_class:
                if (getSupportFragmentManager().getBackStackEntryCount() >= 1) {
                    for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
                if (new Show_Class().getUserVisibleHint()) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                } else {
                   getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new Show_Class())
                            .commit();
                }
                break;
            case R.id.nav_scan:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Scan_Fragment())
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.nav_setting:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Setting())
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_report:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Report_us())
                        .addToBackStack(null).commit();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.nav_guide:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Guidence()).addToBackStack(null).commit();
                break;
            case R.id.nav_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Sure you want to logout?");
                builder.setCancelable(true);
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout();
                    }
                });
                builder.create().show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Yes Sir");
            String shareMessage = "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() + ".YesSir" + "\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //
        }
    }

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut();

        FirebaseAuth.getInstance().signOut();

        fragmentManager.beginTransaction().replace(R.id.fragment_container, new Login(), null).commit();
    }

    @Override
    public void setDrawerEnabled(boolean enabled) {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawerLayout.setDrawerLockMode(lockMode);
        toggle.setDrawerIndicatorEnabled(enabled);
    }

}