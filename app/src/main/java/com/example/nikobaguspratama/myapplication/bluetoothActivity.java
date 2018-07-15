package com.example.nikobaguspratama.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class bluetoothActivity extends AppCompatActivity{
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final UUID hm10 = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket btSocket = null;
    private BluetoothAdapter mybluetooth = null;
    private InputStreamReader aReader = null;
    private InputStream mmInputStream = null;
    private BufferedReader mBufferedReader = null;
    private String address = null;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    private String aString;
    private TextView data;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity);
        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        data = findViewById(R.id.data);
        new connectBluetooth().execute();

        Thread textSpeedThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                                try {
                                    mmInputStream=btSocket.getInputStream();
                                    aReader = new InputStreamReader(mmInputStream);
                                    mBufferedReader = new BufferedReader(aReader);
                                    aString = mBufferedReader.readLine();
                                    Log.d("test",aString);
                                    Log.d("test2",mBufferedReader.toString());
                                    if(aString==null){
                                        aString = "0";
                                    }
                                    data.setText(String.valueOf(aString));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        textSpeedThread.start();
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();//displays a message on screen
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progress!=null){
            progress.dismiss();
        }
    }

    private class connectBluetooth extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;


        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(bluetoothActivity.this, "Connecting", "Please Wait");// show a progress of connection
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progressdialog is shown, connection is done in background
        {
            try {
                if (btSocket == null || isBtConnected)//when bluetooth device not connected
                {
                    Log.d("masuk","masuk");
                    mybluetooth = BluetoothAdapter.getDefaultAdapter();//getthe local device's bluetooth adapter
                    BluetoothDevice device = mybluetooth.getRemoteDevice(address);// connects to the device's address
                    //btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);// creates a connection
                    btSocket = device.createRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    Log.d("masuk",mybluetooth.toString()+","+device.toString()+","+btSocket.toString());

                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)// after executing doInBackground, it checks if everything went fine]
        {
            if(progress!=null){
                progress.dismiss();//closes progress bar
            }

            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Connection Failed! Try Again!");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }


        }

    }
}
