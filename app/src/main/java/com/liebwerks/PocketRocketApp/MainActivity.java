package com.liebwerks.PocketRocketApp;

import android.graphics.Color;
import android.media.Image;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View;
import android.graphics.PorterDuff;

import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.AudioManager;
import android.net.Uri;
import android.app.Activity;

import android.location.Location;
import android.location.LocationManager;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.os.Handler;
import android.content.Context;
import android.widget.ImageButton;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private static Context context;
    private static final int READ_REQUEST_CODE = 42;
    private static final int LOAD_MUSIC_REQUEST_CODE = 43;

    LinearLayout rl = null;

    int activePointerId;
    MediaPlayer mPlayer;
    FileInputStream fis;
    InputStream inputStream;

    PocketRocketLocationListener pocketRocketLocationListener;
    SendRGBCommand sendRGBCommand;

    boolean redState = false,
            blueState = false,
            greenState = false,
            orangeState = false,
            yellowState = false,
            purpleState = false;

    boolean recordMode = false,
            playbackMode = false,
            stopMode = true;

    Button redButton,
            greenButton,
            blueButton,
            orangeButton,
            yellowButton,
            purpleButton;

    ImageButton recordButton,
           stopButton,
           playButton,
           resetButton;

    ArrayList recordedSequence;

    ArrayList<Integer> musicFileList;
    Iterator musicFileListIterator = null;

    ArrayList<Integer> lightsFileList;
    Iterator lightsFileListIterator = null;

    long counter = 0l;

    Timer timer;
    TimerTask timerTask;

    /* experimental sensor stuff, remmed out for now */
    //private SensorManager senSensorManager;
    //private Sensor senAccelerometer;

    boolean state = false;

    final Handler handler = new Handler();

    int white = 0xFFFFFFFF;
    int red = 0xFFFF0000;
    int green = 0xFF00FF00;
    int blue = 0xFF0000FF;
    int orange = 0xFFFFA500;
    int yellow = 0xFFFFFF00;
    int purple = 0xFF8A2BE2;    

    byte[] offByteArray = {(byte)0x71,(byte)0x24, (byte)0x0f,(byte)0xa4};
    byte[] onByteArray = {(byte)0x71,(byte)0x23, (byte)0x0f,(byte)0xa3};//on!

    byte[] realredByteArray = {(byte)0x31, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0f, (byte)0xb4};
    byte[] realgreenByteArray = {(byte)0x31, (byte)0x00, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0f, (byte)0xb4};
    byte[] realblueByteArray = {(byte)0x31, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x0f, (byte)0xb4};
    byte[] realorangeByteArray = {(byte)0x31, (byte)0xff, (byte)0xA5, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0f, (byte)0xb4};
    byte[] realyellowByteArray = {(byte)0x31, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0f, (byte)0xb4};
    byte[] realpurpleByteArray = {(byte)0x31, (byte)0x8A, (byte)0x2B, (byte)0xE2, (byte)0x00, (byte)0x00, (byte)0x0f, (byte)0xb4};
    byte[] realoffByteArray = {(byte)0x31, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0f, (byte)0xb4};

    final RgbCommand realred = new RgbCommand("red",realredByteArray);
    final RgbCommand realgreen = new RgbCommand("green",realgreenByteArray);
    final RgbCommand realblue = new RgbCommand("blue",realblueByteArray);
    final RgbCommand realorange = new RgbCommand("orange",realorangeByteArray);
    final RgbCommand realyellow = new RgbCommand("yellow",realyellowByteArray);
    final RgbCommand realpurple = new RgbCommand("purple",realpurpleByteArray);
    final RgbCommand realoff = new RgbCommand("realoff",realoffByteArray);

    String musicFilename = "";

    Iterator playbackIterator;

    /* experimenting with touch coordinates instead of button presses, not currently used */
    public boolean onTouchEvent(MotionEvent event) {
        activePointerId = event.getPointerId(0);

        int pointerIndex = event.findPointerIndex(activePointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        Log.d("TouchEvent", "X is " + x + " and y is " + y + "and pointer index is " + pointerIndex);
        return(true);
    }

    public void startTimer() {
        Log.d("Sequence","Started startTimer()");
        recordedSequence = new ArrayList();
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        timerTask = new TimerTask() {
            public void run() {
                counter ++;
                if(recordMode == true) {
                    recordedSequence.add(new ButtonState(counter, redState, greenState, blueState, orangeState, yellowState, purpleState));
                }

                if(playbackMode == true) {
                    if(playbackIterator.hasNext()) {
                        ButtonState state = (ButtonState) playbackIterator.next();
                        if(state.redState != redState) {
                            redState = state.redState;
                            if(state.redState == true) {
                                sendRGBCommand.command = realred;

                            }
                            else {
                                sendRGBCommand.command = realoff;
                            }
                            new Thread(sendRGBCommand).start();
                            Log.d("STATE","RED At: " + state.time);
                        }
                        else {
                            if (state.greenState != greenState) {
                                greenState = state.greenState;
                                if (state.greenState == true) {
                                    sendRGBCommand.command = realgreen;
                                } else {
                                    sendRGBCommand.command = realoff;
                                }
                                new Thread(sendRGBCommand).start();
                            } else {
                                if (state.blueState != blueState) {
                                    blueState = state.blueState;
                                    if (state.blueState == true) {
                                        sendRGBCommand.command = realblue;
                                    } else {
                                        sendRGBCommand.command = realoff;
                                    }
                                    new Thread(sendRGBCommand).start();
                                }
                                else {
                                    if (state.orangeState != orangeState) {
                                        orangeState = state.orangeState;
                                        if (state.orangeState == true) {
                                            sendRGBCommand.command = realorange;
                                        } else {
                                            sendRGBCommand.command = realoff;
                                        }
                                        new Thread(sendRGBCommand).start();
                                    }
                                    else {
                                        if (state.yellowState != yellowState) {
                                            yellowState = state.yellowState;
                                            if (state.yellowState == true) {
                                                sendRGBCommand.command = realyellow;
                                            } else {
                                                sendRGBCommand.command = realoff;
                                            }
                                            new Thread(sendRGBCommand).start();
                                        }
                                        else {
                                            if (state.purpleState != purpleState) {
                                                purpleState = state.purpleState;
                                                if (state.purpleState == true) {
                                                    sendRGBCommand.command = realpurple;
                                                } else {
                                                    sendRGBCommand.command = realoff;
                                                }
                                                new Thread(sendRGBCommand).start();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        //schedule the timer, after the first 1000ms the TimerTask will run every 1000ms
        timer.schedule(timerTask, 50l, 50l); //
    }

    protected void loadMusic(Integer resourceId) {
        Log.d("loadMusic", "Loading resource ID " + resourceId);
        mPlayer = new MediaPlayer().create(this, resourceId);
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                Log.d("COMPLETE", "Completion event 2");
                if(musicFileListIterator.hasNext()) {
                    loadMusic((Integer)musicFileListIterator.next());
                    mPlayer.start();
                }
                if(lightsFileListIterator.hasNext()) {
                    InputStream inputStream = context.getResources().openRawResource((Integer)lightsFileListIterator.next());
                    loadLightShow(inputStream);
                    //loadMusic((Integer)lightsFileListIterator.next()); !!!!
                    //mPlayer.start();
                }
            }
        });

        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //mPlayer.setDataSource(inputStream.);
        try {
            //mPlayer.prepare();
        }
        catch(Exception e) {
            Log.e("loadMusic", "Failed to load music with resource ID " + resourceId);
            e.printStackTrace();
        }

    }

    protected void loadMusic(Uri uri) {
        try {
            inputStream = getContentResolver().openInputStream(uri);
            //Uri myUri = Uri.parse("file:///storage/emulated/0/Download/SeV15_SlingShotAlarm.mp3");
            //fis = new FileInputStream("/storage/emulated/0/SeV15_SlingShotAlarm.mp3");
            //fis = new FileInputStream("/storage/emulated/0/JoeWalshRockyMountainWay.mp3");
            //fis = new FileInputStream("/storage/emulated/0/Meghan Trainor - All About That Bass.mp3");
            //fis = new FileInputStream("/storage/emulated/0/bottle_90bpm_4-4time_610beats_T1yMuB.mp3");
            //fis = new FileInputStream(filename);

            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(this, uri);
            //mPlayer.setDataSource(inputStream.);
            mPlayer.prepare();
        }
        catch(Exception e) {
            Log.e("MediaPlayer", "Could not load file: " + e.getMessage());
            e.printStackTrace();
        }
    }

     void loadMusic(String filename) {
        try {
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(filename);
            mPlayer.prepare();

        }
        catch(Exception e) {
            Log.e("MediaPlayer", "Could not load file" + e.getMessage());
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.context = getApplicationContext();

        musicFileList = new ArrayList<Integer>();
        musicFileList.add(R.raw.desk_bell_sound);
        musicFileList.add(R.raw.polaris_audio_vo1);
        musicFileList.add(R.raw.polaris_audio_music1);
        musicFileList.add(R.raw.polaris_audio_vo2);
        musicFileList.add(R.raw.polaris_audio_music2);
        musicFileList.add(R.raw.polaris_audio_vo3);
        musicFileList.add(R.raw.polaris_audio_music3);
        musicFileList.add(R.raw.polaris_audio_vo4);
        musicFileList.add(R.raw.polaris_audio_music4);
        musicFileList.add(R.raw.polaris_audio_vo5);

        lightsFileList = new ArrayList<Integer>();
        lightsFileList.add(R.raw.desk_bell_lights);
        lightsFileList.add(R.raw.polaris_lights_vo1);
        //lightShowFileList.add(R.raw.polaris_lights_music1);
        //lightShowFileList.add(R.raw.polaris_lights_vo2);
        //lightShowFileList.add(R.raw.polaris_lights_music2);
        //lightShowFileList.add(R.raw.polaris_lights_vo3);
        //lightShowFileList.add(R.raw.polaris_lights_music3);
        //lightShowFileList.add(R.raw.polaris_lights_vo4);
        //lightShowFileList.add(R.raw.polaris_lights_music4);
        //lightShowFileList.add(R.raw.polaris_lights_vo5);

        sendRGBCommand = new SendRGBCommand();

        /* included when hunting down the ip address of the light controller */
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        Log.d("IP",wm.getConnectionInfo().toString());
        Log.d("IP","IP address is: " + ip);

        /* hack to set the ip address, whether it is connecting to access point (10...) or as member of network (192...) */
        if(ip.toString().startsWith("10.10.123")) {
            sendRGBCommand.ip = "10.10.123.3";
        }
        else {
            sendRGBCommand.ip = "192.168.0.29";
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleSentStuff(intent);
        }

        //rl = (LinearLayout)findViewById(R.id.lay);


        /* Log the location of the app all the time */
        pocketRocketLocationListener = new PocketRocketLocationListener();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000l, 0, pocketRocketLocationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null) {
            Log.d("PocketRocketLocationListener", "Can't get location");
        }
        else {
            Log.d("PocketRocketLocationListener", "Initial location: " + location.getLatitude() + ", " + location.getLongitude() + " Speed: " + location.getSpeed());
        }

        /* rem out the sensor stuff for now
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        */

        /* not currently used */
        //RgbCommand off = new RgbCommand("off", offByteArray); //off!
        //RgbCommand on = new RgbCommand("on", onByteArray);

        sendRGBCommand = new SendRGBCommand();

        redButton = (Button) findViewById(R.id.redButton);
        greenButton = (Button) findViewById(R.id.greenButton);
        blueButton = (Button) findViewById(R.id.blueButton);
        orangeButton = (Button) findViewById(R.id.orangeButton);
        yellowButton = (Button) findViewById(R.id.yellowButton);
        purpleButton = (Button) findViewById(R.id.purpleButton);

        recordButton = (ImageButton) findViewById(R.id.recordButton);
        stopButton = (ImageButton) findViewById(R.id.stopButton);
        playButton = (ImageButton) findViewById(R.id.playButton);
        resetButton = (ImageButton) findViewById(R.id.resetButton);

        redButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        greenButton.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
        blueButton.getBackground().setColorFilter(0xFF0000FF, PorterDuff.Mode.MULTIPLY);
        orangeButton.getBackground().setColorFilter(0xFFFFA500, PorterDuff.Mode.MULTIPLY);
        yellowButton.getBackground().setColorFilter(0xFFFFFF00, PorterDuff.Mode.MULTIPLY);
        purpleButton.getBackground().setColorFilter(0xFF8A2BE2, PorterDuff.Mode.MULTIPLY);

        redButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TAG", "Red");
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    redState = true;
                    try {
                        sendRGBCommand.command = realred;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending red");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    redState = false;
                    try {
                        sendRGBCommand.command = realoff;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending off red");
                    }
                }
                return(true);
            }
        });

        greenButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                greenState = true;
                Log.d("TAG", "Green");
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        sendRGBCommand.command = realgreen;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending green");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    greenState = false;
                    try {
                        sendRGBCommand.command = realoff;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending green off");
                    }
                }
                return(true);
            }
        });

        blueButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TAG", "Blue");
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    blueState = true;
                    try {
                        sendRGBCommand.command = realblue;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending blue");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    blueState = false;
                    try {
                        sendRGBCommand.command = realoff;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending blue");
                    }
                }
                return (true);
            }
        });

        orangeButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TAG", "Orange");
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    orangeState = true;
                    try {
                        sendRGBCommand.command = realorange;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending orange");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    orangeState = false;
                    try {
                        sendRGBCommand.command = realoff;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending orange");
                    }
                }
                return (true);
            }
        });

        yellowButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TAG", "Yellow");
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    yellowState = true;
                    try {
                        sendRGBCommand.command = realyellow;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending blue");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    yellowState = false;
                    try {
                        sendRGBCommand.command = realoff;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending blue");
                    }
                }
                return (true);
            }
        });

        purpleButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TAG", "Purple");
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    purpleState = true;
                    try {
                        sendRGBCommand.command = realpurple;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending purple");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    purpleState = false;
                    try {
                        sendRGBCommand.command = realoff;
                        new Thread(sendRGBCommand).start();
                    } catch (Exception e) {
                        Log.e("TAG", "Error sending purple");
                    }
                }
                return (true);
            }
        });

        Log.d("COLOR", "Background color is " + recordButton.getDrawingCacheBackgroundColor());
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(recordMode != true) {
                    setButtonState(recordButton);
                    recordMode = true;
                    playbackMode = false;
                    stopMode = false;
                    //mPlayer.seekTo(82900);
                    mPlayer.seekTo(0);
                    mPlayer.start();
                }
                return (true);
            }
        });

        playButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(playbackMode != true) {
                    setButtonState(playButton);
                    playbackIterator = recordedSequence.iterator();
                    recordMode = false;
                    playbackMode = true;
                    stopMode = false;
                    counter = 0;
                    //mPlayer.seekTo(82900);
                    mPlayer.seekTo(0);
                    mPlayer.start();
                }
                return (true);
            }
        });

        stopButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                setButtonState(stopButton);
                if(stopMode == false) {
                    recordMode = false;
                    playbackMode = false;
                    stopMode = true;
                    mPlayer.stop();
                    try {
                        mPlayer.prepare();
                    } catch (Exception e) {
                        Log.e("MediaPlayer", "Error preparing media for playback" + e.getMessage());
                    }
                    /* Save the recorded light sequence */
                    //writeLightSequenceToFile();
                }
                return (true);
            }
        });

        /* reset button clears the sequence */
        resetButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                recordedSequence.clear();
                recordMode = false;
                playbackMode = false;
                return (true);
            }
        });

        startTimer();

        mPlayer = new MediaPlayer();

        /* initial load of music */
        musicFileListIterator = musicFileList.iterator();
        if(musicFileListIterator.hasNext()) {
            loadMusic((Integer)musicFileListIterator.next());
        }

        /* initial load of lights */
        lightsFileListIterator = lightsFileList.iterator();
        if(lightsFileListIterator.hasNext()) {
            InputStream inputStream = context.getResources().openRawResource((Integer)lightsFileListIterator.next());
            loadLightShow(inputStream);
        }
    }

    public void handleSentStuff(Intent intent) {
        Log.d("INTENT", intent.getData().toString());
    }


    public void writeLightSequenceToFile() {
        String filename = musicFilename + "_" + (System.currentTimeMillis() / 1000) + ".txt";

        try {
            /* @TODO: get the real path of the storage location */
            //String path = getFilesDir().getAbsolutePath();
            String path = "/storage/emulated/0/Download/";
            String pathAndFilename = path + filename;
            File theFile = new File(pathAndFilename);
            boolean createFileSuccess = theFile.createNewFile();
            if(createFileSuccess == true) {
                Log.d("writeLightSequenceToFile","Created file: " + pathAndFilename);
            }
            else {
                Log.d("writeLightSequenceToFile","Failed to create file");
            }
            theFile.setReadable(true,false);

            FileOutputStream outputStream = new FileOutputStream(theFile);

            playbackIterator = recordedSequence.iterator();
            while (playbackIterator.hasNext()) {
                ButtonState tempButtonState = (ButtonState)playbackIterator.next();
                outputStream.write((tempButtonState.toString() + "\n").getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            Log.e("writeLightSequenceToFile", "Error writing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setButtonState(ImageButton button) {
        if(button.equals(recordButton)) {
            button.setBackgroundColor(Color.RED);
            playButton.setBackgroundColor(Color.LTGRAY);
        }
        if(button.equals(playButton)) {
            button.setBackgroundColor(Color.RED);
            recordButton.setBackgroundColor(Color.LTGRAY);
        }
        if(button.equals(stopButton)) {
            recordButton.setBackgroundColor(Color.LTGRAY);
            playButton.setBackgroundColor(Color.LTGRAY);
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public Handler _handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("MESSAGE", "Handler.handleMessage(): msg=" + msg);

            if(rl != null) {
                rl.setBackgroundColor(msg.what);
            }
        }
    };


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

        if(id == R.id.action_load) {
            performFileSearchForLightShow();
        }

        if(id == R.id.action_load_music) {
            performFileSearchForMusic();
        }        

        if(id == R.id.action_save) {
            /* Save the recorded light sequence */
            writeLightSequenceToFile();
        }

        return super.onOptionsItemSelected(item);
    }

    /* Sensor stuff is not used for now */
    public void onSensorChanged(SensorEvent sensorEvent) {
            Sensor mySensor = sensorEvent.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = Math.abs(sensorEvent.values[0]);
                float y = Math.abs(sensorEvent.values[1]);
                float z = Math.abs(sensorEvent.values[2]);
                if(x > 10.0 || y > 10.0 || z > 10.0) {
                    float shakeStrength = Math.max(x, Math.max(y, z));
                    if(shakeStrength > 30) {
                        sendRGBCommand.command = realred;

                    }
                    else {
                        if(shakeStrength > 20) {
                            sendRGBCommand.command = realblue;

                        }
                        else {
                            if(shakeStrength > 10) {
                                sendRGBCommand.command = realgreen;

                            }
                        }

                    }
                    new Thread(sendRGBCommand).start();
                    //startTimer();
                }
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /* @TODO: pick the light sequence to load */
    public void performFileSearchForLightShow() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        //intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("text/plain");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }


    public void performFileSearchForMusic() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        //intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, LOAD_MUSIC_REQUEST_CODE);
    }


    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == LOAD_MUSIC_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.d("FileRead", "MUSIC Uri: " + uri.getLastPathSegment());
                musicFilename = uri.getLastPathSegment().replace(".mp3", "").replace("primary:", "");
                loadMusic(uri);
                //MediaPlayer mPlayer = MediaPlayer.create(this,R.raw.polaris_audio_music1);
                List<File> theList = getListFiles("/storage/emulated/0/Download/", musicFilename);
                Iterator iterator = theList.iterator();
                while(iterator.hasNext()) {
                    File theFile = (File)iterator.next();
                    Log.d("FILES", "the file is: " + theFile.getName());
                }

                /* Load the light show that is the last file in the list returned (newest, sorted by filename) */
                if(theList.size() > 0) {
                    try {
                        Log.d("Load","Trying to load: " + theList.toArray()[theList.size() - 1]);
                        InputStream inputStream = new FileInputStream((File)theList.toArray()[theList.size() - 1]);
                        loadLightShow(inputStream);
                    } catch (Exception e) {
                        Log.e("ERROR", "Error loading light show");
                        e.printStackTrace();
                    }
                }
                else {
                    Log.d("ERROR", "No light show to load");
                }


                //loadMusic("/storage/emulated/0/" + uri.getLastPathSegment());
            }
        }
        else {
            if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (resultData != null) {
                    uri = resultData.getData();
                    Log.d("FileRead", "Uri: " + uri.getPath());
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        loadLightShow(inputStream);
                    }
                    catch(Exception e) {
                        Log.e("ERROR", "Error loading light show for music");
                    }

                }
            } else {
                Log.d("FileRead", "Something else happened: ignore");
            }
        }
    }

    void loadLightShow(InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            recordedSequence.clear();
            String line;
            while ((line = br.readLine()) != null) {
                Log.d("loadLightShow", line);
                String[] read = line.split(",");
                recordedSequence.add(new ButtonState(Long.parseLong(read[0]), Boolean.valueOf(read[1]), Boolean.valueOf(read[2]), Boolean.valueOf(read[3]), Boolean.valueOf(read[4]), Boolean.valueOf(read[5]), Boolean.valueOf(read[6])));

            }
            playbackIterator = recordedSequence.iterator();
        } catch (Exception e) {
            Log.e("loadLightShow", "Unable to get InputStream: " + e.getMessage());
        }
    }

    private List<File> getListFiles(String parentDir, String songTitle) {
        File parentDirFile = new File(parentDir);
        Log.d("getListFiles","It is " + parentDirFile);
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDirFile.listFiles();
        Log.d("getListFiles","It is 2: " + files);
        for (File file : files) {
            if (file.isDirectory()) {
                //ignore
            } else {
                Log.d("getListFiles", "Filename: " + file.getName());
                if(file.getName().startsWith(songTitle)){
                    inFiles.add(file);
                }
            }
        }
        Collections.sort(inFiles);
        return inFiles;
    }
}

