package com.android.countdowntimer.home;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.completedevents.CompletedEventsActivity;
import com.android.countdowntimer.detail.EventDetailActivity;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.android.countdowntimer.utils.NotificationUtils;
import com.android.countdowntimer.utils.RemindType;
import com.android.countdowntimer.utils.ReminderUtils;
import com.android.countdowntimer.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.snatik.storage.Storage;

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
                alert.show();
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
                deleteEvent(eventId);
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
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getEndDate() < System.currentTimeMillis()) {
               deleteEvent(event.getId());
            }
        }
    }

    private void deleteEvent(String id) {
        boolean hasChanged = false;
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getId().equals(id)) {
                addToCompletedEvents(event);
                iterator.remove();
                hasChanged = true;
            }
        }
        if(hasChanged) {
            Paper.book().write("events", events);
        }
    }

    private void addToCompletedEvents(Event event) {
        List<Event> completedEvents = Paper.book().read("completed_events");
        if(completedEvents == null) {
            completedEvents = new ArrayList<>();
        }
        completedEvents.add(event);
        Paper.book().write("completed_events", completedEvents);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_show_completed_events:
                showCompletedEvents();
                return true;
            case R.id.action_export_events:
                exportEventsToJson();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exportEventsToJson() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        Gson gson = new Gson();
                        String userJson = gson.toJson(events);
                        // init
                        Storage storage = new Storage(getApplicationContext());

                        // get external storage
                        String path = storage.getExternalStorageDirectory();
                        storage.createFile(path+"/CountdownTimer.json", userJson);
                        Toast.makeText(getApplicationContext(),"Events exported to Json in file:" + path + "/CountdownTimer.json",Toast.LENGTH_LONG).show();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
    }

    private void showCompletedEvents() {
        Intent intent = new Intent(MainActivity.this, CompletedEventsActivity.class);
        startActivity(intent);
    }
}
