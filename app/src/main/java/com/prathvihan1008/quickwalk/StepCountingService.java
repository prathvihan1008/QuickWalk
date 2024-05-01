package com.prathvihan1008.quickwalk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StepCountingService extends Service {
    public static final String ACTION_STOP_SERVICE = "com.prathvihan1008.quickwalk.STOP_FOREGROUND_SERVICE";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "StepCountingChannel";
    private TextToSpeech textToSpeech;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            //related to stopping service
        initializeTTS();
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_STOP_SERVICE.equals(action)) {
                stopService();
            }
        }

        // Create notification channel
        createNotificationChannel();

        // Build the notification
        Notification notification = buildNotification();

        // Start the service in the foreground with the notification
        startForeground(NOTIFICATION_ID, notification);

        // Continue step counting logic here

        return START_STICKY;
    }

    private void initializeTTS() {
        // Initialize TextToSpeech instance
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // TTS engine initialized successfully
                } else {
                    // TTS engine initialization failed
                    Toast.makeText(StepCountingService.this, "Failed to initialize TTS engine", Toast.LENGTH_SHORT).show();
                }
            }
        });}

    private void speakText(String text) {
        if (textToSpeech != null) {
            // Speak the specified text
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onDestroy() {
        // Release TextToSpeech resources when the service is destroyed
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void stopService() {
        // Stop service logic
        stopForeground(true);
        stopSelf();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counting Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        // Add appropriate flags to the notification intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Specify the appropriate PendingIntent flags
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build and return the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo2)
                .setContentTitle("Tracking activity")
                .setContentText("Tap to see progress")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return builder.build();
    }


    public void stopForegroundService() {
        // Stop showing the notification and remove the service from the foreground
        stopForeground(true);

        // Optional: Stop the service if it no longer has any work to do
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
