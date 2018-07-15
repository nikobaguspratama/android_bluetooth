package com.example.nikobaguspratama.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikobaguspratama.myapplication.model.dataBluetooth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class bluetoothActivity extends AppCompatActivity{
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket = null;
    private BluetoothAdapter mybluetooth = null;
    private InputStreamReader aReader = null;
    private InputStream mmInputStream = null;
    private BufferedReader mBufferedReader = null;
    private String address = null;
    private ProgressDialog progress;
    private DatabaseReference mDatabaseR;
    private boolean isBtConnected = false;
    private boolean clickedButton = false;
    private String aString="0";
    private TextView data;
    private Button upload;
    private int counter =0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity);
        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        mDatabaseR = FirebaseDatabase.getInstance().getReference().child("datanya");

        data = findViewById(R.id.data);
        upload = findViewById(R.id.upload);
        new connectBluetooth().execute();


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton = true;
            }
        });
        Thread textSpeedThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(3000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                                try {
                                    mmInputStream=btSocket.getInputStream();
                                    aReader = new InputStreamReader(mmInputStream);
                                    mBufferedReader = new BufferedReader(aReader);
                                    aString = mBufferedReader.readLine();

                                    if(aString==null){
                                        aString="no data";
                                        data.setText(aString);
                                    }else{
                                        data.setText(String.valueOf(aString));
                                    }
                                    Log.d("dataGet",aString);
                                    if(counter<20 && clickedButton==true){
                                        Log.d("masuk","masuk");
                                        counter++;
                                        final dataBluetooth Data = new dataBluetooth(aString);
                                        mDatabaseR.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                mDatabaseR.push().setValue(Data);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }else if(counter==20 && clickedButton==true ){
                                        counter=0;
                                        clickedButton=false;
                                        mDatabaseR.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                mDatabaseR.push().setValue("///////////data End Here//////////");
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
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
                    mybluetooth = BluetoothAdapter.getDefaultAdapter();//getthe local device's bluetooth adapter
                    BluetoothDevice device = mybluetooth.getRemoteDevice(address);// connects to the device's address
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);// creates a connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
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
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Connection Failed! Try Again!");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }
            progress.dismiss();//closes progress bar

        }
    }
}
