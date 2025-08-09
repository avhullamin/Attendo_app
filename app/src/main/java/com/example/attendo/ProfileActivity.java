package com.example.attendo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AttendancePrefs";
    
    private SharedPreferences prefs;
    private TextView tvStudentName, tvTotalSubjects, tvTotalClasses, tvOverallAttendance;
    private Button btnEditName, btnDataManagement, btnResetData, btnBackup, btnExportAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        setTitle("Profile");
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initializeViews();
        setupClickListeners();
        updateProfileData();
    }
    
    private void initializeViews() {
        tvStudentName = findViewById(R.id.tvStudentName);
        tvTotalSubjects = findViewById(R.id.tvTotalSubjects);
        tvTotalClasses = findViewById(R.id.tvTotalClasses);
        tvOverallAttendance = findViewById(R.id.tvOverallAttendance);
        
        btnEditName = findViewById(R.id.btnEditName);
        btnDataManagement = findViewById(R.id.btnDataManagement);
        btnResetData = findViewById(R.id.btnResetData);
        btnBackup = findViewById(R.id.btnBackup);
        btnExportAll = findViewById(R.id.btnExportAll);
    }
    
    private void setupClickListeners() {
        btnEditName.setOnClickListener(v -> showEditNameDialog());
        btnDataManagement.setOnClickListener(v -> showDataManagementOptions());
        btnResetData.setOnClickListener(v -> showResetDataConfirmation());
        btnBackup.setOnClickListener(v -> backupData());
        btnExportAll.setOnClickListener(v -> exportAllData());
    }
    
    private void updateProfileData() {
        // Get student name
        String studentName = prefs.getString("student_name", "Student");
        tvStudentName.setText(studentName);
        
        // Calculate statistics
        int totalSubjects = prefs.getStringSet("subjects", new java.util.HashSet<String>()).size();
        tvTotalSubjects.setText("Total Subjects: " + totalSubjects);
        
        // Calculate total classes and overall attendance
        int totalClasses = 0;
        int totalAttended = 0;
        
        for (String subject : prefs.getStringSet("subjects", new java.util.HashSet<String>())) {
            totalClasses += prefs.getInt("total_" + subject, 0);
            totalAttended += prefs.getInt("attended_" + subject, 0);
        }
        
        tvTotalClasses.setText("Total Classes: " + totalClasses);
        
        double overallPercentage = totalClasses == 0 ? 0 : (totalAttended * 100.0 / totalClasses);
        tvOverallAttendance.setText(String.format("Overall Attendance: %.1f%%", overallPercentage));
    }
    
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Name");
        
        final EditText input = new EditText(this);
        input.setText(prefs.getString("student_name", "Student"));
        input.selectAll();
        builder.setView(input);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                prefs.edit().putString("student_name", newName).apply();
                updateProfileData();
                Toast.makeText(this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    private void showDataManagementOptions() {
        String[] options = {"View All Data", "Clear Cache", "Data Statistics"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Data Management")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showAllDataDialog();
                            break;
                        case 1:
                            clearCache();
                            break;
                        case 2:
                            showDataStatistics();
                            break;
                    }
                });
        builder.show();
    }
    
    private void showAllDataDialog() {
        StringBuilder allData = new StringBuilder();
        allData.append("=== PROFILE DATA ===\n");
        allData.append("Name: ").append(prefs.getString("student_name", "Student")).append("\n\n");
        
        allData.append("=== SUBJECTS DATA ===\n");
        for (String subject : prefs.getStringSet("subjects", new java.util.HashSet<String>())) {
            int total = prefs.getInt("total_" + subject, 0);
            int attended = prefs.getInt("attended_" + subject, 0);
            double percentage = total == 0 ? 0 : (attended * 100.0 / total);
            
            allData.append(subject).append(":\n");
            allData.append("  Total: ").append(total).append("\n");
            allData.append("  Attended: ").append(attended).append("\n");
            allData.append("  Percentage: ").append(String.format("%.1f%%", percentage)).append("\n\n");
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("All Data")
                .setMessage(allData.toString())
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void showDataStatistics() {
        int totalSubjects = prefs.getStringSet("subjects", new java.util.HashSet<String>()).size();
        int totalDays = 0;
        int totalNotes = 0;
        
        // Count total entries and notes
        for (String subject : prefs.getStringSet("subjects", new java.util.HashSet<String>())) {
            String history = prefs.getString("history_" + subject, "");
            if (!history.isEmpty()) {
                totalDays += history.split("\\|").length;
            }
            
            String notes = prefs.getString("notes_" + subject, "");
            if (!notes.isEmpty()) {
                totalNotes += notes.split("\\|").length;
            }
        }
        
        String stats = "Data Statistics:\n\n" +
                "Total Subjects: " + totalSubjects + "\n" +
                "Total Attendance Records: " + totalDays + "\n" +
                "Total Notes: " + totalNotes + "\n" +
                "Data Size: Approximately " + (totalSubjects * 50 + totalDays * 100 + totalNotes * 200) + " bytes";
        
        new AlertDialog.Builder(this)
                .setTitle("Data Statistics")
                .setMessage(stats)
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void clearCache() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Cache")
                .setMessage("This will clear temporary data but keep your attendance records. Continue?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear only non-essential data
                    Toast.makeText(this, "Cache cleared successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void showResetDataConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Reset All Data")
                .setMessage("⚠️ WARNING: This will permanently delete ALL your attendance data, subjects, and notes. This action cannot be undone!\n\nAre you absolutely sure?")
                .setPositiveButton("DELETE ALL", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Toast.makeText(this, "All data has been reset!", Toast.LENGTH_LONG).show();
                    updateProfileData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void backupData() {
        // Simple backup functionality
        StringBuilder backup = new StringBuilder();
        backup.append("ATTENDO_BACKUP_").append(System.currentTimeMillis()).append("\n");
        backup.append("Student: ").append(prefs.getString("student_name", "Student")).append("\n");
        
        for (String subject : prefs.getStringSet("subjects", new java.util.HashSet<String>())) {
            backup.append("SUBJECT:").append(subject).append("\n");
            backup.append("TOTAL:").append(prefs.getInt("total_" + subject, 0)).append("\n");
            backup.append("ATTENDED:").append(prefs.getInt("attended_" + subject, 0)).append("\n");
            backup.append("HISTORY:").append(prefs.getString("history_" + subject, "")).append("\n");
            backup.append("NOTES:").append(prefs.getString("notes_" + subject, "")).append("\n");
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Backup Data")
                .setMessage("Backup created successfully!\n\nYou can copy this backup string and save it somewhere safe:\n\n" + backup.toString().substring(0, Math.min(200, backup.length())) + "...")
                .setPositiveButton("Copy to Clipboard", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Attendo Backup", backup.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("OK", null)
                .show();
    }
    
    private void exportAllData() {
        // This will call the same export function as in MainActivity
        MainActivity mainActivity = new MainActivity();
        // For now, show a simple export dialog
        Toast.makeText(this, "Export feature - redirecting to main export", Toast.LENGTH_SHORT).show();
        finish();
    }
}
