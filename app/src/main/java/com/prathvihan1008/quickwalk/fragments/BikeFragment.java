package com.prathvihan1008.quickwalk.fragments;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.prathvihan1008.quickwalk.MyDBHelper;
import com.prathvihan1008.quickwalk.R;
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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private ProgressBar progressBar;
   // private TextView steps;
    private EditText goalEditText;

    private TextView distance,calories,tvTimer,notation,speed;
    private ImageView ivPauseResume,stopbtn;
    private long startTime = 0L;
    private long elapsedTime = 0L;
    private boolean isRunning = false;
    private boolean isSensorRegistered=false;
    private boolean isStepCountingEnabled = false;

    private int stepsDuringUnregistered = 0;
    private Handler handler = new Handler();
    private TextToSpeech textToSpeech;
    private double selectedDis;
    public static final String PREFS_NAME="MyPref1";
    public static final String FRAGMENT_LOADED_KEY="FragmentLoaded";
    public static boolean isFirstTimeLoaded;
    private AdView mAdView;
    private AdView mAdView1;
    private View glassyView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike, container, false);

      //  goalEditText=view.findViewById(R.id.goalEditText);
        distance=view.findViewById(R.id.distance);
        calories=view.findViewById(R.id.calories);
        tvTimer=view.findViewById(R.id.tvTimer);
        notation=view.findViewById(R.id.notation);
        ivPauseResume=view.findViewById(R.id.ivPauseResume);
        stopbtn=view.findViewById(R.id.stopbtn);
        speed=view.findViewById(R.id.speed);
        glassyView = view.findViewById(R.id.glassyView);

        progressBar = view.findViewById(R.id.progressBar);
        // steps = view.findViewById(R.id.steps);
       // steps.setText(String.valueOf(0));

        progressBar.setMax(100);
        progressBar.setProgress(0);

        textToSpeech = new TextToSpeech(getContext(), this);


        //Spinner for Goal selection
        Spinner spinnerDisGoal = view.findViewById(R.id.spinnerDisGoal);
        // Generating steps from 50 to 10000 with a gap of 500
        double mindis = 0.5;
        double maxdis = 5.0;
        double gap = 0.5;
       String[] stepsArray = new String[10];
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




        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopClick(v);
            }
        });

        // Set a listener to update progress bar when goal is entered
    /*    goalEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateProgressBar();
                    hideKeyboardAndClearFocus(); // Hide keyboard and clear focus
                    return true;
                }
                return false;
            }
        });*/

        ivPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseResumeClick(v);
            }
        });

        resetSteps();
        loadData();

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);





        //solving the steps problem
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isFirstTimeLoaded = preferences.getBoolean(FRAGMENT_LOADED_KEY, true);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = view.findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest1 = new AdRequest.Builder().build();

        mAdView.loadAd(adRequest);


        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();
                // Toast.makeText(getContext(),"Add",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(adError);
                mAdView.loadAd(adRequest);
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                super.onAdOpened();
            }
        });


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


    // Handle the result of the permission request








    // Method to hide keyboard and clear focus
   /* private void hideKeyboardAndClearFocus() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(goalEditText.getWindowToken(), 0);
        goalEditText.clearFocus();
    }*/



    // Method to update progress bar based on user-entered goal
   /* private void updateProgressBar() {
        String goalText = goalEditText.getText().toString().trim();  // Trim any leading/trailing whitespace
        if (!TextUtils.isEmpty(goalText)) {
            try {
                int goalValue = Integer.parseInt(goalText);
                progressBar.setMax(100);//using percentage concept since progress bar do not accept float values
                progressBar.setProgress(0);
            } catch (NumberFormatException e) {
                // Handle the case where the string cannot be parsed as an integer
                e.printStackTrace();
                Log.e("BikeFragment", "Error parsing goalText: " + goalText);
            }
        } else {
            // If goalText is empty, set the goal to 0 and set progress bar to max
            progressBar.setMax(0);
            progressBar.setProgress(0);
            Log.d("BikeFragment", "GoalText is empty. Setting goal to 0.");
        }
    }*/





    public   void onResume()
    {
        super.onResume();
        if (isSensorRegistered) {
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
            stopbtn.setVisibility(View.VISIBLE);
            glassyView.setVisibility(View.INVISIBLE);
            handler.removeCallbacks(updateTimerTask);
            ivPauseResume.setImageResource(R.drawable.resume);
            notation.setText("Start");
        } else {
            // Resume
            isRunning = true;
            glassyView.setVisibility(View.VISIBLE);
            stopbtn.setVisibility(View.INVISIBLE);
            startTime = SystemClock.elapsedRealtime() - elapsedTime;  // Adjust for paused time
            handler.post(updateTimerTask);
            ivPauseResume.setImageResource(R.drawable.pause1);
            notation.setText("Pause");
            adjustTransparency();
        }
    }
    private void adjustTransparency() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark theme
            glassyView.setBackgroundColor(Color.parseColor("#2EFFFFFF")); // Semi-transparent dark color
        } else {
            // Light theme
            glassyView.setBackgroundColor(Color.parseColor("#2E000000")); // Semi-transparent light color
        }
    }

    public void onStopClick(View view) {
        // Reset timer and step count
        isRunning = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Data");
        builder.setMessage("Do you want to save the data?");



        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Yes, save data to DBMS
                saveDataToDB();

                handler.removeCallbacks(updateTimerTask);
                startTime = 0L;
                elapsedTime = 0L;
                tvTimer.setText("00:00");
                distance.setText("0.00");
                calories.setText("0.00");
                notation.setText("Start");

                previewsTotalSteps = totalSteps;
               // steps.setText(String.valueOf(0));
                progressBar.setProgress(0);

                resetSteps();
                unregisterSensor();
                halfgoalReached=false;
                goalReached=false;

                ivPauseResume.setImageResource(R.drawable.resume);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked No, do nothing or handle accordingly
                handler.removeCallbacks(updateTimerTask);
                startTime = 0L;
                elapsedTime = 0L;
                tvTimer.setText("00:00");
                distance.setText("0.00");
                calories.setText("0.00");
                notation.setText("Start");

                previewsTotalSteps = totalSteps;
               // steps.setText(String.valueOf(0));
                progressBar.setProgress(0);

                resetSteps();
                unregisterSensor();
                halfgoalReached=false;
                goalReached=false;

                ivPauseResume.setImageResource(R.drawable.resume);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Create and show the AlertDialog

        // Set image back to resume icon
        //updateViewModelWithData();
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

        dbHelper.addData(time, steps, distance, calories,currentDate,savingTime);
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


                // Update SharedPreferences to indicate that the fragment has been loaded
//                SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putBoolean(FRAGMENT_LOADED_KEY, false);
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

                speakText("Well Done!! Half of your fitness goal is completed. Keep walking!");
                showToast("Half the goal reached");

                // Set a custom view to the Toast (you can design your own layout)

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
}
