package com.android.countdowntimer.home;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.detail.EventDetailActivity;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.android.countdowntimer.utils.NotificationUtils;
import com.android.countdowntimer.utils.RemindType;
import com.android.countdowntimer.utils.ReminderUtils;
import com.android.countdowntimer.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    public static final String EVENT_NOTIFICATION_ID = "EVENT_NOTIFICATION";
    private EventsAdapter mEventsAdapter;
    private static final String DIALOG_DATE = "DATE";
    private List<Event> events;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.tasks_toolbar);
        setSupportActionBar(toolbar);
        Utils.setupSystemUI(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(EVENT_NOTIFICATION_ID, getString(R.string.event_notification), NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Add Event");
                // Create TextView
                final EditText input = new EditText (MainActivity.this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(input.getText() == null || input.getText().equals("")) {
                            Toast.makeText(getApplicationContext(),"Please Enter valid Name for the Event",Toast.LENGTH_LONG).show();
                        }
                        // Do something with value!
                        name = input.getText().toString();
                        long date = DateTimeUtils.getCurrentTimeWithoutSec();
                        FragmentManager fm = getSupportFragmentManager();
                        DateDialogFragment dialogFragment = DateDialogFragment.newInstance(date, false);
                        dialogFragment.show(fm, DIALOG_DATE);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                final AlertDialog show = alert.show();
            }
        });
        setupEventList();
    }

    private void setupEventList() {
        RecyclerView recyclerView = findViewById(R.id.list_events);
        mEventsAdapter = new EventsAdapter(this);
        recyclerView.setAdapter(mEventsAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new EventTouchHelperCallback(mEventsAdapter));
        touchHelper.attachToRecyclerView(recyclerView);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animationController);

        events = Paper.book().read("events");
        if(events == null) {
            events = new ArrayList<>();
            Paper.book().write("events", events);
        }
        filterCompletedEvents();
        refreshList(events);

        mEventsAdapter.setEventItemActionListener(new EventItemActionListener() {
            @Override
            public void onItemSwiped(String eventId) {
                Iterator<Event> iterator = events.iterator();
                while (iterator.hasNext()) {
                    Event event = iterator.next();
                    if(event.getId().equals(eventId)) {
                        iterator.remove();
                    }
                }
                Paper.book().write("events", events);
                refreshList(events);
            }

            @Override
            public void onItemClicked(String eventId) {
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            }
        });
    }

    private void filterCompletedEvents() {
        boolean hasChanged = false;
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getEndDate() < System.currentTimeMillis()) {
                iterator.remove();
                hasChanged = true;
            }
        }
        if(hasChanged) {
            Paper.book().write("events", events);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        long endDate = data.getLongExtra(DateDialogFragment.EXTRA_DATE, 0);
        if (endDate - System.currentTimeMillis() < 0) {
            Toast.makeText(getApplicationContext(),"End Date cannot be in Past",Toast.LENGTH_LONG).show();
            return;
        }
        String eventId = String.valueOf(System.currentTimeMillis());
        events.add(new Event(name, "", 0, endDate, StateType.ONGOING, false, 0, eventId, 0, "", System.currentTimeMillis()));
        NotificationUtils.buildNormalReminder(getApplication(), endDate, name, RemindType.SINGLE_DUE_DATE, ReminderUtils.getSingleRemindInterval(RemindType.SINGLE_DUE_DATE), eventId);
        Paper.book().write("events", events);
        refreshList(events);
    }

    private void refreshList(List<Event> events) {
        mEventsAdapter.setEvents(events);
    }
}
