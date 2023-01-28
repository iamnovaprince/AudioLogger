package com.novaapps.audiologger.fragments;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.novaapps.audiologger.R;
import com.novaapps.audiologger.database.DBHandler;
import com.novaapps.audiologger.database.StopWatch;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import zeroonezero.android.audio_mixer.AudioMixer;
import zeroonezero.android.audio_mixer.input.AudioInput;
import zeroonezero.android.audio_mixer.input.BlankAudioInput;
import zeroonezero.android.audio_mixer.input.GeneralAudioInput;

public class RecorderFragment extends Fragment {



    public RecorderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recorder, container, false);
    }

    ImageView recordButton;
    TextView timerView;

    boolean isActive = false;
    private String recordDate = "";
    private String recordTime = "";
    private String recordHash = "";
    private boolean running;
    private long seconds = 0;
    private static String mFileName = null;
    private MediaRecorder mRecorder;
    private DBHandler dbHandler;
    private Button mergeButton;
    String newFileName = null ;
    public static String todayHash;
    public static String DIRECTORY_TODAY ;
    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHandler = new DBHandler(getContext());
        init(view);

        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHandler.updateHash(newFileName,todayHash);
            }
        });

        File file = new File(DIRECTORY_TODAY);
        if(!file.exists()){
            file.mkdirs();
        }

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("TAG","Pressed");
                        if (CheckPermissions()) {
                            try {
                                if(!isActive)
                                    startRecording();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(getActivity(), "Permission request", Toast.LENGTH_SHORT).show();
                            RequestPermissions();
                        }
//
                        return true;
                    case MotionEvent.ACTION_UP:
                        recordButton.setImageResource(R.drawable.ic_recorder_circle);
                        Log.d("TAG","Un-Pressed");
                        if (CheckPermissions()) {
                            if(isActive) {
                                stopRecording();
                            }
                        }else{
                            RequestPermissions();
                        }
//                        recordButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
//                        Log.i("TAG", "touched up");
                        return true;
                }
                return false;
            }
        });

    }

    void init(View view){
        mergeButton = view.findViewById(R.id.mergeButton);
        recordButton = view.findViewById(R.id.recordButton);
        timerView = view.findViewById(R.id.timerView);
        todayHash = getTodayDateHashed();
        DIRECTORY_TODAY = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Log/"+todayHash+"/";
    }

    private void mergeAudio(String prevAudio) {
//        AudioInput input1 = new GeneralAudioInput();
        AudioInput input1 ;
        AudioInput input2;
        AudioMixer audioMixer;
        newFileName = getCurrentTimeHashed();
        String output = DIRECTORY_TODAY + newFileName + ".mp3";
        try {
           input1 = new GeneralAudioInput(DIRECTORY_TODAY+prevAudio+".mp3");
           input2 = new GeneralAudioInput(mFileName);
           audioMixer = new AudioMixer(output);
           audioMixer.addDataSource(input1);
           audioMixer.addDataSource(input2);
           audioMixer.setMixingType(AudioMixer.MixingType.SEQUENTIAL);
           audioMixer.setProcessingListener(new AudioMixer.ProcessingListener() {
               @Override
               public void onProgress(double progress) {
                   Log.d("TAG-Mixer","mixing in progress " + progress);
               }

               @Override
               public void onEnd() {
//                   Toast.makeText(getContext(), "Mixing complete", Toast.LENGTH_SHORT).show();
                   audioMixer.release();
               }
           });

           audioMixer.start();
           audioMixer.processAsync();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    private void runTimer()
    {
        // Creates a new Handler
        final Handler handler
                = new Handler();
        running = true;
        handler.post(new Runnable() {
            @Override

            public void run()
            {

                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                // Set the text view text.
                timerView.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                    Log.d("TAG_WATCH","running");
                    handler.postDelayed(this, 1000);
                }else{
                    seconds = 0;
                    timerView.setText("0:00:00");
                }

                // Post the code again
                // with a delay of 1 second.

            }
        });
    }

    private void stopTimer(){
        running = false;
    }

    boolean isDirectoryInit() throws NoSuchAlgorithmException {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Log/" + getTodayDateHashed();
        File file = new File(path);
        if(!file.isDirectory() || !file.exists()){
            return file.mkdirs();
        }
        return false;
    }

    void startRecording() throws NoSuchAlgorithmException {
        runTimer();
        recordButton.setImageResource(R.drawable.ic_recorder_red);
        Log.d("TAG", "Recording Started");

        recordHash = getCurrentTimeHashed();

        mFileName = DIRECTORY_TODAY + recordHash +".mp3";

        if (Environment.isExternalStorageManager()){
            Log.d("TAG","Access");
        }else{
            Log.d("TAG","No Access");
        }

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed " + e);
        }
        isActive = true;
    }

    void stopRecording(){
        if(mRecorder == null) return;
        stopTimer();
        try {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            isActive = false;
        }catch(RuntimeException stopException) {
        // handle cleanup here
            Log.d("TAG-ERROR",stopException.toString());
    }
    }

    String getCurrentTimeHashed(){
        recordTime = String.valueOf(System.currentTimeMillis());
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // digest() method is called to calculate message digest
            // of an input digest() return array of byte
            byte[] messageDigest = md.digest(recordTime.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            recordHash = hashtext;
            return recordHash;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    String getTodayDateHashed()  {
        DateTimeFormatter dtf = null;
        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        recordDate = dtf.format(now);
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // digest() method is called to calculate message digest
            // of an input digest() return array of byte
            byte[] messageDigest = md.digest(recordDate.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(getActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }


}