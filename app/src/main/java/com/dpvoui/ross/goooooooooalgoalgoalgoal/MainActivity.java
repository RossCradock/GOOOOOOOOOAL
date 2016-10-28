package com.dpvoui.ross.goooooooooalgoalgoalgoal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
// MainActivity class requires an onCreate method to initialise the app screen and record its current state if paused
// When MainActivity implements SensorEventListener the methods onAccuracyChanged and onSensorChanged must be created

    private SensorManager sensorManger;
    private Sensor accelerometer; // SensorManager and Sensor are the API classes used to receive raw data from android accelerometer
    private Boolean accelerometerPresent;
    private long timeSignature;
    private MediaPlayer mediaPlayerGoalRecurring;
    private MediaPlayer mediaPlayerGoalLong;
    private long timerStart;
    private long shakeTimeStamp;
    private int shakeCount;
    private static final float SHAKE_THRESHOLD_GRAVITY = 6f;
    private static final int SHAKE_SLOP_TIME_MS = 100;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ToggleButton onOff = (ToggleButton)findViewById(R.id.toggleButton);
        final RadioButton goalG = (RadioButton) findViewById(R.id.radioG);
        final RadioButton goalg = (RadioButton) findViewById(R.id.radiog);
        final Button buttonG = (Button)findViewById(R.id.buttonG);
        final Button buttong = (Button)findViewById(R.id.buttong);
        final Button start = (Button)findViewById(R.id.start);
        final EditText editText = (EditText)findViewById(R.id.editText);
        final Button about = (Button)findViewById(R.id.about);

        final AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        final int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mediaPlayerGoalRecurring = MediaPlayer.create(this, R.raw.goalgoalgoal);
        mediaPlayerGoalLong = MediaPlayer.create(this, R.raw.goooooooooal);
        sensorManger = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        timeSignature = System.currentTimeMillis();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if(android.provider.Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1){
            Toast.makeText(getApplicationContext(), "Auto-Rotate Must Be Turned Off For Shake Sensors To Work Properly", Toast.LENGTH_LONG).show();
        }

        if(sensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null){ // Check to see if the phone has a operational accelerometer
            // Accelerometer not found
            accelerometerPresent = false;
            Toast.makeText(getApplicationContext(), "Warning: No Accelerometer Detected", Toast.LENGTH_LONG).show();
        } else{
            // Accelerometer found
            accelerometer = sensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // Initialise the accelerometer
            accelerometerPresent = true;
        }

        buttong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // goal is on ie goal goal goal
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                if (goalg.isChecked()) {
                    buttonG.setEnabled(false);
                    mediaPlayerGoalRecurring.start();
                } else{
                    goalg.setChecked(true);
                    goalG.setChecked(false);
                    editor.putBoolean("goal", true);
                    editor.apply();
                    buttonG.setEnabled(false);
                    mediaPlayerGoalRecurring.start();
                }
            }
        });

        buttonG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                if (goalG.isChecked()) {
                    buttong.setEnabled(false);
                    mediaPlayerGoalLong.start();
                }else{
                    goalG.setChecked(true);
                    goalg.setChecked(false);
                    editor.putBoolean("goal", false);
                    editor.apply();
                    buttong.setEnabled(false);
                    mediaPlayerGoalLong.start();
                }
            }
        });

        onOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appOnOff();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button start = (Button) v;
                if(start.getText().equals("Stop")){
                    timeHandler.removeCallbacks(timeRunnable);
                    start.setText(R.string.startMatchTimerButton);
                    editText.setText(R.string.startMatchTimerNumber);
                    if(onOff.isChecked()){
                        onOff.setChecked(false);
                        appOnOff();
                    } else{
                        appOnOff();
                    }
                } else{
                    timerStart = System.currentTimeMillis();
                    timeHandler.postDelayed(timeRunnable, 0);
                    start.setText(R.string.stopMatchTimerButton);
                    onOff.setChecked(true);
                    appOnOff();
                }
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, about.class);
                startActivity(i);
            }
        });

        mediaPlayerGoalLong.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                buttong.setEnabled(true);
            }
        });

        mediaPlayerGoalRecurring.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                buttonG.setEnabled(true);
            }
        });

    }

    //--------------------------------------------------------------------------------------------\\

    // handeler for the timer
    Handler timeHandler = new Handler();
    Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            final EditText editText = (EditText)findViewById(R.id.editText);
            final ToggleButton onoff = (ToggleButton)findViewById(R.id.toggleButton);
            final Button start = (Button)findViewById(R.id.start);
            long millis = (System.currentTimeMillis() - (timerStart + 6300000)) * (-1);
            if(millis < 1){
                timeHandler.removeCallbacks(timeRunnable);
                start.setText(R.string.startMatchTimerButton);
                editText.setText(R.string.startMatchTimerNumber);
                if(onoff.isChecked()){
                    onoff.setChecked(false);
                    appOnOff();
                } else{
                    appOnOff();
                }
                return;
            }
            int seconds = (int) (millis/1000);
            int minutes = seconds/60;
            seconds = seconds%60;
            editText.setText(String.format("%d:%02d", minutes, seconds));
            timeHandler.postDelayed(this, 500);
        }
    };

    public void onAccuracyChanged(Sensor sensor, int i){
        // onAccuracyChanged method is mandatory when implementing SensorEventListener, but is not required
    }

    public void onSensorChanged(SensorEvent event){
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();
            // ignore shake events too close to each other (400ms)
            if (shakeTimeStamp + SHAKE_SLOP_TIME_MS > now) {
                return;
            }
            // reset the shake count after 3 seconds of no shakes
            if (shakeTimeStamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCount = 0;
            }
            shakeTimeStamp = now;
            shakeCount++;
            if (shakeCount > 2){
                play();
            }
        }
    }

    public void play(){
        final AudioManager mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean goal = sharedPreferences.getBoolean("goal", false);
        final MediaPlayer mediaPlayerGoalRecurring = MediaPlayer.create(this, R.raw.goalgoalgoal);
        final MediaPlayer mediaPlayerGoalLong = MediaPlayer.create(this, R.raw.goooooooooal);

        if((timeSignature + 10000) < System.currentTimeMillis()){
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

            if(goal){
                mediaPlayerGoalRecurring.start();
                timeSignature = System.currentTimeMillis();
            } else{
                mediaPlayerGoalLong.start();
                timeSignature = System.currentTimeMillis();
            }
        }

        mediaPlayerGoalLong.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                mediaPlayerGoalLong.release();
            }
        });

        mediaPlayerGoalRecurring.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                mediaPlayerGoalRecurring.release();
            }
        });
    }

    public void appOnOff(){
        final ToggleButton onoff = (ToggleButton)findViewById(R.id.toggleButton);

        if(accelerometerPresent) {
            if (onoff.isChecked()) {
                // Toggle button is turned on
                registerListener();
            }

            if (!onoff.isChecked()) {
                // Toggle button is turned off
                unregisterListener();
            }
        }else{
            Toast.makeText(getApplicationContext(),"No Accelerometer present",Toast.LENGTH_LONG).show();
            onoff.setChecked(false);
        }
    }

    public void registerListener(){
        sensorManger.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST); // Register a listener for accelerometer changes as fast as possible
    }

    public void unregisterListener(){
        sensorManger.unregisterListener(this);
    }

    public void onPause(){
        super.onPause();
    }

    public void onResume(){
        super.onResume();
    }

    public void onDestroy(){
        super.onDestroy();
        unregisterListener();
        mediaPlayerGoalRecurring.release();
        mediaPlayerGoalLong.release();
    }

}