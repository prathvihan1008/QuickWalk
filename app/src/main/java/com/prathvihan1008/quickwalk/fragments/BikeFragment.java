package com.prathvihan1008.quickwalk.fragments;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.prathvihan1008.quickwalk.MainActivity;
import com.prathvihan1008.quickwalk.MyDBHelper;
import com.prathvihan1008.quickwalk.OnStartButtonClickListener;
import com.prathvihan1008.quickwalk.R;
import com.prathvihan1008.quickwalk.StepCountingService;
import com.prathvihan1008.quickwalk.TTSListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BikeFragment extends Fragment implements SensorEventListener,TextToSpeech.OnInitListener , TTSListener {
    private LocationManager locationManager;//speed
    private SensorManager mSensorManager=null;
    private Sensor stepSensor;
    private int totalSteps=0;
    private int previewsTotalSteps=0;

    private ProgressBar progressBar;
   // private TextView steps;


    private TextView distance,calories,tvTimer,notation,speed;
    private ImageView ivPauseResume,savebtn;
    private long startTime = 0L;
    private long elapsedTime = 0L;
    private boolean isRunning = false;
    private boolean isSensorRegistered=false;

    private Handler handler = new Handler();
    private TextToSpeech textToSpeech;
    private double selectedDis;
    public static final String PREFS_NAME="MyPref1";
    public static final String FRAGMENT_LOADED_KEY="FragmentLoaded";
    public static boolean isFirstTimeLoaded;



    private TextView countdownText;
    private boolean flag=true;
    private boolean isButtonEnabled = true;
    private OnStartButtonClickListener listener;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike, container, false);

      //  goalEditText=view.findViewById(R.id.goalEditText);
        distance=view.findViewById(R.id.distance);
        calories=view.findViewById(R.id.calories);
        tvTimer=view.findViewById(R.id.tvTimer);
        notation=view.findViewById(R.id.notation);
        ivPauseResume=view.findViewById(R.id.ivPauseResume);
        savebtn=view.findViewById(R.id.savebtn);
        speed=view.findViewById(R.id.speed);


        progressBar = view.findViewById(R.id.progressBar);
        countdownText = view.findViewById(R.id.countdown_text);

        // steps = view.findViewById(R.id.steps);
       // steps.setText(String.valueOf(0));

        progressBar.setMax(100);
        progressBar.setProgress(0);

        textToSpeech = new TextToSpeech(getContext(), this);


        //Spinner for Goal selection
        Spinner spinnerDisGoal = view.findViewById(R.id.spinnerDisGoal);
        // Generating steps from 50 to 10000 with a gap of 500
        double mindis = 0.5;
        double maxdis = 10.0;
        double gap = 0.5;
       String[] stepsArray = new String[20];
        int arrayLength = (int) Math.ceil((maxdis - mindis) / gap) + 1;


        for (int i = 0; i < arrayLength; i++) {
            double value = mindis + i * gap;
            stepsArray[i] = String.valueOf(value);
        }
        // Set up the spinner with a custom adapter
        // Use a custom layout for the dropdown items
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.custom_spinner_dropdown_item, stepsArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisGoal.setAdapter(adapter);

        spinnerDisGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Retrieve the selected steps
                String selectedDisg = stepsArray[position].replaceAll("[^\\d.]", ""); // Extract numeric value
                selectedDis = Float.parseFloat(selectedDisg);
                // Update ProgressBar max value
                progressBar.setMax(100);//using percentage concept since progress bar do not accept float values
                progressBar.setProgress(0);

                // You can also update the ProgressBar progress if needed
                // progressBar.setProgress(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });




        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick(v);
            }
        });



        ivPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isButtonEnabled) {
                    onPauseResumeClick(v);
                }
            }
        });

        resetSteps();
        loadData();

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);





        //solving the steps problem
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isFirstTimeLoaded = preferences.getBoolean(FRAGMENT_LOADED_KEY, true);


        return view;
    }

    public void startTTS(String text) {
        // Initialize Text-to-Speech if not initialized or if it has been stopped
        if (textToSpeech == null || !textToSpeech.isSpeaking()) {
            textToSpeech = new TextToSpeech(getContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                   // Toast.makeText(getContext(), "Start TTS", Toast.LENGTH_SHORT).show();
                    // TTS initialized successfully
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    // Handle TTS initialization failure
                    Toast.makeText(getContext(), "Failed to start TTS", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // TTS is already initialized, speak the new text
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void stopTTS() {
        //   speakText( "off");
        // Stop Text-to-Speech
        if (textToSpeech != null) {


            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null; // Set to null after stopping
           // Toast.makeText(getContext(), "TTS Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Text-to-speech engine is initialized successfully.
            textToSpeech.setLanguage(Locale.getDefault());
        } else {
            // Handle initialization failure.
            Log.e("TextToSpeech", "Initialization failed");
        }
    }
    @Override
    public void onDestroyView() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroyView();
    }


    public   void onResume()
    {
        super.onResume();
        if(isRunning){
            registerSensor();

        }


    }

    public void onPause()
    {
        super.onPause();
        unregisterSensor();
    }


    public void onPauseResumeClick(View view) {


        if (isSensorRegistered) {
            unregisterSensor();
        } else {
            registerSensor();
        }

        if (isRunning) {
            // Pause
            isRunning = false;
            savebtn.setVisibility(View.VISIBLE);

            handler.removeCallbacks(updateTimerTask);
            ivPauseResume.setImageResource(R.drawable.resume);
            notation.setText("Start");
        } else {
            // Resume

            isRunning = true;


            if(flag) {
                isButtonEnabled=false;
                countdownText.setVisibility(View.VISIBLE);
                startCountdown();
                flag=false;
            }

            else {

                startTime = SystemClock.elapsedRealtime() - elapsedTime;  // Adjust for paused time
                handler.post(updateTimerTask);
                onStartButtonClicked();

            }

            savebtn.setVisibility(View.GONE);

            // startTime = SystemClock.elapsedRealtime() - elapsedTime;  // Adjust for paused time
            //handler.post(updateTimerTask);
            ivPauseResume.setImageResource(R.drawable.pause1);
            notation.setText("Pause");
        }
    }


    private void startCountdown() {
        new CountDownTimer(4000, 1000) {


            public void onTick(long millisUntilFinished) {
                countdownText.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                isButtonEnabled=true;
                countdownText.setVisibility(View.GONE);
                startTime = SystemClock.elapsedRealtime() - elapsedTime;  // Adjust for paused time
                handler.post(updateTimerTask);
                onStartButtonClicked();
                // Continue with normal execution here
                // For example:
                // performNormalExecution();
            }
        }.start();
    }

    private void stopStepCountingService() {
        Intent intent = new Intent(getActivity(), StepCountingService.class);
        intent.setAction(StepCountingService.ACTION_STOP_SERVICE);
        getActivity().startService(intent);
    }

    public void onSaveClick(View view) {


        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setFlagRunning(false);
        }

        stopStepCountingService(); //stop the service
        savebtn.setVisibility(View.GONE);


        ivPauseResume.setImageResource(R.drawable.resume);
        saveDataToDB();
        isRunning = false;
        handler.removeCallbacks(updateTimerTask);
        startTime = 0L;
        elapsedTime = 0L;
        tvTimer.setText("00:00");
        distance.setText("0.00");
        calories.setText("0.00");
        speed.setText("0.00");
        notation.setText("Start");

        previewsTotalSteps = totalSteps;
        // steps.setText(String.valueOf(0));
        progressBar.setProgress(0);

        resetSteps();
        unregisterSensor();
        halfgoalReached=false;
        goalReached=false;

    }

    private void saveDataToDB() {
        // Obtain the required data (time, steps, distance, calories) from your fragment
        String time = tvTimer.getText().toString(); // replace with your method to get time
        String steps = String.valueOf(currentSteps);// replace with your method to get steps
        String distance =  String.format(Locale.getDefault(), "%.2f", distanceInMiles);// replace with your method to get distance
        String calories = String.valueOf(caloriesBurned); // replace with your method to get calories

        // Insert data into the database
        MyDBHelper dbHelper = new MyDBHelper(getActivity());
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentDate = sdfDate.format(new Date());
        String savingTime = sdfTime.format(new Date());

        dbHelper.addData(time, steps, distance, calories,currentDate,savingTime, "3");
        showToast("Data saved");

        // Optionally, you can perform additional actions after insertion
    }
    private void showToast(String text){
      //  LayoutInflater inflater = getLayoutInflater();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_toast_layout, null);

// Customize the layout, e.g., set text, background, etc.
        TextView textView = view.findViewById(R.id.toasttext);
        textView.setText(text);

// Create and display the Toast
        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0); // Set gravity to center of the screen
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }




    private void registerSensor() {
        if (stepSensor == null) {
            //Toast.makeText(getContext(), "Step counter sensor not available", Toast.LENGTH_SHORT).show();
        } else {
            mSensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            //Toast.makeText(getContext(), "Step counter sensor Registered", Toast.LENGTH_SHORT).show();
            isSensorRegistered = true;

        }
    }

    // Method to unregister the sensor
    private void unregisterSensor() {
        mSensorManager.unregisterListener(this);
        //Toast.makeText(getContext(), "Step counter sensor Unregistered", Toast.LENGTH_SHORT).show();
        isSensorRegistered = false;


    }


    // Method to convert text to speech
    private void speakText(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    //private boolean isInitialCountSet = false;

    private int stepLengthInInches = 30; // Assume a default step length in inches

    private boolean halfgoalReached = false; // Variable to track if the Half goal is reached
    private boolean goalReached=false;
    private int currentSteps;
    private double caloriesBurned;
    private double distanceInMiles;


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Toast.makeText(getContext(), "Sensor changed", Toast.LENGTH_SHORT).show();
            totalSteps = (int) event.values[0];
            currentSteps = totalSteps - previewsTotalSteps;

            // Update step count

            if (isFirstTimeLoaded) {
                // Perform tasks for the first time only
               // Toast.makeText(getContext(), "laoded first time", Toast.LENGTH_SHORT).show();
              //  steps.setText(String.valueOf(0));
                currentSteps=0;
                progressBar.setProgress(0);

                totalSteps = (int) event.values[0];
                previewsTotalSteps= totalSteps ;
                saveData();


                isFirstTimeLoaded=false;
                // editor.apply();
            }

            // Update step count
           // steps.setText(String.valueOf(currentSteps));


            // Calculate and update distance (assuming average stride length)
             distanceInMiles = calculateDistance(currentSteps, stepLengthInInches);
            distance.setText(String.format(Locale.getDefault(), "%.2f", distanceInMiles));

            double averageSpeed = calculateAverageSpeed(distanceInMiles, elapsedTime);
            speed.setText(String.format(Locale.getDefault(), "%.2f", averageSpeed));

          //  String goalText = goalEditText.getText().toString().trim();  // Trim any leading/trailing whitespace
            double goalValue=selectedDis;



            int progressValue = (int) ((distanceInMiles / goalValue) * 100);
            progressBar.setProgress(progressValue);



            // Calculate and update calories burned (assuming a rough estimate)
            caloriesBurned = calculateCalories(currentSteps);
            calories.setText(String.format(Locale.getDefault(), "%.2f", caloriesBurned));

            if ((progressBar.getProgress() >= progressBar.getMax()/2.0) && !halfgoalReached){
                // Create a custom Toast with longer duration
                halfgoalReached=true;


            }

            if ((progressBar.getProgress() >= progressBar.getMax()) && !goalReached){
                // Create a custom Toast with longer duration
                goalReached=true;

                speakText("Congratulations! your fitness goal is completed.!");
                showToast("Goal reached");



            }
        }
    }
    private double calculateAverageSpeed(double distanceInMiles, long elapsedTime) {
        // Convert distance to meters (1 mile = 1609.34 meters)
        double distanceInMeters = distanceInMiles * 1609.34;

        // Convert elapsedTime to seconds
        double elapsedTimeInSeconds = elapsedTime / 1000.0;

        // Ensure elapsedTimeInSeconds is greater than zero to avoid division by zero
        if (elapsedTimeInSeconds > 0) {
            // Calculate average speed in m/s
            return distanceInMeters / elapsedTimeInSeconds;
        } else {
            // Handle the case where elapsedTimeInSeconds is zero or negative
            return 0.0;
        }
    }


    private double calculateDistance(int steps, int stepLengthInInches) {
        // Convert steps to miles (assuming average stride length)
        return steps * stepLengthInInches / 63360.0; // 63360 inches in a mile
    }

    private double calculateCalories(int steps) {
        // Convert steps to calories burned (rough estimate)
        // This is a simple conversion, and the actual formula may vary based on factors like weight, speed, etc.
        return steps * 0.04; // Assuming 0.04 calories burned per step
    }




    public void resetSteps()
    {
        distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Long Press to reset steps", Toast.LENGTH_SHORT).show();


            }
        });

        distance.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                previewsTotalSteps = totalSteps;
                distance.setText(String.valueOf(0.00));
                progressBar.setProgress(0);
                saveData();
                halfgoalReached=false;
                goalReached=false;
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Code to be executed before the fragment is destroyed
        previewsTotalSteps = totalSteps;

        progressBar.setProgress(0);
        saveData();
        stopStepCountingService();



        // For example, save any necessary data or perform cleanup

    }


    private Runnable updateTimerTask = new Runnable() {
        @Override
        public void run() {
            elapsedTime = SystemClock.elapsedRealtime() - startTime;
            int seconds = (int) (elapsedTime / 1000);
            int minutes = seconds / 60;
            seconds %= 60;
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            handler.postDelayed(this, 1000);
        }
    };


    private void saveData() {
        SharedPreferences sharedPref = requireContext().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("key1", String.valueOf(previewsTotalSteps));
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPref = requireContext().getSharedPreferences("myPref", Context.MODE_PRIVATE);

        // Retrieve the saved string value
        String savedString = sharedPref.getString("key1", null);

        if (savedString != null) {
            try {
                // Try to parse the string as an integer
                previewsTotalSteps = Integer.parseInt(savedString);
            } catch (NumberFormatException e) {
                // Handle the case where the string cannot be parsed as an integer
                previewsTotalSteps = 0;
                Log.e("WalkFragment", "Error parsing saved steps: " + savedString);
            }
        } else {
            // If the saved value does not exist, set steps to 0
            previewsTotalSteps = 0;
        }

        Log.d("WalkFragment", "Loaded steps from SharedPreferences: " + previewsTotalSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {



    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStartButtonClickListener) {
            listener = (OnStartButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnStartButtonClickListener");
        }
    }
    private void onStartButtonClicked() {
        if (listener != null) {

            listener.onStartButtonClicked();
        }
}}
