package com.example.attendo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

public class SubjectPagerAdapter extends FragmentStateAdapter {
    private List<String> subjects;

    public SubjectPagerAdapter(@NonNull FragmentActivity fa, List<String> subjects) {
        super(fa);
        this.subjects = subjects;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return AttendanceFragment.newInstance(subjects.get(position));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }
}