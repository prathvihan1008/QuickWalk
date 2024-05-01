package com.prathvihan1008.quickwalk;

import android.Manifest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.prathvihan1008.quickwalk.fragments.BikeFragment;
import com.prathvihan1008.quickwalk.fragments.JogFragment;
import com.prathvihan1008.quickwalk.fragments.WalkFragment;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnStartButtonClickListener  {
    private static final int ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 123;

    private static final String TAG = "MainActivity";

    public boolean isSoundOn=true;
    private ImageView action_sound;
    private ImageView action_bars;
    private boolean flag_running=false;
    private AdView mAdView;
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_FIRST_TIME = "FirstTime";
    private boolean permissionRequestHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);

        if (isFirstTime) {
            // Display the dialog
            displayDialog();

            // Store flag indicating app is opened for the first time

        }


        setContentView(R.layout.activity_main);
        action_sound=findViewById(R.id.action_sound);
        action_bars=findViewById(R.id.action_bars);
        Log.d(TAG, "MainActivity onCreate() method called");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Load the initial fragment (HistoryFragment) dynamically
        loadInitialFragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }


        ActionBar actionBar = getSupportActionBar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }


        action_bars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);


                startActivity(intent);

            }
        });

        action_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSoundOn) {
                    action_sound.setImageResource(R.drawable.sound_off);

                    isSoundOn=false;
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

                    if (fragment instanceof WalkFragment) {
                        WalkFragment walkFragment = (WalkFragment) fragment;
                        walkFragment.stopTTS();

                    } else if (fragment instanceof JogFragment) {
                        JogFragment jogFragment = (JogFragment) fragment;
                        jogFragment.stopTTS();
                    }else if(fragment instanceof BikeFragment)
                    {
                        BikeFragment bikeFragment=(BikeFragment)  fragment;
                        bikeFragment.stopTTS();

                    }

                } else {
                    action_sound.setImageResource(R.drawable.sound_on);
                    isSoundOn=true;
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (fragment instanceof WalkFragment) {
                        WalkFragment walkFragment = (WalkFragment) fragment;
                        walkFragment.startTTS("On");
                    } else if (fragment instanceof JogFragment) {
                        JogFragment jogFragment = (JogFragment) fragment;
                        jogFragment.startTTS("On");
                    }


                    else if(fragment instanceof BikeFragment)
                    {
                        BikeFragment bikeFragment=(BikeFragment)  fragment;
                        bikeFragment.startTTS("On");


                    }



                }

            }
        });

        // Initialize the Spinner after the BottomSheetBehavior setup

        Spinner customSpinner = findViewById(R.id.customSpinner);

        List<String> spinnerItems = Arrays.asList("Walking", "Jogging", "Biking"); // Replace with your data

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, R.layout.custom_spinner_dropdown_item, spinnerItems);
        customSpinner.setAdapter(adapter);

        customSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = spinnerItems.get(position);
                loadFragment(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);


        AdRequest adRequest = new AdRequest.Builder().build();


        mAdView.loadAd(adRequest);


        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();
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





    }

    private void displayDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setMessage("Turning on the following feature is required for the app to function as expected,\n\n Please select 'Allow' on following screen" )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button clicked, request the permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            checkAndRequestPermission();

                            SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(KEY_FIRST_TIME, false);
                            editor.apply();

                        }
                    }
                })
                .setCancelable(false) // Prevent dialog from being dismissed by clicking outside
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();



    }

    private void checkAndRequestPermission() {
        // Check if the permission is not granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION) !=
                PackageManager.PERMISSION_GRANTED) {

            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE);

        } else {
            // Permission is already granted, you can proceed with your logic
           // showToast("Permission granted!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE && !permissionRequestHandled) {
            permissionRequestHandled = true;
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with your logic
                // showToast("Permission granted!");
            } else {
                Toast.makeText(this,"Please provide the permission manually :\nclick and hold the app icon-->'app-info'-->permissions",Toast.LENGTH_LONG).show();
                finish();
                // Permission denied, handle accordingly
                // For example, display a message to the user and continue with the app's normal behavior
                //Toast.makeText(this, "Permission denied, but the app will proceed as usual", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadInitialFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the fragmentContainer with the HistoryFragment
        Fragment initialFragment = new WalkFragment();
        fragmentTransaction.replace(R.id.fragmentContainer, initialFragment);
        // Commit the transaction
        fragmentTransaction.commit();


    }
    private void loadFragment(String selectedItem) {



        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        // Create the appropriate fragment based on the selected item
        if (selectedItem.equals("Walking")) {
            fragment = new WalkFragment();
        } else if (selectedItem.equals("Jogging")) {
            fragment = new JogFragment();
        } else if (selectedItem.equals("Biking")) {
            fragment = new BikeFragment();
        } else {
            // Handle the default case or show an error
            return;
        }
        // Replace the current fragment with the new one
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
       // fragmentTransaction.addToBackStack(null); // Optional: Add to back stack for navigation
        fragmentTransaction.commit();


    }

    //code for handling onBackPressed


    public void onBackPressed() {
        // Check if the activity is running (e.g., if the sensor is registered)
        if (flag_running) {
            // Do not call super.onBackPressed() to prevent the activity from being destroyed
            // Instead, navigate the user to the home screen or perform other actions
            navigateToHomeScreen();
        } else {
            // If the activity is not running, allow the default behavior (destroy the activity)
            super.onBackPressed();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
       // Toast.makeText(this,"resume",Toast.LENGTH_LONG).show();
        // Check and request permission if not granted
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);
        if(!isFirstTime){
            checkAndRequestPermission();

        }

    }

    private void navigateToHomeScreen() {
        // Implement logic to navigate the user to the home screen
        // For example:
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
    public void setFlagRunning(boolean running) {
        this.flag_running = running;
    }




    @Override
    public void onStartButtonClicked() {
        // Start the StepCountingService as a foreground service and show notification
        flag_running = true;

        Intent serviceIntent = new Intent(this, StepCountingService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        // Add appropriate flags to the notification intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Specify the appropriate PendingIntent flags
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Now you can use the pendingIntent as needed, for example, to set it in a notification.
    }



}