package com.ddt.attendance.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ddt.attendance.Others.ConnectionChecker;
import com.ddt.attendance.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.ddt.attendance.Activity.MainScreenActivity.fragmentManager;

public class Login extends Fragment implements View.OnClickListener {

    private ProgressBar progressBar;
    private final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FrameLayout root;
    private Context context;

    public Login() {
        // Required empty public constructor
    }

    public interface DrawerLocker {
        public void setDrawerEnabled(boolean enabled);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        view.findViewById(R.id.login_signinButton).setOnClickListener(this);
        progressBar = view.findViewById(R.id.login_progress);
        root = (FrameLayout) view.findViewById(R.id.Login_root);
        context = container.getContext();
        ImageView imageView = view.findViewById(R.id.mainScreenLogo);
        createSignInRequest();
        ((DrawerLocker) getActivity()).setDrawerEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        Animation logoMoveAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_close);
        Animation logoMoveAnimation2 = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        imageView.startAnimation(logoMoveAnimation);
        imageView.startAnimation(logoMoveAnimation2);
        return view;
    }

    private void createSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    @Override
    public void onClick(View view) {
        ConnectionChecker conCheck = new ConnectionChecker(context);
        if (view.getId() == R.id.login_signinButton) {
            if (conCheck.isConnectingToInternet()) {
                signIn();
            } else {
                progressBar.setVisibility(View.GONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("No internet connection");
                builder.setIcon(R.drawable.net_error);
                builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            Show_Class show_class = new Show_Class();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new SplashScreen(), null).commit();
        }
    }

    private void signIn() {
        Log.d("TAG", "signIn: ");
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG", "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Snackbar snackbar = Snackbar.make(
                                    root, "Login Success", Snackbar.LENGTH_SHORT
                            );
                            snackbar.show();
                            Show_Class show_class = new Show_Class();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, show_class, null)
                                    .commit();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Snackbar snackbar = Snackbar.make(
                                    root, "Login Failed", Snackbar.LENGTH_SHORT
                            );
                            snackbar.show();
                        }
                    }
                });
    }

}