package com.ip.fastgate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class UserProfileActivity extends AppCompatActivity {

    private TextView nume;
    private TextView divizie;
    private TextView nr_masina;
    private TextView orar;
    private Button btnBack;
    private ImageView profile;
    private static Context context;
    private static final byte[] header = {'G', 'A', 'T', 0x07};
    private static final byte[] deviceUID = "357940090908175".getBytes();
    private static final String TAG = "UserProfileActivityTag";
    private static final String hardcodedImage = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAChSURBVDhPrY+xDQMhDEUZgIIRGIGCioqGnpKF2IVtGIURyA9YkU84ii66V5xs6z98VvMmDwmttVJKrZV6hiyEENSCeoYspJTENJCn4B/BOUcNQxCQe/++UsaY07kIO4ocahT4ol6uijGuCBMw2tHNFk7kKbgtYNsYgxqGIOxLvPda6x9H40kkPpdYa889FyHn3HunZoEWDt/z9QYOVlE15wt9po3p1rYlJAAAAABJRU5ErkJggg==";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initializare
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);
        context = this;

        nume = (TextView) findViewById(R.id.textDataNume);
        divizie = (TextView) findViewById(R.id.textDataDivizie);
        nr_masina = (TextView) findViewById(R.id.textDataNrCar);
        orar = (TextView) findViewById(R.id.textOrarAcces);
        btnBack = (Button) findViewById(R.id.btnBack);
        profile = (ImageView) findViewById(R.id.imageProfil);

        String none = "<unavailable>";
        SetDataFromDB(new com.ip.fastgate.UserProfile(none, none, none, none, ""));

        com.ip.fastgate.Bluetooth.start_handler(context);
        connectToBluetoothDevice();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.ip.fastgate.Bluetooth.GOT_PROFILE_DATA);
        BluetoothBroadcastReceiver bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
        context.registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        requestProfileData();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

    private void requestProfileData() {
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
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, "Cerere date profil", Toast.LENGTH_SHORT).show();
            }
        });
        com.ip.fastgate.Bluetooth.sendMessage(message);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void SetDataFromDB(com.ip.fastgate.UserProfile user) {
        nume.setText(user.getNume());
        divizie.setText(user.getDivizie());
        nr_masina.setText(user.getNrMasina());
        orar.setText(user.getOrar());
        viewPhoto(user.getUserImage());
    }

    private void viewPhoto(String file) {

/*        Resources res = getResources();
        String mDrawableName = file;
        int resID = res.getIdentifier(mDrawableName , "drawable", getPackageName());
        Drawable drawable = res.getDrawable(resID);
        profile.setImageDrawable(drawable );*/

/*        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);*/

/*        Bitmap image = BitmapFactory.decodeFile(file);
        ;*/
        byte[] decodedString = Base64.decode(file, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        profile.setImageBitmap(decodedByte);
        //profile.setImageAlpha(150);
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == com.ip.fastgate.Bluetooth.GOT_PROFILE_DATA) {
                byte[] message = intent.getByteArrayExtra("message");
                // debug
                String msgStr = "";
                for (byte b : message) {
                    msgStr += (char) b;
                }
                Log.d(TAG, "GOT profile data: " + msgStr);
                //

                int idx = 4;
                int nameLen = message[idx];
                idx++;
                String name = new String(Arrays.copyOfRange(message, idx, idx + nameLen));

                idx += nameLen;
                int divisionLen = message[idx];
                idx++;
                String division = new String(Arrays.copyOfRange(message, idx, idx + divisionLen));

                idx += divisionLen;
                int carNoLen = message[idx];
                idx++;
                String carNo = new String(Arrays.copyOfRange(message, idx, idx + carNoLen));

                idx += carNoLen;
                int scheduleLen = message[idx];
                idx++;
                String schedule = new String(Arrays.copyOfRange(message, idx, idx + scheduleLen));

                Log.d(TAG, "Name is: " + name);
                Log.d(TAG, "Division is: " + division);
                Log.d(TAG, "Car number plate is: " + carNo);
                Log.d(TAG, "Schedule number is: " + schedule);
                com.ip.fastgate.UserProfile user = new com.ip.fastgate.UserProfile(name, division, carNo, schedule, hardcodedImage);
                SetDataFromDB(user);
            }
        }
    }
}
