package com.purestorage.dev.pureintent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.provider.Settings.Secure;
import android.location.LocationManager;
import android.location.Location;

import com.purestorage.dev.pureintent.messaging.incoming.HelpRequestMessage;
import com.purestorage.dev.pureintent.messaging.incoming.OMWMessage;
import com.purestorage.dev.pureintent.messaging.incoming.RegistrationMessage;
import com.purestorage.dev.pureintent.messaging.*;
import com.purestorage.dev.pureintent.messaging.outgoing.HelperRequestMessage;
import com.purestorage.dev.pureintent.messaging.outgoing.HelperUpdateMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Button mEmergencyButton;
    Thread mThread;
    Boolean mThreadRunning;
    MainActivity mSelfPointer;
    Boolean mActive;
    Boolean mEmergency;
    String androidID;
    PrintWriter messageWriter;
    BufferedReader messageReader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelfPointer = this;
        mActive = false;
        mEmergency = false;
        setContentView(R.layout.activity_main);

        mEmergencyButton = (Button) findViewById(R.id.EmergencyButton);
        mEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mSelfPointer, "Emergency button pressed!", Toast.LENGTH_SHORT).show();
                emergencyReported();
            }
        });

    }
    @Override
    public void onResume ()
    {
        super.onResume();
        setContentView(R.layout.activity_main);

        mEmergencyButton = (Button) findViewById(R.id.EmergencyButton);
        mEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mSelfPointer, "Emergency button pressed!", Toast.LENGTH_SHORT).show();
                emergencyReported();
            }
        });

        mThreadRunning = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

                    androidID = tm.getDeviceId();
                    String ip = "currentlyIrrelevant";

                    LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();

                    String strLongitude = location.convert(location.getLongitude(), location.FORMAT_DEGREES);
                    String strLatitude = location.convert(location.getLatitude(), location.FORMAT_DEGREES);

                    int defaultThresh = 10;

                    //Connections for use at all communication
                    Socket connection = new Socket("10.202.103.135", 10987);

                    RegistrationMessage rm = new RegistrationMessage(strLatitude, strLongitude, latitude, longitude, defaultThresh, ip, androidID);

                    //Writers and readers
                    messageWriter = new PrintWriter(connection.getOutputStream());;
                    messageReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    messageWriter.println(rm.serialize());
                    messageWriter.flush();
                    boolean notCalled = true;

                    // TODO: make connection to server
                    while (mThreadRunning) {
                        if (true || mActive) {
                            for (String line = messageReader.readLine(); line != null; line = messageReader.readLine()) {
                                handleServerMessages(line);
                                if (mEmergency && notCalled) {
                                    // TODO: notify server
                                    notCalled = false;
                                    Location currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    double currLongitude = location.getLongitude();
                                    double currLatitude = location.getLatitude();

                                    String cstrLongitude = location.convert(location.getLongitude(), location.FORMAT_DEGREES);
                                    String cstrLatitude = location.convert(location.getLatitude(), location.FORMAT_DEGREES);
                                    HelpRequestMessage request = new HelpRequestMessage(cstrLatitude, cstrLongitude, currLatitude, currLongitude, androidID);
                                    messageWriter.println(request.serialize());
                                    messageWriter.flush();
                                }
                            }
                        }

                        try {
                            Thread.sleep(1000);                 //1000 milliseconds is one second.
                        } catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }catch(Exception e){

                }
            }
        });
        mThread.start();

    }

    public void handleServerMessages(String line){
        Message m = Message.deserialize(line);
        if(m.getMessageType() == MessageType.ON_MY_WAY_MESSAGE){
            Toast.makeText(this, "Help is on the way!", Toast.LENGTH_SHORT).show();
        }else if(m.getMessageType() == MessageType.GET_HELPER_MESSAGE){
            //display dialog
            String requestString = "Emergency Situation At: ";
            final HelperRequestMessage hr = (HelperRequestMessage) m;
            requestString += hr.getLatitude() + ", " + hr.getLongitude() + ". Can you assist?";

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            dispatchOMW(hr.getID());
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(requestString).setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        }else if(m.getMessageType() == MessageType.UPDATE_HELPER_MESSAGE){
            HelperUpdateMessage hum = (HelperUpdateMessage) m;
            String updateString = "Update at " + hum.getLat() +", " + hum.getLon() + ": " + hum.getMessage();
            Toast.makeText(this, updateString, Toast.LENGTH_SHORT);

        }else if(m.getMessageType() == MessageType.REQUEST_CLOSED_MESSAGE){

        }
    }

    public void dispatchOMW(String target){
        System.out.println("omw reached");
        OMWMessage omw = new OMWMessage(target, androidID);

        messageWriter.println(omw.serialize());
        messageWriter.flush();
        System.out.println(omw.serialize());
    }

    @Override
    public void onPause ()
    {
        mThreadRunning = false;

        try {
            mThread.join(1000);
            if (mThread.isAlive())
                Toast.makeText(this, "Thread failed to die.", Toast.LENGTH_SHORT).show();
            else
                mThread = null;
        } catch (InterruptedException e) {
            Toast.makeText(this, "Failed to kill thread.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToSettings(View view)
    {
        setContentView(R.layout.user_settings_activity);
    }
    public void goActive()
    {
        Toast.makeText(this,"You have opted to recieve Medical Alerts",Toast.LENGTH_SHORT).show();
    }
    public void cardiacArrest(View view)
    {

    }
    public void emergencyReported()
    {
        mEmergency = true;
        setContentView(R.layout.emergencylist);
        Button CardiacArrest = (Button) findViewById(R.id.CardiacArrest);
        CardiacArrest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mSelfPointer, "Your medical emergency has been broadcasted!\n if you haven't called 911, please do", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_main);

                mEmergencyButton = (Button) findViewById(R.id.EmergencyButton);
                mEmergencyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mSelfPointer, "Emergency button pressed!", Toast.LENGTH_SHORT).show();
                        emergencyReported();
                    }
                });
        }
        });
    }
}
