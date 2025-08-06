package com.example.attendo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AttendancePrefs";
    private static final String KEY_SUBJECTS = "subjects";

    private EditText etNewSubject;
    private Button btnAddSubject;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SharedPreferences prefs;
    private ArrayList<String> subjectList;
    private SubjectPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNewSubject = findViewById(R.id.etNewSubject);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        subjectList = new ArrayList<>(prefs.getStringSet(KEY_SUBJECTS, new HashSet<String>()));
        pagerAdapter = new SubjectPagerAdapter(this, subjectList);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(subjectList.get(position));
        }).attach();

        btnAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = etNewSubject.getText().toString().trim();
                if (!TextUtils.isEmpty(subject) && !subjectList.contains(subject)) {
                    subjectList.add(subject);
                    saveSubjects();
                    pagerAdapter.notifyDataSetChanged();
                    tabLayout.removeAllTabs();
                    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                        tab.setText(subjectList.get(position));
                    }).attach();
                    etNewSubject.setText("");
                }
            }
        });
    }

    private void saveSubjects() {
        prefs.edit().putStringSet(KEY_SUBJECTS, new HashSet<>(subjectList)).apply();
    }
}