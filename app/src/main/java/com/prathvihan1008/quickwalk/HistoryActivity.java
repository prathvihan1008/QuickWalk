package com.prathvihan1008.quickwalk;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DataModelAdapter adapter;
    private List<DataModel> dataModels;
    private MyDBHelper dbHelper;
    private CalendarView calendarView;
    private static final String TAG = "MainActivity";
    private InterstitialAd mInterstitialAd;
    private TextView selectedDateTextView;
    private Calendar calendar;
    private ImageView action_back;

    private int wFrag=1;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        setContentView(R.layout.history_fragment_layout);





        //it keeps the screen awake (only applied to particular activity not whole device)
        action_back=findViewById(R.id.action_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        selectedDateTextView = findViewById(R.id.idTVSelectedDate);
        ImageButton pickDateButton = findViewById(R.id.idBtnPickDate);
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set initial text for TextView
        selectedDateTextView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        //adds
        AdRequest adRequest = new AdRequest.Builder().build();
        //This is Asynchronous method:which executes irespective of the flow of main program sequential instructions
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
/*this is also a object*/            new InterstitialAdLoadCallback() {//InterstitialAdLoadCallback  is a abstractclass used for provideing call back
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) { //object reference is received fro server
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                        // Show the ad when it's loaded
                        showInterstitialAd();//this mehod is called when ever the add is loaded so even though there is a delay in loading the add the thread continus with notmal execution and it can again return to this method due to call back
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        super.onAdFailedToLoad(loadAdError);

                        Log.d(TAG, "Failed to load ad");
                       // Toast.makeText(HistoryActivity.this, "Failed to load ad", Toast.LENGTH_LONG).show();
                        mInterstitialAd = null;
                    }
                });









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
        action_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();



            }
        });


        // Use HistoryActivity.this as the context
        dbHelper = new MyDBHelper(HistoryActivity.this);
       // calendarView = findViewById(R.id.calendarView);

        recyclerView = findViewById(R.id.recyclerView);
        //dataModels = dbHelper.fetchData(); // Call the fetchData method from MyDBHelper

        adapter = new DataModelAdapter(dataModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
        recyclerView.setAdapter(adapter);
//

        updateData(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),1);

        //tablayout

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Walk"));
        tabLayout.addTab(tabLayout.newTab().setText("Jog"));
        tabLayout.addTab(tabLayout.newTab().setText("Bike"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Assuming you have a TextView named selectedDateTextView
                String dateText = selectedDateTextView.getText().toString();

// Split the date text by "/"
                String[] dateParts = dateText.split("/");//String.split return the string array

// Parse day, month, and year from the split parts
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1; // Subtract 1 as months are zero-indexed
                int year = Integer.parseInt(dateParts[2]);

                // Handle tab selection
                switch (tab.getPosition()) {
                    case 0:
                        // Update data for walk activity
                        updateData(year,month,day,1);
                        wFrag=1;
                        break;
                    case 1:
                        // Updatedata for jog activity
                        updateData(year,month,day,2);
                        wFrag=2;
                        break;
                    case 2:
                        // Update data for bike activity
                        updateData(year,month,day,3);
                        wFrag=3;
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselection
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselection
            }
        });



    }


    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                // Implement callback methods
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(TAG, "Ad was clicked.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d(TAG, "Ad dismissed fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e(TAG, "Ad failed to show fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(TAG, "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(TAG, "Ad showed fullscreen content.");
                }

            });

            mInterstitialAd.show(HistoryActivity.this);
       } else {    Log.d(TAG, "The interstitial ad wasn't ready yet.");
       }
    }



    private void showDatePickerDialog() {
        // Get current date
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String dateText = selectedDateTextView.getText().toString();

// Split the date text by "/"
        String[] dateParts = dateText.split("/");//String.split return the string array

// Parse day, month, and year from the split parts
        int dayOfMonth = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Subtract 1 as months are zero-indexed
        int year= Integer.parseInt(dateParts[2]);

        // Create DatePickerDialog and set its listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // Display the selected date in TextView
                        selectedDateTextView.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                      if(wFrag==1) {
                          updateData(year, monthOfYear, dayOfMonth,1);

                      }
                      else if(wFrag==2){
                          updateData(year, monthOfYear, dayOfMonth,2);

                        }
                      else if(wFrag==3){
                          updateData(year, monthOfYear, dayOfMonth,3);
                      }



                    }
                }, year, month, dayOfMonth);

// Set the maximum date to the current date to disable future dates
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());


        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void updateData(int year, int month, int dayOfMonth ,int frag ){
        // Fetch data based on the selected date
        String selectedDate = formatDate(year, month, dayOfMonth);
        dataModels = dbHelper.fetchDataForDate(selectedDate,frag);

        // Update the RecyclerView with the filtered data
        adapter = new DataModelAdapter(dataModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

    }


    private String formatDate(int year, int month, int dayOfMonth) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
    }


    @Override
    public void onBackPressed() {
        // Handle back button press
        if (!handleBackPressed()) {
            super.onBackPressed();
        }
    }
    private boolean handleBackPressed() {
        // Custom logic for handling back press, if needed
        // Return true if back press is handled, false otherwise
        return false;

    // Other methods and logic as needed
}}
