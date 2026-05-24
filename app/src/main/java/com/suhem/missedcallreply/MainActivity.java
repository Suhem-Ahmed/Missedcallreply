package com.suhem.missedcallreply;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private Switch switchAllCalls;
    private Switch switchTimeWindow;
    private TextView tvStartTime, tvEndTime;
    private EditText etMessage;
    private Button btnSaveStart, btnSaveEnd, btnSave;
    private TextView tvStatus;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("MissedCallReply", MODE_PRIVATE);

        switchAllCalls  = findViewById(R.id.switchAllCalls);
        switchTimeWindow = findViewById(R.id.switchTimeWindow);
        tvStartTime     = findViewById(R.id.tvStartTime);
        tvEndTime       = findViewById(R.id.tvEndTime);
        btnSaveStart    = findViewById(R.id.btnPickStart);
        btnSaveEnd      = findViewById(R.id.btnPickEnd);
        etMessage       = findViewById(R.id.etMessage);
        btnSave         = findViewById(R.id.btnSave);
        tvStatus        = findViewById(R.id.tvStatus);

        loadPrefs();
        requestPermissions();

        btnSaveStart.setOnClickListener(v -> pickTime(true));
        btnSaveEnd.setOnClickListener(v -> pickTime(false));

        switchTimeWindow.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) switchAllCalls.setChecked(false);
        });

        switchAllCalls.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) switchTimeWindow.setChecked(false);
        });

        btnSave.setOnClickListener(v -> savePrefs());

        // Start background service
        Intent serviceIntent = new Intent(this, CallMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void pickTime(boolean isStart) {
        int hour   = prefs.getInt(isStart ? "startHour" : "endHour", isStart ? 10 : 15);
        int minute = prefs.getInt(isStart ? "startMin"  : "endMin",  0);

        new TimePickerDialog(this, (view, h, m) -> {
            String label = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            if (isStart) {
                tvStartTime.setText(label);
                prefs.edit().putInt("startHour", h).putInt("startMin", m).apply();
            } else {
                tvEndTime.setText(label);
                prefs.edit().putInt("endHour", h).putInt("endMin", m).apply();
            }
        }, hour, minute, true).show();
    }

    private void savePrefs() {
        String msg = etMessage.getText().toString().trim();
        if (msg.isEmpty()) {
            Toast.makeText(this, "Please enter a reply message", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit()
            .putBoolean("allCalls",   switchAllCalls.isChecked())
            .putBoolean("timeWindow", switchTimeWindow.isChecked())
            .putString("message",     msg)
            .apply();

        updateStatus();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadPrefs() {
        switchAllCalls.setChecked(prefs.getBoolean("allCalls", false));
        switchTimeWindow.setChecked(prefs.getBoolean("timeWindow", false));
        etMessage.setText(prefs.getString("message", "Hi! I missed your call. I'll get back to you soon."));

        int sh = prefs.getInt("startHour", 10), sm = prefs.getInt("startMin", 0);
        int eh = prefs.getInt("endHour",   15), em = prefs.getInt("endMin",   0);
        tvStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", sh, sm));
        tvEndTime.setText(String.format(Locale.getDefault(),   "%02d:%02d", eh, em));

        updateStatus();
    }

    private void updateStatus() {
        boolean all  = prefs.getBoolean("allCalls",   false);
        boolean time = prefs.getBoolean("timeWindow", false);
        if (all)       tvStatus.setText("✅ Active — replying to ALL missed calls");
        else if (time) tvStatus.setText("✅ Active — replying during time window");
        else           tvStatus.setText("⏸ Inactive — enable a mode above");
    }

    private void requestPermissions() {
        String[] perms = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        };
        boolean needsRequest = false;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needsRequest = true;
                break;
            }
        }
        if (needsRequest) {
            ActivityCompat.requestPermissions(this, perms, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERMISSION_REQUEST_CODE) {
            for (int r : results) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "All permissions are required for the app to work!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }
}
