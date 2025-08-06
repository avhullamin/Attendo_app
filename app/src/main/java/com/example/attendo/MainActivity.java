package com.example.attendo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_SUBJECTS = "subjects";

    private EditText etNewSubject;
    private Button btnAddSubject;
    private LinearLayout subjectButtonContainer;
    private SharedPreferences prefs;
    private ArrayList<String> subjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNewSubject = findViewById(R.id.etNewSubject);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        subjectButtonContainer = findViewById(R.id.subjectButtonContainer);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        subjectList = new ArrayList<>(prefs.getStringSet(KEY_SUBJECTS, new HashSet<String>()));
        renderSubjectButtons();

        btnAddSubject.setOnClickListener(v -> {
            String subject = etNewSubject.getText().toString().trim();
            if (!TextUtils.isEmpty(subject) && !subjectList.contains(subject)) {
                subjectList.add(subject);
                saveSubjects();
                renderSubjectButtons();
                etNewSubject.setText("");
            }
        });
    }

    private void renderSubjectButtons() {
        subjectButtonContainer.removeAllViews();
        for (String subject : subjectList) {
            Button btn = new Button(this);
            btn.setText(subject);
            btn.setOnClickListener(v -> {
                Intent intent = SubjectActivity.newIntent(this, subject);
                startActivity(intent);
            });
            subjectButtonContainer.addView(btn);
        }
    }

    private void saveSubjects() {
        prefs.edit().putStringSet(KEY_SUBJECTS, new HashSet<>(subjectList)).apply();
    }
}