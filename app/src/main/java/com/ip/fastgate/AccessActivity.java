package com.ip.fastgate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AccessActivity extends AppCompatActivity {
    private static Context context;
    private final static String TAG = "AccessActivityTag";
    private static final byte[] header = {'G', 'A', 'T', 0x68};
    private static final byte[] deviceUID = "357940090908175".getBytes();
    private Button btnAccesPoarta;
    private ImageView imageViewPoarta;
    private Button btnBack2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);
        context = this;
        
        imageViewPoarta = (ImageView) findViewById(R.id.imageViewPoarta);
        btnAccesPoarta = (Button) findViewById(R.id.openGateButton);
        btnBack2 = (Button) findViewById(R.id.btnBack2);
        btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
            });
        if (!Bluetooth.isConnected()) {
            btnAccesPoarta.setEnabled(false);
        }
        GateOnClickListener onClickListener = new GateOnClickListener();

        btnAccesPoarta.setOnClickListener(onClickListener);

        Bluetooth.start_handler(context);
        connectToBluetoothDevice();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Bluetooth.CONNECTED_TO_DEVICE);
        intentFilter.addAction(Bluetooth.GATE_CLOSED);
        intentFilter.addAction(Bluetooth.GATE_CLOSING);
        intentFilter.addAction(Bluetooth.GATE_OPENING);
        intentFilter.addAction(Bluetooth.GATE_OPENED);
        BluetoothBroadcastReceiver bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
        context.registerReceiver(bluetoothBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bluetooth.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Bluetooth.REQUEST_ENABLE_BT) {
            connectToBluetoothDevice();
        }
    }

    private void requestGateOpen() {
        byte[] message = new byte[header.length + deviceUID.length];
        int k = 0;
        for (byte b : header) {
            message[k] = b;
            k++;
        }
        for (byte b : deviceUID) {
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
                Toast.makeText(context, "Trimitere cerere acces", Toast.LENGTH_SHORT).show();
            }
        });
        Bluetooth.sendMessage(message);
    }

    private static void connectToBluetoothDevice() {
        Log.d(TAG, "Setting up bluetooth");
        boolean status = Bluetooth.setup();
        if (status) {
            if (!Bluetooth.isConnected()) {
                Bluetooth.connect();
            }
        }
    }

    private class GateOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Sending gate open request");
            requestGateOpen();
        }
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == Bluetooth.CONNECTED_TO_DEVICE) {
                btnAccesPoarta.setEnabled(true);
            } else if (action.equals(Bluetooth.GATE_CLOSED)) {
                imageViewPoarta.setImageResource(R.drawable.gate_closed);
            } else if (action.equals(Bluetooth.GATE_OPENING) || action.equals(Bluetooth.GATE_CLOSING)) {
                imageViewPoarta.setImageResource(R.drawable.gate_opening);
            } else if (action.equals(Bluetooth.GATE_OPENED)) {
                imageViewPoarta.setImageResource(R.drawable.gate_opened);
            }
        }
    }
}
