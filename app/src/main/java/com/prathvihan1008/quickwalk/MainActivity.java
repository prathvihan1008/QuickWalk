package com.prathvihan1008.quickwalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import com.prathvihan1008.quickwalk.fragments.BikeFragment;
import com.prathvihan1008.quickwalk.fragments.JogFragment;
import com.prathvihan1008.quickwalk.fragments.WalkFragment;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 123;



    private ImageView img;
    private static final String TAG = "MainActivity";
    private PowerManager.WakeLock wakeLock;




    public boolean isSoundOn=true;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        //Database
       // MyDBHelper dbHelper=new MyDBHelper(this);






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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkAndRequestPermission();

        }



    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release the wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
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

        if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with your logic
               // showToast("Permission granted!");
            } else {
                // Permission not granted, show a toast and terminate the app
                showToast("App requires physical activity permission.\nPlease provide it manually to continue");
                finish();
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }





    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        // Handle item selection based on item ID
        int itemId = item.getItemId();
        if (itemId == R.id.action_sound) {
            if (isSoundOn) {
                item.setIcon(R.drawable.sound_off);
              //  Toast.makeText(this, "Sound On", Toast.LENGTH_SHORT).show();
              //  textToSpeech.stop();
                isSoundOn=false;
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                // WalkFragment fragment= (WalkFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
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
                item.setIcon(R.drawable.sound_on);
              //  Toast.makeText(this, "Sound Off", Toast.LENGTH_SHORT).show();
                isSoundOn=true;
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                // WalkFragment fragment= (WalkFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
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
            return true;
        } else if (itemId == R.id.action_bars) {
            // When you want to navigate to HistoryActivity, use an Intent
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);


            startActivity(intent);


            // Handle Item 2 click
            // Example: Toast.makeText(this, "Item 2 Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showExitConfirmationDialog();

    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.onBackPressed();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked "No", do nothing or dismiss the dialog
                        dialog.dismiss();
                    }
                });

        // Create and show the AlertDialog
        builder.create().show();
    }

}