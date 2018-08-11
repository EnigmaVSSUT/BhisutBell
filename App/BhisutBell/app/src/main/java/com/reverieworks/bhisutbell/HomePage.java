package com.reverieworks.bhisutbell;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.Wave;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.reverieworks.bhisutbell.Data.Notice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomePage extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, View.OnTouchListener {

    private static final String BHISUT_PREFS = "BhisutBellDB";
    private boolean KEYBOARD_SHOWING = false;
    private ListView listView_Notifications;
    private ArrayAdapter<String> adapter_notifications;
    private DatabaseReference database_notificatons;
    private DatabaseReference database_lastUpdate;

    private ArrayList<String> listNotificationTitles = new ArrayList<>();
    private ArrayList<String> listNotificationTimes = new ArrayList<>();
    private ArrayList<String> listNotificationURLs = new ArrayList<>();
    private Typeface custom_font;
    private Toolbar mTopToolbar;
    private TextView textView_timer;
    private TextView textView_lastUpdated;
    private ProgressBar progressBar;

    private AutoCompleteTextView autocompleteNotices;

    private long timeCountInMilliSeconds = 60000;
    private CountDownTimer countDownTimer;
    private int noticesCount = 999999;
    private ImageView imageView_info;


    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private ProgressBar progressBarCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
//        getSupportActionBar().display;
//        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setSupportActionBar(mTopToolbar);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        Wave foldingCube = new Wave();
        progressBar.setIndeterminateDrawable(foldingCube);
        progressBar.setVisibility(View.VISIBLE);

        initializeNotificationService();
        initViews();
        initListeners();
        initDatabases();
        initLocalDatabase();
        setNotificationsList();
        setLastUpdate();


        //set font style
        custom_font = Typeface.createFromAsset(getAssets(),  "fonts/lato_light.ttf");

        //hide keyboard on opening activity
        hideSoftKeyboard(autocompleteNotices);

    }


    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        KEYBOARD_SHOWING = false;
    }
    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            KEYBOARD_SHOWING = true;
        }
    }
    private void toogleKeyboard () {
        if(KEYBOARD_SHOWING){
            hideSoftKeyboard(autocompleteNotices);
        }else
            showSoftKeyboard(autocompleteNotices);
    }

    private void setAutoCompleteSearchBox() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.autocomplete_dropdown, listNotificationTitles);
        autocompleteNotices.setAdapter(adapter);
        autocompleteNotices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listNotificationURLs.get(position)));
                startActivity(browserIntent);
            }
        });
        autocompleteNotices.setOnTouchListener(this);
    }

    private void initLocalDatabase() {
        SharedPreferences prefs = getSharedPreferences(BHISUT_PREFS, MODE_PRIVATE);
        noticesCount = prefs.getInt("LOCAL-NoticesCount", 99999);
        Log.e("count", String.valueOf(noticesCount));
    }

    private void storeNoticesLocally() {
        SharedPreferences.Editor editor = getSharedPreferences(BHISUT_PREFS, MODE_PRIVATE).edit();
        Log.e("count", String.valueOf(listNotificationTitles.size()));
        editor.putInt("LOCAL-NoticesCount", listNotificationTitles.size());
        editor.apply();
    }


    private void setLastUpdate() {

        ValueEventListener lastUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textView_lastUpdated.setText(getDifferenceInDates(dataSnapshot.getValue().toString()));
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("ss", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        database_lastUpdate.addValueEventListener(lastUpdateListener);
    }

    private String getDifferenceInDates(String lastUpdate) {
        Date lastUpdateDate = null, currentDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-dd HH:mm:ss", Locale.US);
        //"2018-08-05 04:06:14.037173"
        try {
            lastUpdateDate = simpleDateFormat.parse(lastUpdate);

            Date date = Calendar.getInstance().getTime();
            currentDate = simpleDateFormat.parse(simpleDateFormat.format(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //milliseconds
        long difference = currentDate.getTime() - lastUpdateDate.getTime();
        Log.e("Time", currentDate.getTime() + " " + lastUpdateDate.getTime() + " " + difference);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;

        // set the timer
        startStop(difference);

//        return ("Updated " + (elapsedDays == 0 ? "" : elapsedDays + " days ") +
//                (elapsedHours == 0 ? "" : elapsedHours + " hours ") +
//                (elapsedMinutes == 0 ? "" : elapsedMinutes + " minutes ") +
//                (elapsedDays == 0 && elapsedHours == 0 && elapsedMinutes == 0 ?
        return ("Last Updated: " + lastUpdate.substring(0, lastUpdate.length() - 7));
    }

    private void initDatabases() {
        database_notificatons = FirebaseDatabase.getInstance().getReference().child("notices");
        database_lastUpdate = FirebaseDatabase.getInstance().getReference().child("lastUpdated");
    }

    private void initViews() {
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        listView_Notifications = (ListView) findViewById(R.id.listView_notifications);
        textView_lastUpdated = (TextView) findViewById(R.id.textView_info);
        textView_timer =  (TextView) findViewById(R.id.textView_time_timer);
        autocompleteNotices = (AutoCompleteTextView) findViewById(R.id.autocomplete_notices);
        imageView_info = (ImageView) findViewById(R.id.imageView_info);

        imageView_info.bringToFront(); //prevent it to hide from toolbar
    }

    private void initListeners() {
        imageView_info.setOnClickListener(this);
    }
    private void initializeNotificationService() {
        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    private void setNotificationsList() {

        //runs after the list has been loaded
        ValueEventListener lastUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeNoticesLocally();
                setAutoCompleteSearchBox();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("ss", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        database_notificatons.addValueEventListener(lastUpdateListener);


        //fetch list of notifications
        adapter_notifications = new StableArrayAdapter();
        listView_Notifications.setAdapter(adapter_notifications);
        listView_Notifications.setOnItemClickListener(this);

        database_notificatons.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Notice notice = dataSnapshot.getValue(Notice.class);

//                Log.e("List ","Adding " + notice.getName() + " to list");

                listNotificationTitles.add(0, notice.getName());
                listNotificationTimes.add(0, notice.getDate());
                listNotificationURLs.add(0, notice.getUrl());

                adapter_notifications.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView_Notifications.post(new Runnable() {
            @Override
            public void run() {
                listView_Notifications.smoothScrollToPosition(0);
            }
        });


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.e( "ItemClicked ",listNotificationTitles.get(i) + " " + l);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listNotificationURLs.get(i)));
        startActivity(browserIntent);
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        public StableArrayAdapter() {
            super(getApplicationContext(), R.layout.listview_notification, listNotificationTitles);

        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO hey it's working
            View view = convertView;

            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_notification, parent, false);

            TextView textView_title = (TextView) view.findViewById(R.id.textView_title_listView);
            TextView textView_time = (TextView) view.findViewById(R.id.textView_time_listView);
            ImageView imageView_newTag = (ImageView) view.findViewById(R.id.imageView_newTag_notification);

            textView_title.setTypeface(custom_font);
            textView_time.setTypeface(custom_font);

            textView_title.setText(listNotificationTitles.get(position));
            textView_time.setText(listNotificationTimes.get(position));

            if(position > noticesCount)
                imageView_newTag.setVisibility(View.VISIBLE);
            else
                imageView_newTag.setVisibility(View.GONE);

            return view;
        }
    }

    private void startStop(long currentTimeInMilliSeconds) {
            // call to initialize the timer values
            setTimerValues();
            // call to initialize the progress bar values
            setProgressBarValues(currentTimeInMilliSeconds);

            // changing the timer status to started
            timerStatus = TimerStatus.STARTED;
            // call to start the count down timer
            startCountDownTimer(currentTimeInMilliSeconds);


    }

    /**
     * method to initialize the values for count down timer
     */
    private void setTimerValues() {
        int time = 15;
        // assigning values after converting to milliseconds
        timeCountInMilliSeconds = time * 60 * 1000;
    }

    private void setProgressAnimate(ProgressBar progressBar, int progressTo, long timeRequiredToProgress)
    {
        Log.e("setProgressAnimate", progressBar.getProgress() + " " + progressTo * 100);
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress",  progressBar.getProgress() ,progressTo * 100);
        animation.setDuration(timeRequiredToProgress);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
    }
    /**
     * method to start count down timer
     */
    private void startCountDownTimer(final long currentTimeInMilliSeconds) {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds - currentTimeInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                textView_timer.setText(hmsTimeFormatter(millisUntilFinished));

            }

            @Override
            public void onFinish() {

                textView_timer.setText("Updating!");
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
            }

        }.start();
        countDownTimer.start();
    }

    /**
     * method to stop count down timer
     */
    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    /**
     * method to set circular progress bar values
     */
    private void setProgressBarValues(long currentProgress) {

        progressBarCircle.setMax(((int) timeCountInMilliSeconds / 1000) * 100);
        progressBarCircle.setProgress(((int) currentProgress / 1000) * 100);
        setProgressAnimate(progressBarCircle, ((int) timeCountInMilliSeconds / 1000), timeCountInMilliSeconds - currentProgress);
    }


    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;
    }

    private void openProjectPage() {

        Log.e("clicked","projectpageopen");
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/EnigmaVSSUT/BhisutBell"));
        startActivity(browserIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_info:
                openProjectPage();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_RIGHT = 2;


        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(event.getRawX() >= (autocompleteNotices.getRight() - autocompleteNotices.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                // your action here
                Log.e("search", "right clicked");
                autocompleteNotices.setText("");
                autocompleteNotices.clearFocus();
                hideSoftKeyboard(autocompleteNotices);

                return true;
            }else if(event.getRawX() >= (autocompleteNotices.getLeft() - autocompleteNotices.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                // your action here
                Log.e("search", "left clicked");
                toogleKeyboard();
                return true;
            }
        }
        return false;
    }

}
