package com.ip.fastgate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    //Declaring components needed
    private static Context context;
    private final static String TAG = "LoginActivityTag";
    private static final byte[] header = {'G', 'A', 'T', 0x33};
    private EditText username;
    private EditText password;
    private String deviceID;
    private TextView alertTextView;
    private Button btnLogIn;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.login);
        context = this;

        username = (EditText) findViewById(R.id.txtUsername);
        password = (EditText) findViewById(R.id.txtPassword);
        alertTextView = (TextView) findViewById(R.id.AlertTextView);

        btnLogIn = (Button)
                findViewById(R.id.btnLogin);
        btnLogIn.setEnabled(false);

        com.ip.fastgate.Bluetooth.start_handler(context);
        connectToBluetoothDevice();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.ip.fastgate.Bluetooth.CONNECTED_TO_DEVICE);
        intentFilter.addAction(com.ip.fastgate.Bluetooth.LOGIN_OK);
        intentFilter.addAction(com.ip.fastgate.Bluetooth.LOGIN_WRONG);
        BluetoothBroadcastReceiver bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
        context.registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Invalid username or password");
        builder.setMessage("Va rugam reintroduceti datele de login!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLogInInfo(username.getText().toString(), password.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == com.ip.fastgate.Bluetooth.REQUEST_ENABLE_BT) {
            connectToBluetoothDevice();
        }
    }

    private static void connectToBluetoothDevice() {
        Log.d(TAG, "Setting up bluetooth");
        boolean status = com.ip.fastgate.Bluetooth.setup();
        if (status) {
            if (!com.ip.fastgate.Bluetooth.isConnected()) {
                com.ip.fastgate.Bluetooth.connect();
            }
        }
    }

    public void openMenuActivity() {
        Intent intent = new Intent(this, com.ip.fastgate.MainPageActivity.class);
        startActivity(intent);
    }

    private void sendLogInInfo(String username, String password) {
        byte[] message = new byte[header.length + username.length() + password.length() + 2];
        int k = 0;
        for (byte b : header) {
            message[k] = b;
            k++;
        }
        byte[] usernameBytes = username.getBytes();
        message[k] = (byte) usernameBytes.length;
        k++;
        for (byte b : usernameBytes) {
            message[k] = b;
            k++;
        }
        byte[] passwordBytes = password.getBytes();
        message[k] = (byte) passwordBytes.length;
        k++;
        for (byte b : passwordBytes) {
            message[k] = b;
            k++;
        }

        // debug
        String msgStr = "";
        for (byte b : message) {
            msgStr += (char) b;
        }
        Log.d(TAG, "Sending message: " + msgStr);
        //
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, "Trimitere detalii login", Toast.LENGTH_SHORT).show();
            }
        });
        com.ip.fastgate.Bluetooth.sendMessage(message);
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == com.ip.fastgate.Bluetooth.CONNECTED_TO_DEVICE) {
                btnLogIn.setEnabled(true);
                Toast.makeText(context, "Successfuly connected to Bluetooth!", Toast.LENGTH_SHORT).show();
            } else if (action == com.ip.fastgate.Bluetooth.LOGIN_OK) {
                openMenuActivity();
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();
            } else if (action == com.ip.fastgate.Bluetooth.LOGIN_WRONG) {
                builder.show();
            }
        }
    }
}

