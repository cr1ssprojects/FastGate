package com.ip.fastgate;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.primitives.Bytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bluetooth {

    private static final String TAG = "BluetoothTag";
    private static Context context;
    private static Handler mHandler;
    //private static final String HC_05_MAC = "94:53:30:45:6C:11";
    private static final String HC_05_MAC = "00:21:11:01:5E:15";
    private static BluetoothAdapter bluetoothAdapter;
    private static ConnectThread mConnectThread;
    private static final int CONNECTING_STATUS = 13;
    private static final int MESSAGE_READ = 33;
    public static final int REQUEST_ENABLE_BT = 420;
    public static final String CONNECTED_TO_DEVICE = "BT_CONNECTED_TO_DEVICE";
    public static final String GATE_CLOSED = "BT_GATE_CLOSED";
    public static final String GATE_OPENING = "BT_GATE_OPENING";
    public static final String GATE_CLOSING = "BT_GATE_CLOSING";
    public static final String GATE_OPENED = "BT_GATE_OPENED";
    public static final String LOGIN_OK = "BT_LOGIN_OK";
    public static final String LOGIN_WRONG = "BT_LOGIN_WRONG";
    public static final String GOT_PROFILE_DATA = "BT_GOT_PROFILE_DATA";
    private static final byte[] GAT_HEADER = {'G', 'A', 'T'};
    private static final byte GATE_OPENING_MESSAGE = 0x01;
    private static final byte GATE_OPENED_MESSAGE = 0x02;
    private static final byte GATE_CLOSING_MESSAGE = 0x03;
    private static final byte GATE_CLOSED_MESSAGE = 0x04;
    private static final byte LOGIN_OK_MESSAGE = 0x05;
    private static final byte LOGIN_WRONG_MESSAGE = 0x06;
    private static final byte PROFILE_DATA_REQUEST_MESSAGE = 0x07;
    private static final UUID uuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    private static boolean isConnected = false;
    private static BluetoothSocket mBTSocket;

    public static void start_handler(Context ctx) {
        context = ctx;
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    dealWithMessage((List<Byte>) msg.obj);
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        isConnected = true;
                        Toast.makeText(context.getApplicationContext(), "Connected to Device: " + (String) (msg.obj), Toast.LENGTH_SHORT).show();
                        context.sendBroadcast(new Intent(CONNECTED_TO_DEVICE));
                    } else
                        Toast.makeText(context.getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    public static boolean setup() {
        getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context.getApplicationContext(), "No bluetooth adapter found.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            enableAdapter();
            return false;
        }
        return true;
    }

    private static void getAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private static void enableAdapter() {
        Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((Activity) context).startActivityForResult(bluetoothEnableIntent, REQUEST_ENABLE_BT);
    }

    private static BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, uuid);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(uuid);
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public static void connect() {
        new Thread() {
            public void run() {
                mBTSocket = null;
                boolean fail = false;
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(HC_05_MAC); // address of HC-05 bluetooth module
                try {
                    mBTSocket = createBluetoothSocket(device);
                    Log.d(TAG, "Created socket");
                } catch (IOException e) {
                    fail = true;
                }
                Log.d(TAG, "Creating socket");
                try {
                    Class<?> clazz = device.getClass();
                    Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

                    Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[]{Integer.valueOf(1)};

                    mBTSocket = (BluetoothSocket) m.invoke(device, params);
                    mBTSocket.connect();
                    Log.d(TAG, "Connected to socket");
                } catch (Exception e) {
                    Log.e(TAG, "Failed connecting to socket");
                    Log.e(TAG, e.getMessage());
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        Log.e(TAG, "Failed closing socket");
                    }
                }
                if (fail == false) {
                    mConnectThread = new ConnectThread(mBTSocket);
                    mConnectThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, "HC-05")
                            .sendToTarget();
                }
            }
        }.start();
    }

    public static void disconnect() {
        mConnectThread.cancel();
        mConnectThread = null;
        try {
            mBTSocket.close();
        } catch (Exception e) {
        }
        mBTSocket = null;
        mHandler = null;
    }

    private static void dealWithMessage(List<Byte> message) {
        String msgStr = "";
        for (int i = 0; i < message.size(); i++) {
            msgStr += (char) message.get(i).byteValue();
        }
        if (message.get(0) == GAT_HEADER[0] && message.get(1) == GAT_HEADER[1] && message.get(2) == GAT_HEADER[2]) {
            Log.d(TAG, "Got message: " + msgStr);
            if (message.get(3) == GATE_CLOSED_MESSAGE) {
                context.sendBroadcast(new Intent(GATE_CLOSED));
            } else if (message.get(3) == GATE_OPENED_MESSAGE) {
                context.sendBroadcast(new Intent(GATE_OPENED));
            } else if (message.get(3) == GATE_CLOSING_MESSAGE) {
                context.sendBroadcast(new Intent(GATE_CLOSING));
            } else if (message.get(3) == GATE_OPENING_MESSAGE) {
                context.sendBroadcast(new Intent(GATE_OPENING));
            } else if (message.get(3) == LOGIN_OK_MESSAGE) {
                context.sendBroadcast(new Intent(LOGIN_OK));
            } else if (message.get(3) == LOGIN_WRONG_MESSAGE) {
                context.sendBroadcast(new Intent(LOGIN_WRONG));
            } else if (message.get(3) == PROFILE_DATA_REQUEST_MESSAGE) {
                Intent intent = new Intent(GOT_PROFILE_DATA);
                intent.putExtra("message", Bytes.toArray(message));
                context.sendBroadcast(intent);
            } else {
                Toast.makeText(context.getApplicationContext(), String.format("Got %d bytes of BT data", message.size()), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Got ALTERED message: " + msgStr);
        }
    }

    public static void sendMessage(byte[] bytes) {
        class SendMessageRunnable implements Runnable {
            private byte[] bytes;

            public SendMessageRunnable(byte[] bytes) {
                this.bytes = bytes;
            }

            public void run() {
                mConnectThread.write(bytes);
            }
        }
        String bytesStr = "";
        for (byte b : bytes) {
            bytesStr += (char) b;
        }
        Log.d(TAG, "Sending bytes: " + bytesStr);
        Thread sendThread = new Thread(new SendMessageRunnable(bytes));
        sendThread.start();
    }

    public static void sendMessage(String message) {
        class SendMessageRunnable implements Runnable {
            private String message;

            public SendMessageRunnable(String message) {
                this.message = message;
            }

            public void run() {
                mConnectThread.write(message);
            }
        }
        Thread sendThread = new Thread(new SendMessageRunnable(message));
        sendThread.start();
    }

    private static class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void write(String input) {
            byte[] bytes = input.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    List<Byte> msg = new ArrayList<>();
                    bytes = mmInStream.available();
                    while (bytes != 0) {
                        buffer = new byte[1024];
                        bytes = mmInStream.read(buffer, 0, bytes);
                        for (int i = 0; i < bytes; i++) {
                            msg.add(buffer[i]);
                        }
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                    }
                    if (msg.size() != 0) {
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, msg).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
