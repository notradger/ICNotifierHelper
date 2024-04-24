package com.example.myguitest;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.AlertDialog;



public class MainActivity extends AppCompatActivity {
    NotificationListenerExampleService mService;
    boolean mBound = false;
    ToggleButton toggle;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;
    EditText batchAmountText;
    int triggerAmount;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, NotificationListenerExampleService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.triggerAmount), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Batch amount trigger button object
        batchAmountText = findViewById(R.id.editTextNumberAmount);

        // Toggle on/off button object
        toggle = (ToggleButton) findViewById(R.id.toggleButton);

        // Save button object
        Button saveButton = (Button) findViewById(R.id.buttonSave);

        //  Retrieve trigger amount from saved preferences and restore text field value
        SharedPreferences settings = getApplicationContext().getSharedPreferences("prefs", 0);
        triggerAmount = settings.getInt("triggerAmount", 0);
        batchAmountText.setText(Integer.toString(triggerAmount));



        // Save button listener to save trigger amount to saved preferences
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "save button clicked", Toast.LENGTH_SHORT).show();
                SharedPreferences settings = getApplicationContext().getSharedPreferences("prefs", 0);
                SharedPreferences.Editor editor = settings.edit();
                triggerAmount = Integer.parseInt(batchAmountText.getText().toString());
                editor.putInt("triggerAmount", triggerAmount);
                editor.apply();

                mService.setBatchTriggerAmount(triggerAmount);
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBound) {
                    Toast.makeText(getApplicationContext(),
                            "Service status changed ok", Toast.LENGTH_SHORT).show();
                    mService.setNotificationStatus(isChecked);
                }
            }
        });

        if (!isNotificationServiceEnabled()) {
            Log.i("myInfoTag", "Notification Service is not enabled..");
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        } else {
            Log.i("myInfoTag", "Notification Service IS enabled..");
        }


    } // onCreate()

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

    /** Defines callbacks for service binding, passed to bindService(). */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            NotificationListenerExampleService.LocalBinder binder = (NotificationListenerExampleService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            toggle.setChecked(mService.getNotificationStatus());
            mService.setBatchTriggerAmount(triggerAmount);

            Log.i("myLogTag", "onServiceConnected called!!");
            if (mService.getNotificationStatus() == true) {
                Log.i("myLogTag", "notification status is true!");
            } else {
                Log.i("myLogTag", "notification status is false!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}