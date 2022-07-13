package com.ddt.attendance.Fragment.NavFragment;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.ddt.attendance.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;


public class Report_us extends Fragment {

    private TextInputEditText report_Text;
    private FloatingActionButton send_Report;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    public Report_us() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_us, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().setTitle("Report Us");
        report_Text = view.findViewById(R.id.ReportUs_Text);
        send_Report = view.findViewById(R.id.ReportUs_Send);

        send_Report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ReportText = report_Text.getEditableText().toString().trim();
                sendReport(ReportText);
            }
        });
        return view;
    }

    private void sendReport(String reportText) {
        if (reportText.isEmpty()) {
            report_Text.setError("Field is empty");
            report_Text.setFocusable(true);
            return;
        }
        String name = mAuth.getCurrentUser().getDisplayName();
        String text = name + "\n" + reportText;
        Intent Sentemail = new Intent(Intent.ACTION_SEND);
        Sentemail.putExtra(Intent.EXTRA_EMAIL, new String[]{"adtech1819@gmail.com"});
        Sentemail.putExtra(Intent.EXTRA_SUBJECT, "Bug report");
        Sentemail.putExtra(Intent.EXTRA_TEXT, text);

        Sentemail.setType("message/rfc822");

        startActivity(Intent.createChooser(Sentemail, "Choose an Email client :"));
    }


}