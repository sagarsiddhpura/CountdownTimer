package com.android.countdowntimer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;


public class EventsViewModel extends AndroidViewModel {

    private Application mApplication;

    private DeadlineRepository mRepository;

    public UpdateEventsDataListener mListener;

    private LiveData<List<Event>> mAllEvents;

    public MutableLiveData<String> category = new MutableLiveData<>();

    public MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();

    public MutableLiveData<Integer> eventCurrentNum = new MutableLiveData<>();

    private List<Event> mEvents;

    public EventsViewModel(@NonNull Application application) {
        super(application);
        mApplication = application;
        mRepository = DeadlineRepository.getInstance(application);
        initData();
    }

    private void initData() {
        mAllEvents = mRepository.getAllEvents();
    }

    public void setListener(UpdateEventsDataListener listener) {
        mListener = listener;
    }

    private void setIsListEmpty(List<Event> events) {
        isListEmpty.setValue(events == null || events.isEmpty());
    }

    LiveData<List<Event>> getAllEvents() {
        return mAllEvents;
    }

    public List<Event> getEvents() {

        mEvents = FilterUtils.filterUncompletedEvents(mAllEvents.getValue());

        return mEvents;
    }

    void updateEventState(Event event, int state) {
        mRepository.updateEventState(event, state);
    }

    void deleteEvent(String id) {
        mRepository.deleteEvent(id);
        mListener.onEventDelete();
    }
}
