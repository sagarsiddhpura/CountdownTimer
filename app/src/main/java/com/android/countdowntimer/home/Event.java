package com.android.countdowntimer.home;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public class Event {

    @NonNull
    private String mId;

    @Nullable
    private String mTitle;

    @Nullable
    private String mNote;

    private long mStartDate;

    private long mEndDate;

    private int mState;

    private boolean mDurableEvent;

    private int mPriority;

    private int mReminder;

    private String mCategory;

    private long mCreationDate;

    //Use this constructor to create a new active Event.
    public Event(@Nullable String title, @Nullable String note, long startDate, long endDate, int state, boolean durableEvent, int priority, int reminder, String category) {
        this(title, note, startDate, endDate, state, durableEvent, priority, UUID.randomUUID().toString(), reminder, category, System.currentTimeMillis());
    }

    public Event(@Nullable String title, @Nullable String note, long startDate, long endDate, int state, boolean durableEvent, int priority, String id, int reminder, String category, long creationDate) {
        mId = id;
        mCreationDate = creationDate;
        mTitle = title;
        mNote = note;
        mStartDate = startDate;
        mEndDate = endDate;
        mState = state;
        mDurableEvent = durableEvent;
        mPriority = priority;
        mReminder = reminder;
        mCategory = category;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    public long getCreationDate() {
        return mCreationDate;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getNote() {
        return mNote;
    }

    public long getStartDate() {
        return mStartDate;
    }

    public long getEndDate() {
        return mEndDate;
    }

    public int getState() {
        return mState;
    }

    public boolean isDurableEvent() {
        return mDurableEvent;
    }

    public int getPriority() {
        return mPriority;
    }

    public int getReminder() {
        return mReminder;
    }

    public String getCategory() {
        return mCategory;
    }
}
