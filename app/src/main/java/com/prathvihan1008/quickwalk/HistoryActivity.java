package com.prathvihan1008.quickwalk;

import android.app.DatePickerDialog;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.his_toolbar_menu, menu);
//        return true;
//    }

    private RecyclerView recyclerView;
    private DataModelAdapter adapter;
    private List<DataModel> dataModels;
    private MyDBHelper dbHelper;
    private CalendarView calendarView;
    private static final String TAG = "MainActivity";
    private InterstitialAd mInterstitialAd;
    private TextView selectedDateTextView;
    private Calendar calendar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.history_fragment_layout);

        //it keeps the screen awake (only applied to particular activity not whole device)
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









        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back);
        if (upArrow != null) {
            upArrow.setColorFilter(getResources().getColor(R.color.back_botton), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }


        // Use HistoryActivity.this as the context
        dbHelper = new MyDBHelper(HistoryActivity.this);
       // calendarView = findViewById(R.id.calendarView);

        recyclerView = findViewById(R.id.recyclerView);
        //dataModels = dbHelper.fetchData(); // Call the fetchData method from MyDBHelper

        adapter = new DataModelAdapter(dataModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
        recyclerView.setAdapter(adapter);
//
//        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
//            // Update the displayed data based on the selected date
//            updateData(year, month, dayOfMonth);
//        });
            //this calls the method with current date time and year
        updateData(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));


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
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog and set its listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // Display the selected date in TextView
                        selectedDateTextView.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        updateData(year, monthOfYear, dayOfMonth);
                    }
                }, year, month, dayOfMonth);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void updateData(int year, int month, int dayOfMonth) {
        // Fetch data based on the selected date
        String selectedDate = formatDate(year, month, dayOfMonth);
        dataModels = dbHelper.fetchDataForDate(selectedDate);

        // Update the RecyclerView with the filtered data
        adapter = new DataModelAdapter(dataModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private String formatDate(int year, int month, int dayOfMonth) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle toolbar item clicks
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the Up button click
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
