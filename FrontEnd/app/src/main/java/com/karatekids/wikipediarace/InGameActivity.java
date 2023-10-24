package com.karatekids.wikipediarace;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class InGameActivity extends AppCompatActivity {

    public static int count;

    public static String startUrl;

    public static String endUrl;

    public static String startPage;

    public static String endPage;

    public static ArrayList<String> pagesVisited;

    public static String time;

    private int seconds = 0;

    // Is the stopwatch running?
    private static boolean running;

    private boolean wasRunning;

    private final static String TAG = "InGameActivity";

    // Followed along with: https://technotalkative.com/android-webviewclient-example/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);

        //TODO: start alert when the server sends a signal saying that the game is over
        AlertDialog.Builder  builder = new AlertDialog.Builder(InGameActivity.this);
        builder.setTitle("Game Over! You lost.");
        builder.setMessage("Do you want to end the game now and see your results?");
        builder.setCancelable(false);
        builder.setPositiveButton("End Game", (DialogInterface.OnClickListener) (dialog, which) -> {
            Intent resultActivity = new Intent(InGameActivity.this, ResultsActivity.class);
            startActivity(resultActivity);
        });
        builder.setNegativeButton("Continue playing current game for second", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();

        // temporary
        //TODO: use server to randomly get pages
        startUrl = "https://en.m.wikipedia.org/wiki/Taco";
        endUrl = "https://en.m.wikipedia.org/wiki/Mexico";
        startPage = "Taco";
        endPage = "Mexico";

        runTimer();

        //TODO: replace with the randomly determined destination page
        TextView destination = (TextView)  findViewById(R.id.destination_page);
        destination.append(" "+endPage);

        WebView web;
        web = (WebView) findViewById(R.id.wikipedia_page_view);
        web.setWebViewClient(new myWebClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(startUrl);
        count = -1;
        pagesVisited = new ArrayList<>();
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        // display the information from the url embedded in the app instead of opening a web viewer external application
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if(!request.getUrl().getHost().equals("en.m.wikipedia.org")){
               return true;
            }
            Log.d(TAG, "URL host: "+request.getUrl().getHost());
            view.loadUrl(String.valueOf(request.getUrl()));
            return true;
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            count++;

            Log.d(TAG, "The number of clicked links is: " + count);

            pagesVisited.add(view.getTitle().substring(0, view.getTitle().indexOf("-")));

            //check if user reaches destination page
            //TODO: change this to take the destination page given from the server
            if(url.equals(endUrl)){
                Intent resultIntent = new Intent(InGameActivity.this, ResultsActivity.class);
                TextView stopwatch = (TextView) findViewById(R.id.stopwatch_text);
                time = stopwatch.getText().toString();
                Log.d(TAG, "Time is: " + time);
                startActivity(resultIntent);
            }
        }
    }

    // https://www.geeksforgeeks.org/how-to-create-a-stopwatch-app-using-android-studio/
    // If the activity is paused,
    // stop the stopwatch.
    @Override
    protected void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    protected void onResume()
    {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }

    // Start the stopwatch running
    // when the Start button is clicked.
    // Below method gets called
    // when the Start button is clicked.
    public static void onClickStart(View view)
    {
        running = true;
    }

    // Sets the NUmber of seconds on the timer.
    // The runTimer() method uses a Handler
    // to increment the seconds and
    // update the text view.
    private void runTimer()
    {

        // Get the text view.
        final TextView stopwatch = (TextView) findViewById(R.id.stopwatch_text);

        // Creates a new Handler
        final Handler handler
                = new Handler();

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                // Set the text view text.
                stopwatch.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
    }

}