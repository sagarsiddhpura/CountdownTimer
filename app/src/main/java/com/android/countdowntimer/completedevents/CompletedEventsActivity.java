package com.android.countdowntimer.completedevents;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.android.countdowntimer.R;
import com.android.countdowntimer.detail.EventDetailActivity;
import com.android.countdowntimer.home.Event;
import com.android.countdowntimer.home.EventItemActionListener;
import com.android.countdowntimer.home.EventTouchHelperCallback;
import com.android.countdowntimer.home.EventsAdapter;
import com.android.countdowntimer.home.MainActivity;
import com.android.countdowntimer.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.paperdb.Paper;

public class CompletedEventsActivity extends AppCompatActivity {

    private List<Event> events;
    private CompletedEventsAdapter mEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_events);
        Toolbar toolbar = findViewById(R.id.tasks_toolbar);
        setSupportActionBar(toolbar);
        Utils.setupSystemUI(this);
        setupEventList();
    }

    private void setupEventList() {
        RecyclerView recyclerView = findViewById(R.id.list_events);
        mEventsAdapter = new CompletedEventsAdapter(this);
        recyclerView.setAdapter(mEventsAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new EventTouchHelperCallback(mEventsAdapter));
        touchHelper.attachToRecyclerView(recyclerView);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animationController);

        events = Paper.book().read("completed_events");
        if(events == null) {
            events = new ArrayList<>();
            Paper.book().write("completed_events", events);
        }
        refreshList(events);

        mEventsAdapter.setEventItemActionListener(new EventItemActionListener() {
            @Override
            public void onItemSwiped(String eventId) {
                deleteEvent(eventId);
                refreshList(events);
            }

            @Override
            public void onItemClicked(String eventId) {
            }
        });
    }

    private void deleteEvent(String id) {
        boolean hasChanged = false;
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getId().equals(id)) {
                iterator.remove();
                hasChanged = true;
            }
        }
        if(hasChanged) {
            Paper.book().write("completed_events", events);
        }
    }

    private void refreshList(List<Event> events) {
        mEventsAdapter.setEvents(events);
    }
}
