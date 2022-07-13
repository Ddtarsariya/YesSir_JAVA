package com.ddt.attendance.Fragment;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.opengl.Visibility;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ddt.attendance.FireBaseReference.FirebaseReference;
import com.ddt.attendance.Others.ConnectionChecker;
import com.ddt.attendance.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import java.util.Objects;

import static com.ddt.attendance.Activity.MainScreenActivity.fragmentManager;


public class SplashScreen extends Fragment {

    private FirebaseReference firebaseReference = new FirebaseReference();
    private TextView name;
    private TextView welcome;
    private TextView logo;
    private Handler h = new Handler();
    Context context;
    Runnable runnable;

    public SplashScreen() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_spash_screen, container, false);

        context = container.getContext();
        logo = view.findViewById(R.id.splash_logo);
        name = view.findViewById(R.id.splash_name);
        welcome = view.findViewById(R.id.splash_welcome);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        if (new ConnectionChecker(container.getContext()).isConnectingToInternet()){
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            name.setText("Hi "+mAuth.getCurrentUser().getDisplayName()+",");
        }else {
            name.setText("Hi ,");
        }

        Animation logoMoveAnimation3 = AnimationUtils.loadAnimation(container.getContext(), R.anim.fab_open);
        name.startAnimation(logoMoveAnimation3);

        Animation logoMoveAnimation4 = AnimationUtils.loadAnimation(container.getContext(), R.anim.fab_close);
        welcome.startAnimation(logoMoveAnimation4);

        Animation logoMoveAnimation2 = AnimationUtils.loadAnimation(container.getContext(), R.anim.fab_open);
        logo.startAnimation(logoMoveAnimation2);

        runnable = new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new Show_Class(), null)
                        .commitAllowingStateLoss();
                Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        };
        h.postDelayed(runnable,2300);
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        h.removeCallbacks(runnable);
    }
}