package com.reverieworks.bhisutbell;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;
import com.reverieworks.bhisutbell.Data.Notice;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listView_Notifications;
    private ArrayAdapter<String> adapter_notifications;
    private DatabaseReference database_notificaitons;

    private ArrayList<String> listNotificationTitles = new ArrayList<>();
    private ArrayList<String> listNotificationTimes = new ArrayList<>();
    private ArrayList<String> listNotificationURLs = new ArrayList<>();
    private Typeface custom_font;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);


        initializeNotificationService();
        setIds();
        setDatabase();
        setNotificationsList();

//        textView_title = (TextView)findViewById(R.id.title_view);
//
        custom_font = Typeface.createFromAsset(getAssets(),  "fonts/lato_light.ttf");

    }

    private void setDatabase() {
        database_notificaitons = FirebaseDatabase.getInstance().getReference().child("notices");
    }

    private void setIds() {
        listView_Notifications = (ListView) findViewById(R.id.listView_notifications);
    }

    private void initializeNotificationService() {
        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    private void setNotificationsList() {

        //fetch list of notifications

        adapter_notifications = new StableArrayAdapter();
        listView_Notifications.setAdapter(adapter_notifications);
        listView_Notifications.setOnItemClickListener(this);

        database_notificaitons.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Notice notice = dataSnapshot.getValue(Notice.class);

                Log.e("List ","Adding " + notice.getName() + " to list");

                listNotificationTitles.add(notice.getName());
                listNotificationTimes.add(notice.getDate());
                listNotificationURLs.add(notice.getUrl());

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


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

//    @Override
//    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Toolbox.showLog2(listUserStatusNames.get(i) + " " + listUserStatusUids.get(i) + " " + l);
//        Intent intent = new Intent(getActivity(), ProfileInfo.class);
//        intent.putExtra("EXTRA-targetUID", listUserStatusUids.get(i));
//        intent.putExtra("EXTRA-profileInfoType", ProfileInfoTypes.SendRequest.toString());
//        startActivity(intent);
//    }

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
            textView_title.setText(listNotificationTitles.get(position));
            textView_title.setTypeface(custom_font);

            return view;
        }
    }
}
