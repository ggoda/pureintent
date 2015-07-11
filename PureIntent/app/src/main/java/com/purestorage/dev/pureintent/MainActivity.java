package com.purestorage.dev.pureintent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    Button mEmergencyButton;
    Thread mThread;
    Boolean mThreadRunning;
    MainActivity mSelfPointer;
    Boolean mActive;
    Boolean mEmergency;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelfPointer = this;
        mActive = false;
        mEmergency = false;
        setContentView(R.layout.activity_main);


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
                // TODO: make connection to server
                while (mThreadRunning) {
                    if (mActive) {
                        // TODO: poll server to see if there are nearby emergencies
                    }
                    if (mEmergency) {
                        // TODO: notify server
                    }

                    try {
                        Thread.sleep(1000);                 //1000 milliseconds is one second.
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        mThread.start();

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
    public void goActive(View view)
    {

    }
    public void cardiacArrest(View view)
    {

    }
    public void emergencyReported()
    {
        mEmergency = true;
        setContentView(R.layout.emergencylist);
    }
}
