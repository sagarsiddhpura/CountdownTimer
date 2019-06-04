package com.android.countdowntimer.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.home.Event;
import com.android.countdowntimer.home.TimerView;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.android.countdowntimer.utils.Utils;

import java.util.List;

import io.paperdb.Paper;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = findViewById(R.id.tasks_toolbar);
        setSupportActionBar(toolbar);
        Utils.setupSystemUI(this);
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("EVENT_ID");
        Event event = null;

        List<Event> events = Paper.book().read("events");
        for (Event e : events) {
            if(e.getId().equals(eventId)) {
                event = e;
            }
        }
        getSupportActionBar().setTitle(event.getTitle());
        TimerView mTimer = findViewById(R.id.event_timer);
        mTimer.start(event.getEndDate() - System.currentTimeMillis());

        TextView mDueDate = findViewById(R.id.event_due_date);
        mDueDate.setText("Timer ends on\n" + DateTimeUtils.longToString(event.getEndDate(), DateTimeUtils.MEDIUM));

    }
}
