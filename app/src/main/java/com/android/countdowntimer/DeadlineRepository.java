package com.android.countdowntimer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeadlineRepository implements EventDataSource {

    private static DeadlineRepository INSTANCE = null;

    private final EventDataSource mTasksLocalDataSource;

    private LiveData<List<Event>> mAllEvents;

    private Map<String, Event> mCachedEvents;

    private Map<String, Event> mCachedDeletedEvents;

    private DeadlineRepository(Application application) {
        DeadlineDatabase db = DeadlineDatabase.getDatabase(application);
        EventsDao eventsDao = db.eventDao();
        mTasksLocalDataSource = EventLocalDataSource.getInstance(new AppExecutors(), eventsDao);
        mAllEvents = eventsDao.getAllEvents();
    }

    public static DeadlineRepository getInstance(Application application) {
        if (INSTANCE == null) {
            return INSTANCE = new DeadlineRepository(application);
        } else {
            return INSTANCE;
        }
    }

    public LiveData<List<Event>> getAllEvents() {
        return mAllEvents;
    }

    @Override
    public void saveEvent(@NonNull Event event) {
        mTasksLocalDataSource.saveEvent(event);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedEvents == null) {
            mCachedEvents = new LinkedHashMap<>();
        }

        mCachedEvents.put(event.getId(), event);
    }

    @Override
    public void updateEventState(@NonNull Event event, int state) {
        mTasksLocalDataSource.updateEventState(event, state);

        Event newStateEvent = new Event(event.getTitle(), event.getNote(), event.getStartDate(), event.getEndDate(), state, event.isDurableEvent(), event.getPriority(), event.getId(), event.getReminder(), event.getCategory(), event.getCreationDate());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedEvents == null) {
            mCachedEvents = new LinkedHashMap<>();
        }
        mCachedEvents.put(event.getId(), newStateEvent);
    }

    @Override
    public void updateEventState(@NonNull String eventId, int state) {
        mTasksLocalDataSource.updateEventState(eventId, state);

        if (mCachedEvents == null) {
            mCachedEvents = new LinkedHashMap<>();
        }
    }

    @Override
    public void clearCompletedEvents() {
        mTasksLocalDataSource.clearCompletedEvents();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedEvents == null) {
            mCachedEvents = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Event>> it = mCachedEvents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Event> entry = it.next();
            if (entry.getValue().getState() == StateType.COMPLETED) {
                it.remove();
            }
        }
    }

    @Override
    public void getEvent(@NonNull final String eventId, @NonNull final GetEventCallback callback) {
        Event cachedEvent = getTaskWithId(eventId);

        // Respond immediately with cache if available
        if (cachedEvent != null) {
            callback.onEventLoaded(cachedEvent);
            return;
        }

        mTasksLocalDataSource.getEvent(eventId, new GetEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedEvents == null) {
                    mCachedEvents = new LinkedHashMap<>();
                }
                mCachedEvents.put(event.getId(), event);
                callback.onEventLoaded(event);
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    private void addCacheDeletedEvent(String eventId, Event event) {
        if (mCachedDeletedEvents == null) {
            mCachedDeletedEvents = new LinkedHashMap<>();
        }

        mCachedDeletedEvents.put(eventId, event);

    }

    public int countCacheDeletedEvents() {
        if (mCachedDeletedEvents == null) {
            mCachedDeletedEvents = new LinkedHashMap<>();
        }
        return mCachedDeletedEvents.size();
    }

    public void undoCacheDeletedEvents() {
        if (mCachedDeletedEvents == null) {
            mCachedDeletedEvents = new LinkedHashMap<>();
        }
        for (Event event : mCachedDeletedEvents.values()) {
            saveEvent(event);
        }

        clearCacheDeletedEvents();
    }

    public void clearCacheDeletedEvents() {
        mCachedDeletedEvents.clear();
    }

    @Override
    public void deleteAllEvents() {
        mTasksLocalDataSource.deleteAllEvents();

        if (mCachedEvents == null) {
            mCachedEvents = new LinkedHashMap<>();
        }
        mCachedEvents.clear();
    }

    @Override
    public void deleteEvent(@NonNull String taskId) {
        addCacheDeletedEvent(taskId, getTaskWithId(taskId));
        mTasksLocalDataSource.deleteEvent(taskId);

        if (mCachedEvents != null) {
            mCachedEvents.remove(taskId);
        }
    }

    @Nullable
    private Event getTaskWithId(@NonNull String id) {
        if (mCachedEvents == null || mCachedEvents.isEmpty()) {
            return null;
        } else {
            return mCachedEvents.get(id);
        }
    }
}
